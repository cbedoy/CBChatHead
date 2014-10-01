package cbedoy.cbchathead.views.bubble;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import cbedoy.cbchathead.interfaces.IBubbleChatViewDelegate;
import cbedoy.cbchathead.interfaces.IUserBubbleViewDelegate;
import cbedoy.cbchathead.utils.Utils;
import cbedoy.cbchathead.utils.FlingListener;
import cbedoy.cbchathead.views.trash.RemoveView;

/**
 * Created by Carlos Bedoy on 14/09/2014.
 */
public class CBChatHeadView extends LinearLayout implements IBubbleChatViewDelegate
{

    private final WindowManager.LayoutParams params;
    private final GestureDetector mGestureDetector;
    private final MoveAnimator mAnimator;
    private final RemoveView mRemoveView;
    private int[] mPos = {0, 0};
    private WindowManager mWindowManager;
    private Spring contentSpring;
    private Spring bubbleSpring;
    private SpringSystem system;
    private SpringConfig springConfig;
    private LinearLayout mBubbles;
    private ChatViewContainer mContent;

    private boolean mbExpanded;
    private boolean shouldFlingAway = true;
    private boolean shouldStickToWall = true;
    private boolean isBeingDragged;
    private int mWidth;
    private int mHeight;
    private long lastTouchDown;
    private long TOUCH_TIME_THRESHOLD = 200;
    private float lastXPose;
    private float lastYPose;
    private boolean mbMoved;
    private int destX;
    private int destY;
    private boolean isOpen;

    //Validation flags
    private long currentDialogId;
    private long CLOSECHAT = -100;

