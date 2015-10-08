package my.ew.wallpaper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.devspark.appmsg.AppMsg;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import net.steamcrafted.loadtoast.LoadToast;

import info.hoang8f.widget.FButton;
import io.fabric.sdk.android.Fabric;
import my.elite.wallpapers.R;
import my.ew.wallpaper.settings.OldSettingsActivity;
import my.ew.wallpaper.settings.SettingsActivity;
import my.ew.wallpaper.task.BitmapCropTask;
import my.ew.wallpaper.utils.AnimUtils;

import my.ew.wallpaper.utils.HelperUtil;
import my.ew.wallpaper.utils.PreferencesHelper;

public class Wallpaper extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = Wallpaper.class.getSimpleName();

    // to good time

//
//    // Сылку нудно будет заменить на актуальную
//    private static final String link_other_apps = "";
//
//    private static final String LICENSE_KEY = "";
//
//
//    private static final int RC_REQUEST = 10001;
//    // Это айди покупки - можно и другой
//    private static final String ADS_DISABLE = "removeads";

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0;

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
    private CreateBitmap createBitmap;
    private Toolbar toolbar;
    private LinearLayout fl;
    private CoordinatorLayout mCoordinatorLayout;
//    private ImageButton buyButton;
    private FrameLayout tbCont;
    private LoadToast mLoadToast;

    int aHeight;

//    private IabHelper mHelper;
    private AdView mAdView;
//    private InterstitialAd mInterstitialAd;


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

        Fabric.with(this, new Crashlytics());

        mLoadToast = new LoadToast(this);

        mPrefs = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferencesHelper.INSTANCE$.loadSettings(this);

//        initAnalytics();

        if (!PreferencesHelper.INSTANCE$.isWelcomeDone(this)){
            showAboutPermission();
            Answers.getInstance().logCustom(new CustomEvent("First start"));
        }

        if (HelperUtil.INSTANCE$.isOnline(this)) {
            initADS();
        }

//        if (HelperUtil.INSTANCE$.isGappsEnable(this)) {
//            if (HelperUtil.INSTANCE$.isOnline(this)) {
//                initBilling();
//            }
//        } else if (HelperUtil.INSTANCE$.isOnline(this)) {
//            initADS();
//            Answers.getInstance().logCustom(new CustomEvent("No Gapps"));
//        } else {
//            Crashlytics.log("initADS isOnline false");
//        }
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(clickListener);
        toolbar.inflateMenu(R.menu.wallpaper);
        toolbar.setTitle(R.string.app_name);

        fl = (LinearLayout) findViewById(R.id.showsAds);
        tbCont = (FrameLayout) findViewById(R.id.tb_cont);

//        buyButton = (ImageButton) findViewById(R.id.buy_btn);
//        buyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                buyBtnEvent();
//            }
//        });

        mImageView = (ImageView) findViewById(R.id.wallpaper);
        mGallery = (Gallery) findViewById(R.id.gallery);
        mGallery.setAdapter(new ImageAdapter(this));
        mGallery.setOnItemSelectedListener(this);
        mGallery.setCallbackDuringFling(false);
        mGallery.setSpacing(getResources().getDimensionPixelSize(R.dimen.gallery_spacing));

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordLayout);

    }

