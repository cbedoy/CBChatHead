package cbedoy.cbchathead.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.util.Timer;
import java.util.TimerTask;

import cbedoy.cbchathead.R;

/**
 * Created by Carlos Bedoy on 14/09/2014.
 */
public class CBChatHeadView {

    private WindowManager mWindowManager;
    private View containerView;
    private LinearLayout bubbleView;
    private int[] mPos = {Utils.getInstance(null).getDialogWidth(), Utils.getInstance(null).getActionBarSize() * 2};
    private boolean mbMoved;
    private boolean mbExpanded;
    private Context context;
    private Handler mHandler;
    private WindowManager.LayoutParams mParams;
    private int mRestX;
    private int mRestY;
    private Timer mAnimationTimer;
    private TimerTaskAnimation mTimerTaskAnimation;

    public CBChatHeadView(Context context){
        this.context = context;
        this.mHandler = new Handler();
        this.initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        bubbleView = (LinearLayout) inflater.inflate(R.layout.bubble, null);
        ViewGroup.LayoutParams layoutParams = bubbleView.findViewById(R.id.bubble_id).getLayoutParams();
        layoutParams.height = Utils.getInstance(null).getPixelsFromDp(48);
        layoutParams.width = Utils.getInstance(null).getPixelsFromDp(48);
        bubbleView.findViewById(R.id.bubble_id).setLayoutParams(layoutParams);
        containerView = inflater.inflate(R.layout.content_view, null);
        containerView.setPivotX(0);
        containerView.setPivotY(0);
        containerView.setScaleX(0.0f);
        containerView.setScaleY(0.0f);
        containerView.setAlpha(0.0f);
        containerView.findViewById(R.id.b_facebook).setOnClickListener(clickListener);
        containerView.findViewById(R.id.b_twitter).setOnClickListener(clickListener);
        containerView.findViewById(R.id.b_github).setOnClickListener(clickListener);
        ((WebView)containerView.findViewById(R.id.webView)).loadUrl("http://cbedoy.github.io/");
        bubbleView.addView(containerView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        mParams.x = mPos[0];
        mParams.y = mPos[1];
        mParams.dimAmount = 0.6f;
        SpringSystem system = SpringSystem.create();
        SpringConfig springConfig = new SpringConfig(200, 20);
        final Spring contentSpring = system.createSpring();
        contentSpring.setSpringConfig(springConfig);
        contentSpring.setCurrentValue(0.0);
        contentSpring.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                //Log.d(TAG, "hardware acc = " + containerView.isHardwareAccelerated() + ", layer type = " + containerView.getLayerType());
                float value = (float) spring.getCurrentValue();
                float clampedValue = Math.min(Math.max(value, 0.0f), 1.0f);
                containerView.setScaleX(value);
                containerView.setScaleY(value);
                containerView.setAlpha(clampedValue);
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                containerView.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onSpringActivate(Spring spring) {
                containerView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });

        final Spring bubbleSpring = system.createSpring();
        bubbleSpring.setSpringConfig(springConfig);
        bubbleSpring.setCurrentValue(1.0);
        bubbleSpring.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                mParams.x = (int) (mPos[0] * value);
                mParams.y = (int) (mPos[1] * value);
                mWindowManager.updateViewLayout(bubbleView, mParams);
                if (spring.isOvershooting() && contentSpring.isAtRest()) {
                    contentSpring.setEndValue(1.0);
                }
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                if (spring.currentValueIsApproximately(1.0)) {
                    mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mWindowManager.updateViewLayout(bubbleView, mParams);
                }
            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {

            }
        });


        bubbleView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mTimerTaskAnimation != null)
                        {
                            mTimerTaskAnimation.cancel();
                            mAnimationTimer.cancel();
                        }
                        mbMoved = false;
                        initialX = mParams.x;
                        initialY = mParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (mbMoved) {
                            mTimerTaskAnimation = new TimerTaskAnimation();
                            mAnimationTimer = new Timer();
                            mAnimationTimer.schedule(mTimerTaskAnimation, 0, 30);
                            return true;
                        }
                        if (!mbExpanded) {
                            bubbleView.getLocationOnScreen(mPos);
                            mPos[1] -= Utils.getInstance(null).getActionBarSize();
                            mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                            mParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            mParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                            bubbleSpring.setEndValue(0.0);
                        } else {
                            mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            mParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                            bubbleSpring.setEndValue(1.0);
                            contentSpring.setEndValue(0.0);
                        }
                        mbExpanded = !mbExpanded;
                        mWindowManager.updateViewLayout(bubbleView, mParams);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        mParams.x = initialX + deltaX;
                        mParams.y = initialY + deltaY;
                        if (deltaX * deltaX + deltaY * deltaY >= 30) {
                            mbMoved = true;
                            mWindowManager.updateViewLayout(bubbleView, mParams);
                        }
                        return true;
                }
                return false;
            }
        });

        mWindowManager.addView(bubbleView, mParams);

    }

    private class TimerTaskAnimation extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (Math.abs(mParams.x - mRestX) < 2 && Math.abs(mParams.y - mRestY) < 2) {
                        TimerTaskAnimation.this.cancel();
                        mAnimationTimer.cancel();
                    }
                    mParams.x = (2 * (mParams.x - mRestX)) / 3 + mRestX;
                    mParams.y = (2 * (mParams.y - mRestY)) / 3 + mRestY;
                    mWindowManager.updateViewLayout(bubbleView, mParams);
                }
            });
        }

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.b_facebook)
                Toast.makeText(context, "Facebook selected", Toast.LENGTH_LONG).show();
            else if (view.getId() == R.id.b_github)
                Toast.makeText(context, "Github selected", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "Twitter selected", Toast.LENGTH_LONG).show();
        }
    };
}