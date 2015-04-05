package ch.patklaey.webdavsync;

import java.util.LinkedList;
import java.util.List;

import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;
import android.os.AsyncTask;

public class WebDavListCommand extends AsyncTask<String, Object, Boolean> {
	
	private Sardine webDavConncetion;
	private List<DavResource> result;
	
	public WebDavListCommand(Sardine sardine) {
		this.webDavConncetion = sardine;
		result = new LinkedList<>();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		if(params.length != 1){
			return false;
		}
		
		List<DavResource> resources;
		try {
			resources = this.webDavConncetion.list(params[0]);
			for (DavResource res : resources)
			{
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
		if( success ) {
			
		}
	}

}
