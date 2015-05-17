package ch.patklaey.webdavsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by uni on 4/16/15.
 */
public class DirectoryListener extends Service {

    public static final int START = 1;
    public static final int STOP = 2;
    private boolean running = true;

    public DirectoryListener() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        String directory = intent.getStringExtra(MainActivity.EXTRA_DIRECTORY_TO_OBSERVE);
        String uploadBaseDir = intent.getStringExtra(MainActivity.EXTRA_UPLOAD_BASE_DIRECTORY);
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job

        AndroidFileObserver observer = new AndroidFileObserver(directory, uploadBaseDir);
        observer.startWatching();
        Log.d("Service", "Observing " + directory);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
