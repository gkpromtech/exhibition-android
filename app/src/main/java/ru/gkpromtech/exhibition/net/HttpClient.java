package ru.gkpromtech.exhibition.net;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.gkpromtech.exhibition.utils.Callback;

public class HttpClient {

    private final static int DEFAULT_TIMEOUT = 30000;
    private final static Handler mHandler = new Handler(Looper.getMainLooper());
    private final static ExecutorService mPool = Executors.newCachedThreadPool();

    public interface ResponseProcessor<T> {
        public T process(InputStream in) throws Exception;
    }


    public static <T> void get(final String url, final Callback<T> callback,
                           final ResponseProcessor<T> processor) {
        mPool.execute(new Runnable() {
            @Override
            public void run() {
                final T result;
                try {
                    InputStream stream = null;
                    //noinspection TryFinallyCanBeTryWithResources
                    try {
                        stream = get(url);
                        result = processor.process(stream);
                    } finally {
                        if (stream != null)
                            stream.close();
                    }
                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e);
                        }
                    });
                    return;
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onSuccess(result);
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    }
                });
            }
        });
    }

    public static InputStream get(String uri) throws Exception {

        Log.d("pt", "calling http " + uri);

        int index = uri.indexOf('?');
        if (index != -1)
            uri = uri.substring(0, index + 1) + Uri.encode(uri.substring(index + 1), "&=");

        URL url = new URL(uri);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(DEFAULT_TIMEOUT);
        connection.setConnectTimeout(DEFAULT_TIMEOUT);

        InputStream stream = connection.getInputStream();
        if (stream == null)
            Log.d("pt", "Failed to get output stream for: " + uri);

        // Get Response
        int statusCode = connection.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            Log.d("pt", "Http Error " + statusCode + "URL: " + uri);
            return null;
        }

        return stream;
    }
//
//    public static String readInputStreamToString(InputStream inputStream)
//            throws IOException {
//        BufferedReader bufferedReader = new BufferedReader(
//                new InputStreamReader(inputStream));
//        String line;
//        StringBuilder result = new StringBuilder();
//        while ((line = bufferedReader.readLine()) != null)
//            result.append(line);
//
//        inputStream.close();
//        return result.toString();
//    }
}
