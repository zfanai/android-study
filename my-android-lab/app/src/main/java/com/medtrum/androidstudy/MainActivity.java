package com.medtrum.androidstudy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ALL_PERMISSIONS = 26;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i != 5; ++i) {
            items.add("我是item " + i);
        }

        CustomListView clv=(CustomListView) findViewById(R.id.clv_1);
        clv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, items));

        //第一种方式
        Button Btn1 = (Button)findViewById(R.id.button1);//获取按钮资源
        Btn1.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                String strTmp = "点击Button01";
                //Ev1.setText(strTmp);
                Toast.makeText(getApplicationContext(), "默认Toast样式",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, DemoActivityOne.class);
                intent.putExtra(DemoActivityOne.EXTRA_DEVICE_TYPE_ID_KEY, 123);
                startActivity(intent);
            }

        });

        //
        Button Btn2 = (Button)findViewById(R.id.button2);//获取按钮资源
        Btn2.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                //String strTmp = "点击Button01";
                //Ev1.setText(strTmp);
                //Toast.makeText(getApplicationContext(), "默认Toast样式",
                //        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, BleTestActivity.class);
                intent.putExtra(BleTestActivity.EXTRA_PARAM, 123);
                startActivity(intent);
            }

        });

        // 测试MPChart
        Button Btn3 = (Button)findViewById(R.id.button3);//获取按钮资源
        Btn3.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                //String strTmp = "点击Button01";
                //Ev1.setText(strTmp);
                //Toast.makeText(getApplicationContext(), "默认Toast样式",
                //        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, MPChartTestActivity.class);
                intent.putExtra(MPChartTestActivity.EXTRA_PARAM, 123);
                startActivity(intent);
            }

        });

        //PDMInstance a=PDMInstance.getInstance();
        //Log.i()
        //测试单例代码无法调试的问题， 后来找到是由于
        TestInstance tins = TestInstance.getInstance();
    }
}
