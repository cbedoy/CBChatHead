package cbedoy.cbchathead.interfaces;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by admin on 9/22/14.
 */
public interface IBubbleChatViewDelegate
{

    public void addBubble(View view);

    public boolean isOpenChat();

    public void closeCurrentBubble();

    public void userTouch(IUserBubbleViewDelegate userBubbleViewDelegate, MotionEvent motionEvent);

}
