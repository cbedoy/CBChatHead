package cbedoy.cbchathead.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.ref.WeakReference;

/**
 * Created by Carlos Bedoy on 14/09/2014.
 */
public class SimpleAnimator {

    private WeakReference<View> mViewRef;
    private int animation;

    public SimpleAnimator(View view, int anim) {
        this.animation = anim;
        this.mViewRef = new WeakReference<View>(view);
    }

    public void startAnimation() {
        startAnimation(null);
    }

    public void startAnimation(Animation.AnimationListener listener) {
        mViewRef.get().clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(mViewRef.get().getContext(), animation);
        if(listener != null) {
            anim.setAnimationListener(listener);
        }
        anim.setFillAfter(true);
        mViewRef.get().startAnimation(anim);
    }

}
