package com.medtrum.androidstudy;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

public class CustomListView extends ListView {
    private final String TAG="coderep";

    public CustomListView(Context ctx, AttributeSet attrs){
        super(ctx, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        Log.i(TAG, "Raw x,y:"+ev.getRawX()+","+ev.getRawY());
        int actEv=ev.getAction();
        String strActEvt="";
        switch (actEv){
            case MotionEvent.ACTION_DOWN:
                strActEvt="DOWN";
                break;
            case MotionEvent.ACTION_UP:
                strActEvt="UP";
                break;
            case MotionEvent.ACTION_MOVE:
                strActEvt="MOVE";
            default:
                break;
        }
        Log.i(TAG, "strActEvt:"+strActEvt);
        return super.onTouchEvent(ev);
    }
}