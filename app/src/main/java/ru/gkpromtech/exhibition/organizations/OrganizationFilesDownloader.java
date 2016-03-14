package ru.gkpromtech.exhibition.organizations;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.net.ServiceClient;
import ru.gkpromtech.exhibition.utils.Callback;
import ru.gkpromtech.exhibition.utils.MiscUtils;
import ru.gkpromtech.exhibition.utils.SharedData;

public class OrganizationFilesDownloader {
    public static void download(final Context context, final Organization organization) {
        final ProgressDialog dialog = ProgressDialog.show(context,
                context.getString(R.string.please_wait),
                context.getString(R.string.loading_materials_info), false);

        ServiceClient.getJson("organizations/files?id=" + organization.id, new Callback<JsonNode>() {
            @Override
            public void onSuccess(JsonNode data) throws Exception {
                dialog.dismiss();

                String url = data.get("url").asText();
                int size = data.get("size").asInt();
                confirmAndDownload(context, organization, url, size);
            }

            @Override
            public void onError(Throwable exception) {
                dialog.dismiss();
                Toast.makeText(context, R.string.error_loading_data, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private static void confirmAndDownload(final Context context, final Organization organization,
                                           final String url, int size) {
        String sizeStr = MiscUtils.humanReadableByteCount(size, true);
        String message = context.getString(R.string.download_materials_to_device, sizeStr);
        new AlertDialog.Builder(context)
                .setTitle(R.string.save_materials)
                .setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        download(context, url, organization);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private static void download(Context context, String url, Organization organization) {
        String dir = SharedData.EXTERNAL_DIR + "army2016/";
        String file = url.substring(url.lastIndexOf('/') + 1);

        //noinspection ResultOfMethodCallIgnored
        new File(dir).mkdirs();

        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
        req.setTitle(organization.fullname);
        req.setDescription(context.getString(R.string.materials));
        req.allowScanningByMediaScanner();
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationUri(Uri.parse("file://" + dir + file));

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(req);
    }
}