//    private void buyBtnEvent() {
//        if (!PreferencesHelper.INSTANCE$.isAdsDisabled()) {
//            if (HelperUtil.INSTANCE$.isOnline(this)) {
//                Answers.getInstance().logPurchase(new PurchaseEvent()
//                        .putItemName("Ads").putItemType("Disable Ads").putItemId(ADS_DISABLE));
////            RandomString randomString = new RandomString(36);
////            String payload = randomString.nextString();
//                String payload = "";
//                mHelper.launchPurchaseFlow(this, ADS_DISABLE, RC_REQUEST,
//                        mPurchaseFinishedListener, payload);
//            } else {
//                noInternetMessage();
//                Crashlytics.log("Click Buy Btn isOnline false");
//            }
//        }
//    }
    
    private Toolbar.OnMenuItemClickListener clickListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			switch (menuItem.getItemId()) {
//            case R.id.other_apps:
//                otherLink();
//                break;
            case R.id.no_wallpaper:
                dialogShow();
                break;
                case R.id.settings:
                    settingShow();
                    break;
//            case R.id.share:
//                share();
//                break;
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
                Answers.getInstance().logCustom(new CustomEvent("onSetWallpapers"));
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
        if (!PreferencesHelper.INSTANCE$.isAdsDisabled()) {
            if (mAdView != null) {
                mAdView.resume();
            }
        }
        mIsWallpaperSet = false;
//        requestNewInterstitial();
    }
    
    @Override
    public void onPause() {
        if (!PreferencesHelper.INSTANCE$.isAdsDisabled()) {
            if (mAdView != null) {
                mAdView.pause();
            }
        }
        super.onPause();
        mIsWallpaperSet = false;
        if (SDK_INT < ICE_CREAM_SANDWICH) {
            AppMsg.cancelAll(this);
        }
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

    @Override
    protected void onDestroy() {
        if (!PreferencesHelper.INSTANCE$.isAdsDisabled()) {
            if (mAdView != null) {
                mAdView.destroy();
            }
        }
        super.onDestroy();
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }
        if (createBitmap != null && createBitmap.getStatus() != CreateBitmap.Status.FINISHED) {
            createBitmap.cancel(true);
            createBitmap = null;
        }

//        if (mHelper != null) mHelper.dispose();
//        mHelper = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void selectWallpaper(int position) {
        if (mIsWallpaperSet) {
            return;
        }

        mIsWallpaperSet = true;

        if (mPrefs.getBoolean("crop", true)) {
            cropAndSetMethod(mImages.get(position));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // code for android android m
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    cropImgTask(position);
                } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            AlertDialog.Builder pDialog = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                            pDialog.setTitle(R.string.about_permission_dialog_title);
                            pDialog.setMessage(R.string.about_permission_dialog_message);
                            pDialog.setCancelable(true);
                            pDialog.setPositiveButton(R.string.permission_yes_btn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(Wallpaper.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                                }
                            });
                            pDialog.setNegativeButton(R.string.permision_no_btn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    settingShow();
                                }
                            });
                            pDialog.show();
                        } else {
                            ActivityCompat.requestPermissions(Wallpaper.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                        }
                }
            } else {
                cropImgTask(position);
            }
        }
    }

    public void onNothingSelected(AdapterView parent) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(mCoordinatorLayout, R.string.all_ok_bro, Snackbar.LENGTH_LONG).show();
                } else {
                    showSnackbar();
                }
                return;
            }
        }
    }

    private void cropAndSetMethod(int position) {
        cropImageAndSetWallpaper(mImages.get(position));
        done();
        Crashlytics.log("crop true");
    }

    private void cropImgTask(int position) {
        if (createBitmap != null && createBitmap.getStatus() != CreateBitmap.Status.FINISHED) {
            createBitmap.cancel(true);
        }
        createBitmap = (CreateBitmap) new CreateBitmap().execute(position);
    }

    private void showSnackbar() {
        Snackbar.make(mCoordinatorLayout, R.string.permission_not_granted, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.app_setting, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingShow();
                    }
                })
                .show();

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
                Crashlytics.log(String.format(
                        "Error decoding thumbnail resId=%d for wallpaper #%d",
                        thumbRes, position));
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

    class CreateBitmap extends AsyncTask<Integer, Void, Bitmap> {

        boolean running;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
            mLoadToast.setText(getResources().getString(R.string.wait))
                    .setBackgroundColor(Color.WHITE)
                    .setTextColor(getResources().getColor(R.color.my_primary_text_color))
                    .setProgressColor(getResources().getColor(R.color.my_accent_color))
                    .setTranslationY(100)
                    .show();
        }

        protected Bitmap doInBackground(Integer... params) {
            if (isCancelled()) return null;

            Bitmap bitmap = null;
            OutputStream outputStream;

            try {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        mImages.get(params[0]));
                File path = getExternalFilesDir(null);
                File dir = new File(path + "/Share Image/");
                if(!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir, "sw.jpg");
                outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                Uri uri = Uri.fromFile(file);
                Intent i = new Intent(Intent.ACTION_ATTACH_DATA);
                i.setDataAndType(uri, "image/jpeg");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.putExtra("mimeType", "image/jpeg");
                startActivity(Intent.createChooser(i, getResources().getString(R.string.set_as)));

            } catch (OutOfMemoryError e) {
                Crashlytics.logException(e);
                mLoadToast.error();
                return null;
            } catch (IOException e) {
                Crashlytics.logException(e);
                mLoadToast.error();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mLoadToast.success();
            running = false;
        }
    }

    // Analytics
