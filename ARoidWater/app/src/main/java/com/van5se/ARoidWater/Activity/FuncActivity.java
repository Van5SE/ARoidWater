package com.van5se.ARoidWater.Activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.van5se.ARoidWater.R;
import com.van5se.ARoidWater.Thread.DeviceLocInfo;
import com.van5se.ARoidWater.Thread.downloadPoiThread;
import com.van5se.ARoidWater.application.MyApplication;
import com.van5se.ARoidWater.service.LocationService;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import map.baidu.ar.ArPageListener;
import map.baidu.ar.camera.SimpleSensor;
import map.baidu.ar.camera.find.FindArCamGLView;
import map.baidu.ar.model.ArLatLng;
import map.baidu.ar.model.ArPoiInfo;
import map.baidu.ar.model.PoiInfoImpl;
import map.baidu.ar.utils.TypeUtils;


/**
 * Ar展示+积水识别 Activity
 */
public class FuncActivity extends AppCompatActivity implements ArPageListener,
        CameraBridgeViewBase.CvCameraViewListener2 {

    private RelativeLayout camRl;
    private FindArCamGLView mCamGLView;
    public static ArrayList<PoiInfoImpl> poiInfos;
    private RelativeLayout mArPoiItemRl;
    private SimpleSensor mSensor;
    private TextView textView_LocInfo;
    private LocationService locationService;
    private TextInputEditText inputboxDistance;
    private ArrayList <String> neededPOI;

    private Gson gson=new Gson();

    private CameraBridgeViewBase cameraView;
    private CascadeClassifier classifier;
    private Mat mGray;
    private Mat mRgba;
    private int mAbsoluteFaceSize = 0;

    private final String TAG="debug_FuncActivity";

    // 手动装载openCV库文件，以保证手机无需安装OpenCV Manager
    static {
        System.loadLibrary("opencv_java3");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_full_func);
        //显示poi物体的relativeLayout
        mArPoiItemRl = (RelativeLayout) findViewById(R.id.ar_poi_item_rl);
        mArPoiItemRl.setVisibility(View.VISIBLE);
        initView();
        initPoiInfo();
        //初始化识别部件
        //cameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        //cameraView.setCvCameraViewListener(this); // 设置相机监听
        initClassifier();
        //cameraView.enableView();
    }


    private void initView() {
        camRl = (RelativeLayout) findViewById(R.id.cam_rl);
        //mCamGLView是百度的摄像头控件
        mCamGLView = (FindArCamGLView) LayoutInflater.from(this).inflate(R.layout.layout_find_cam_view, null);
        mCamGLView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom == 0 || oldBottom != 0 || mCamGLView == null) {
                    return;
                }
                RelativeLayout.LayoutParams params = TypeUtils.safeCast(
                        mCamGLView.getLayoutParams(), RelativeLayout.LayoutParams.class);
                if (params == null) {
                    return;
                }
                params.height = bottom - top;
                mCamGLView.requestLayout();
            }
        });
        camRl.addView(mCamGLView);
        //初始化传感器以实时监听手机位置更新
        initSensor();
        // 保持屏幕不锁屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textView_LocInfo=findViewById(R.id.textView_LocInfo);
        inputboxDistance=findViewById(R.id.inputbox_distance);
        inputboxDistance.setText("500");
    }

    private void initPoiInfo()
    {
        poiInfos = new ArrayList<PoiInfoImpl>();
        ArPoiInfo poiInfo = new ArPoiInfo();
        ArLatLng arLatLng = new ArLatLng(28.165979,112.94082);
        poiInfo.name ="中南海";
        poiInfo.location = arLatLng;
        PoiInfoImpl poiImpl = new PoiInfoImpl();
        poiImpl.setPoiInfo(poiInfo);
        poiInfos.add(poiImpl);
    }

    /***
     * Stop location service
     */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
        //cameraView.disableView();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // -----------location config ------------
        locationService = ((MyApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.start();
        }
        locationService.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    private void initSensor() {
        if (mSensor == null) {
            mSensor = new SimpleSensor(this, new HoldPositionListenerImp());
        }
        mSensor.startSensor();
    }

    /**
     * 在此处更新附近的水坑信息并显示出来
     */
    private class HoldPositionListenerImp implements SimpleSensor.OnHoldPositionListener {
        @Override
        public void onOrientationWithRemap(float[] remapValue) {
            if (mCamGLView != null && mArPoiItemRl != null) {
                if (poiInfos.size() <= 0) {
                    Log.d(TAG,"no poi to show");
                    mArPoiItemRl.setVisibility(View.GONE);
                    initPoiInfo();//没有侦测到任何点，则展示中南海点
                } else {
                    mCamGLView.setFindArSensorState(remapValue, getLayoutInflater(),
                            mArPoiItemRl, FuncActivity.this, poiInfos, FuncActivity.this);
                    mArPoiItemRl.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 从服务器上下载附近Xm范围内的水坑信息，包括经纬度及名称信息
     */
    protected void downloadPoiInfos() {
        neededPOI=new ArrayList<String>();
        Log.d(TAG,"downloadPoiThread.neededPOI:"+downloadPoiThread.neededPOI.size());
        if(downloadPoiThread.neededPOI.size()!=0)
        {
            neededPOI=downloadPoiThread.neededPOI;
            poiInfos = new ArrayList<PoiInfoImpl>();
            for(int i=0;i<neededPOI.size();i+=3)
            {
                ArPoiInfo poiInfo = new ArPoiInfo();
                ArLatLng arLatLng = new ArLatLng(Double.parseDouble(neededPOI.get(i)),Double.parseDouble(neededPOI.get(i+1)));
                poiInfo.name = neededPOI.get(i+2);
                poiInfo.location = arLatLng;
                PoiInfoImpl poiImpl = new PoiInfoImpl();
                poiImpl.setPoiInfo(poiInfo);
                poiInfos.add(poiImpl);
            }
            Log.d(TAG,"poiInfos.size()"+poiInfos.size());
        }
    }

    private void finishCamInternal() {
        if (mCamGLView != null) {
            mCamGLView.stopCam();
            camRl.removeAllViews();
            mCamGLView = null;

        }
        if (mArPoiItemRl != null) {
            mArPoiItemRl.removeAllViews();
        }
        if (mSensor != null) {
            mSensor.stopSensor();
        }
        // 恢复屏幕自动锁屏
        FuncActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(FuncActivity.this).cloneInContext(FuncActivity.this);
    }

    @Override
    public void noPoiInScreen(boolean isNoPoiInScreen) {
    }

    @Override
    public void selectItem(Object iMapPoiItem) {
        if (iMapPoiItem instanceof PoiInfoImpl) {
            Toast.makeText(this, "点击poi: " + ((PoiInfoImpl) iMapPoiItem).getPoiInfo().name, Toast
                    .LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishCamInternal();
        if (mCamGLView != null) {
            if (mCamGLView.getmDialog() != null) {
                mCamGLView.getmDialog().dismiss();
            }
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，每次接受到了返回的地址信息后发往服务器，让其返回位置信息
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nlocType : ");// 定位类型
            sb.append(location.getLocType());
            sb.append("\nlocType description : ");// *****对应的定位类型说明*****
            sb.append(location.getLocTypeDescription());
            sb.append("\nlatitude : ");// 纬度
            sb.append(location.getLatitude());
            sb.append("\nlongtitude : ");// 经度
            sb.append(location.getLongitude());
            sb.append("\nDirection(not all devices have value): ");
            sb.append(location.getDirection());// 方向
            textView_LocInfo.setText(sb.toString());
            //在线程中与服务器进行socket通信，获取最新信息
            //传输纬度，经度，距离，时间四个参数
            DeviceLocInfo deviceLocInfo=new DeviceLocInfo(location.getLatitude()+"",location.getLongitude()+"",
                    inputboxDistance.getText().toString(),location.getTime());
            String dLIstring=gson.toJson(deviceLocInfo);
            Log.i(TAG, "onReceiveLocation: "+dLIstring);

            Thread updatePoiThread=new Thread(new downloadPoiThread(dLIstring));
            updatePoiThread.start();
            downloadPoiInfos();
        }
    };


    /**
     * 以上部分函数负责ar数据获取+显示 以下部分函数负责积水识别
     */
    // 初始化人脸级联分类器，必须先初始化
    private void initClassifier() {
        try {
            InputStream is = getResources()
                    .openRawResource(R.raw.water_cascade);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "water_cascade.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    // 这里执行人脸检测的逻辑, 根据OpenCV提供的例子实现(face-detection)
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        float mRelativeFaceSize = 0.2f;
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }
        MatOfRect faces = new MatOfRect();
        if (classifier != null)
            classifier.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesArray = faces.toArray();
        Scalar faceRectColor = new Scalar(0, 255, 0, 255);
        for (Rect faceRect : facesArray)
            Imgproc.rectangle(mRgba, faceRect.tl(), faceRect.br(), faceRectColor, 3);
        return mRgba;
    }


}
