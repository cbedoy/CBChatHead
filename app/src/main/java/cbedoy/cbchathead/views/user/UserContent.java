package cbedoy.cbchathead.views.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import cbedoy.cbchathead.R;

/**
 * Created by admin on 10/1/14.
 */
public class UserContent
{
    private View view;

    public UserContent(Context context){
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.content_view, null);
        }
    }

    public View getView() {
        return view;
    }
}