//    private void initAnalytics() {
//        Tracker t = ((Analytics)getApplication()).getTracker(Analytics.TrackerName.APP_TRACKER);
//        t.setScreenName("Wallpaper");
//        t.send(new HitBuilders.AppViewBuilder().build());
//
//    }

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
//
//    private void noInternetMessage() {
//        AppMsg nc = AppMsg.makeText(this, R.string.no_connect, AppMsg.STYLE_ALERT);
//        nc.setParent(R.id.fam);
//        nc.setDuration(AppMsg.LENGTH_SHORT);
//        nc.setAnimation(android.R.anim.fade_in, android.R.anim.slide_out_right);
//        nc.show();
//    }
//
//    private void otherLink() {
//        Intent semen = new Intent(Intent.ACTION_VIEW, Uri.parse(link_other_apps));
//        startActivity(semen);
//        Answers.getInstance().logCustom(new CustomEvent("other link"));
//    }
//
//    private void share() {
//        Answers.getInstance().logShare(new ShareEvent()
//        .putMethod("share")
//        .putContentName("sharing link")
//        .putContentType("link on Google play")
//        .putContentId("link"));
//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        // Тут тоже надо ссылку заменить на актуальную
//        sendIntent.putExtra(Intent.EXTRA_TEXT, " #Download #Elite #Wallpapers on #Google #Play - тут должна быть ссылка");
//        sendIntent.setType("text/plain");
//        startActivity(sendIntent);
//    }

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
        PreferencesHelper.INSTANCE$.markWelcomeDone(this);
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
            Crashlytics.logException(e);
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
        Answers.getInstance().logCustom(new CustomEvent("No Wallpaper"));
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
            wm.setBitmap(bitmap);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

    }

    private void initShowADS() {
        Answers.getInstance().logContentView(new ContentViewEvent()
        .putContentName("initShowADS")
        .putContentType("Ads Layout Show"));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fl.getVisibility() == View.GONE) {
                    AnimUtils startAH = new AnimUtils(fl, 1500, AnimUtils.EXPAND);
                    startAH.setHeight(aHeight);
                    fl.startAnimation(startAH);
                }
                if (fl.getVisibility() == View.VISIBLE) {
                    mAdView.setVisibility(View.VISIBLE);
                }
                if (mPrefs.getBoolean("gapps", true)) {
                    tbCont.setVisibility(View.VISIBLE);
//                    buyButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void disableShowADS() {
        Crashlytics.log("disableShowADS");
        if (fl.getVisibility() == View.VISIBLE) {
            AnimUtils helper = new AnimUtils(fl, 1000, AnimUtils.COLLAPSED);
            aHeight = helper.getHeight();
            fl.startAnimation(helper);
        }

        if (mAdView != null) {
            mAdView.destroy();
        }

        if (tbCont.getVisibility() == View.VISIBLE) {
            tbCont.setVisibility(View.GONE);
//            buyButton.setVisibility(View.GONE);
        }
    }

    //ADS
    private void initADS() {

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("5DQOOZ4HKJ95S85L")
                .addTestDevice("TA17606LXJ")
                .addTestDevice("TA2470I7O")
                .build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Crashlytics.log("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Crashlytics.log("Banner Failed To Load: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                showThksToast();
                Answers.getInstance().logCustom(new CustomEvent("Click on ads"));
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                initShowADS();
            }
        });
        mAdView.loadAd(adRequest);
    }

//    private void requestNewInterstitial() {
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(INTERSTITAL_KEY);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("5DQOOZ4HKJ95S85L")
//                .addTestDevice("TA17606LXJ")
//                .addTestDevice("TA2470I7O")
//                .build();
//        mInterstitialAd.loadAd(adRequest);
//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
//                super.onAdClosed();
//                Crashlytics.log("Interstitial on Ad Closed");
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                super.onAdFailedToLoad(errorCode);
//                Crashlytics.log("Interstitial Failed To Load: " + errorCode);
//            }
//
//            @Override
//            public void onAdOpened() {
//                super.onAdOpened();
//                showThksToast();
//                Answers.getInstance().logCustom(new CustomEvent("Interstitial Click"));
//            }
//        });
//    }

    private void showThksToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Wallpaper.this, R.string.thanks_for_click, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // INIT BILLING
//    private void initBilling() {
//        mHelper = new IabHelper(this, LICENSE_KEY);
//        mHelper.enableDebugLogging(true);
//        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//            public void onIabSetupFinished(IabResult result) {
//                if (!result.isSuccess()) {
//                    Crashlytics.log("Problem setting up in-app billing: " + result);
//                    return;
//                }
//
//                if (mHelper == null) return;
//
//                mHelper.queryInventoryAsync(mGotInventoryListener);
//            }
//        });
//    }
//
//    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
//
//            if (mHelper == null) return;
//
//            // Is it a failure?
//            if (result.isFailure()) {
//
//                return;
//            }
//
//            Purchase purchase = inventory.getPurchase(ADS_DISABLE);
//            PreferencesHelper.INSTANCE$.savePurchase(getApplicationContext(), PreferencesHelper.Purchase.DISABLE_ADS, purchase != null && verifyDeveloperPayload(purchase));
//            if (!PreferencesHelper.INSTANCE$.isAdsDisabled()) {
//                initADS();
//            } else if (fl.getVisibility() == View.VISIBLE) {
//                disableShowADS();
//            } else {
//                Crashlytics.log("Just Fuck mGotInvertoryListener");
//            }
//        }
//    };
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
//
//    /** Verifies the developer payload of a purchase. */
//    boolean verifyDeveloperPayload(Purchase p) {
//        String payload = p.getDeveloperPayload();
//
//        Crashlytics.log("verifyDeveloperPayload: " + payload);
//		/*
//		 * TODO: здесь когда нибудб будет верификация
//		 * возможно даже со своим сервером
//		 */
//
//        return true;
//    }
//
//    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
//        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//
//            if (result.isFailure()) {
//                return;
//            }
//            if (!verifyDeveloperPayload(purchase)) {
//                return;
//            }
//
//            if (purchase.getSku().equals(ADS_DISABLE)) {
//
//                PreferencesHelper.INSTANCE$.savePurchase(getApplication(), PreferencesHelper.Purchase.DISABLE_ADS, true);
//                if (!PreferencesHelper.INSTANCE$.isAdsDisabled()) {
//                    initADS();
//                } else if (fl.getVisibility() == View.VISIBLE) {
//                    disableShowADS();
//                } else {
//                    Crashlytics.log("Just Fuck mPurchaseFinishedListener");
//                }
//            }
//        }
//    };

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