package ch.patklaey.webdavsync;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import ch.patklaey.webdavsync.webdav.WebDavConnectionFactory;
import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;

import java.util.List;


public class MainActivity extends Activity {

    public static final String EXTRA_SELECTED_REMOTE_PATH = "ch.patklaey.webdavsync.extraSelectedRemotePath";
    public static final String EXTRA_WEBDAV_URL_TO_BROWSE = "ch.patklaey.webdavsync.extraWebdavUrlToBrowse";
    public static final String EXTRA_SELECTED_LOCAL_PATH = "ch.patklaey.webdavsync.extraSelectedLocalPath";
    public static final String EXTRA_STARTED_BY_APPLICATION = "ch.patklaey.webdavsync.extraStartedByApplication";

    private static Settings settings = new Settings();
    private boolean connectionWorks = false;

    private static Sardine webdavConnection;

    private static final String PREF_USERNAME = "ch.patklaey.webdavsync.username";
    private static final String PREF_PASSWORD = "ch.patklaey.webdavsync.password";
    private static final String PREF_WEBDAV_URL = "ch.patklaey.webdavsync.webdavUrl";
    private static final String PREF_LOCAL_DIRECTORY = "ch.patklaey.webdavsync.localDirectory";
    private static final String PREF_REMOTE_DIRECTORY = "ch.patklaey.webdavsync.remoteDirectory";
    private static final String PREF_AUTH_REQUIRED = "ch.patklaey.webdavsync.authRequired";
    private static final String PREF_CHECK_CERT = "ch.patklaey.webdavsync.checkCert";
    private static final String PREF_WIFI_ONLY = "ch.patklaey.webdavsync.wifiOnly";
    private static final String PREF_SETTINGS_SAVED = "ch.patklaey.webdavsync.settingsSaved";
    private static final String PREF_CONNECTION_WORKS = "ch.patklaey.webdavsync.connectionWorks";
    private static final int REQUEST_BROWSE_REMOTE_DIRECTORY = 1;
    private static final int REQUEST_BROWSE_LOCAL_DIRECTORY = 2;

