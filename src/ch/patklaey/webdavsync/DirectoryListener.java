package ch.patklaey.webdavsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DirectoryListener extends Service {

    AndroidFileObserver observer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        String directory = intent.getStringExtra(MainActivity.EXTRA_DIRECTORY_TO_OBSERVE);
        String uploadBaseDir = intent.getStringExtra(MainActivity.EXTRA_UPLOAD_BASE_DIRECTORY);

        this.observer = new AndroidFileObserver(directory, uploadBaseDir);
        this.observer.startWatching();
        Log.d("Service", "Observing " + directory);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Stopping observation");
        this.observer.stopWatching();
    }


}
