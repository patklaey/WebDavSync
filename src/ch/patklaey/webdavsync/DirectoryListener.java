package ch.patklaey.webdavsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DirectoryListener extends Service {

    public static final String LOG_TAG = DirectoryListener.class.getSimpleName();
    private AndroidFileObserver observer;
    private UploadQueue uploadQueue;
    private Settings settings;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Starting service");

        SettingSaver settingSaver = new SettingSaver(this.getApplicationContext());

        if (intent != null && intent.getBooleanExtra(MainActivity.EXTRA_STARTED_BY_APPLICATION, false)) {
            this.settings = MainActivity.getSettings();
            if (settingSaver.save(settings)) {
                Log.d(LOG_TAG, "Settings saved successfully");
            } else {
                Log.w(LOG_TAG, "Could not write settings to database");
            }
        } else {
            this.settings = settingSaver.load();
        }

        Log.d(LOG_TAG, "Using settings: " + this.settings.toString());

        this.uploadQueue = new UploadQueue(this.getApplicationContext(), this.settings);

        this.observer = new AndroidFileObserver(this.settings, this);
        this.observer.startWatching();
        Log.d(LOG_TAG, "Observing " + this.settings.getLocalDirectory());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Stopping observation");
        this.observer.stopWatching();
    }

    public boolean addFileToUploadQueue(String filename) {
        return this.uploadQueue.add(filename, this.settings.getWebdavUrl() + this.settings.getRemoteDirectory());
    }


}
