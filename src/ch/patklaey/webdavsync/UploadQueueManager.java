package ch.patklaey.webdavsync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uni on 6/2/15.
 */
public class UploadQueueManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "uploadQueueManager";
    private static final String KEY_REMOTE_LOCATION = "remote_location";
    private static final String DATABASE_NAME = "patklaey_webdav_upload";
    private static final String KEY_LOCAL_FILE = "local_file";

    private static final int INDEX_LOCAL_FILE = 0;
    private static final int INDEX_REMOTE_LOCATION = 1;

    private static final String CREATE_TBALE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + KEY_LOCAL_FILE + " TEXT PRIMARY KEY," + KEY_REMOTE_LOCATION + " TEXT )";

    public UploadQueueManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TBALE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean add(String localFile, String remoteLocation) {

        // Get writable access to the database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create the attributes
        ContentValues attributes = new ContentValues();

        attributes.put(KEY_LOCAL_FILE, localFile);
        attributes.put(KEY_REMOTE_LOCATION, remoteLocation);

        // Insert the settings into the database
        long success = db.insert(TABLE_NAME, null, attributes);
        db.close();

        Log.d("UploadQueueManager", "Added " + localFile + ", " + remoteLocation + " to database: " + success);

        return success != -1;

    }

    public boolean isEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();

        String count = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(count, null);
        cursor.moveToFirst();
        boolean isEmpty = cursor.getInt(0) == 0;
        db.close();

        return isEmpty;
    }

    public Map<String, String> getNextUploadFile() {
        SQLiteDatabase db = this.getReadableDatabase();

        String count = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        Cursor cursor = db.rawQuery(count, null);

        HashMap<String, String> returnMap = null;
        if (cursor.moveToFirst()) {
            returnMap = new HashMap<>();
            returnMap.put("file", cursor.getString(INDEX_LOCAL_FILE));
            returnMap.put("remote", cursor.getString(INDEX_REMOTE_LOCATION));
        }

        db.close();

        return returnMap;
    }

    public boolean deleteNextFile() {

        String filename = this.getNextUploadFile().get("file");

        SQLiteDatabase db = this.getWritableDatabase();

        long success = db.delete(TABLE_NAME, KEY_LOCAL_FILE + " = ?",
                new String[]{String.valueOf(filename)});

        db.close();
        return success == 1;
    }
}
