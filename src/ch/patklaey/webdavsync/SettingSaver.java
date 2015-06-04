package ch.patklaey.webdavsync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SettingSaver extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "ch_patklaey_webdavsync";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "settings";
    private static final String KEY_WEBDAV_URL = "wedav_url";
    private static final String KEY_CHECK_CERT = "check_cert";
    private static final String KEY_AUTH_REQUIRED = "auth_required";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOCAL_DIRECTORY = "local_directory";
    private static final String KEY_REMOTE_DIRECTORY = "remote_directory";
    private static final String KEY_WIFI_ONLY = "wifi_only";

    private static final int INDEX_WEBDAV_URL = 0;
    private static final int INDEX_CHECK_CERT = 1;
    private static final int INDEX_AUTH_REQUIRED = 2;
    private static final int INDEX_USERNAME = 3;
    private static final int INDEX_PASSWORD = 4;
    private static final int INDEX_LOCAL_DIRECTORY = 5;
    private static final int INDEX_REMOTE_DIRECTORY = 6;
    private static final int INDEX_WIFI_ONLY = 7;

    private static final int TRUE = 1;
    private static final int FALSE = 0;


    private static final String CREATE_TBALE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + KEY_WEBDAV_URL + " TEXT PRIMARY KEY," + KEY_CHECK_CERT + " INTEGER," + KEY_AUTH_REQUIRED + " INTEGER,"
            + KEY_USERNAME + " TEXT," + KEY_PASSWORD + " TEXT," + KEY_LOCAL_DIRECTORY + " TEXT,"
            + KEY_REMOTE_DIRECTORY + " TEXT," + KEY_WIFI_ONLY + " INTEGER )";

    public SettingSaver(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TBALE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean save(Settings settings) {

        // Get writable access to the database
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, KEY_WEBDAV_URL + " = ?",
                new String[]{settings.getWebdavUrl()});

        // Create the attributes
        ContentValues attributes = new ContentValues();

        // Add all  attributes
        int authRequired = settings.authRequired() ? TRUE : FALSE;
        int checkCert = settings.checkCert() ? TRUE : FALSE;
        int wifiOnly = settings.wifiOnly() ? TRUE : FALSE;
        attributes.put(KEY_WEBDAV_URL, settings.getWebdavUrl());
        attributes.put(KEY_AUTH_REQUIRED, authRequired);
        attributes.put(KEY_CHECK_CERT, checkCert);
        attributes.put(KEY_LOCAL_DIRECTORY, settings.getLocalDirectory());
        attributes.put(KEY_PASSWORD, settings.getPassword());
        attributes.put(KEY_REMOTE_DIRECTORY, settings.getRemoteDirectory());
        attributes.put(KEY_USERNAME, settings.getUsername());
        attributes.put(KEY_WIFI_ONLY, wifiOnly);

        // Insert the settings into the database
        long success = db.insert(TABLE_NAME, null, attributes);
        db.close();

        return success != -1;
    }

    public Settings load() {
        Settings settings = new Settings();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " + TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        settings.setWebdavUrl(cursor.getString(INDEX_WEBDAV_URL));
        settings.setRemoteDirectory(cursor.getString(INDEX_REMOTE_DIRECTORY));
        settings.setLocalDirectory(cursor.getString(INDEX_LOCAL_DIRECTORY));
        settings.setUsername(cursor.getString(INDEX_USERNAME));
        settings.setPassword(cursor.getString(INDEX_PASSWORD));
        settings.setAuthRequired(cursor.getInt(INDEX_AUTH_REQUIRED) == 1);
        settings.setCheckCert(cursor.getInt(INDEX_CHECK_CERT) == 1);
        settings.setWifiOnly(cursor.getInt(INDEX_WIFI_ONLY) == 1);

        return settings;
    }
}
