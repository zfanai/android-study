package com.medtrum.androidstudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DemoActivityOne extends AppCompatActivity {
    public static final String EXTRA_DEVICE_TYPE_ID_KEY = "extra_device_type_id_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);

        //initView();
    }
}