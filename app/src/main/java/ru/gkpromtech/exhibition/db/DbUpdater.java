package ru.gkpromtech.exhibition.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.gkpromtech.exhibition.net.ServiceClient;
import ru.gkpromtech.exhibition.utils.Profile;
import ru.gkpromtech.exhibition.utils.SharedData;

public class DbUpdater {

    public interface OnProgressListener {
        void onUpdateProgress(int progress);
        void onServerUpdateFailed();
    }

    private final Context mContext;

    public DbUpdater(Context context) {
        mContext = context;
    }

    private int getAvailableDbRevision() {
        InputStream stream = null;
        int result = 0;
        try {
            // доступная ревизия хранится в отдельном файле
            stream = mContext.getAssets().open("db/revision");
            byte[] buff = new byte[32];
            int read = stream.read(buff);
            result = Integer.parseInt(new String(buff, 0, read));
            stream.close();
            stream = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return result;
    }

    private void removeDb(String dbPath) {
        //noinspection ResultOfMethodCallIgnored
        new File(dbPath).delete();
        //noinspection ResultOfMethodCallIgnored
        new File(dbPath + "-journal").delete();
    }

    private void installDb(String assetName, String outPath) throws IOException {
        removeDb(outPath);

        //noinspection ResultOfMethodCallIgnored
        new File(outPath).getParentFile().mkdirs();

        InputStream input = null;
        OutputStream output = null;
        try {
            input = mContext.getAssets().open("db/" + assetName);
            output = new BufferedOutputStream(new FileOutputStream(outPath));

            byte[] buff = new byte[4096];
            int read;
            while ((read = input.read(buff)) > 0) {
                output.write(buff, 0, read);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private int getInstalledDbRevision() {
        return getPrefs().getInt("installed_revision", 0);
    }

    private void setInstalledDbRevision(int revision) {
        getPrefs().edit().putInt("installed_revision", revision).apply();
        mContext.getSharedPreferences(DbHelper.class.getName(), Context.MODE_PRIVATE)
                .edit().putInt("revision", revision).apply();
    }

    private SharedPreferences getPrefs() {
        return mContext.getSharedPreferences("exhibition_db", Context.MODE_PRIVATE);
    }

    public boolean updateDb(final OnProgressListener listener) {

        try {
            int availableRevision = getAvailableDbRevision();
            int installedRevision = getInstalledDbRevision();
            int locale = Profile.getInstance(mContext).getLangId();
            String mainDbPath = DbHelper.getDbPath(mContext, SharedData.EXHIBITION_DATABASE_NAME);

            if (installedRevision == 0) {
                // для миграции на новую структуру БД
                removeDb(DbHelper.getDbPath(mContext, SharedData.LOCAL_DATABASE_NAME));
            }

            if (availableRevision > installedRevision) {
                // upgrade
                String inputPath = SharedData.EXHIBITION_DATABASE_NAME + "_" + locale;
                installDb(inputPath, mainDbPath);
                setInstalledDbRevision(availableRevision);
            }

            listener.onUpdateProgress(3);

            // no DB calls before this line!
            final DbHelper dbHelper = DbHelper.getInstance(mContext);

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            listener.onUpdateProgress(5);

            try {
                JsonNode data = ServiceClient.getJson("changes/getUpdates?revision="
                        + dbHelper.getDbRevision());

                listener.onUpdateProgress(8);

                dbHelper.applyUpdates(db, data, false);

            } catch (Exception e) {
                listener.onServerUpdateFailed();
                e.printStackTrace();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }
}
