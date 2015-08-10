package my.ew.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import my.elite.wallpapers.R;
import my.ew.wallpaper.utils.PreferencesHelper;

public class Splash extends Activity {

    private static int SPLASH_OUT = 1500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splah_activity);

        checkEnableGS();
    }

    @Override
    public void onResume() {
        super.onResume();
        initStop();
    }

    private void initStop() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent start = new Intent(Splash.this, Wallpaper.class);
                startActivity(start);
                finish();
            }
        }, SPLASH_OUT);
    }

    private void checkEnableGS() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if(status == ConnectionResult.SUCCESS) {
            // если есть
            PreferencesHelper.enableGAPPS(this);
        } else {
            // если нету сервисов
            PreferencesHelper.disableGAPPS(this);
        }
    }
}
