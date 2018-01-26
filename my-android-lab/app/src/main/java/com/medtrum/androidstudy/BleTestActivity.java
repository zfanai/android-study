package com.medtrum.androidstudy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;

import static android.os.Build.VERSION_CODES.M;
import static com.medtrum.androidstudy.MainActivity.REQUEST_CODE_ALL_PERMISSIONS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleTestActivity extends AppCompatActivity {
    public static final String EXTRA_PARAM = "extra_param";
    private static final String TAG = BleTestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_test);

        initView();
    }

    private void initView(){
        Button btn1 = (Button)findViewById(R.id.button1);//获取按钮资源
        Button btn2 = (Button)findViewById(R.id.button2);//获取按钮资源
        Button btn3 = (Button)findViewById(R.id.button3);//获取按钮资源

        boolean r=needRequestCameraAndStoragePermission();
        Log.i(TAG, "DFS:"+r);
        btn1.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {
                //PDMInstance.getInstance().startScan(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PDMInstance.getInstance().startScan(0);
                    }
                }, 600);
            }
        });

        btn2.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                PDMInstance.getInstance().stopScan();
            }
        });


        btn3.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                //PDMInstance.getInstance().stopScan();
                //tryCon
                PDMInstance.getInstance().readDate(BleTestActivity.this, 0, 0);
            }
        });
    }

    public boolean needRequestCameraAndStoragePermission() {
        if (Build.VERSION.SDK_INT >= M) {
            //版本为6.0以上，那么进行权限检测
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                //如果已经具备了权限，那么可以操作
                return false;
            } else {
                //没有权限，那么申请权限
                requestPermissions();
                return true;
            }
        } else {
            //版本为6.0以下，直接进行操作
            return false;
        }
    }

    @TargetApi(M)
    public void requestPermissions() {
        List<String> permissionsList = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CAMERA);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionsList.size() == 0) {
            return;
        }
        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ALL_PERMISSIONS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_ALL_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();

            perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);

            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i], grantResults[i]);
            }

            /*
            if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                URLUtils.downloadHeadImage(SettingsPDMActivity2.this);//获取服务器中存储的个人头像
            }

            if (perms.get(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(SettingsPDMActivity2.this, CaptureActivity.class);
                intent.putExtra(SettingsDeviceActivateActivity.EXTRA_DEVICE_TYPE_ID_KEY, BLEBaseCentral.DeviceType.PDM.getValue());
                startActivity(intent);
            } */
        }
    }
}