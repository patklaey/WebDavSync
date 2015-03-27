package ch.patklaey.webdavsync;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;

import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;
import de.aflx.sardine.SardineFactory;
import de.aflx.sardine.impl.SardineImpl;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        new SardineTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class SardineTask extends AsyncTask<String, Object, String>{

		@Override
		protected String doInBackground(String... params) {

			Sardine sardine = getSardineImplementation();
			
			List<DavResource> resources;
			try {
				resources = sardine.list("https://webdav.patklaey.ch/pat/");
				for (DavResource res : resources)
				{
				     System.out.println(res); // calls the .toString() method.
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		private Sardine getSardineImplementation() {
			
			Sardine sardine = new SardineImpl() {
				
		        @Override
		        protected SSLSocketFactory createDefaultSecureSocketFactory() {
		        	
					KeyStore keyStore = null;
					try {
						keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
						keyStore.load(null, null);  
					} catch (KeyStoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CertificateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	
		        	SSLSocketFactory factory = null;
	                try {
						factory = new MySSLSocketFactory(keyStore);
					} catch (KeyManagementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnrecoverableKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (KeyStoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                return factory;
		        }
		                               
			};
			
			return sardine;
		}
    	
    }
}
