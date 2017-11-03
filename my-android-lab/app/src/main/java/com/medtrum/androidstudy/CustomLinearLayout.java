package com.medtrum.androidstudy;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class CustomLinearLayout extends LinearLayout {
    private final String TAG="coderep";
    public CustomLinearLayout(final Context ctx, AttributeSet attrs){
        super(ctx, attrs);
    }

    // LinearLayout只接收到了DOWN事件，估计是在上层进行了过滤。根据不同的View类型接收到不同的MotionEvent
    // 事件。
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