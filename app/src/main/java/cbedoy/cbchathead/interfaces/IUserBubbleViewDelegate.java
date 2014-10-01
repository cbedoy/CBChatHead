package cbedoy.cbchathead.interfaces;


import android.view.View;


/**
 * Created by admin on 9/29/14.
 */
public interface IUserBubbleViewDelegate
{
    public View getChatViewFromUser();
    public long getDialogIdFromUser();
    public View getNotificationArrowFromUser();
    public View getNotificationCountFromUser();

}
