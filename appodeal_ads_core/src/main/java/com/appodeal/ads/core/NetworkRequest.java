package com.appodeal.ads.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.explorestack.protobuf.AbstractMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> {

    private static final Executor executor = Executors.newFixedThreadPool(2);

    public enum Method {
        Get("GET"), Post("POST");

        private String methodString;

        Method(String methodString) {
            this.methodString = methodString;
        }

        public String getMethodString() {
            return methodString;
        }

        public void apply(URLConnection connection) throws ProtocolException {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod(methodString);
            }
        }
    }

    public enum State {
        Idle, Running, Success, Fail, Canceled
    }

    @Nullable
    private String path;
    @NonNull
    private Method method;
    @Nullable
    private RequestDataType requestData;
    @Nullable
    private RequestResultType requestResult;
    @Nullable
    private ErrorResultType errorResult;
    @Nullable
    private URLConnection currentConnection;

    @Nullable
    @SuppressWarnings("unchecked")
    private RequestDataBinder<RequestDataType, RequestResultType, ErrorResultType> dataBinder;
    @Nullable
    @SuppressWarnings("unchecked")
    private ArrayList<RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType>> dataEncoders;
    @Nullable
    @SuppressWarnings("unchecked")
    private ArrayList<RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType>> contentEncoders;
    @Nullable
    private Callback<RequestResultType, ErrorResultType> callback;
    @Nullable
    private CancelCallback cancelCallback;

    private State currentState = State.Idle;

    public NetworkRequest(@Nullable String path, @NonNull Method method,
                          @Nullable RequestDataType requestData) {
        this.path = path;
        this.method = method;
        this.requestData = requestData;
    }

    public void setDataBinder(@Nullable RequestDataBinder<RequestDataType, RequestResultType, ErrorResultType> dataBinder) {
        this.dataBinder = dataBinder;
    }

    public void addDataEncoder(RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> encoder) {
        if (dataEncoders == null) {
            dataEncoders = new ArrayList<>();
        }
        dataEncoders.add(encoder);
    }

    public void addContentEncoder(RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> encoder) {
        if (contentEncoders == null) {
            contentEncoders = new ArrayList<>();
        }
        contentEncoders.add(encoder);
    }

    public void setCallback(@Nullable Callback<RequestResultType, ErrorResultType> callback) {
        this.callback = callback;
    }

    public void setCancelCallback(@Nullable CancelCallback cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    @NonNull
    public Method getMethod() {
        return method;
    }

    public void request() {
        executor.execute(new NetworkRequestRunner());
    }

    private void process() {
        currentState = State.Running;

        URLConnection connection = null;
        try {
            currentConnection = connection =
                    (path != null ? new URL(String.format("%s/%s", getBaseUrl(), path))
                            : new URL(getBaseUrl())).openConnection();

            method.apply(connection);
            prepareRequestParams(connection);

            byte[] contentBytes = obtainRequestData(connection);

            if (contentBytes != null) {
                contentBytes = encodeRequestData(connection, contentBytes);

                connection.setDoOutput(true);
                BufferedOutputStream writer = null;
                try {
                    writer = new BufferedOutputStream(connection.getOutputStream());
                    writer.write(contentBytes);
                } finally {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                }
            }

            InputStream isResponse = null;
            ByteArrayOutputStream osBytes = null;

            try {
                int responseCode = obtainResponseCode(connection);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    errorResult = obtainError(connection, obtainErrorStream(connection),
                            responseCode);
                } else {
                    isResponse = connection.getInputStream();
                    osBytes = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = isResponse.read(buffer)) != -1) {
                        osBytes.write(buffer, 0, length);
                    }
                    byte[] responseBytes = osBytes.toByteArray();
                    if (responseBytes != null) {
                        responseBytes = decodeResponseData(connection, responseBytes);
                    }
                    if (responseBytes == null || responseBytes.length == 0) {
                        errorResult = obtainError(connection, (RequestResultType) null, responseCode);
                    } else if (dataBinder != null) {
                        requestResult = dataBinder.createSuccessResult(this, connection,
                                responseBytes);
                        if (requestResult == null) {
                            errorResult = dataBinder.createFailResult(this, connection,
                                    responseBytes);
                        }
                    }
                }
            } finally {
                if (osBytes != null) {
                    osBytes.flush();
                    osBytes.close();
                }
                if (isResponse != null) {
                    isResponse.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorResult = obtainError(connection, e);
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
            currentConnection = null;
            if (!isCanceled()) {
                currentState = errorResult == null ? State.Success : State.Fail;
            }
        }
    }

    protected void prepareRequestParams(URLConnection connection) {
        connection.setConnectTimeout(40 * 1000);
        connection.setReadTimeout(40 * 1000);
    }

    protected byte[] obtainRequestData(URLConnection connection) throws Exception {
        if (dataBinder != null) {
            dataBinder.prepareRequest(this, connection);
            dataBinder.prepareHeaders(this, connection);
            return dataBinder.obtainData(this, connection, requestData);
        }
        return null;
    }

    protected byte[] encodeRequestData(URLConnection connection, byte[] requestData) throws Exception {
        byte[] result = requestData;
        if (dataEncoders != null) {
            for (RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> encoder
                    : dataEncoders) {
                encoder.prepareHeaders(this, connection);
                result = encoder.encode(this, connection, result);
            }
        }
        if (contentEncoders != null) {
            for (RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> encoder
                    : contentEncoders) {
                encoder.prepareHeaders(this, connection);
                result = encoder.encode(this, connection, result);
            }
        }
        return result;
    }

    protected byte[] decodeResponseData(URLConnection connection, byte[] responseData) throws Exception {
        byte[] result = responseData;
        if (contentEncoders != null) {
            for (RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> encoder
                    : contentEncoders) {
                result = encoder.decode(this, connection, result);
            }
        }
        if (dataEncoders != null) {
            for (RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> encoder
                    : dataEncoders) {
                result = encoder.decode(this, connection, result);
            }
        }
        return result;
    }

    protected abstract ErrorResultType obtainError(URLConnection connection, @Nullable RequestResultType resultType, int responseCode);

    protected abstract ErrorResultType obtainError(URLConnection connection, @Nullable InputStream errorStream, int responseCode);

    protected abstract ErrorResultType obtainError(URLConnection connection, @Nullable Exception e);

    private int obtainResponseCode(URLConnection connection) throws IOException {
        if (connection instanceof HttpURLConnection) {
            return ((HttpURLConnection) connection).getResponseCode();
        }
        return -1;
    }

    private InputStream obtainErrorStream(URLConnection connection) {
        return connection instanceof HttpURLConnection
                ? ((HttpURLConnection) connection).getErrorStream() : null;
    }

    protected String getBaseUrl() throws Exception {
        return "TODO: implement url";
    }

    public void cancel() {
        currentState = State.Canceled;
        if (currentConnection instanceof HttpURLConnection) {
            ((HttpURLConnection) currentConnection).disconnect();
        }
        if (cancelCallback != null) {
            cancelCallback.onCanceled();
        }
    }

    public boolean isCanceled() {
        return currentState == State.Canceled;
    }

    public interface Callback<RequestResultType, ErrorResultType> {
        void onSuccess(@Nullable RequestResultType result);

        void onFail(@Nullable ErrorResultType result);
    }

    public interface CancelCallback {
        void onCanceled();
    }
    
    /*
    Request data/params binders
     */

    public static abstract class RequestDataBinder<RequestDataType, RequestResultType, ErrorResultType> {

        protected void prepareRequest(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                      URLConnection connection) {
        }

        protected abstract void prepareHeaders(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                               URLConnection connection);

        @Nullable
        protected abstract byte[] obtainData(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                             URLConnection connection, @Nullable RequestDataType requestData) throws Exception;

        protected abstract RequestResultType createSuccessResult(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                                                 URLConnection connection, byte[] resultData) throws Exception;

        protected ErrorResultType createFailResult(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                                   URLConnection connection, byte[] resultData) throws Exception {
            return null;
        }

    }

    static abstract class ProtobufDataBinder<RequestDataType extends AbstractMessage, RequestResultType, ErrorResultType>
            extends RequestDataBinder<RequestDataType, RequestResultType, ErrorResultType> {

        @Override
        protected void prepareHeaders(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                      URLConnection connection) {
            if (BuildConfig.DEBUG) {
                connection.setRequestProperty("Content-Type", "application/x-protobuf; messageType=\"com.appodeal.ads.Request\";");
            } else {
                connection.setRequestProperty("Content-Type", "application/x-protobuf");
            }
        }

        @Override
        @Nullable
        protected byte[] obtainData(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                    URLConnection connection, RequestDataType requestData) {
            return requestData != null ? requestData.toByteArray() : null;
        }

    }

    abstract static class JsonDataBinder<RequestResultType, ErrorResultType>
            extends RequestDataBinder<JSONObject, RequestResultType, ErrorResultType> {

        @Override
        protected void prepareHeaders(NetworkRequest<JSONObject, RequestResultType, ErrorResultType> request, URLConnection connection) {
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }

        @Nullable
        @Override
        protected byte[] obtainData(NetworkRequest<JSONObject, RequestResultType, ErrorResultType> request,
                                    URLConnection connection, @Nullable JSONObject requestData) throws Exception {
            return requestData != null ? requestData.toString().getBytes("UTF-8") : null;
        }
    }

    public static class SimpleJsonObjectDataBinder<ErrorResultType> extends JsonDataBinder<JSONObject, ErrorResultType> {

        @Override
        protected JSONObject createSuccessResult(NetworkRequest<JSONObject, JSONObject, ErrorResultType> request,
                                                 URLConnection connection, byte[] resultData) throws Exception {
            return new JSONObject(new String(resultData));
        }

    }

    public static class SimpleJsonArrayDataBinder<ErrorResultType> extends JsonDataBinder<JSONArray, ErrorResultType> {

        @Override
        protected JSONArray createSuccessResult(NetworkRequest<JSONObject, JSONArray, ErrorResultType> request,
                                                URLConnection connection, byte[] resultData) throws Exception {
            return new JSONArray(new String(resultData));
        }

    }

    /*
    Encoders
     */

    public static abstract class RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> {

        protected void prepareHeaders(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                      URLConnection connection) {
        }

        protected abstract byte[] encode(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                         URLConnection connection, byte[] data) throws Exception;

        protected abstract byte[] decode(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                         URLConnection connection, byte[] data) throws Exception;
    }

    public static class Base64RequestDataEncoder extends RequestDataEncoder {

        @Override
        protected byte[] encode(NetworkRequest request, URLConnection connection, byte[] data) {
            return Base64.encode(data, Base64.DEFAULT);
        }

        @Override
        protected byte[] decode(NetworkRequest request, URLConnection connection, byte[] data) {
            return Base64.decode(data, Base64.DEFAULT);
        }

    }

    public static class GZIPRequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType>
            extends RequestDataEncoder<RequestDataType, RequestResultType, ErrorResultType> {

        @Override
        protected void prepareHeaders(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                      URLConnection connection) {
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Content-Encoding", "gzip");
        }

        @Override
        protected byte[] encode(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                URLConnection connection, byte[] data) throws Exception {
            ByteArrayOutputStream osBytes = null;
            GZIPOutputStream osGzip = null;
            try {
                osBytes = new ByteArrayOutputStream();
                osGzip = new GZIPOutputStream(osBytes);
                osGzip.write(data);
                //required for write all pending bytes
                osGzip.close();
                osGzip = null;
                return osBytes.toByteArray();
            } finally {
                if (osBytes != null) {
                    osBytes.flush();
                    osBytes.close();
                }
                if (osGzip != null) {
                    osGzip.flush();
                    osGzip.close();
                }
            }
        }

        @Override
        protected byte[] decode(NetworkRequest<RequestDataType, RequestResultType, ErrorResultType> request,
                                URLConnection connection, byte[] data) throws Exception {
            if ("gzip".equals(connection.getContentEncoding())) {
                ByteArrayOutputStream osBytes = null;
                ByteArrayInputStream isBytes = null;
                GZIPInputStream isGzip = null;
                try {
                    osBytes = new ByteArrayOutputStream();
                    isBytes = new ByteArrayInputStream(data);
                    isGzip = new GZIPInputStream(isBytes);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = isGzip.read(buffer)) != -1) {
                        osBytes.write(buffer, 0, bytesRead);
                    }
                    return osBytes.toByteArray();
                } finally {
                    if (osBytes != null) {
                        osBytes.flush();
                        osBytes.close();
                    }
                    if (isBytes != null) {
                        isBytes.close();
                    }
                    if (isGzip != null) {
                        isGzip.close();
                    }
                }
            }
            return data;
        }

    }

    private final class NetworkRequestRunner implements Runnable {
        @Override
        public void run() {
            process();
            if (callback != null && !isCanceled()) {
                if (currentState == State.Success) {
                    callback.onSuccess(requestResult);
                } else {
                    callback.onFail(errorResult);
                }
            }
        }
    }

}
