package ch.patklaey.webdavsync;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;

public class LocalFileSystemBrowser extends ListActivity {

    public static final String ROOT_DIRECTORY = "/";
    private String currentPath = "";
    LinkedList<String> displayDirectories = new LinkedList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system_browser);

        this.currentPath = Environment.getExternalStorageDirectory().getPath() + "/";
        this.listResources(this.currentPath);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String selectedDir = this.displayDirectories.get(position);
        File selectedPath = new File(this.currentPath + selectedDir);
        if (selectedPath.canRead()) {
            this.currentPath += selectedDir;
            this.listResources(this.currentPath);
        }
    }

    public void selectCurrentPath(View view) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_SELECTED_LOCAL_PATH, this.currentPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void directoryBack(View view) {
        if (directoryIsRootDirectory(this.currentPath))
            return;

        this.currentPath = new File(this.currentPath).getParent();

        if (!directoryIsRootDirectory(this.currentPath))
            this.currentPath += "/";

        this.listResources(this.currentPath);
    }

    private boolean directoryIsRootDirectory(String currentDir) {
        return directoryIsRootDirectory(new File(currentDir));
    }

    private boolean directoryIsRootDirectory(File currentDir) {
        return currentDir.getPath().equals(ROOT_DIRECTORY);
    }

    public void listResources(String path) {
        File directory = new File(path);

        ((TextView) findViewById(R.id.current_path_textview)).setText(this.currentPath);

        this.displayDirectories.clear();
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                this.displayDirectories.add(file.getName() + "/");
        }

        ArrayAdapter<String> directoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                this.displayDirectories);

        ((ListView) findViewById(android.R.id.list)).setAdapter(directoryAdapter);

        directoryAdapter.notifyDataSetChanged();

    }
}