package com.medtrum.androidstudy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MPChartTestActivity extends AppCompatActivity {
    public static final String EXTRA_PARAM = "extra_param";
    private static final String TAG = MPChartTestActivity.class.getSimpleName();

    private CombinedChart mChart;

    private static final String GLUCOSE_HIGH_LIMIT_DATA_SET = "GlucoseHighLimitDataSet";
    private static final String GLUCOSE_DATA_SET = "GlucoseDataSet";
    private static final String CALIBRATION_DATA_SET = "CalibrationDataSet";
    private static final String HYDRATION_DATA_SET = "HydrationDataSet";
    private static final String NEW_DATA_SET = "NewDataSet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpchart_test);

        initView();
        initChart();
    }

    private void initView(){
        //Button btn1 = (Button)findViewById(R.id.button1);//获取按钮资源
        //Button btn2 = (Button)findViewById(R.id.button2);//获取按钮资源
        //Button btn3 = (Button)findViewById(R.id.button3);//获取按钮资源
        mChart=(CombinedChart)findViewById(R.id.basalSetChart);
    }

    private void initChart() {

        LimitLine limitLine2 = new LimitLine((float)3.1);
        limitLine2.setLineWidth(1f);
        limitLine2.setLineColor(Color.argb(255, 255, 42, 0));

        //横坐标格式化
        IAxisValueFormatter xAxisFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int intValue = Math.round(value);
                int hour = intValue / 60;
                int minute = intValue % 60;
                if (hour < 0) {
                    hour += 24;
                }
                return String.format("%02d:%02d", hour, minute);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.argb(255, 195, 196, 200));
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setGridColor(Color.argb(255, 221, 222, 226));
        xAxis.setAxisLineColor(Color.argb(255, 221, 222, 226));
        xAxis.setGridLineWidth(1f);
        xAxis.setAxisLineWidth(1f);
        xAxis.setLabelCount(5, true);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(60f); // only intervals of 1 day
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setAxisMaximum(24 * 60);
        xAxis.setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(false);

        mChart.setDrawBorders(true);
        mChart.setBorderColor(Color.argb(255, 221, 222, 226));
        mChart.setBorderWidth(1f);

        final YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.argb(255, 166, 167, 171));
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setGridColor(Color.argb(255, 221, 222, 226));
        leftAxis.setAxisLineColor(Color.argb(255, 221, 222, 226));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setAxisLineWidth(1f);
//        leftAxis.setLabelCount(6, true);
        leftAxis.setLabelCount(6);

        mChart.setDrawBorders(true);
        mChart.setBorderColor(Color.argb(255, 221, 222, 226));
        mChart.setBorderWidth(1f);

        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(limitLine2);
        leftAxis.setDrawLimitLinesBehindData(true);

        //if (MyApplication.getUnitGlucose() == 1) {
            leftAxis.setAxisMaximum(22.201f);
            leftAxis.setAxisMinimum(1.669f);

            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float v, AxisBase axisBase) {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    numberFormat.setMaximumFractionDigits(2);
                    numberFormat.setMinimumFractionDigits(2);
                    if (v != 1.669f) {
                        numberFormat.setMaximumFractionDigits(1);
                        numberFormat.setMinimumFractionDigits(1);
                    }
                    return numberFormat.format(v);
                }

                @Override
                public int getDecimalDigits() {
                    return 0;
                }
            });

        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(xAxis.getAxisMinimum(), (float)8.9));
        values.add(new Entry(xAxis.getAxisMaximum(), (float)8.9));

        LineDataSet dataSet = new LineDataSet(values, GLUCOSE_HIGH_LIMIT_DATA_SET);
        dataSet.setColor(Color.TRANSPARENT);
        dataSet.setHighlightEnabled(false);
        dataSet.setDrawValues(false);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setFillColor(Color.argb((int) (255 / 0.3), 145, 243, 137));
        dataSet.setLineWidth(1f);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);
        dataSet.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return (float)4.7;
            }
        });
        LineData lineData = new LineData(dataSet);

        CombinedData data = new CombinedData();
        data.setData(lineData);

        mChart.setData(data);
        mChart.invalidate();
    }




}