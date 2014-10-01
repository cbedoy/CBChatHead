package cbedoy.cbchathead.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import cbedoy.cbchathead.service.CBService;
import cbedoy.cbchathead.R;
import cbedoy.cbchathead.utils.Utils;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.getInstance(this);
        getActionBar().hide();
        startService(new Intent(getApplicationContext(), CBService.class));
    }
}
