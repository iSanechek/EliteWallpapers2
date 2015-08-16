package my.ew.wallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;

import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.crashlytics.android.Crashlytics;
import com.devspark.appmsg.AppMsg;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import my.elite.wallpapers.BuildConfig;
import my.elite.wallpapers.R;
import my.ew.wallpaper.settings.OldSettingsActivity;
import my.ew.wallpaper.settings.SettingsActivity;
import my.ew.wallpaper.task.BitmapCropTask;
import my.ew.wallpaper.util.IabHelper;
import my.ew.wallpaper.util.IabResult;
import my.ew.wallpaper.util.Inventory;
import my.ew.wallpaper.util.Purchase;
import my.ew.wallpaper.utils.AnimUtils;
import my.ew.wallpaper.utils.PreferencesHelper;


public class Wallpaper extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = Wallpaper.class.getSimpleName();

    // Сылку нудно будет заменить на актуальную
    private static final String link_other_apps = "";

    private static final String LICENSE_KEY = "";

    private static final String appKey = "";

    private static final int RC_REQUEST = 10001;
    // Это айди покупки - можно и другой
    private static final String ADS_DISABLE = "removeads";

    protected static final float WALLPAPER_SCREENS_SPAN = 2f;
    protected static final String WALLPAPER_WIDTH_KEY = "wallpaper.width";
    protected static final String WALLPAPER_HEIGHT_KEY = "wallpaper.height";
    private static final String PREF_KEY = "wallpaper_prefs";
    private static SharedPreferences mPrefs;

	private Gallery mGallery;
    private ImageView mImageView;
    private boolean mIsWallpaperSet;
    private Bitmap mBitmap;
    private ArrayList<Integer> mThumbs;
    private ArrayList<Integer> mImages;
    private WallpaperLoader mLoader;
    private Toolbar toolbar;
    private LinearLayout fl;
    private ImageButton buyButton;
    private FrameLayout tbCont;

    int aHeight;

    private IabHelper mHelper;

    /**
     * to good time
     */
