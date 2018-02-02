package my.ew.wallpaper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;
import com.yandex.mobile.ads.AdEventListener;
import com.yandex.mobile.ads.AdRequest;
import com.yandex.mobile.ads.AdRequestError;
import com.yandex.mobile.ads.AdSize;
import com.yandex.mobile.ads.AdView;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import my.elite.wallpapers.BuildConfig;
import my.elite.wallpapers.R;
import my.ew.wallpaper.settings.SettingsActivity;
import my.ew.wallpaper.task.BitmapCropTask;
import my.ew.wallpaper.utils.AnimUtils;
import my.ew.wallpaper.utils.HelperUtil;
import my.ew.wallpaper.utils.PreferencesHelper;

public class Wallpaper extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = Wallpaper.class.getSimpleName();

    private static final String link_other_apps = "https://play.google.com/store/apps/details?id=my.ew.wallpapernew";
    private static final String my_web_site = "http://averdsoft.ru/";

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
    private LinearLayout fl;
    private CoordinatorLayout mCoordinatorLayout;
    private LoadToast mLoadToast;

    int aHeight;

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        findWallpapers();
        setContentView(R.layout.activity_wallpaper);
        initUI();

        mLoadToast = new LoadToast(this);

        mPrefs = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferencesHelper.loadSettings(this);

        Button button = findViewById(R.id.set);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetWallpapers(v);
            }
        });

        if (!PreferencesHelper.isWelcomeDone(this)) {
            showAboutPermission();
        }

        if (HelperUtil.INSTANCE.isOnline(this)) {
            initADS();
        }
    }

    private void initUI() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(clickListener);
        toolbar.inflateMenu(R.menu.wallpaper);
        toolbar.setTitle(R.string.app_name);

        fl = findViewById(R.id.showsAds);

        mImageView = findViewById(R.id.wallpaper);
        mGallery = findViewById(R.id.gallery);
        mGallery.setAdapter(new ImageAdapter(this));
        mGallery.setOnItemSelectedListener(this);
        mGallery.setCallbackDuringFling(false);
        mGallery.setSpacing(getResources().getDimensionPixelSize(R.dimen.gallery_spacing));

        mCoordinatorLayout = findViewById(R.id.coordLayout);

    }
    
    private Toolbar.OnMenuItemClickListener clickListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			switch (menuItem.getItemId()) {
			    case R.id.other_apps:
			        otherLink();
			        break;
			        case R.id.my_web:
			            share2();
			            break;
            case R.id.no_wallpaper:
                dialogShow();
                break;
                case R.id.settings:
                    settingShow();
                    break;
            case R.id.share:
                share();
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
                break;
            default:
                break;
        }
    }

    private void otherLink() {
        Intent semen = new Intent(Intent.ACTION_VIEW, Uri.parse(link_other_apps));
        startActivity(semen);
    }

    private void share2() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://averdsoft.ru/"));
        startActivity(browserIntent);
    }

    private void findWallpapers() {
        mThumbs = new ArrayList<>(24);
        mImages = new ArrayList<>(24);

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
        mIsWallpaperSet = false;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mIsWallpaperSet = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }
        if (createBitmap != null && createBitmap.getStatus() != CreateBitmap.Status.FINISHED) {
            createBitmap.cancel(true);
            createBitmap = null;
        }
    }

    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel();
        }
        mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
    }

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
        editor.apply();

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

    private void selectWallpaper(int position) {
        if (mIsWallpaperSet) {
            return;
        }

        mIsWallpaperSet = true;

        if (mPrefs.getBoolean("crop", true)) {
            cropAndSetMethod(mImages.get(position));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        cropImageAndSetWallpaper(position);
        done();
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
            }
            return image;
        }
    }

    // TASKS
    private class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {
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

    private class CreateBitmap extends AsyncTask<Integer, Void, Uri> {
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

        protected Uri doInBackground(Integer... params) {
            if (isCancelled()) return null;

            Bitmap bitmap;
            OutputStream outputStream;
            Uri uri = null;
            try {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        mImages.get(params[0]));
                File file = new File(getExternalCacheDir(), "temp.jpg");
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                uri = FileProvider.getUriForFile(Wallpaper.this, BuildConfig.APPLICATION_ID + ".provider", file);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                } else {
//                    uri = Uri.fromFile(file);
//                }

            } catch (OutOfMemoryError e) {
                showErrorView();
                return null;
            } catch (IOException e) {
                showErrorView();
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri path) {
            super.onPostExecute(path);
            Log.e("TEST", "PAth " + path);
            if (path != null) {
                Intent intent = HelperUtil.INSTANCE.getSetAsWallpaper(Wallpaper.this, path);
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.set_as)));

            }
            mLoadToast.success();
            running = false;
        }

        private void showErrorView() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadToast.error();
                }
            });
        }
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

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        // Тут тоже надо ссылку заменить на актуальную
        sendIntent.putExtra(Intent.EXTRA_TEXT, " #Download #Elite #Wallpapers on #Google #Play - https://play.google.com/store/apps/dev?id=6812241770877419123");
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
        final WebView webView = customView.findViewById(R.id.webview);
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
        Intent stIntent = new Intent(this, SettingsActivity.class);
        startActivity(stIntent);

    }

    private void noWallpapers() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
            wm.setBitmap(bitmap);
        } catch (IOException e) {
        }

    }

    private void initShowADS() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fl.getVisibility() == View.GONE) {
                    AnimUtils startAH = new AnimUtils(fl, 1500, AnimUtils.EXPAND);
                    startAH.setHeight(aHeight);
                    fl.startAnimation(startAH);
                }
//                if (fl.getVisibility() == View.VISIBLE) {
//                    mAdView.setVisibility(View.VISIBLE);
//                }
            }
        });

    }

    private void disableShowADS() {
        if (fl.getVisibility() == View.VISIBLE) {
            AnimUtils helper = new AnimUtils(fl, 1000, AnimUtils.COLLAPSED);
            aHeight = helper.getHeight();
            fl.startAnimation(helper);
        }

//        if (tbCont.getVisibility() == View.VISIBLE) {
//            tbCont.setVisibility(View.GONE);
//        }
    }

    //ADS
    private void initADS() {
        Log.e("TEST", "Init ADS");
        AdView mAdView = findViewById(R.id.banner_view);
        mAdView.setBlockId("R-M-DEMO-320x50");
//        mAdView.setBlockId("R-M-262926-1");
        mAdView.setAdSize(AdSize.BANNER_320x50);
        AdRequest mAdRequest = AdRequest
                .builder()
                .build();
        mAdView.loadAd(mAdRequest);
        mAdView.setVisibility(View.VISIBLE);
//        initShowADS();
        mAdView.setAdEventListener(new AdEventListener.SimpleAdEventListener() {

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError error) {
                super.onAdFailedToLoad(error);
                Log.e("TEST", "Error " + error.getCode() + " Desc " + error.getDescription());
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
//                initShowADS();

                Log.e("TEST", "Load");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
//                disableShowADS();
                Log.e("TEST", "Open");

            }
        });

//        initShowADS();
    }


    private void showThksToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Wallpaper.this, R.string.thanks_for_click, Toast.LENGTH_SHORT).show();
            }
        });
    }
}