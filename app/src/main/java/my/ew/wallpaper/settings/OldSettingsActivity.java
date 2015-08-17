package my.ew.wallpaper.settings;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import my.elite.wallpapers.R;
import my.ew.wallpaper.utils.PreferencesHelper;

/**
 * Created by isanechek on 25.07.15.
 */
public class OldSettingsActivity extends PreferenceActivity {

//    private static final String LOG_TAG = "OldSettingActivity";

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

        final CheckBoxPreference ach = (CheckBoxPreference) findPreference("anal");
        if (!PreferencesHelper.isAdsDisabled()) {
            ach.setEnabled(false);
            ach.setSummary(R.string.enable_after_inapp);
        }

        final PreferenceScreen sc = (PreferenceScreen) findPreference("perm");
        sc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Log.d(LOG_TAG, "onPrefClick perm");
                final View customView;
                try {
                    customView = LayoutInflater.from(OldSettingsActivity.this).inflate(R.layout.dialog_webview, null);
                } catch (InflateException e) {
                    throw new IllegalStateException("This device does not support Web Views.");
                }
                final AlertDialog.Builder dialogAP = new AlertDialog.Builder(OldSettingsActivity.this, R.style.MyAlertDialogStyle);
                dialogAP.setView(customView);
                dialogAP.setTitle(R.string.about_permission);
                dialogAP.setCancelable(true);
                dialogAP.setPositiveButton(R.string.close_about_dialog, null);
                dialogAP.show();
                final WebView webView = (WebView) customView.findViewById(R.id.webview);
                try {
                    // Load from changelog.html in the assets folder
                    StringBuilder buf = new StringBuilder();
                    InputStream json = OldSettingsActivity.this.getAssets().open("aboutpermission.html");
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
                }
                return false;
            }
        });

        final PreferenceScreen presc = (PreferenceScreen) findPreference("libs");
        presc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Log.d(LOG_TAG, "onPrefClick libs");
                final View customView;
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
                }
                return false;
            }
        });

        final PreferenceScreen sss = (PreferenceScreen) findPreference("whatisnew");
        sss.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final View customView;

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
}
