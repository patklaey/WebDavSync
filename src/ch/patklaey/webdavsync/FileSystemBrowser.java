package ch.patklaey.webdavsync;

import android.app.ListActivity;
import android.content.Intent;
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

    private List<String> displayDirectories;
    private String basePath = "";
    private String selectedPath = "";
    private String currentPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system_browser);

        this.displayDirectories = new LinkedList<>();

        this.basePath = this.getIntent().getStringExtra("start");

        this.listResources(this.basePath);

    }

    private void listResources(String path) {
        Log.i(this.getLocalClassName(), "Listing WebDav resources for " + path);
        new WebDavListAction(MainActivity.getWebDavConnection(), this).execute(path);
    }

    public void directoryBack(View view) {
        this.currentPath = this.removeLastDirectoryFromPath(this.currentPath);
        this.selectedPath = "";
        this.listResources(this.basePath + this.currentPath);
    }

    public void selectCurrentPath(View view) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_SELECTED_REMOTE_PATH, this.currentPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private String removeLastDirectoryFromPath(String path) {
        // Remove the last slash
        path = path.substring(0, path.length() - 1);

        // Remove the last directory from the path
        int end = path.lastIndexOf("/");
        return path.substring(0, end + 1);
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
        this.selectedPath = this.displayDirectories.get(position);
        this.listResources(this.basePath + this.currentPath + this.selectedPath);
    }

    private void setListContent(LinkedList<DavResource> resources) {

        this.displayDirectories.clear();

        for (DavResource res : resources) {
            if (res.isDirectory()) {

                String item = res.toString().replace(this.currentPath, "");
                if (item.startsWith("/"))
                    item = item.substring(1);

                this.displayDirectories.add(item);
            }
        }

        // Replace the first item in the list as it is the current directory
        // which is an empty string after the call replace(this.currentPath, "")
        this.displayDirectories.remove(0);
        this.displayDirectories.add(0, ".");

        ArrayAdapter<String> directoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                this.displayDirectories);

        ((ListView) findViewById(android.R.id.list)).setAdapter(directoryAdapter);

        directoryAdapter.notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActionResult(Object result) {
        this.currentPath += this.selectedPath;
        this.setListContent((LinkedList<DavResource>) result);
        ((TextView) findViewById(R.id.current_path_textview)).setText(this.basePath + this.currentPath);
    }
}
