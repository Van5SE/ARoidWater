package com.van5se.ARoidWater.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.van5se.ARoidWater.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static String server ="192.168.43.97";//电脑开热点时的服务器地址
    //public static String server = "192.168.43.96";//手机开热点时的服务器地址
    public static final int port = 8888;//服务器端口
    public final static int timeoutsecond = 2000;// 连接超时时间

    MaterialButton button_report;
    MaterialButton button_start_detect;
    MaterialButton button_position_test;
    MaterialButton button_water_detect;
    MaterialButton button_func_test;
    MaterialButton button_server_change;
    MaterialButton button_map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        button_report=findViewById(R.id.button_report);
        button_report.setOnClickListener(this);
        button_start_detect=findViewById(R.id.button_start_detect);
        button_start_detect.setOnClickListener(this);
        button_position_test=findViewById(R.id.button_position_test);
        button_position_test.setOnClickListener(this);
        button_water_detect=findViewById(R.id.button_water_detect);
        button_water_detect.setOnClickListener(this);
        button_func_test=findViewById(R.id.button_func_test);
        button_func_test.setOnClickListener(this);
        button_server_change=findViewById(R.id.button_server);
        button_server_change.setOnClickListener(this);
        button_map=findViewById(R.id.button_map);
        button_map.setOnClickListener(this);

        // 判断权限
        PermissionsChecker permissionsChecker = new PermissionsChecker(this);
        if (permissionsChecker.lacksPermissions()) {
            Toast.makeText(this, "缺少权限，请开启权限！", Toast.LENGTH_LONG).show();
            openSetting();
        }
    }

    /**
     * 点击跳转
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_report:
                Intent intent_Report = new Intent(MainActivity.this, ReportActivity.class);
                MainActivity.this.startActivity(intent_Report);
                break;
            case R.id.button_start_detect:
                Intent intent_ARact=new Intent(this,ArActivity.class);
                MainActivity.this.startActivity(intent_ARact);
                break;
            case R.id.button_position_test:
                Intent intent_LOCact=new Intent(this, LocationActivity.class);
                MainActivity.this.startActivity(intent_LOCact);
                break;
            case R.id.button_water_detect:
                Intent intent_Detect=new Intent(this, DetectActivity.class);
                MainActivity.this.startActivity(intent_Detect);
                break;
            case R.id.button_func_test:
                Intent intent_Func=new Intent(this, FuncActivity.class);
                MainActivity.this.startActivity(intent_Func);
                break;
            case R.id.button_server:
                Intent intent_server=new Intent(this,ServerActivity.class);
                MainActivity.this.startActivity(intent_server);
                break;
            case R.id.button_map:
                Intent intent_map=new Intent(this,MapActivity.class);
                MainActivity.this.startActivity(intent_map);
                break;
        }
    }

    /**
     * 打开设置权限界面
     *
     * @param
     */
    public void openSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

}