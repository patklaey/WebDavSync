package ch.patklaey.webdavsync;

import android.os.FileObserver;
import android.util.Log;
import ch.patklaey.webdavsync.actions.WebDavActionCaller;
import ch.patklaey.webdavsync.actions.WebDavUploadAction;

/**
 * Created by uni on 4/16/15.
 */
public class AndroidFileObserver extends FileObserver implements WebDavActionCaller {

    private String basePath;
    private String uploadBasePath;

    public AndroidFileObserver(String path, String uploadBaseDir) {
        super(path, FileObserver.ALL_EVENTS);
        this.basePath = path;
        this.uploadBasePath = uploadBaseDir;
        Log.d("Observer", "Stated for directory " + this.basePath + " and upload directory " + this.uploadBasePath);
    }

    @Override
    public void onEvent(int event, String path) {
        if (path == null)
            return;

        if (event == FileObserver.CREATE) {
            String filename = this.basePath + path;
            Log.d("Observer", "File " + filename + " created!");
            new WebDavUploadAction(MainActivity.getWebDavConnection(), this, this.uploadBasePath).execute(filename);
        }
    }

    @Override
    public void onActionResult(Object result) {
        Log.d("Observer", "Result from upload action: " + result.toString());
    }
}
