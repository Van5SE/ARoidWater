package com.van5se.ARoidWater.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.van5se.ARoidWater.R;
import com.van5se.ARoidWater.application.MyApplication;
import com.van5se.ARoidWater.service.LocationService;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText inputbox_server_address;
    MaterialButton button_change_server;
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        initView();
    }

    public void initView()
    {
        inputbox_server_address=findViewById(R.id.inputbox_server_address);
        button_change_server=findViewById(R.id.button_change_server);
        button_change_server.setOnClickListener(this);
        locationService = ((MyApplication) getApplication()).locationService;
        locationService.start();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button_change_server)
        {
            //显示对话框
            new AlertDialog.Builder(ServerActivity.this)
                    .setTitle(getString(R.string.button_change_server))
                    .setMessage(getString(R.string.change_server_confirm))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(inputbox_server_address.getText().toString().contains("."))
                            {
                                MainActivity.server=inputbox_server_address.getText().toString();
                                Log.d("debug_ServerActivity","现在服务器为"+MainActivity.server);
                            }
                            new AlertDialog.Builder(ServerActivity.this)
                                    .setTitle(getString(R.string.server_address))
                                    .setMessage(getString(R.string.now_server_address)+MainActivity.server)
                                    .setPositiveButton(getString(R.string.ok),null)
                                    .show();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

}