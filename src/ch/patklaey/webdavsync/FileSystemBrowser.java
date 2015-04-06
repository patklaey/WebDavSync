package ch.patklaey.webdavsync;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ch.patklaey.webdavsync.actions.WebDavActionCaller;
import ch.patklaey.webdavsync.actions.WebDavListAction;
import de.aflx.sardine.DavResource;

import java.util.LinkedList;
import java.util.List;

public class FileSystemBrowser extends ListActivity implements WebDavActionCaller {

    private List<DavResource> remoteResources;
    private List<String> displayDirectories;
    private String basePath;
    private ArrayAdapter<String> directoryAdapter;
    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system_browser);

        this.remoteResources = null;
        this.displayDirectories = new LinkedList<>();

        this.currentPath = "";
        this.basePath = this.getIntent().getStringExtra("start");

        this.executeAction();

    }

    private void executeAction() {
        Log.i(this.getLocalClassName(), "Listing WebDav resources for " + this.basePath + this.currentPath);
        new WebDavListAction(MainActivity.getWebDavConnection(), this).execute(this.basePath + this.currentPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file_system_browser, menu);
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.currentPath += this.displayDirectories.get(position);
        this.executeAction();
    }

    private void setListContent() {

        this.displayDirectories.clear();

        for (DavResource res : this.remoteResources) {
            if (res.isDirectory()) {
                this.displayDirectories.add(res.toString().replace(this.currentPath, ""));
            }
        }

        // Replace the first item in the list as it is the current directory
        // which is an empty string after the call replace(this.currentPath, "")
        this.displayDirectories.remove(0);
        this.displayDirectories.add(0, ".");

        this.directoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                this.displayDirectories);

        ((ListView) findViewById(android.R.id.list)).setAdapter(this.directoryAdapter);

        this.directoryAdapter.notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActionResult(Object result) {
        this.remoteResources = (LinkedList<DavResource>) result;
        this.setListContent();
        ((TextView) findViewById(R.id.current_path_textview)).setText(this.basePath + this.currentPath);
    }
}
