package com.medtrum.androidstudy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DemoActivityOne extends AppCompatActivity {
    public static final String EXTRA_DEVICE_TYPE_ID_KEY = "extra_device_type_id_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);

        initView();
    }

    private void initView(){
        Button Btn1 = (Button)findViewById(R.id.btnSimpleToast);//获取按钮资源
        Btn1.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                String strTmp = "动画";
                //Ev1.setText(strTmp);
                //Toast.makeText(getApplicationContext(), "默认Toast样式",
                //        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(DemoActivityOne.this, AnimaActivity.class);
                intent.putExtra(AnimaActivity.EXTRA_PARAM, 123);
                startActivity(intent);
            }

        });
    }
}