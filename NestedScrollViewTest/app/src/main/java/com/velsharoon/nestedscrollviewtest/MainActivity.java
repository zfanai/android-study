package com.velsharoon.nestedscrollviewtest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //内部嵌套一个recyclerview的nestedscrollview 未完全实现
//        setContentView(R.layout.main1);
//        initNestedScrollView();

        //自己实现的嵌套滑动的固顶效果
        setContentView(R.layout.main2);
    }

    private void initNestedScrollView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        MyNestedScrollView nestedScrollView = (MyNestedScrollView) findViewById(R.id.nestedscrollview);
        nestedScrollView.setNestedScrollChild(recyclerView);
        initRecyclerView(recyclerView);
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<String> datas = initData();
        final RecyclerView.Adapter adapter = new MyAdapter(datas);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> initData() {
        ArrayList<String> mDatas = new ArrayList<String>();
        for (int i = 0; i < 15; i++) {
            mDatas.add("这是第" + i + "个");
        }
        return mDatas;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private List<String> mData;

        public MyAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new MyViewHolder(inflater.inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.textview.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textview;

        public MyViewHolder(View view) {
            super(view);
            textview = (TextView) view.findViewById(R.id.text);
        }
    }
}
