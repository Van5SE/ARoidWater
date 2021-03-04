package com.van5se.ARoidWater.application;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import android.app.Application;
import android.widget.Toast;
import map.baidu.ar.init.ArSdkManager;
import map.baidu.ar.init.MKGeneralListener;
import map.baidu.ar.utils.ArBDLocation;

import com.van5se.ARoidWater.service.LocationService;
import com.van5se.ARoidWater.utils.LocSdkClient;

/**
 * Ar sdk application
 */
public class MyApplication extends Application {

    private static MyApplication mInstance = null;
    public LocationService locationService;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        // ArSDK模块初始化
        ArSdkManager.getInstance().initApplication(this, new MyGeneralListener());
        // 若用百度定位sdk,需要在此初始化定位SDK
        LocSdkClient.getInstance(this).getLocationStart();
        // 若用探索功能需要再这集成检索模块 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext

        locationService = new LocationService(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        // 检索模块 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);

    }

    public static MyApplication getInstance() {
        return mInstance;
    }


    static class MyGeneralListener implements MKGeneralListener {
        // 事件监听，用来处理通常的网络错误，授权验证错误等
        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(MyApplication.getInstance().getApplicationContext(),
                        "arsdk 验证异常，请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast
                                .LENGTH_LONG).show();
            } else {

                //Toast.makeText(MyApplication.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_LONG)
                //       .show();
            }
        }

        // 回调给ArSDK获取坐标（demo调用百度定位sdk）
        @Override
        public ArBDLocation onGetBDLocation() {

            BDLocation location =
                    LocSdkClient.getInstance(ArSdkManager.getInstance().getAppContext()).getLocationStart()
                            .getLastKnownLocation();
            if (location == null) {
                return null;
            }
            ArBDLocation arBDLocation = new ArBDLocation();
            // 设置经纬度信息
            arBDLocation.setLongitude(location.getLongitude());
            arBDLocation.setLatitude(location.getLatitude());
            return arBDLocation;
        }
    }

}