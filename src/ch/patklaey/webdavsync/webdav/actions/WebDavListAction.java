package ch.patklaey.webdavsync.webdav.actions;

import android.os.AsyncTask;
import android.util.Log;
import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;

import java.util.LinkedList;
import java.util.List;

public class WebDavListAction extends AsyncTask<String, Object, Boolean> {

    private Sardine webDavConncetion;
    private List<DavResource> result;
    private WebDavActionCaller caller;

    public WebDavListAction(Sardine sardine, WebDavActionCaller caller) {
        this.webDavConncetion = sardine;
        this.caller = caller;
        result = new LinkedList<>();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params.length != 1) {
            return false;
        }

        List<DavResource> resources;
        try {
            String url = params[0];
            url = url.replace(" ", "%20");
            if (!url.endsWith("/"))
                url += "/";
            Log.i(this.getClass().getSimpleName(), "Query resources in " + url);
            resources = this.webDavConncetion.list(url);
            for (DavResource res : resources) {
                this.result.add(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            this.caller.onActionResult(this.result);
        }
    }

}