    // TODO: Make seed secure
    private static final String SEED = "myVerySecretSeed";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings.setLocalDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());

        try {
            loadSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setUpUi();
    }

    public static Sardine getWebDavConnection() {
        return webdavConnection;
    }

    private void setUpUi() {
        ((EditText) findViewById(R.id.settings_url_edittext)).setText(settings.getWebdavUrl());
        ((CheckBox) findViewById(R.id.settings_do_not_check_certs_checkbox)).setChecked(!settings.checkCert());
        ((CheckBox) findViewById(R.id.settings_auth_required_checkbox)).setChecked(settings.authRequired());
        ((EditText) findViewById(R.id.settings_username_edittext)).setText(settings.getUsername());
        ((EditText) findViewById(R.id.settings_password_edittext)).setText(settings.getPassword());
        ((EditText) findViewById(R.id.settings_local_directory_edittext)).setText(settings.getLocalDirectory());
        ((EditText) findViewById(R.id.settings_remote_directory_edittext)).setText(settings.getRemoteDirectory());
        ((CheckBox) findViewById(R.id.settings_wifi_only_checkbox)).setChecked(settings.wifiOnly());

        this.authenticationRequiredCheckboxChecked(findViewById(R.id.settings_auth_required_checkbox));

        if (this.connectionWorks) {
            findViewById(R.id.settings_save_button).setEnabled(true);
            findViewById(R.id.settings_sync_activation_togglebutton).setEnabled(true);
            findViewById(R.id.settings_wifi_only_checkbox).setEnabled(true);
            findViewById(R.id.settings_wifi_only_explanation_textview).setEnabled(true);
        }

        ((ToggleButton) findViewById(R.id.settings_sync_activation_togglebutton)).setChecked(isServiceRunning(DirectoryListener.class));
    }

    private void loadSettings() throws Exception {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(PREF_SETTINGS_SAVED, false)) {
            settings.setWebdavUrl(sharedPref.getString(PREF_WEBDAV_URL, ""));
            settings.setCheckCert(sharedPref.getBoolean(PREF_CHECK_CERT, true));
            settings.setAuthRequired(sharedPref.getBoolean(PREF_AUTH_REQUIRED, false));
            if (settings.authRequired()) {
                // TODO: decrypt password for example with SimpleCrypto.decrypt(SEED,sharedPref.getStrung(PREF_PASSWORD,""))
                settings.setPassword(sharedPref.getString(PREF_PASSWORD, ""));
                settings.setUsername(sharedPref.getString(PREF_USERNAME, ""));
            }
            settings.setLocalDirectory(sharedPref.getString(PREF_LOCAL_DIRECTORY, ""));
            settings.setRemoteDirectory(sharedPref.getString(PREF_REMOTE_DIRECTORY, ""));
            settings.setWifiOnly(sharedPref.getBoolean(PREF_WIFI_ONLY, true));
            this.connectionWorks = sharedPref.getBoolean(PREF_CONNECTION_WORKS, false);
        }
    }

    public void activateSyncButtonClicked(View view) {
        Intent intent = new Intent(this, DirectoryListener.class);
        if (((ToggleButton) view).isChecked()) {
            intent.putExtra(EXTRA_STARTED_BY_APPLICATION, true);
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    public static Settings getSettings() {
        return settings;
    }

    public void testConnection(View view) {

        if (verifyUserInput()) {
            webdavConnection = WebDavConnectionFactory.fromSettings(settings);
            new SardineTask(webdavConnection, this).execute(settings.getWebdavUrl());
        }

    }

    public void saveConnectionSettings(View view) {

        this.testConnection(null);

        if (this.connectionWorks) {
            try {
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                // TODO: encrypt password for example with SimpleCrypto.encrypt(SEED,this.password)
                editor.putString(PREF_PASSWORD, settings.getPassword());
                editor.putString(PREF_USERNAME, settings.getUsername());
                editor.putString(PREF_WEBDAV_URL, settings.getWebdavUrl());
                editor.putString(PREF_LOCAL_DIRECTORY, settings.getLocalDirectory());
                editor.putString(PREF_REMOTE_DIRECTORY, settings.getRemoteDirectory());
                editor.putBoolean(PREF_AUTH_REQUIRED, settings.authRequired());
                editor.putBoolean(PREF_CHECK_CERT, settings.checkCert());
                editor.putBoolean(PREF_WIFI_ONLY, settings.wifiOnly());
                editor.putBoolean(PREF_CONNECTION_WORKS, this.connectionWorks);
                editor.putBoolean(PREF_SETTINGS_SAVED, true);
                if (!editor.commit()) {
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
        Intent intent = new Intent(this, RemoteFileSystemBrowser.class);
        intent.putExtra(EXTRA_WEBDAV_URL_TO_BROWSE, settings.getWebdavUrl());
        this.startActivityForResult(intent, REQUEST_BROWSE_REMOTE_DIRECTORY);
    }

    public void browseLocal(View view) {
        Intent intent = new Intent(this, LocalFileSystemBrowser.class);
        this.startActivityForResult(intent, REQUEST_BROWSE_LOCAL_DIRECTORY);
    }

    public void authenticationRequiredCheckboxChecked(View view) {
        if (((CheckBox) view).isChecked()) {
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

        String webdavUrl = ((TextView) findViewById(R.id.settings_url_edittext)).getText().toString();
        if (!webdavUrl.endsWith("/")) {
            webdavUrl += "/";
            ((TextView) findViewById(R.id.settings_url_edittext)).setText(webdavUrl);
        }


        if ("".equals(webdavUrl)) {
            Context context = getApplicationContext();
            String message = "Please enter the webdav server url";
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return false;
        }

        settings.setWebdavUrl(webdavUrl);

        settings.setCheckCert(!((CheckBox) findViewById(R.id.settings_do_not_check_certs_checkbox)).isChecked());

        settings.setAuthRequired(((CheckBox) findViewById(R.id.settings_auth_required_checkbox)).isChecked());

        settings.setWifiOnly(((CheckBox) findViewById(R.id.settings_wifi_only_checkbox)).isChecked());

        if (settings.authRequired()) {
            String username = ((TextView) findViewById(R.id.settings_username_edittext)).getText().toString();
            String password = ((TextView) findViewById(R.id.settings_password_edittext)).getText().toString();

            if ("".equals(username) || "".equals(password)) {
                Context context = getApplicationContext();
                String message = "Please enter username and password";
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return false;
            }

            settings.setPassword(password);
            settings.setUsername(username);
        }

        String localDirectory = ((TextView) findViewById(R.id.settings_local_directory_edittext)).getText().toString();
        String remoteDirectory = ((TextView) findViewById(R.id.settings_remote_directory_edittext)).getText().toString();

        if ("".equals(localDirectory)) {
            Context context = getApplicationContext();
            String message = "Please specify the local directory";
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return false;
        }

        settings.setLocalDirectory(localDirectory);

        if ("".equals(remoteDirectory)) {
            Context context = getApplicationContext();
            String message = "Please specify the remote directory";
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return false;
        }

        settings.setRemoteDirectory(remoteDirectory);

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

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data) {
        super.onActivityResult(request_code, result_code, data);

        if (request_code == REQUEST_BROWSE_REMOTE_DIRECTORY && result_code == RESULT_OK) {
            String selectedDirectory = data.getStringExtra(EXTRA_SELECTED_REMOTE_PATH);
            ((EditText) findViewById(R.id.settings_remote_directory_edittext)).setText(selectedDirectory);
        }

        if (request_code == REQUEST_BROWSE_LOCAL_DIRECTORY && result_code == RESULT_OK) {
            String selectedDirectory = data.getStringExtra(EXTRA_SELECTED_LOCAL_PATH);
            ((EditText) findViewById(R.id.settings_local_directory_edittext)).setText(selectedDirectory);
        }
    }

    public void setConnectionWorks(boolean works) {
        this.connectionWorks = works;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class SardineTask extends AsyncTask<String, Object, String> {

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
                for (DavResource res : resources) {
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

            if ("success".equals(result)) {
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
        }


    }
}
