package ch.patklaey.webdavsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DirectoryListener extends Service {

    public static final String LOG_TAG = DirectoryListener.class.getSimpleName();
    private AndroidFileObserver observer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Starting service");

        SettingSaver settingSaver = new SettingSaver(this.getApplicationContext());
        Settings settings;

        if (intent != null && intent.getBooleanExtra(MainActivity.EXTRA_STARTED_BY_APPLICATION, false)) {
            settings = MainActivity.getSettings();
            if (settingSaver.save(settings)) {
                Log.d(LOG_TAG, "Settings saved successfully");
            } else {
                Log.w(LOG_TAG, "Could not write settings to database");
            }
        } else {
            settings = settingSaver.load();
        }

        Log.d(LOG_TAG, "Using settings: " + settings.toString());

        this.observer = new AndroidFileObserver(settings);
        this.observer.startWatching();
        Log.d(LOG_TAG, "Observing " + settings.getLocalDirectory());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Stopping observation");
        this.observer.stopWatching();
    }


}