    public CBChatHeadView(Context context)
    {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        mBubbles = new LinearLayout(getContext());
        mBubbles.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mContent = new ChatViewContainer(getContext());
        mPos[0] = (int) ((getResources().getDisplayMetrics().widthPixels - Utils.getInstance(null).convertDPtoPixels(60)) / 2);
        mPos[1] = (getResources().getDisplayMetrics().heightPixels - CBChatHeadView.this.getWidth()) / 2;
        mContent.setPivotX(0);
        mContent.setPivotY(0);
        mContent.setScaleX(0.0f);
        mContent.setScaleY(0.0f);
        mContent.setAlpha(0.0f);

        addView(mBubbles);
        addView(mContent);

        mWindowManager = (WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);



        mGestureDetector = new GestureDetector(context, new FlingListener());
        mAnimator = new MoveAnimator();
        mRemoveView = new RemoveView(getContext());
        params.dimAmount = 0.6f;
        system = SpringSystem.create();
        springConfig = new SpringConfig(200, 25);
        contentSpring = system.createSpring();
        contentSpring.setSpringConfig(springConfig);
        contentSpring.setCurrentValue(0.0);
        contentSpring.addListener(new SpringListener()
        {
            @Override
            public void onSpringUpdate(Spring spring)
            {
                float value = (float) spring.getCurrentValue();
                float clampedValue = Math.min(Math.max(value, 0.0f), 1.0f);
                mContent.setScaleX(value);
                mContent.setScaleY(value);
                mContent.setAlpha(clampedValue);
            }

            @Override
            public void onSpringAtRest(Spring spring)
            {
                mContent.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onSpringActivate(Spring spring)
            {
                mContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override
            public void onSpringEndStateChange(Spring spring)
            {

            }
        });
        bubbleSpring = system.createSpring();
        bubbleSpring.setSpringConfig(springConfig);
        bubbleSpring.setCurrentValue(1.0);
        bubbleSpring.addListener(new SpringListener()
        {
            @Override
            public void onSpringUpdate(Spring spring)
            {
                float value = (float) spring.getCurrentValue();
                params.x = (int) (mPos[0] * value);
                params.y = (int) (mPos[1] * value);
                mWindowManager.updateViewLayout(CBChatHeadView.this, params);
                if (spring.isOvershooting() && contentSpring.isAtRest())
                {
                    contentSpring.setEndValue(1.0);
                }
            }

            @Override
            public void onSpringAtRest(Spring spring)
            {

            }

            @Override
            public void onSpringActivate(Spring spring)
            {

            }

            @Override
            public void onSpringEndStateChange(Spring spring)
            {

            }
        });

        mWindowManager.addView(this, params);
        updateSize();
        goToWall();
        changeToCloseChat();
        currentDialogId = CLOSECHAT;
    }


    private void changeToOpenChat()
    {
        for (int i=0; i<mBubbles.getChildCount(); i++)
        {
            View view = mBubbles.getChildAt(i);
            view.setVisibility(VISIBLE);
        }
    }

    private void changeToCloseChat()
    {
        for (int i=1; i < mBubbles.getChildCount(); i++)
        {
            View view = mBubbles.getChildAt(i);
            view.setVisibility(GONE);
        }
    }

    @Override
    public void addBubble(View view) {
        mBubbles.addView(view);
        changeToCloseChat();
        if(mBubbles.getChildCount() == 4){
            mBubbles.removeView(mBubbles.getChildAt(0));
        }

    }

    @Override
    public boolean isOpenChat() {
        return mbExpanded;
    }


    @Override
    public void closeCurrentBubble() {
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        bubbleSpring.setEndValue(1.0);
        contentSpring.setEndValue(0.0);
        changeToCloseChat();
        mbExpanded = !mbExpanded;
        hideRemoveView();
        mWindowManager.updateViewLayout(CBChatHeadView.this, params);
    }

    @Override
    public void userTouch(IUserBubbleViewDelegate userBubbleViewDelegate, MotionEvent motionEvent)
    {
        boolean eaten = false;
        if (shouldFlingAway)
        {
            eaten = mGestureDetector.onTouchEvent(motionEvent);
        }
        if (eaten)
        {
            flingAway();
        }
        else
        {
            float x = motionEvent.getRawX();
            float y = motionEvent.getRawY();
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN)
            {
                showRemoveView();
                lastTouchDown = System.currentTimeMillis();
                mAnimator.stop();
                updateSize();
                isBeingDragged = true;
                mbMoved = false;
            }
            else if (action == MotionEvent.ACTION_UP)
            {
                if (System.currentTimeMillis() - lastTouchDown < TOUCH_TIME_THRESHOLD | !mbMoved)
                {
                    boolean noRebound = false;
                    //USER GOING TO OPEN THE CHAT

                    if(!isOpen)
                    {
                        destX = (int) x;
                        destY = (int) y;
                    }
                    if(currentDialogId == CLOSECHAT)
                    {
                        currentDialogId = userBubbleViewDelegate.getDialogIdFromUser();
                        if(mContent.getChildCount()>0)mContent.removeAllViews();
                        mContent.addView(userBubbleViewDelegate.getChatViewFromUser(), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
                        userBubbleViewDelegate.getNotificationArrowFromUser().setVisibility(View.VISIBLE);
                    }
                    //USER GOING TO CLOSE CURRENT CHAT
                    else if(currentDialogId == userBubbleViewDelegate.getDialogIdFromUser())
                    {
                        currentDialogId = CLOSECHAT;
                        userBubbleViewDelegate.getNotificationArrowFromUser().setVisibility(View.GONE);
                    }
                    //USER SELECTED A OTHER CHAT HEAD WITH CHAT OPEN
                    else
                    {
                        currentDialogId = userBubbleViewDelegate.getDialogIdFromUser();
                        if(mContent.getChildCount()>0)mContent.removeAllViews();
                        mContent.addView(userBubbleViewDelegate.getChatViewFromUser(), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
                        noRebound = true;
                    }
                    if(!noRebound) {
                        if (!mbExpanded && currentDialogId > 0) {
                            CBChatHeadView.this.getLocationOnScreen(mPos);
                            params.width = WindowManager.LayoutParams.MATCH_PARENT;
                            params.height = WindowManager.LayoutParams.MATCH_PARENT;
                            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                            bubbleSpring.setEndValue(0.0);
                            currentDialogId = userBubbleViewDelegate.getDialogIdFromUser();
                            mbExpanded = !mbExpanded;
                            mWindowManager.updateViewLayout(CBChatHeadView.this, params);
                            changeToOpenChat();
                            hideRemoveView();
                        } else {
                            params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                            bubbleSpring.setEndValue(1.0);
                            contentSpring.setEndValue(0.0);
                            isBeingDragged = true;
                            currentDialogId = CLOSECHAT;
                            mbExpanded = !mbExpanded;
                            mWindowManager.updateViewLayout(CBChatHeadView.this, params);
                            changeToCloseChat();
                        }
                    }
                }
                else
                {
                    hideRemoveView();
                    isBeingDragged = true;
                    goToWall();
                }
            }
            else if (action == MotionEvent.ACTION_MOVE)
            {
                mbMoved = true;
                if (isBeingDragged && !isOpenChat())
                {
                    move(x - lastXPose, y - lastYPose);
                }
            }

            lastXPose = x;
            lastYPose = y;
        }
        updateSize();
    }


    private void updateSize()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mWidth = (metrics.widthPixels - CBChatHeadView.this.getWidth()) / 2;
        mHeight = (metrics.heightPixels - CBChatHeadView.this.getHeight()) / 2;
    }

    private void flingAway() {
        if (shouldFlingAway) {
            int y = getContext().getResources().getDisplayMetrics().heightPixels / 2;
            int x = 0;
            mAnimator.start(x, y);
            destroy();
        }
    }

    private void showRemoveView() {
        if (mRemoveView != null && shouldFlingAway) {
            mRemoveView.show();
        }
    }

    private void hideRemoveView() {
        if (mRemoveView != null && shouldFlingAway) {
            mRemoveView.hide();
        }
    }

    private void goToWall() {
        if (shouldStickToWall) {
            float nearestXWall = params.x >= 0 ? mWidth : -mWidth;
            float nearestYWall = params.y > 0 ? mHeight : -mHeight;
            if (Math.abs(params.x - nearestXWall) < Math.abs(params.y - nearestYWall)) {
                mAnimator.start(nearestXWall, params.y);
            } else {
                mAnimator.start(params.x, nearestYWall);
            }
        }
    }

    private void move(float deltaX, float deltaY) {
        params.x += deltaX;
        params.y += deltaY;
        if (mRemoveView != null && shouldFlingAway) {
            mRemoveView.onMove(params.x, params.y);
        }
        mWindowManager.updateViewLayout(CBChatHeadView.this, params);
        if (shouldFlingAway && !isBeingDragged && Math.abs(params.x) < 50
                && Math.abs(params.y - (getContext().getResources().getDisplayMetrics().heightPixels / 2)) < 250) {
            flingAway();
        }
    }

    public void destroy()
    {
        mWindowManager.removeView(CBChatHeadView.this);
        if (mRemoveView != null)
        {
            mRemoveView.destroy();
        }
    }

    private class MoveAnimator implements Runnable
    {

        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        private void start(float x, float y)
        {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run()
        {
            if (CBChatHeadView.this != null && CBChatHeadView.this.getParent() != null) {
                float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                float deltaX = (destinationX - params.x) * progress;
                float deltaY = (destinationY - params.y) * progress;
                move(deltaX, deltaY);
                if (progress < 1)
                {
                    handler.post(this);
                }
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }

    }





}

