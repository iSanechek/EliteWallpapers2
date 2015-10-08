package my.ew.wallpaper.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil

object HelperUtil {

    fun isOnline(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.getActiveNetworkInfo()
        return networkInfo != null && networkInfo.isConnectedOrConnecting()
    }
}
