package my.ew.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import io.fabric.sdk.android.Fabric;
import my.elite.wallpapers.R;
import my.ew.wallpaper.utils.PreferencesHelper;
import timber.log.Timber;

public class Splash extends Activity {

    private static final String TAG = Splash.class.getSimpleName();

    private static int SPLASH_OUT = 1500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.splah_activity);

        checkEnableGS();
        initStop();
    }

    private void initStop() {
        Timber.d("splash initStop");
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
        Timber.d("checkEnableGS");
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
