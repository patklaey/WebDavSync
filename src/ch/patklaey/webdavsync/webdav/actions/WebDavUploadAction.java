package ch.patklaey.webdavsync.webdav.actions;

import android.os.AsyncTask;
import android.util.Log;
import de.aflx.sardine.Sardine;

import java.io.*;

/**
 * Created by uni on 5/17/15.
 */
public class WebDavUploadAction extends AsyncTask<String, Object, Boolean> {

    public static final String LOG_TAG = WebDavUploadAction.class.getSimpleName();
    private Sardine webDavConncetion;
    private WebDavActionCaller caller;
    private boolean result;
    private String uploadBaseDir;

    public WebDavUploadAction(Sardine sardine, WebDavActionCaller caller, String uploadBaseDir) {
        this.webDavConncetion = sardine;
        this.caller = caller;
        this.result = false;
        this.uploadBaseDir = uploadBaseDir;
        if (this.uploadBaseDir.endsWith("/")) {
            this.uploadBaseDir = this.uploadBaseDir.substring(0, this.uploadBaseDir.length() - 1);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params.length != 1) {
            return false;
        }

        Log.d(LOG_TAG, "File to upload: " + params[0]);
        File file = new File(params[0]);
        try {
            if (file.exists()) {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                this.webDavConncetion.put(this.uploadBaseDir + "/" + file.getName(), buf);
                Log.d(LOG_TAG, "Uploaded file " + this.uploadBaseDir + "/" + file.getName());
            } else {
                Log.d(LOG_TAG, "The file " + file.getAbsolutePath() + " no longer exists, skipping it");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.result = true;
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            this.caller.onActionResult(this.result);
        }
    }

}
