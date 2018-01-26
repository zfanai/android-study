package com.medtrum.androidstudy;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class AnimaActivity extends AppCompatActivity {
    public static final String EXTRA_PARAM = "extra_param";

    private ImageView radarbttom;
    private ImageView radartop;
    private ImageView mAnnularImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anima);

        mAnnularImg = (ImageView) findViewById(R.id.radar_img);
        radartop = (ImageView) findViewById(R.id.radar_top_img);
        radarbttom = (ImageView) findViewById(R.id.radar_bttom_img);

        startAnima();
        startcircularAnima();
    }

    AnimationSet grayAnimal;

    private void startannularAnimat() {
        mAnnularImg.setVisibility(View.VISIBLE);
        AnimationSet annularAnimat = getAnimAnnular();
        annularAnimat.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnnularImg.setVisibility(View.GONE);
            }
        });
        mAnnularImg.startAnimation(annularAnimat);
    }

    private void startwhiteAnimal() {
        AnimationSet whiteAnimal = playHeartbeatAnimation();
        whiteAnimal.setRepeatCount(0);
        whiteAnimal.setDuration(700);
        radartop.setVisibility(View.VISIBLE);
        radartop.startAnimation(whiteAnimal);
        whiteAnimal.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnnularImg.setVisibility(View.GONE);
                radartop.setVisibility(View.GONE);
                startcircularAnima();
            }
        });

    }

    private void startcircularAnima() {
        grayAnimal = playHeartbeatAnimation();
        radarbttom.startAnimation(grayAnimal);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startwhiteAnimal();
            }
        }, 400);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startannularAnimat();
            }
        }, 600);
    }

    /**
     * 打开界面
     */
    private void startAnima() {
        // 中心圆形的旋转动画
        Animation operatingAnim = AnimationUtils
                .loadAnimation(this, R.anim.sss);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        findViewById(R.id.radar_imageing).startAnimation(operatingAnim);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 两个圆环动画
     *
     * @return
     */
    private AnimationSet playHeartbeatAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        // animationSet.addAnimation(new AlphaAnimation(1.0f, 0.9f));
        sa.setDuration(900);
        sa.setFillAfter(true);
        sa.setRepeatCount(0);
        sa.setInterpolator(new LinearInterpolator());
        animationSet.addAnimation(sa);
        return animationSet;
    }

    /**
     * 最外围环形动画
     *
     * @return
     */
    private AnimationSet getAnimAnnular() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        // 透明度变化
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.1f));
        animationSet.setDuration(400);
        sa.setDuration(500);
        sa.setFillAfter(true);
        sa.setRepeatCount(0);
        sa.setInterpolator(new LinearInterpolator());
        animationSet.addAnimation(sa);
        return animationSet;
    }
}
