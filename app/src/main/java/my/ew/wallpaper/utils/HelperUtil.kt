package my.ew.wallpaper.utils

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build

object HelperUtil {

    fun isOnline(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.getActiveNetworkInfo()
        return networkInfo != null && networkInfo.isConnectedOrConnecting()
    }

    fun getSetAsWallpaper(ctx: Context, uri: Uri) : Intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WallpaperManager.getInstance(ctx).getCropAndSetWallpaperIntent(uri)
            } else {
                val intent = Intent(Intent.ACTION_ATTACH_DATA)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.setDataAndType(uri, "image/jpeg")
                intent.putExtra("mimeType", "image/jpeg")
            }
}
