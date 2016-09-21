package my.ew.wallpaper;

import android.app.Application;
import android.support.coreutils.BuildConfig;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by isanechek on 21.09.16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlytics);
    }
}
