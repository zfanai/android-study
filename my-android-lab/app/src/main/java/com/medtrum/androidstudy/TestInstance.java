package com.medtrum.androidstudy;

import android.util.Log;

public  class TestInstance {

    private static class InstanceHolder {
        private static final TestInstance sInstance = new TestInstance();

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