package com.lyxsh.gps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity
        implements BaiduMap.OnMapClickListener {
    private String mMockProviderName = LocationManager.GPS_PROVIDER;
    private Button bt_Ok;
    public static LocationManager locationManager;
    public static double latitude = 30.823271, longitude = 120.875627;
    private LocationClient mLocClient;
    private MyLocationConfiguration.LocationMode mCurrentMode;// 定位模式
    private BitmapDescriptor mCurrentMarker;// 定位图标
    private MapView mMapView;
    private BaiduMap mBaiduMap;

// 初始化全局 bitmap 信息，不用时及时 recycle

    private BitmapDescriptor bd;
    private Marker mMarker;
    private LatLng curLatlng;

    private Boolean isFrist = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplication());
        SDKInitializer.setCoordType(CoordType.GCJ02);
        setContentView(R.layout.activity_main);
        iniView();
        iniListner();
        inilocation();
        iniMap();
    }

    /**
     * iniView 界面初始化
     */
    private void iniView() {
        bt_Ok = (Button) findViewById(R.id.bt_Ok);
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
    }

    /**
     * iniListner 接口初始化
     */
    private void iniListner() {
        bt_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                latitude = curLatlng.latitude;
                longitude = curLatlng.longitude;

                Intent serverIntent = new Intent(MainActivity.this, Floatservice.class);
                startService(serverIntent);
            }
        });
        mBaiduMap.setOnMapClickListener(this);
    }

    /**
     * iniMap 初始化地图
     */
    private void iniMap() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("gcj02"); // 设置坐标类型
        option.setScanSpan(1000);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 缩放
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(22f);
        mBaiduMap.setMapStatus(msu);

        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, bd, 0, 0));
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null || mMapView == null) {
                    return;
                }
//                if (isFrist) {
//                    isFrist = false;
//                    LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
//                    MapStatus.Builder builder = new MapStatus.Builder();
//                    builder.target(latLng).zoom(16f);
//                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//                    setCurrentMapLatLng(latLng);
//                }
            }
        });
        mLocClient.start();

        initOverlay();
    }

    /**
     * initOverlay 设置覆盖物，这里就是地图上那个点
     */
    private void initOverlay() {
        curLatlng = new LatLng(latitude, longitude);
        bd = BitmapDescriptorFactory.fromResource(R.mipmap.dingwei);
        OverlayOptions oo = new MarkerOptions().position(curLatlng).icon(bd).zIndex(9).draggable(true);
        mMarker = (Marker) (mBaiduMap.addOverlay(oo));
    }

    /**
     * inilocation 初始化 位置模拟
     */
    @SuppressLint("MissingPermission")
    private void inilocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider(mMockProviderName, false, true, false, false, true, true,
                true, 0, 5);
        locationManager.setTestProviderEnabled(mMockProviderName, true);
        locationManager.requestLocationUpdates(mMockProviderName, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        LatLng latLng = new LatLng(latitude, longitude);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng).zoom(16f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory
                .newMapStatus(builder.build()));
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        thisFinish();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        bd.recycle();
        super.onDestroy();
    }

    private void thisFinish() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("提示");
        build.setMessage("退出后，将不再提供定位服务，继续退出吗？");
        build.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        build.setNeutralButton("最小化", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        });
        build.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        build.show();
    }


    @Override
    public void onMapClick(LatLng arg0) {
        setCurrentMapLatLng(arg0);
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    /**
     * setCurrentMapLatLng 设置当前坐标
     */
    private void setCurrentMapLatLng(LatLng arg0) {
        curLatlng = arg0;
        mMarker.setPosition(arg0);

        // 设置地图中心点为这是位置
        LatLng ll = new LatLng(arg0.latitude, arg0.longitude);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(u);
    }
}
