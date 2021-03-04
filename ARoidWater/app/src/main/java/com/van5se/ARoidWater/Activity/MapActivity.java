package com.van5se.ARoidWater.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.van5se.ARoidWater.R;
import com.van5se.ARoidWater.Thread.DeviceLocInfo;
import com.van5se.ARoidWater.Thread.downloadPoiThread;
import com.van5se.ARoidWater.utils.CustomImageDialog;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MaterialButton button_locate;
    //构建Marker图标
    private BitmapDescriptor marker_pic = BitmapDescriptorFactory
            .fromResource(R.drawable.watermarker);
    private ArrayList <String> neededPOI;
    private List<OverlayOptions> options;
    private List<LatLng> latLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map_view);

        button_locate=findViewById(R.id.button_locate);

        button_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回原位
                MyLocationConfiguration myCofig=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,true,null);
                mBaiduMap.setMyLocationConfiguration(myCofig);
            }
        });


        mBaiduMap = mMapView.getMap();
        //显示卫星图层
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setCompassEnable(true);
        //直接缩放至缩放级别17
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));

        //定位初始化
        mLocationClient = new LocationClient(this);

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        //设置locationClientOption
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
        //自定义定位
        MyLocationConfiguration myCofig=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,true,null);
        mBaiduMap.setMyLocationConfiguration(myCofig);

        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                //自定义定位
                MyLocationConfiguration newCofig=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,null);
                mBaiduMap.setMyLocationConfiguration(newCofig);
            }
        });

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("MapActivity",marker.getId()+"");

                    if(marker.getPosition().equals(latLngs.get(0)))
                    {
                        new CustomImageDialog(MapActivity.this,R.drawable.watera).show();
                    }else if(marker.getPosition().equals(latLngs.get(1)))
                    {
                        new CustomImageDialog(MapActivity.this,R.drawable.waterb).show();
                    }else if(marker.getPosition().equals(latLngs.get(2)))
                    {
                        new CustomImageDialog(MapActivity.this,R.drawable.waterc).show();
                    }else if(marker.getPosition().equals(latLngs.get(3)))
                    {
                        new CustomImageDialog(MapActivity.this,R.drawable.waterd).show();
                    }
                    else
                    {
                        new CustomImageDialog(MapActivity.this,R.drawable.undefined_pic).show();
                    }

                return true;
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            //传输纬度，经度，距离，时间四个参数下载附近的水坑信息
            Gson gson=new Gson();
            DeviceLocInfo deviceLocInfo=new DeviceLocInfo(location.getLatitude()+"",location.getLongitude()+"",
                    "2000",location.getTime());
            String dLIstring=gson.toJson(deviceLocInfo);

            Thread updatePoiThread=new Thread(new downloadPoiThread(dLIstring));
            updatePoiThread.start();

            latLngs=new ArrayList<LatLng>();
            neededPOI=new ArrayList<String>();
            if(downloadPoiThread.neededPOI.size()!=0)
            {
                mBaiduMap.clear();
                //创建OverlayOptions的集合
                options = new ArrayList<OverlayOptions>();
                neededPOI=downloadPoiThread.neededPOI;
                for(int i=0;i<neededPOI.size();i+=6)
                {
                   LatLng thispoint=new LatLng(Double.parseDouble(neededPOI.get(i)),
                           Double.parseDouble(neededPOI.get(i+1)));
                   latLngs.add(thispoint);
                    //创建OverlayOptions属性
                    OverlayOptions thisoption =  new MarkerOptions()
                            .position(thispoint)
                            .icon(marker_pic)
                            .perspective(true);
                    options.add(thisoption);
                }
                //在地图上批量添加
                mBaiduMap.addOverlays(options);
            }
        }
    }


}