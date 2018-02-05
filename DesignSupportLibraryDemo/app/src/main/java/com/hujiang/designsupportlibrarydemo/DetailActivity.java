package com.hujiang.designsupportlibrarydemo;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.os.Build;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_4);

        //Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //int version = android.provider.Settings.System.getInt(
        //        getApplicationContext().getContentResolver(),
        //        android.provider.Settings.System.SYS_PROP_SETTING_VERSION,
        //        3);
        Log.i("a", "version:"+Build.VERSION.SDK_INT);

        //CollapsingToolbarLayout collapsingToolbar =
        //        (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        //collapsingToolbar.setTitle("我的课程");
    }

    public void checkin(View view) {
        Snackbar.make(view, "checkin success!", Snackbar.LENGTH_SHORT).show();
    }
}
