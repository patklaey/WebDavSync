package ch.patklaey.webdavsync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by uni on 4/16/15.
 */
public class DirectoryListener extends IntentService {

    public static final int START = 1;
    public static final int STOP = 2;
    private boolean running = true;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DirectoryListener() {
        super("DirectoryListener");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        String directory = intent.getStringExtra(MainActivity.EXTRA_DIRECTORY_TO_OBSERVE);
        String uploadBaseDir = intent.getStringExtra(MainActivity.EXTRA_UPLOAD_BASE_DIRECTORY);

        AndroidFileObserver observer = new AndroidFileObserver(directory, uploadBaseDir);
        observer.startWatching();
        Log.d("Service", "Observing " + directory);

        while (true) {
            try {
                Thread.sleep(10000);
                Log.d("Service", "Still running");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
