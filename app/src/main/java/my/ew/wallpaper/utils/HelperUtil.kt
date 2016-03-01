package my.ew.wallpaper.utils

import android.content.Context
import android.net.ConnectivityManager

object HelperUtil {

    fun isOnline(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.getActiveNetworkInfo()
        return networkInfo != null && networkInfo.isConnectedOrConnecting()
    }
}
