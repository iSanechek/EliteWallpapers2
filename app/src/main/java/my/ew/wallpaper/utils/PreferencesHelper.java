package my.ew.wallpaper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by isanechek on 24.07.15.
 */
public class PreferencesHelper {

    private static final String TAG = "pref_helper";

    private static final String SETTINGS = "wallpaper_prefs_a";
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    private static final String GAPPS_SETTING = "gappspref";
    private static final String TAG_DISABLED_ADS = "disabledADS";
    protected static final String WALLPAPER_WIDTH_KEY = "wallpaper.width";
    protected static final String WALLPAPER_HEIGHT_KEY = "wallpaper.height";

    private static boolean disabledADS;

    public static boolean isAdsDisabled() {
        Log.d(TAG, "boolean isAdsDisabled");
//		LOG.d("disabledADS = "+disabledADS);
        return disabledADS;
    }

    public static enum Purchase {
        DISABLE_ADS
    };

    public static void savePurchase(Context c, Purchase p, boolean v) {
        Log.d(TAG, "savePurchase");
        SharedPreferences settings = c.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (p) {
            case DISABLE_ADS:
                editor.putBoolean("TAG_DISABLED_ADS", v);
                disabledADS = v;
                Log.d(TAG, "disable ads " + v);
                break;
        }
        editor.apply();
    }

    public static void loadSettings(Context c) {
        Log.d(TAG, "loadSettings");

        SharedPreferences settings = c.getSharedPreferences(SETTINGS, 0);
        if (settings.getAll().size() == 0) {
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.apply();

            disabledADS = false;
            Log.d(TAG, "load setting disable false");
        } else {
            disabledADS = settings.getBoolean(TAG_DISABLED_ADS, false);
            Log.d(TAG, "else get boolean false");
        }

    }

    public static void saveSettings(Context c) {
        Log.d(TAG, "saveSettings");
        SharedPreferences settings = c.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(TAG_DISABLED_ADS, disabledADS);

        editor.apply();
    }
    public static void enableGAPPS(Context c) {
        SharedPreferences settings = c.getSharedPreferences(GAPPS_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("gapps", true);
        editor.apply();
    }

    public static void disableGAPPS(Context c) {
        SharedPreferences settings = c.getSharedPreferences(GAPPS_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("gapps", false);
        editor.apply();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

}
