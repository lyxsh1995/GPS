package com.lyxsh.gps;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.baidu.mapapi.utils.CoordinateConverter;
import com.kongqw.rockerlibrary.view.RockerView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/11/10.
 */

public class Floatservice extends Service {
    public static Floatservice floatservicethis;
    public WindowManager windowManager;
    public WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    FloatView floatView;
    private Thread thread;// 需要一个线程一直刷新
    private Boolean RUN = true;
    private String mMockProviderName = LocationManager.GPS_PROVIDER;
    private LocationManager locationManager;
    private double latitude, longitude;
    private double horizontal, vertical;
    private double G = 0.0001;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        floatservicethis = this;
        locationManager = MainActivity.locationManager;
        latitude = GPSUtil.gcj02_To_Gps84(MainActivity.latitude, MainActivity.longitude)[0];
        longitude = GPSUtil.gcj02_To_Gps84(MainActivity.latitude, MainActivity.longitude)[1];
        initfloat();
        initview();
        start();
        timer.schedule(timerTask, 0, 200);
    }

    private void initfloat() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = new FloatView(Floatservice.this);
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        windowManager.addView(floatView, params);
    }

    private void initview() {
        RockerView rockerView = floatView.findViewById(R.id.rockerView);
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_MOVE);
        rockerView.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {
                flag = true;
            }

            @Override
            public void direction(RockerView.Direction direction) {
                switch (direction) {
                    case DIRECTION_UP:
                        vertical = -G;
                        horizontal = 0;
                        break;
                    case DIRECTION_LEFT:
                        vertical = 0;
                        horizontal = -G;
                        break;
                    case DIRECTION_RIGHT:
                        vertical = 0;
                        horizontal = G;
                        break;
                    case DIRECTION_DOWN:
                        vertical = G;
                        horizontal = 0;
                        break;
                    case DIRECTION_UP_RIGHT:
                        vertical = -G;
                        horizontal = G;
                        break;
                    case DIRECTION_UP_LEFT:
                        vertical = -G;
                        horizontal = -G;
                        break;
                    case DIRECTION_DOWN_RIGHT:
                        vertical = G;
                        horizontal = G;
                        break;
                    case DIRECTION_DOWN_LEFT:
                        vertical = G;
                        horizontal = -G;
                        break;
                    case DIRECTION_CENTER:
                        vertical = 0;
                        horizontal = 0;
                        break;
                }
            }

            @Override
            public void onFinish() {
                flag = false;
            }
        });
    }

    private void start() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (RUN) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setLocation(longitude, latitude);
                }
            }
        });
        thread.start();
    }

    Boolean flag = false;
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (flag) {
                latitude += horizontal;
                longitude += vertical;
            }
        }
    };

    /**
     * setLocation 设置GPS的位置
     */
    private void setLocation(double longitude, double latitude) {
        Location location = new Location(mMockProviderName);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(2.0f);
        location.setAccuracy(3.0f);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        locationManager.setTestProviderLocation(mMockProviderName, location);
    }

    @Override
    public void onDestroy() {
        RUN = false;
        thread = null;
        flag = false;
        timer.cancel();
        windowManager.removeView(floatView);
    }
}
