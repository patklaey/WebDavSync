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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private String webdavUrl = "";
	private boolean checkCert = true;
	private boolean authRequired = false;
	private String username = "";
	private String password = "";
	private String localDirectory = "";
	private String remoteDirectory = "";
	private boolean connectionWorks = false;
	
	private Sardine webdavConnection;
	
	private static final String PREF_USERNAME = "ch.patklaey.webdavsync.username";
	private static final String PREF_PASSWORD = "ch.patklaey.webdavsync.password";
	private static final String PREF_WEBDAV_URL = "ch.patklaey.webdavsync.webdavUrl";
	private static final String PREF_LOCAL_DIRECTORY = "ch.patklaey.webdavsync.localDirectory";
	private static final String PREF_REMOTE_DIRECTORY = "ch.patklaey.webdavsync.remoteDirectory";
	private static final String PREF_AUTH_REQUIRED = "ch.patklaey.webdavsync.authRequired";
	private static final String PREF_CHECK_CERT = "ch.patklaey.webdavsync.checkCert";
	private static final String PREF_SETTINGS_SAVED = "ch.patklaey.webdavsync.settingsSaved";
	private static final String PREF_CONNECTION_WORKS = "ch.patklaey.webdavsync.connectionWorks";
	
	// TODO: Make seed secure
	private static final String SEED = "myVerySecretSeed";

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
			loadSettings();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        setUpUi();
    }
    
    private void setUpUi() {
    	((EditText) findViewById(R.id.settings_url_edittext)).setText(this.webdavUrl);
    	((CheckBox) findViewById(R.id.settings_do_not_check_certs_checkbox)).setChecked(!this.checkCert);
    	((CheckBox) findViewById(R.id.settings_auth_required_checkbox)).setChecked(this.authRequired);
    	((EditText) findViewById(R.id.settings_username_edittext)).setText(this.username);
    	((EditText) findViewById(R.id.settings_password_edittext)).setText(this.password);
    	((EditText) findViewById(R.id.settings_local_directory_edittext)).setText(this.localDirectory);
    	((EditText) findViewById(R.id.settings_remote_directory_edittext)).setText(this.remoteDirectory);
    	
    	this.authenticationRequiredCheckboxChecked( findViewById(R.id.settings_auth_required_checkbox) );
    	
    	if(this.connectionWorks) {
    		findViewById(R.id.settings_save_button).setEnabled(true);
        	findViewById(R.id.settings_sync_activation_togglebutton).setEnabled(true);
        	findViewById(R.id.settings_wifi_only_checkbox).setEnabled(true);
        	findViewById(R.id.settings_wifi_only_explanation_textview).setEnabled(true);
    	}
    }
    
    private void loadSettings() throws Exception {
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		if( sharedPref.getBoolean(PREF_SETTINGS_SAVED, false) ) {
			this.webdavUrl = sharedPref.getString(PREF_WEBDAV_URL, "");
			this.checkCert = sharedPref.getBoolean(PREF_CHECK_CERT, true);
			this.authRequired = sharedPref.getBoolean(PREF_AUTH_REQUIRED, false);
			if ( this.authRequired ) {
        		// TODO: decrypt password for example with SimpleCrypto.decrypt(SEED,sharedPref.getStrung(PREF_PASSWORD,""))
				this.password = sharedPref.getString(PREF_PASSWORD, "");
				this.username = sharedPref.getString(PREF_USERNAME, "");
			}
			this.localDirectory = sharedPref.getString(PREF_LOCAL_DIRECTORY, "");
			this.remoteDirectory = sharedPref.getString(PREF_REMOTE_DIRECTORY, "");
			this.connectionWorks = sharedPref.getBoolean(PREF_CONNECTION_WORKS, false);
		}
    }
    
    public void testConnection(View view) {
    	
    	if ( verifyUserInput() ) {
	    	this.webdavConnection = getSardineImplementation(this.checkCert);
	    	new SardineTask(this.webdavConnection, this).execute(this.webdavUrl);
    	}
    	
    }
    
    public void saveConnectionSettings(View view) {
    	
    	this.testConnection(null);
    	
    	if ( this.connectionWorks ) {
    		try {
        		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        		SharedPreferences.Editor editor = sharedPref.edit();
        		// TODO: encrypt password for example with SimpleCrypto.encrypt(SEED,this.password)
				editor.putString(PREF_PASSWORD, this.password);
				editor.putString(PREF_USERNAME, this.username);
				editor.putString(PREF_WEBDAV_URL, this.webdavUrl);
				editor.putString(PREF_LOCAL_DIRECTORY, this.localDirectory);
				editor.putString(PREF_REMOTE_DIRECTORY, this.remoteDirectory);
				editor.putBoolean(PREF_AUTH_REQUIRED, this.authRequired);
				editor.putBoolean(PREF_CHECK_CERT, this.checkCert);
				editor.putBoolean(PREF_CONNECTION_WORKS, this.connectionWorks);
				editor.putBoolean(PREF_SETTINGS_SAVED, true);
				if( ! editor.commit() ) {
					throw new RuntimeException("Writting shared preferences failed");
				} else {
					Context context = getApplicationContext();
					String message = "Settings saved";
					Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			} catch (Exception e) {
				throw new RuntimeException("Saving settings failed:", e);
			}
    	}
    }
    
    public void browseRemote(View view) {
    	Intent intent = new Intent(this, FileSystemBrowser.class);
    	intent.putExtra("start", this.webdavUrl);
    	this.startActivity(intent);
    }
    
    public void authenticationRequiredCheckboxChecked(View view) {
    	if ( ((CheckBox) view).isChecked() ) {
    		findViewById(R.id.settings_username_edittext).setEnabled(true);
    		findViewById(R.id.settings_password_edittext).setEnabled(true);
    		findViewById(R.id.settings_username_textview).setEnabled(true);
    		findViewById(R.id.settings_password_textview).setEnabled(true);

    	} else {
    		findViewById(R.id.settings_username_edittext).setEnabled(false);
    		findViewById(R.id.settings_password_edittext).setEnabled(false);
    		findViewById(R.id.settings_username_textview).setEnabled(false);
    		findViewById(R.id.settings_password_textview).setEnabled(false);

    	}
    }

    private boolean verifyUserInput() {
    	
    	this.webdavUrl = ((TextView)findViewById(R.id.settings_url_edittext)).getText().toString();
    	
    	if ( "".equals(this.webdavUrl) ) {
			Context context = getApplicationContext();
			String message = "Please enter the webdav server url";
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
    	}
    	
    	this.checkCert  = ! ((CheckBox)findViewById(R.id.settings_do_not_check_certs_checkbox)).isChecked();
    	
    	this.authRequired = ((CheckBox)findViewById(R.id.settings_auth_required_checkbox)).isChecked();

    	if ( this.authRequired ) {
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
    	
    	this.localDirectory = ((TextView)findViewById(R.id.settings_local_directory_edittext)).getText().toString();
    	this.remoteDirectory = ((TextView)findViewById(R.id.settings_remote_directory_edittext)).getText().toString();
    	
    	if( "".equals(this.localDirectory) ) {
			Context context = getApplicationContext();
			String message = "Please specify the local directory";
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
    	}
    	
    	if ( "".equals(this.remoteDirectory) ) {
    		Context context = getApplicationContext();
			String message = "Please specify the remote directory";
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
    	}
    	
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
    
    public void setConnectionWorks(boolean works) {
    	this.connectionWorks = works;
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
    	
    	if (this.authRequired) {
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
            	message = "Connection tested successfully";
            	this.activity.findViewById(R.id.settings_save_button).setEnabled(true);
            	this.activity.findViewById(R.id.settings_sync_activation_togglebutton).setEnabled(true);
            	this.activity.findViewById(R.id.settings_wifi_only_checkbox).setEnabled(true);
            	this.activity.findViewById(R.id.settings_wifi_only_explanation_textview).setEnabled(true);
            	this.activity.setConnectionWorks(true);
            } else {
            	message = "Connection could not be tested successfully";
            	this.activity.setConnectionWorks(false);
            }
            
			Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
       }

    	
    }
}
