package ch.patklaey.webdavsync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.FileObserver;
import android.util.Log;
import ch.patklaey.webdavsync.webdav.WebDavConnectionFactory;
import ch.patklaey.webdavsync.webdav.actions.WebDavActionCaller;
import ch.patklaey.webdavsync.webdav.actions.WebDavUploadAction;
import de.aflx.sardine.Sardine;

/**
 * Created by uni on 4/16/15.
 */
public class AndroidFileObserver extends FileObserver implements WebDavActionCaller {

    private String basePath;
    private String uploadBasePath;
    private Sardine webdavConnection;
    private Settings settings;
    private Context context;

    public AndroidFileObserver(Settings settings, Context context) {
        super(settings.getLocalDirectory(), FileObserver.ALL_EVENTS);
        this.basePath = settings.getLocalDirectory();
        this.uploadBasePath = settings.getWebdavUrl() + settings.getRemoteDirectory();
        this.webdavConnection = WebDavConnectionFactory.fromSettings(settings);
        this.settings = settings;
        this.context = context;
        Log.d("Observer", "Stated for directory " + this.basePath + " and upload directory " + this.uploadBasePath);
    }

    @Override
    public void onEvent(int event, String path) {
        if (path == null)
            return;

        if (event == FileObserver.CREATE) {
            String filename = this.basePath + path;
            Log.d("Observer", "File " + filename + " created!");
            if (canUploadFile()) {
                new WebDavUploadAction(this.webdavConnection, this, this.uploadBasePath).execute(filename);
            } else {

            }
        }
    }

    private boolean canUploadFile() {
        if (this.settings.wifiOnly()) {
            return phoneHasWifiConnection();
        } else {
            return phoneHasInternetConnection();
        }
    }

    private boolean phoneHasWifiConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                && activeNetwork.isConnectedOrConnecting();
    }

    private boolean phoneHasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onActionResult(Object result) {
        Log.d("Observer", "Result from upload action: " + result.toString());
    }
}
