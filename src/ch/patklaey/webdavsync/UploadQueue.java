package ch.patklaey.webdavsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import ch.patklaey.webdavsync.webdav.WebDavConnectionFactory;
import ch.patklaey.webdavsync.webdav.actions.WebDavActionCaller;
import ch.patklaey.webdavsync.webdav.actions.WebDavUploadAction;
import de.aflx.sardine.Sardine;

import java.util.Map;

/**
 * Created by uni on 6/2/15.
 */
public class UploadQueue extends BroadcastReceiver implements WebDavActionCaller {

    private static UploadQueueManager uploadQueueManager;
    private static Settings settings;
    private static boolean wifiConnectedReceived;

    public UploadQueue(Context context, Settings settings) {
        UploadQueue.uploadQueueManager = new UploadQueueManager(context);
        UploadQueue.settings = settings;
        UploadQueue.wifiConnectedReceived = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("UploadQueue", "Received event");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        Log.d("UploadQueue", "Connection: " + activeNetInfo);

        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        if (isConnected) {
            if (UploadQueue.settings.wifiOnly() && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI && !UploadQueue.wifiConnectedReceived) {
                UploadQueue.wifiConnectedReceived = true;
                this.uploadNextFileFromQueue();
            } else if (!UploadQueue.settings.wifiOnly() && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                this.uploadNextFileFromQueue();
            } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                UploadQueue.wifiConnectedReceived = false;
            }
        } else {
            UploadQueue.wifiConnectedReceived = false;
        }
    }

    private void uploadNextFileFromQueue() {
        if (!UploadQueue.uploadQueueManager.isEmpty()) {
            Log.d("UploadQueue", "Uploading next file");
            Sardine webdavConnection = WebDavConnectionFactory.fromSettings(UploadQueue.settings);
            Map<String, String> resultMap = UploadQueue.uploadQueueManager.getNextUploadFile();
            new WebDavUploadAction(webdavConnection, this, resultMap.get("remote")).execute(resultMap.get("file"));
            Log.d("UploadQueue", "Local file: " + resultMap.get("file") + ", remote location: " + resultMap.get("remote"));
        }
    }


    public UploadQueue() {
    }

    public boolean add(String filename, String uploadLocation) {
        return UploadQueue.uploadQueueManager.add(filename, uploadLocation);
    }


    @Override
    public void onActionResult(Object result) {
        Log.d("UploadQueue", "Received result " + result.toString());
        if ((boolean) result && UploadQueue.uploadQueueManager.deleteNextFile()) {
            this.uploadNextFileFromQueue();
        }
    }
}
