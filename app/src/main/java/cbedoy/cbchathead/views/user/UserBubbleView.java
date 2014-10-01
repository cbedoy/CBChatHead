package cbedoy.cbchathead.views.user;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cbedoy.cbchathead.R;
import cbedoy.cbchathead.interfaces.IBubbleChatViewDelegate;
import cbedoy.cbchathead.interfaces.IUserBubbleViewDelegate;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by admin on 10/1/14.
 */
public class UserBubbleView extends FrameLayout implements IUserBubbleViewDelegate
{
    private Bundle bundle;
    private CircleImageView userAvatarView;
    private TextView notificationCount;
    private View notificationArrow;
    private IBubbleChatViewDelegate bubbleChatViewDelegate;
    private UserContent userContent;

    private boolean isOpenChat;

    public UserBubbleView(Context context)
    {
        super(context);
    }

    private void createContentView()
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.bubble, null);
        userContent = new UserContent(getContext());
        userAvatarView = (CircleImageView) view.findViewById(R.id.user_avatar);
        notificationCount = (TextView) view.findViewById(R.id.user_message_count);
        notificationArrow = view.findViewById(R.id.user_arrow_indicator);

        addView(view);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                        bubbleChatViewDelegate.userTouch(UserBubbleView.this, motionEvent);
                }
                return true;
            }
        });
    }

    public void setBubbleChatViewDelegate(IBubbleChatViewDelegate bubbleChatViewDelegate) {
        this.bubbleChatViewDelegate = bubbleChatViewDelegate;
    }


    @Override
    public View getChatViewFromUser() {
        return this.userContent.getView();
    }

    @Override
    public long getDialogIdFromUser() {
        return 0;
    }

    @Override
    public View getNotificationArrowFromUser() {
        return notificationArrow;
    }

    @Override
    public View getNotificationCountFromUser() {
        return notificationCount;
    }


}