//    private static final char[] symbols = new char[36];
//    static {
//        for (int idx = 0; idx < 10; ++idx)
//            symbols[idx] = (char) ('0' + idx);
//        for (int idx = 10; idx < 36; ++idx)
//            symbols[idx] = (char) ('a' + idx - 10);
//    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        findWallpapers();
        setContentView(R.layout.activity_wallpaper);
        initUI();

        mPrefs = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);

        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        PreferencesHelper.loadSettings(this);

        if (mPrefs.getBoolean("anal", true)) {
            initAnalytics();
        }

        if (!PreferencesHelper.isWelcomeDone(this)){
            showAboutPermission();
        }

        if (mPrefs.getBoolean("gapps", true)) {
            initBilling();
        } else {
            initADS();
        }
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(clickListener);
        toolbar.inflateMenu(R.menu.wallpaper);
        toolbar.setTitle(R.string.app_name);

        fl = (LinearLayout) findViewById(R.id.showsAds);
        tbCont = (FrameLayout) findViewById(R.id.tb_cont);

        buyButton = (ImageButton) findViewById(R.id.buy_btn);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyBtnEvent();
            }
        });

        mImageView = (ImageView) findViewById(R.id.wallpaper);
        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(new ImageAdapter(this));
        mGallery.setOnItemSelectedListener(this);
        mGallery.setCallbackDuringFling(false);
        mGallery.setSpacing(getResources().getDimensionPixelSize(R.dimen.gallery_spacing));

    }

    private void buyBtnEvent() {
        if (!PreferencesHelper.isAdsDisabled()) {
//            RandomString randomString = new RandomString(36);
//            String payload = randomString.nextString();
            String payload = "";
            mHelper.launchPurchaseFlow(this, ADS_DISABLE, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPrefs.getBoolean("anal", true)) {
            GoogleAnalytics.getInstance(this).reportActivityStart(this);
        }
    }
    
    private Toolbar.OnMenuItemClickListener clickListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			switch (menuItem.getItemId()) {
            case R.id.other_apps:
                otherLink();
//                disableShowADS();
                break;
            case R.id.no_wallpaper:
            	dialogShow();
                break;
                case R.id.settings:
                    settingShow();
                    break;
            case R.id.share:
                share();
//                initShowADS();
                break;
            default:
                break;
			}
			return false;
		}
	};

    public void onSetWallpapers(View view) {
        switch (view.getId()) {
            case R.id.set:
                selectWallpaper(mGallery.getSelectedItemPosition());
                done();
                break;
            default:
                break;
        }
    }

    private void findWallpapers() {
        mThumbs = new ArrayList<Integer>(24);
        mImages = new ArrayList<Integer>(24);

        final Resources resources = getResources();
        final String packageName = getApplication().getPackageName();

        addWallpapers(resources, packageName, R.array.wallpapers);
        addWallpapers(resources, packageName, R.array.extra_wallpapers);
    }
    
    private void addWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                final int thumbRes = resources.getIdentifier(extra + "_232x206",
                        "drawable", packageName);
                if (thumbRes != 0) {
                    mThumbs.add(thumbRes);
                    mImages.add(res);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!PreferencesHelper.isAdsDisabled()) {
            Appodeal.onResume(this, Appodeal.BANNER);
        }
        mIsWallpaperSet = false;

    }
    
    @Override
    public void onPause() {
        super.onPause();
        mIsWallpaperSet = false;
        if (SDK_INT < ICE_CREAM_SANDWICH) {
            AppMsg.cancelAll(this);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mPrefs.getBoolean("anal", true)) {
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }

        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!PreferencesHelper.isAdsDisabled()) {
            if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                Appodeal.show(this, Appodeal.INTERSTITIAL);
            }
        }
    }

    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel();
        }
        mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
    }

    /**
     *  START MAGIC
     */
    protected boolean isScreenLarge(Resources res) {
        Configuration config = res.getConfiguration();
        return config.smallestScreenWidthDp >= 720;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected Point getDefaultWallpaperSize(Resources res, WindowManager windowManager) {
        // Uses suggested size if available
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        int suggestedWidth = wallpaperManager.getDesiredMinimumWidth();
        int suggestedHeight = wallpaperManager.getDesiredMinimumHeight();
        if (suggestedWidth != 0 && suggestedHeight != 0) {
            return new Point(suggestedWidth, suggestedHeight);
        }


//         Else, calculate desired size from screen size
        Point minDims = new Point();
        Point maxDims = new Point();
        windowManager.getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);


        int maxDim = Math.max(maxDims.x, maxDims.y);
        int minDim = Math.max(minDims.x, minDims.y);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point realSize = new Point();
            windowManager.getDefaultDisplay().getRealSize(realSize);
            maxDim = Math.max(realSize.x, realSize.y);
            minDim = Math.min(realSize.x, realSize.y);
        }

        // We need to ensure that there is enough extra space in the wallpaper
        // for the intended
        // parallax effects
        final int defaultWidth, defaultHeight;
        if (isScreenLarge(res)) {
            defaultWidth = (int) (maxDim * wallpaperTravelToScreenWidthRatio(maxDim, minDim));
            defaultHeight = maxDim;
        } else {
            defaultWidth = Math.max((int) (minDim * WALLPAPER_SCREENS_SPAN), maxDim);
            defaultHeight = maxDim;
        }
        return new Point(defaultWidth, defaultHeight);
    }

    // As a ratio of screen height, the total distance we want the parallax effect to span
    // horizontally
    protected float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16/10f;
        final float ASPECT_RATIO_PORTRAIT = 10/16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
                (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
                        (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }

    protected RectF getMaxCropRect(
            int inWidth, int inHeight, int outWidth, int outHeight, boolean leftAligned) {
        RectF cropRect = new RectF();
        // Get a crop rect that will fit this
        if (inWidth / (float) inHeight > outWidth / (float) outHeight) {
            cropRect.top = 0;
            cropRect.bottom = inHeight;
            cropRect.left = (inWidth - (outWidth / (float) outHeight) * inHeight) / 2;
            cropRect.right = inWidth - cropRect.left;
            if (leftAligned) {
                cropRect.right -= cropRect.left;
                cropRect.left = 0;
            }
        } else {
            cropRect.left = 0;
            cropRect.right = inWidth;
            cropRect.top = (inHeight - (outHeight / (float) outWidth) * inWidth) / 2;
            cropRect.bottom = inHeight - cropRect.top;
        }
        return cropRect;
    }

    protected void cropImageAndSetWallpaper(int resId) {
        Point outSize = getDefaultWallpaperSize(getResources(), getWindowManager());
        final BitmapCropTask cropTask = new BitmapCropTask(this, getResources(), resId,
                null, 0, outSize.x, outSize.y, true, false, null);
        Point inSize = cropTask.getImageBounds();
        final RectF crop = getMaxCropRect(inSize.x, inSize.y, outSize.x, outSize.y, false);
        cropTask.setCropBounds(crop);
        Runnable onEndCrop = new Runnable() {
            public void run() {
                Point point = cropTask.getImageBounds();
                Wallpaper.this.updateWallpaperDimensions(point.x, point.y);
                setResult(Activity.RESULT_OK);
            }
        };
        cropTask.setOnEndRunnable(onEndCrop);
        cropTask.execute();
    }

    protected void updateWallpaperDimensions(int width, int height) {
        SharedPreferences.Editor editor = mPrefs.edit();
        if (width != 0 && height != 0) {
            editor.putInt(WALLPAPER_WIDTH_KEY, width);
            editor.putInt(WALLPAPER_HEIGHT_KEY, height);
        } else {
            editor.remove(WALLPAPER_WIDTH_KEY);
            editor.remove(WALLPAPER_HEIGHT_KEY);
        }
        editor.commit();

        suggestWallpaperDimension(getResources(), getWindowManager(), WallpaperManager.getInstance(this));
    }

    public void suggestWallpaperDimension(Resources res,
                                          WindowManager windowManager,
                                          final WallpaperManager wallpaperManager) {
        final Point defaultWallpaperSize = getDefaultWallpaperSize(res, windowManager);

        new Thread("suggestWallpaperDimension") {
            public void run() {
                // If we have saved a wallpaper width/height, use that instead
                int savedWidth = mPrefs.getInt(WALLPAPER_WIDTH_KEY, defaultWallpaperSize.x);
                int savedHeight = mPrefs.getInt(WALLPAPER_HEIGHT_KEY, defaultWallpaperSize.y);
                wallpaperManager.suggestDesiredDimensions(savedWidth, savedHeight);
            }
        }.start();
    }

    /**
     *  FINISH MAGIC
     */

    private void selectWallpaper(int position) {
        if (mIsWallpaperSet) {
            return;
        }

        mIsWallpaperSet = true;

        if (mPrefs.getBoolean("crop", true)) {
            cropImageAndSetWallpaper(mImages.get(position));
        } else {
            try {
                InputStream stream = getResources().openRawResource(mImages.get(position));
                setWallpaper(stream);
                setResult(RESULT_OK);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    public void onNothingSelected(AdapterView parent) {
    }

    // ADAPTER
    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(Wallpaper context) {
            mLayoutInflater = context.getLayoutInflater();
        }

        public int getCount() {
            return mThumbs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;

            if (convertView == null) {
                image = (ImageView) mLayoutInflater.inflate(R.layout.wallpaper_item, parent, false);
            } else {
                image = (ImageView) convertView;
            }
            
            int thumbRes = mThumbs.get(position);
            image.setImageResource(thumbRes);
            Drawable thumbDrawable = image.getDrawable();
            if (thumbDrawable != null) {
                thumbDrawable.setDither(true);
            } else {
//                .e(String.format(
//                        "Error decoding thumbnail resId=%d for wallpaper #%d",
//                        thumbRes, position));
            }
            return image;
        }
    }

    // TASKS
    class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {
        BitmapFactory.Options mOptions;

        WallpaperLoader() {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inSampleSize = 2;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;            
        }
        
        protected Bitmap doInBackground(Integer... params) {
            if (isCancelled()) return null;
            try {
                return BitmapFactory.decodeResource(getResources(),
                        mImages.get(params[0]), mOptions);
            } catch (OutOfMemoryError e) {
                Crashlytics.logException(e);
                return null;
            }            
        }

        @Override
        protected void onPostExecute(Bitmap btwo) {
            if (btwo == null) return;

            if (!isCancelled() && !mOptions.mCancel) {
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }

                final ImageView view = mImageView;
                view.setImageBitmap(btwo);

                mBitmap = btwo;

                final Drawable drawable = view.getDrawable();
                drawable.setFilterBitmap(true);
                drawable.setDither(true);

                view.postInvalidate();

                mLoader = null;
            } else {
               btwo.recycle(); 
            }
        }
        void cancel() {
            mOptions.requestCancelDecode();
            super.cancel(true);
        }
    }

    // OTHER
    private void initAnalytics() {
//        try {
//            Tracker t = ((Analytics)getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);
//            t.setScreenName("Wallpaper");
//            t.send(new HitBuilders.AppViewBuilder().build());
//        } catch (Exception e) {
//            Timber.e("Analytics: " + e);
//            Crashlytics.logException(e);
//        }
        Tracker t = ((Analytics)getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);
        t.setScreenName("Wallpaper");
        t.send(new HitBuilders.AppViewBuilder().build());
//        t.send(new HitBuilders.EventBuilder().setAction(vBuyBtn).build());
        t.setScreenName(null);
    }

    // TOAST
    private void done() {
        AppMsg dm = AppMsg.makeText(this, R.string.img_install_done, AppMsg.STYLE_CONFIRM);
        dm.setParent(R.id.fam);
        dm.setDuration(AppMsg.LENGTH_SHORT);
        dm.setAnimation(android.R.anim.fade_in, android.R.anim.slide_out_right);
        dm.show();
    }

    private void noDone() {
        AppMsg nd = AppMsg.makeText(this, R.string.negative_button_clicked, AppMsg.STYLE_CONFIRM);
        nd.setParent(R.id.fam);
        nd.setDuration(AppMsg.LENGTH_SHORT);
        nd.setAnimation(android.R.anim.fade_in, android.R.anim.slide_out_right);
        nd.show();
    }

    private void otherLink() {
        Intent semen = new Intent(Intent.ACTION_VIEW, Uri.parse(link_other_apps));
        startActivity(semen);
    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        // Тут тоже надо ссылку заменить на актуальную
        sendIntent.putExtra(Intent.EXTRA_TEXT, " #Download #Elite #Wallpapers on #Google #Play - тут должна быть ссылка");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void dialogShow() {

        AlertDialog.Builder dialogBuy = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        dialogBuy.setTitle(R.string.buy_dialog);
        dialogBuy.setMessage(R.string.no_wallpapers_message);
        dialogBuy.setCancelable(true);
        dialogBuy.setPositiveButton(R.string.ad_nw_yes_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                done();
                noWallpapers();
            }
        });
        dialogBuy.setNegativeButton(R.string.ad_nw_no_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                noDone();
            }
        });
        dialogBuy.show();
    }

    private void showAboutPermission() {
        PreferencesHelper.markWelcomeDone(this);
        final View customView;

        try {
            customView = LayoutInflater.from(this).inflate(R.layout.dialog_webview, null);
        } catch (InflateException e) {
            throw new IllegalStateException("This device does not support Web Views.");
        }
        final AlertDialog.Builder dialogAP = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        dialogAP.setView(customView);
        dialogAP.setCancelable(true);
        dialogAP.setPositiveButton(R.string.close_about_dialog, null);
        dialogAP.show();
        final WebView webView = (WebView) customView.findViewById(R.id.webview);
        try {
            // Load from changelog.html in the assets folder
            StringBuilder buf = new StringBuilder();
            InputStream json = this.getAssets().open("changelog.html");
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

    private void settingShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent stIntent = new Intent(this, SettingsActivity.class);
            startActivity(stIntent);
        } else {
            Intent intent = new Intent(this, OldSettingsActivity.class);
            startActivity(intent);
        }

    }

    private void noWallpapers() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
            wm.setBitmap(bitmap);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

    }

    private void initShowADS() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fl.getVisibility() == View.GONE) {
                    AnimUtils startAH = new AnimUtils(fl, 1000, AnimUtils.EXPAND);
                    startAH.setHeight(aHeight);
                    fl.startAnimation(startAH);

                }
                Appodeal.show(Wallpaper.this, Appodeal.BANNER_VIEW);
                if (mPrefs.getBoolean("gapps", true)) {
                    tbCont.setVisibility(View.VISIBLE);
                    buyButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void disableShowADS() {
        if (fl.getVisibility() == View.VISIBLE) {
            AnimUtils helper = new AnimUtils(fl, 1000, AnimUtils.COLLAPSED);
            aHeight = helper.getHeight();
            fl.startAnimation(helper);
        }
        Appodeal.hide(Wallpaper.this, Appodeal.BANNER_VIEW);
        if (tbCont.getVisibility() == View.VISIBLE) {
            tbCont.setVisibility(View.GONE);
            buyButton.setVisibility(View.GONE);
        }
    }

    //ADS
    private void initADS() {
        Appodeal.setBannerViewId(R.id.appodealBannerView);
        Appodeal.initialize(this, appKey, Appodeal.INTERSTITIAL | Appodeal.BANNER_VIEW);
        Appodeal.setBannerCallbacks(new BannerCallbacks() {
            @Override
            public void onBannerLoaded() {
                initShowADS();
            }

            @Override
            public void onBannerFailedToLoad() {}

            @Override
            public void onBannerShown() {}

            @Override
            public void onBannerClicked() {
                showThksToast();
            }
        });

        Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
            @Override
            public void onInterstitialLoaded(boolean b) {}

            @Override
            public void onInterstitialFailedToLoad() {}

            @Override
            public void onInterstitialShown() {}

            @Override
            public void onInterstitialClicked() {
                showThksToast();
            }

            @Override
            public void onInterstitialClosed() {}
        });
    }

    private void showThksToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Wallpaper.this, R.string.thanks_for_click, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // INIT BILLING
    private void initBilling() {
        mHelper = new IabHelper(this, LICENSE_KEY);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Crashlytics.log("Problem setting up in-app billing: " + result);
                    return;
                }

                if (mHelper == null) return;

                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Crashlytics.log("Failed to query inventory: " + result);
                return;
            }

            Purchase purchase = inventory.getPurchase(ADS_DISABLE);
            PreferencesHelper.savePurchase(getApplicationContext(), PreferencesHelper.Purchase.DISABLE_ADS, purchase != null && verifyDeveloperPayload(purchase));
            if (!PreferencesHelper.isAdsDisabled()) {
                initADS();
            } else if (fl.getVisibility() == View.VISIBLE) {
                disableShowADS();
            } else {
                Crashlytics.log("Oops. mGotInventoryListener");
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Crashlytics.log("onActivityResult handle by IABUtil");
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
		/*
		 * TODO: здесь когда нибудб будет верификация
		 * возможно даже со своим сервером
		 */

        return true;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) {
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                return;
            }

            if (purchase.getSku().equals(ADS_DISABLE)) {

                PreferencesHelper.savePurchase(getApplication(), PreferencesHelper.Purchase.DISABLE_ADS, true);
                if (!PreferencesHelper.isAdsDisabled()) {
                    initADS();
                } else if (fl.getVisibility() == View.VISIBLE) {
                    disableShowADS();
                } else {
                    Crashlytics.log("Oops. mPurchaseFinishedListener");
                }
            }
        }
    };

    /**
     * also, to good time
     */
//    public class RandomString {
//
//        private final Random random = new Random();
//
//        private final char[] buf;
//
//        public RandomString(int length) {
//            if (length < 1)
//                throw new IllegalArgumentException("length < 1: " + length);
//            buf = new char[length];
//        }
//
//        public String nextString() {
//            for (int idx = 0; idx < buf.length; ++idx)
//                buf[idx] = symbols[random.nextInt(symbols.length)];
//            return new String(buf);
//        }
//
//    }
}