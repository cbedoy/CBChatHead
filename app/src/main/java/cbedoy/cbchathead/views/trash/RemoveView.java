package cbedoy.cbchathead.views.trash;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;

import cbedoy.cbchathead.R;
import cbedoy.cbchathead.utils.SimpleAnimator;


/**
 * Created by Carlos Bedoy on 14/09/2014.
 */

public class RemoveView {

    private View mLayout;
    private View mButton;
    private View mShadow;
    private ImageView mButtonImage;
    private WindowManager mWindowManager;
    private SimpleAnimator mShowAnim;
    private SimpleAnimator mHideAnim;

    private SimpleAnimator mShadowFadeOut;
    private SimpleAnimator mShadowFadeIn;

    private final int buttonBottomPadding;

    boolean shouldBeResponsive = true;

    public RemoveView(Context context) {
        mLayout = LayoutInflater.from(context).inflate(R.layout.x_holder, null);
        mButton = mLayout.findViewById(R.id.xButton);
        mButtonImage = (ImageView) mLayout.findViewById(R.id.xButtonImg);
        mButtonImage.setImageResource(R.drawable.ic_launcher);
        buttonBottomPadding = mButton.getPaddingBottom();
        mShadow = mLayout.findViewById(R.id.shadow);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        addToWindow(mLayout);
        mShowAnim = new SimpleAnimator(mButton, R.anim.slide_up);
        mHideAnim = new SimpleAnimator(mButton, R.anim.slide_down);
        mShadowFadeIn = new SimpleAnimator(mShadow, android.R.anim.fade_in);
        mShadowFadeOut = new SimpleAnimator(mShadow, android.R.anim.fade_out);
        hide();
    }

    void setIconResId(int id) {
        mButtonImage.setImageResource(id);
    }

    void setShadowBG(int shadowBG) {
        mShadow.setBackgroundResource(shadowBG);
    }

    public void show() {
        if (mLayout != null && mLayout.getParent() == null) {
            addToWindow(mLayout);
        }
        mShadowFadeIn.startAnimation();
        mShowAnim.startAnimation();
    }

    public void hide() {
        mShadowFadeOut.startAnimation();
        mHideAnim.startAnimation(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mLayout != null && mLayout.getParent() != null) {
                    mWindowManager.removeView(mLayout);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void onMove(final float x, final float y) {
        if (shouldBeResponsive) {
            final int xTransformed = (int) Math.abs(x * 100 / (mButton.getContext().getResources().getDisplayMetrics().widthPixels / 2));
            final int bottomPadding = buttonBottomPadding - (xTransformed / 5);
            if (x < 0) {
                mButton.setPadding(0, 0, xTransformed, bottomPadding);
            } else {
                mButton.setPadding(xTransformed, 0, 0, bottomPadding);
            }
        }
    }

    public void destroy() {
        if (mLayout != null && mLayout.getParent() != null) {
            mWindowManager.removeView(mLayout);
        }
        mLayout = null;
        mWindowManager = null;
    }

    private void addToWindow(View layout) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        mWindowManager.addView(layout, params);
    }
}
