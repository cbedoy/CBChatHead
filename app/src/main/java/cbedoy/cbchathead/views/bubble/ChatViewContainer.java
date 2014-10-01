package cbedoy.cbchathead.views.bubble;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * Created by admin on 10/1/14.
 */
public class ChatViewContainer extends LinearLayout
{

    public ChatViewContainer(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundColor(Color.WHITE);
    }


}
