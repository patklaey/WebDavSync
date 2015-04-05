package ch.patklaey.webdavsync.actions;

import android.os.AsyncTask;
import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;

import java.util.LinkedList;
import java.util.List;

public class WebDavListAction extends AsyncTask<String, Object, Boolean> {

    private Sardine webDavConncetion;
    private List<DavResource> result;

    public WebDavListAction(Sardine sardine) {
        this.webDavConncetion = sardine;
        result = new LinkedList<>();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params.length != 1) {
            return false;
        }

        List<DavResource> resources;
        try {
            resources = this.webDavConncetion.list(params[0]);
            for (DavResource res : resources) {
                result.add(res);
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

        }
    }

}
