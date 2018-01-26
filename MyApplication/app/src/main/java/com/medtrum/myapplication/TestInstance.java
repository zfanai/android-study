package com.medtrum.myapplication;

import android.util.Log;

public  class TestInstance {
    private static volatile TestInstance instance;


    private static class InstanceHolder {
        private static final TestInstance sInstance = new TestInstance();

    }



    public static TestInstance getInstance2() {
//        return TestInstance.InstanceHolder.sInstance;
        if (instance==null){
            synchronized (TestInstance.class){
                if (instance==null){
                    instance= new TestInstance();
                    return instance;
                }
            }
        }
        return instance;
    }

    public static TestInstance getInstance() {
        return TestInstance.InstanceHolder.sInstance;
    }

    public TestInstance() {
        //super();
        //this.init();
        int a=123;
        Log.i("df", "sdfe");
    }



}