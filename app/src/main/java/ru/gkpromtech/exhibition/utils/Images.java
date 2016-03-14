package ru.gkpromtech.exhibition.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

import ru.gkpromtech.exhibition.net.HttpClient;

public final class Images {

    public final static class Size {
        public int width;
        public int height;

        public Size() {
        }

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public String toString() {
            return width + "x" + height;
        }
    }

    public static final int BUFFER_SIZE = 1024 * 8;
    private static String mCachePath;

    public static void setCachePath(String path) {
        mCachePath = path;
        //noinspection ResultOfMethodCallIgnored
        new File(mCachePath).mkdirs();
    }

    public static String getLocalPath(String url) throws NoSuchAlgorithmException {
        return getLocalPath(url, null);
    }

    public static String getLocalPath(String url, Size outputSize) throws NoSuchAlgorithmException {
        int pos = url.lastIndexOf(".");
        String ext = "";
        if (pos != -1)
            ext = url.substring(pos);

        return mCachePath + Hash.hash(url) + (outputSize != null ? ("_" + outputSize): "") + ext;
    }

    public static void get(final String url, final Callback<Bitmap> callback, final Size outputSize,
                           final boolean keepAspectRatio) {
        get(url, callback, outputSize, keepAspectRatio, true);
    }


    public static void get(final String url, final Callback<Bitmap> callback, final Size outputSize,
                           final boolean keepAspectRatio, final boolean enableDownload) {

        if (url == null || url.isEmpty()) {
            callback.onError(new InvalidParameterException("url cannot be null or empty"));
            return;
        }

        final String cachePath;
        final String fullSizeCachePath;
        try {
            cachePath = getLocalPath(url, outputSize);
            fullSizeCachePath = (outputSize == null) ? cachePath : getLocalPath(url);
        } catch (Throwable e) {
            callback.onError(e);
            return;
        }

        new AsyncTask<String, Void, Bitmap>() {
            private Throwable mErr;
            @Override
            protected Bitmap doInBackground(String... params) {

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeFile(cachePath, options);
                    if (bitmap != null)
                        return bitmap;

                    if (outputSize != null)
                        bitmap = BitmapFactory.decodeFile(fullSizeCachePath, options);

                    if (bitmap == null && enableDownload) {
                        InputStream in = HttpClient.get(url);
                        if (in == null) {
                            Log.e("PP", "Failed to get image: " + url);
                            return null;
                        }

                        FileOutputStream out = new FileOutputStream(fullSizeCachePath);
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int read;
                            while ((read = in.read(buffer)) != -1)
                                out.write(buffer, 0, read);
                        } finally {
                            out.close();
                            in.close();
                        }

                        bitmap = BitmapFactory.decodeFile(fullSizeCachePath, options);
                    }

                    if (bitmap == null) {
                        Log.e("PP", "Failed to get image: " + url);
                        return null;
                    }

                    if (outputSize != null && outputSize.width < bitmap.getWidth()
                            && outputSize.height < bitmap.getHeight()) {
                        int width = outputSize.width;
                        int height = outputSize.height;
                        if (keepAspectRatio) {
                            float origWidth = bitmap.getWidth();
                            float origHeight = bitmap.getHeight();
                            float ratio = origWidth / origHeight;
                            if ((width / origWidth) > (height / origHeight)) {
                                height = (int) (width / ratio);
                            } else {
                                width = (int) (height * ratio);
                            }
                        }
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                        writeBitmap(bitmap, cachePath);
                    }

                    return bitmap;
                } catch (Exception | OutOfMemoryError e) {
                    mErr = e;
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    try {
                        callback.onSuccess(bitmap);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                } else {
                    callback.onError(mErr);
                }
            }
        }.execute(url);
    }


    static void writeBitmap(Bitmap bitmap, String path) {
        try {
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            if (path.toLowerCase().endsWith(".png"))
                format = Bitmap.CompressFormat.PNG;
            File file = new File(path);
            if (!file.createNewFile())
                Log.d("PP", "Can't create new file " + file.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
            bitmap.compress(format, 85, bos);
            bos.flush();
            bos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
