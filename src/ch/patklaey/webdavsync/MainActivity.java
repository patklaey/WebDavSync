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
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private String webdav_url;
	private boolean check_cert = true;
	private boolean auth_required = false;
	private String username;
	private String password;
	private String local_directory;
	private String remove_directory;

	private Sardine webdavConnection;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void testConnection(View view) {
    	
    	if ( saveUserInput() ) {
    	
	    	this.webdavConnection = getSardineImplementation(this.check_cert);
	    	
	    	new SardineTask(this.webdavConnection, this).execute(this.webdav_url);
	    	
    	}
    	
    }

    private boolean saveUserInput() {
    	
    	this.webdav_url = ((TextView)findViewById(R.id.settings_url_edittext)).getText().toString();
    	
    	if ( "".equals(this.webdav_url) ) {
			Context context = getApplicationContext();
			String message = "Please enter the webdav server url";
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
    	}
    	
    	this.check_cert  = ! ((CheckBox)findViewById(R.id.settings_do_not_check_certs_checkbox)).isChecked();
    	
    	this.auth_required = ((CheckBox)findViewById(R.id.settings_auth_required_checkbox)).isChecked();

    	if ( this.auth_required ) {
    		this.username = ((TextView)findViewById(R.id.settings_username_edittext)).getText().toString();
    		this.password = ((TextView)findViewById(R.id.settings_password_edittext)).getText().toString();
    		
    		if ( "".equals(this.username) || "".equals(this.password) ) {
    			Context context = getApplicationContext();
    			String message = "Please enter username and password";
    			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
    			toast.setGravity(Gravity.CENTER, 0, 0);
    			toast.show();
    			return false;
    		}
    	}
    	
    	this.local_directory = ((TextView)findViewById(R.id.settings_local_directory_edittext)).getText().toString();
    	this.remove_directory = ((TextView)findViewById(R.id.settings_remote_directory_edittext)).getText().toString();
    	
    	return true;
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
    
    private Sardine getSardineImplementation(boolean check_cert) {
    	Sardine sardine;
    	
    	if ( check_cert ) {
    		sardine = SardineFactory.begin();
    	} else {
    		sardine = new SardineImpl() {
				
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
    	}
    	
    	if (this.auth_required) {
    		sardine.setCredentials(this.username, this.password);
    	}
    	
    	return sardine;
    }
    
    private class SardineTask extends AsyncTask<String, Object, String>{

    	private Sardine webdavConnection;
    	private MainActivity activity;
    	
    	public SardineTask(Sardine impl, MainActivity act) {
    		this.webdavConnection = impl;
    		this.activity = act;
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			
			List<DavResource> resources;
			try {
				resources = this.webdavConnection.list(params[0]);
				for (DavResource res : resources)
				{
				     System.out.println(res); // calls the .toString() method.
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "failed";
			}
			
			return "success";
		}
		
		@Override
        protected void onPostExecute(String result) {
			String message;

            if ( "success".equals(result) ) {
            	message = "Connection tested successful";
            	this.activity.findViewById(R.id.settings_save_button).setEnabled(true);
            } else {
            	message = "Connection could not be tested successfully";
            }
            
			Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
       }

    	
    }
}
