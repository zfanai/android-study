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
    }
}
