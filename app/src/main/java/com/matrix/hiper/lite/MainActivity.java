package com.matrix.hiper.lite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.matrix.hiper.lite.hiper.HiPerCallback;
import com.matrix.hiper.lite.hiper.HiPerVpnService;
import com.matrix.hiper.lite.hiper.Sites;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int START_HIPER_CODE = 1000;

    private ImageFilterButton addNewInstance;
    private ImageFilterButton refresh;
    private ListView instanceListView;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNewInstance = findViewById(R.id.add_new_instance);
        refresh = findViewById(R.id.refresh);
        addNewInstance.setOnClickListener(this);
        refresh.setOnClickListener(this);
        instanceListView = findViewById(R.id.instance_list);

        refreshList();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_HIPER_CODE && resultCode == Activity.RESULT_OK && name != null) {
            Intent intent = new Intent(this, HiPerVpnService.class);
            HiPerVpnService.setHiPerCallback(new HiPerCallback() {
                @Override
                public void run(int code) {
                    System.out.println(code == 1 ? "success" : "failed");
                    refreshList();
                }

                @Override
                public void onExit(String error) {
                    if (error != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage(error);
                        builder.setPositiveButton("OK", null);
                        builder.create().show();
                    }
                }
            });
            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == addNewInstance) {
            AddInstanceDialog dialog = new AddInstanceDialog(this, this::refreshList);
            dialog.show();
        }
        if (view == refresh) {
            refreshList();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void refreshList() {
        ArrayList<Sites.Site> list = new ArrayList<>();
        String[] ids = new File(getFilesDir().getAbsolutePath()).list();
        if (ids != null) {
            for (String id : ids) {
                if (new File(getFilesDir().getAbsolutePath() + "/" + id + "/hiper_config.json").exists()) {
                    Sites.Site site = Sites.Site.fromFile(this, id);
                    list.add(site);
                }
            }
        }
        SiteListAdapter adapter = new SiteListAdapter(this, this, list);
        instanceListView.setAdapter(adapter);
    }
}