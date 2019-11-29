package io.bidmachine.nativead.tasks;

import android.net.Uri;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import io.bidmachine.core.Logger;
import io.bidmachine.nativead.utils.NoSSLv3SocketFactory;

class ConnectionUtils {

    static InputStream getInputStream(String remoteUrl, int timeout) throws Exception {
        try {
            URL url = new URL(remoteUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);
            setupNoSSLv3(urlConnection);
            urlConnection.connect();
            return urlConnection.getInputStream();
        } catch (Exception e) {
            Uri.Builder builder = Uri.parse(remoteUrl).buildUpon();
            builder.scheme("http");

            URL url = new URL(builder.build().toString());
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);
            urlConnection.connect();
            return urlConnection.getInputStream();
        }
    }

    private static void setupNoSSLv3(URLConnection connection) {
        try {
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                SSLSocketFactory delegate = httpsURLConnection.getSSLSocketFactory();
                httpsURLConnection.setSSLSocketFactory(new NoSSLv3SocketFactory(delegate));
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

}