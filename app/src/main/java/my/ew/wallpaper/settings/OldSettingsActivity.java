package my.ew.wallpaper.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import my.elite.wallpapers.R;

/**
 * Created by isanechek on 25.07.15.
 */
public class OldSettingsActivity extends PreferenceActivity {

    private static final String TAG = OldSettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d(LOG_TAG, "onCreate");

        addPreferencesFromResource(R.xml.settings);

        final PreferenceScreen screen = (PreferenceScreen) findPreference("about_us");
        screen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Log.d(LOG_TAG, "onPrefClick About us");
                final View cv;

                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Dialog")
                        .putContentType("Old About Dialog"));

                try {
                    cv = LayoutInflater.from(OldSettingsActivity.this).inflate(R.layout.about_activity, null);
                } catch (InflateException e) {
                    throw new IllegalStateException("This device does not support Web Views.");
                }
                AlertDialog.Builder dialogA = new AlertDialog.Builder(OldSettingsActivity.this, R.style.MyAlertDialogStyle);
                dialogA.setView(cv);
                dialogA.setCancelable(true);
                dialogA.setPositiveButton(R.string.close_about_dialog, null);
                dialogA.show();
                return false;
            }
        });


        final CheckBoxPreference ccb = (CheckBoxPreference) findPreference("crop");
        ccb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (o.toString().equals("true")) {
//                    AnalyticsManager.getInstance().trackEvent("Setting", "crop", "true", 1);
                }
                return true;
            }
        });

//        final CheckBoxPreference ach = (CheckBoxPreference) findPreference("anal");
//        if (!PreferencesHelper.isAdsDisabled()) {
//            ach.setEnabled(false);
//            ach.setTitle(R.string.disable_anal);
//            ach.setSummary(R.string.enable_after_inapp);
//        }
//        ach.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object o) {
//                if (o.toString().equals("false")) {
//                    AlertDialog.Builder rd = new AlertDialog.Builder(OldSettingsActivity.this, R.style.MyAlertDialogStyle);
//                    rd.setMessage(R.string.restart);
//                    rd.setPositiveButton(R.string.now_restart, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Intent rApp = OldSettingsActivity.this.getPackageManager().getLaunchIntentForPackage(OldSettingsActivity.this.getPackageName());
//                            rApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(rApp);
//                        }
//                    });
//                    rd.setNegativeButton(R.string.later_restart, null);
//                    rd.setCancelable(true);
//                    rd.show();
//                } else {
//                    Toast.makeText(OldSettingsActivity.this, R.string.thks, Toast.LENGTH_SHORT).show();
//                    Answers.getInstance().logCustom(new CustomEvent("GA on"));
//                }
//                return true;
//            }
//        });

