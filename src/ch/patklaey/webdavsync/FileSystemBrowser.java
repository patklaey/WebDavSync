package ch.patklaey.webdavsync;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ch.patklaey.webdavsync.actions.WebDavListAction;
import de.aflx.sardine.DavResource;

import java.util.LinkedList;
import java.util.List;

public class FileSystemBrowser extends ListActivity implements WebDavActionCaller {

    private List<DavResource> remoteResources;
    private List<String> directories;
    private WebDavListAction listAction;
    private ArrayAdapter<String> directoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system_browser);

        this.remoteResources = null;
        this.directories = new LinkedList<>();

        this.listAction = new WebDavListAction(MainActivity.getWebDavConnection(), this);

        String startingPoint = this.getIntent().getStringExtra("start");
        this.listAction.execute(startingPoint);

    }

    private void setListContent() {

        for (DavResource res : this.remoteResources) {
            if (res.isDirectory()) {
                this.directories.add(res.toString());
            }
        }

        this.directoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                this.directories);

        ((ListView) findViewById(android.R.id.list)).setAdapter(this.directoryAdapter);

        this.directoryAdapter.notifyDataSetChanged();
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
    @SuppressWarnings("unchecked")
    public void setActionResult(Object result) {
        this.remoteResources = (LinkedList<DavResource>) result;
        this.setListContent();
    }
}
