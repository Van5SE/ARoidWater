package com.van5se.ARoidWater.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.van5se.ARoidWater.R;
import com.van5se.ARoidWater.Thread.WarningInfo;
import com.van5se.ARoidWater.Thread.ReportPoiThread;
import com.van5se.ARoidWater.application.MyApplication;
import com.van5se.ARoidWater.service.LocationService;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText inputbox_latitude;
    TextInputEditText inputbox_longtitude;
    TextInputEditText inputbox_warntype;
    TextInputEditText inputbox_time;
    TextInputEditText inputbox_deep;
    TextInputEditText inputbox_area;
    MaterialButton button_report;
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        initView();
    }

    public void initView()
    {
        inputbox_latitude=findViewById(R.id.inputbox_latitude);
        inputbox_longtitude=findViewById(R.id.inputbox_longtitude);
        inputbox_warntype=findViewById(R.id.inputbox_warn_type);
        inputbox_time=findViewById(R.id.inputbox_time);
        inputbox_deep=findViewById(R.id.inputbox_deep);
        inputbox_area=findViewById(R.id.inputbox_area);

        button_report=findViewById(R.id.button_report);
        button_report.setOnClickListener(this);

        locationService = ((MyApplication) getApplication()).locationService;
        locationService.start();
    }

    /***
     * Stop location service
     */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //???????????????
        locationService.stop(); //??????????????????
        super.onStop();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // -----------location config ------------
        locationService = ((MyApplication) getApplication()).locationService;
        //??????locationservice????????????????????????????????????1???location???????????????????????????????????????????????????activity?????????????????????????????????locationservice?????????
        locationService.registerListener(mListener);
        //????????????
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.start();
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button_report)
        {
            //???????????????
            new AlertDialog.Builder(ReportActivity.this)
                    .setTitle(getString(R.string.button_report))
                    .setMessage(getString(R.string.report_confirm))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //??????????????????
                            WarningInfo wi=new WarningInfo(inputbox_latitude.getText().toString(),inputbox_longtitude.getText().toString(),
                                    inputbox_warntype.getText().toString(),inputbox_time.getText().toString(),inputbox_deep.getText().toString(),inputbox_area.getText().toString());
                            Gson gson=new Gson();
                            String wijson=gson.toJson(wi);
                            Log.i("ReportActivityjson", "onClick: "+wijson);

                            Thread reportPoiThread=new Thread(new ReportPoiThread(wijson,ReportActivity.this));
                            reportPoiThread.start();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            inputbox_latitude.setText(location.getLatitude()+"");
            inputbox_longtitude.setText(location.getLongitude()+"");
            inputbox_warntype.setText("??????");
            inputbox_time.setText(location.getTime());
            //????????????????????????????????????
            locationService.stop();
        }
    };
}