//        final PreferenceScreen sc = (PreferenceScreen) findPreference("perm");
//        sc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
////                Log.d(LOG_TAG, "onPrefClick perm");
//                final View customView;
//
//                Answers.getInstance().logContentView(new ContentViewEvent()
//                        .putContentName("Dialog")
//                        .putContentType("Old Permission Dialog"));
//
//                try {
//                    customView = LayoutInflater.from(OldSettingsActivity.this).inflate(R.layout.dialog_webview, null);
//                } catch (InflateException e) {
//                    throw new IllegalStateException("This device does not support Web Views.");
//                }
//                final AlertDialog.Builder dialogAP = new AlertDialog.Builder(OldSettingsActivity.this, R.style.MyAlertDialogStyle);
//                dialogAP.setView(customView);
//                dialogAP.setTitle(R.string.about_permission);
//                dialogAP.setCancelable(true);
//                dialogAP.setPositiveButton(R.string.close_about_dialog, null);
//                dialogAP.show();
//                final WebView webView = (WebView) customView.findViewById(R.id.webview);
//                try {
//                    // Load from changelog.html in the assets folder
//                    StringBuilder buf = new StringBuilder();
//                    InputStream json = OldSettingsActivity.this.getAssets().open("aboutpermission.html");
//                    BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
//                    String str;
//                    while ((str = in.readLine()) != null)
//                        buf.append(str);
//                    in.close();
//
//                    // Inject color values for WebView body background and links
//                    final int accentColor = getResources().getColor(R.color.my_accent_color);
//                    webView.loadData(buf.toString()
//                            .replace("{link-color}", colorToHex(shiftColor(accentColor, true)))
//                            .replace("{link-color-active}", colorToHex(accentColor))
//                            , "text/html", "UTF-8");
//                } catch (Throwable e) {
//                    webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
//                    Crashlytics.logException(e);
//                }
//                return false;
//            }
//        });

        final PreferenceScreen presc = (PreferenceScreen) findPreference("libs");
        presc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final View customView;

                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Dialog")
                        .putContentType("Old About Libs Dialog"));

                try {
                    customView = LayoutInflater.from(OldSettingsActivity.this).inflate(R.layout.dialog_webview, null);
                } catch (InflateException e) {
                    throw new IllegalStateException("This device does not support Web Views.");
                }
                AlertDialog.Builder dialogAL = new AlertDialog.Builder(OldSettingsActivity.this, R.style.MyAlertDialogStyle);
                dialogAL.setView(customView);
                dialogAL.setCancelable(true);
                dialogAL.setPositiveButton(R.string.close_about_dialog, null);
                dialogAL.show();
                final WebView webView = (WebView) customView.findViewById(R.id.webview);
                try {
                    // Load from changelog.html in the assets folder
                    StringBuilder buf = new StringBuilder();
                    InputStream json = OldSettingsActivity.this.getAssets().open("about_libs.html");
                    BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
                    String str;
                    while ((str = in.readLine()) != null)
                        buf.append(str);
                    in.close();

                    // Inject color values for WebView body background and links
                    final int accentColor = getResources().getColor(R.color.my_accent_color);
                    webView.loadData(buf.toString()
                            .replace("{link-color}", colorToHex(shiftColor(accentColor, true)))
                            .replace("{link-color-active}", colorToHex(accentColor))
                            , "text/html", "UTF-8");
                } catch (Throwable e) {
                    webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
                    Crashlytics.logException(e);
                }
                return false;
            }
        });

        final PreferenceScreen sss = (PreferenceScreen) findPreference("whatisnew");
        sss.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final View customView;

                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Dialog")
                        .putContentType("Old What Is New Dialog"));

                try {
                    customView = LayoutInflater.from(OldSettingsActivity.this).inflate(R.layout.dialog_webview, null);
                } catch (InflateException e) {
                    throw new IllegalStateException("This device does not support Web Views.");
                }
                final AlertDialog.Builder dialogAP = new AlertDialog.Builder(OldSettingsActivity.this, R.style.MyAlertDialogStyle);
                dialogAP.setView(customView);
                dialogAP.setCancelable(true);
                dialogAP.setPositiveButton(R.string.close_about_dialog, null);
                dialogAP.show();
                final WebView webView = (WebView) customView.findViewById(R.id.webview);
                try {
                    // Load from changelog.html in the assets folder
                    StringBuilder buf = new StringBuilder();
                    InputStream json = OldSettingsActivity.this.getAssets().open("changelog.html");
                    BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
                    String str;
                    while ((str = in.readLine()) != null)
                        buf.append(str);
                    in.close();

                    // Inject color values for WebView body background and links
                    final int accentColor = getResources().getColor(R.color.my_accent_color);
                    webView.loadData(buf.toString()
                            .replace("{link-color}", colorToHex(shiftColor(accentColor, true)))
                            .replace("{link-color-active}", colorToHex(accentColor))
                            , "text/html", "UTF-8");
                } catch (Throwable e) {
                    webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
                    Crashlytics.logException(e);
                }
                return false;
            }
        });

    }

    private String colorToHex(int color) {
        return Integer.toHexString(color).substring(2);
    }

    private int shiftColor(int color, boolean up) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= (up ? 1.1f : 0.9f); // value component
        return Color.HSVToColor(hsv);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        GoogleAnalytics.getInstance(this).reportActivityStart(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        GoogleAnalytics.getInstance(this).reportActivityStop(this);
//    }
}
