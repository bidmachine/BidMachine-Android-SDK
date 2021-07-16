package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.explorestack.protobuf.openrtb.Openrtb;
import com.explorestack.protobuf.openrtb.Request;
import com.explorestack.protobuf.openrtb.Response;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.UnknownHostException;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.protobuf.InitRequest;
import io.bidmachine.protobuf.InitResponse;
import io.bidmachine.utils.BMError;

class ApiRequest<RequestDataType, ResponseType> extends NetworkRequest<RequestDataType, ResponseType, BMError> {

    @VisibleForTesting
    static int REQUEST_TIMEOUT = 10 * 1000;

    @VisibleForTesting
    String requiredUrl;
    @VisibleForTesting
    int timeOut;

    private ApiRequest(@NonNull Method method,
                       @Nullable String path,
                       @Nullable RequestDataType requestData) {
        super(method, path, requestData);

        addContentEncoder(new GZIPRequestDataEncoder<RequestDataType, ResponseType, BMError>());
    }

    @NonNull
    @Override
    protected BMError obtainError(URLConnection connection, int responseCode) {
        BMError bmError;
        if (responseCode >= 200 && responseCode < 300) {
            bmError = BMError.noFill();
            bmError.setTrackError(false);
        } else if (responseCode >= 400 && responseCode < 500) {
            bmError = BMError.Request;
        } else if (responseCode >= 500 && responseCode < 600) {
            bmError = BMError.Server;
        } else {
            bmError = BMError.internal("Unknown server error");
        }
        return bmError;
    }

    @NonNull
    @Override
    protected BMError obtainError(URLConnection connection, @Nullable Throwable t) {
        Logger.log("obtainError: " + t + "(" + connection + ")");
        if (t instanceof UnknownHostException) {
            return BMError.NoConnection;
        } else if (t instanceof SocketTimeoutException || t instanceof ConnectTimeoutException) {
            return BMError.TimeoutError;
        }
        return BMError.internal("Unknown server error");
    }

    @Override
    protected String getBaseUrl() {
        return requiredUrl;
    }

    @Override
    protected void prepareRequestParams(URLConnection connection) {
        super.prepareRequestParams(connection);

        connection.setConnectTimeout(timeOut);
        connection.setReadTimeout(timeOut);
    }


    public static class Builder<RequestDataType, ResponseDataType> {

        private String url;
        private RequestDataType requestData;
        private int timeOut = REQUEST_TIMEOUT;
        private ApiDataBinder<RequestDataType, ResponseDataType> dataBinder;
        private NetworkRequest.Callback<ResponseDataType, BMError> callback;
        private NetworkRequest.CancelCallback cancelCallback;

        private Method method = Method.Post;

        public Builder<RequestDataType, ResponseDataType> url(String url) {
            this.url = url;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setDataBinder(
                ApiDataBinder<RequestDataType, ResponseDataType> dataBinder) {
            this.dataBinder = dataBinder;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setRequestData(RequestDataType requestData) {
            this.requestData = requestData;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setLoadingTimeOut(int timeOut) {
            this.timeOut = timeOut > 0 ? timeOut : REQUEST_TIMEOUT;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setCallback(Callback<ResponseDataType, BMError> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setCancelCallback(CancelCallback cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        public Builder<RequestDataType, ResponseDataType> setMethod(@NonNull Method method) {
            this.method = method;
            return this;
        }

        public ApiRequest<RequestDataType, ResponseDataType> build() {
            ApiRequest<RequestDataType, ResponseDataType> request = new ApiRequest<>(method,
                                                                                     null,
                                                                                     requestData);
            request.setCallback(callback);
            request.setCancelCallback(cancelCallback);
            request.setDataBinder(dataBinder);
            request.requiredUrl = url;
            request.timeOut = timeOut;
            return request;
        }

        public ApiRequest<RequestDataType, ResponseDataType> request() {
            ApiRequest<RequestDataType, ResponseDataType> apiRequest = build();
            apiRequest.request();
            return apiRequest;
        }
    }

    public static abstract class ApiDataBinder<RequestDataType, ResponseDataType>
            extends NetworkRequest.RequestDataBinder<RequestDataType, ResponseDataType, BMError> {
    }

    public static class ApiInitDataBinder extends ApiDataBinder<InitRequest, InitResponse> {

        @Override
        protected void prepareHeaders(NetworkRequest<InitRequest, InitResponse, BMError> networkRequest,
                                      URLConnection urlConnection) {
            if (BuildConfig.DEBUG) {
                urlConnection.setRequestProperty("Content-Type",
                                                 "application/x-protobuf; messageType=\"bidmachine.protobuf.InitRequest\"");
            } else {
                urlConnection.setRequestProperty("Content-Type", "application/x-protobuf");
            }
        }

        @Nullable
        @Override
        protected byte[] obtainData(NetworkRequest<InitRequest, InitResponse, BMError> networkRequest,
                                    URLConnection urlConnection,
                                    @Nullable InitRequest initRequest) throws Exception {
            OrtbUtils.dump("Init request", initRequest);
            return initRequest != null ? initRequest.toByteArray() : null;
        }

        @Override
        protected InitResponse createSuccessResult(NetworkRequest<InitRequest, InitResponse, BMError> networkRequest,
                                                   URLConnection urlConnection,
                                                   byte[] bytes) throws Exception {
            return InitResponse.parseFrom(bytes);
        }
    }

    public static class ApiResponseAuctionDataBinder extends ApiDataBinder<Request, Response> {

        @Override
        protected void prepareHeaders(NetworkRequest<Request, Response, BMError> request,
                                      URLConnection connection) {
            if (BuildConfig.DEBUG) {
                connection.setRequestProperty("Content-Type",
                                              "application/x-protobuf; messageType=\"bidmachine.protobuf.openrtb.Openrtb\"");
            } else {
                connection.setRequestProperty("Content-Type", "application/x-protobuf");
            }
            Logger.log("Auction request headers", connection.getRequestProperties());
        }

        @Nullable
        @Override
        protected byte[] obtainData(NetworkRequest<Request, Response, BMError> request,
                                    URLConnection connection,
                                    @Nullable Request requestData) throws Exception {
            return null;
        }

        @Override
        protected Response createSuccessResult(NetworkRequest<Request, Response, BMError> request,
                                               URLConnection connection,
                                               byte[] resultData) throws Exception {
            final Openrtb openrtb = Openrtb.parseFrom(resultData);
            if (openrtb != null) {
                // Debug response dump
                OrtbUtils.dump("Response", openrtb);
                return openrtb.getResponse();
            }
            return null;
        }

    }

    public static class ApiAuctionDataBinder extends ApiResponseAuctionDataBinder {

        @Nullable
        @Override
        protected byte[] obtainData(NetworkRequest<Request, Response, BMError> request,
                                    URLConnection connection,
                                    @Nullable Request requestData) throws Exception {
            final Openrtb.Builder openrtb = Openrtb.newBuilder();
            openrtb.setRequest(requestData);
            openrtb.setVer("3.0");
            openrtb.setDomainspec("adcom");
            openrtb.setDomainver("1.0");
            // Debug request dump
            OrtbUtils.dump("Auction request", openrtb);
            return openrtb.build().toByteArray();
        }

    }

}