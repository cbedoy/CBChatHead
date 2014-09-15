package cbedoy.cbchathead;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cbedoy.cbchathead.views.CBChatHeadView;


/**
 * Created by Carlos on 14/09/2014.
 */
public class CBService  extends Service {

    private List<CBChatHeadView> chatHeadViewList;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CBChatHeadView view = new CBChatHeadView(getApplicationContext());


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
