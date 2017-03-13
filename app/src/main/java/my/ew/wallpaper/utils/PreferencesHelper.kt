package my.ew.wallpaper.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

/**
 * Created by isanechek on 24.07.15.
 */
object PreferencesHelper {

    private val TAG = "pref_helper"

    private val SETTINGS = "wallpaper_prefs_a"
    public val PREF_WELCOME_DONE: String = "pref_welcome_done"
    //    private static final String GAPPS_SETTING = "gappspref";
    private val TAG_DISABLED_ADS = "disabledADS"

    private var disabledADS: Boolean = false

    @JvmStatic
    fun isAdsDisabled(): Boolean {
        Log.d(TAG, "boolean isAdsDisabled")
        return disabledADS
    }

    enum class Purchase {
        DISABLE_ADS
    }

    fun savePurchase(c: Context, p: Purchase, v: Boolean) {
        Log.d(TAG, "savePurchase")
        val settings = c.getSharedPreferences(SETTINGS, 0)
        val editor = settings.edit()
        when (p) {
            PreferencesHelper.Purchase.DISABLE_ADS -> {
                editor.putBoolean("TAG_DISABLED_ADS", v)
                disabledADS = v
                Log.d(TAG, "disable ads " + v)
            }
        }
        editor.apply()
    }

    @JvmStatic
    fun loadSettings(c: Context) {
        Log.d(TAG, "loadSettings")

        val settings = c.getSharedPreferences(SETTINGS, 0)
        if (settings.all.isNotEmpty()) {
            val editor = settings.edit()
            editor.clear()
            editor.apply()

            disabledADS = false
            Log.d(TAG, "load setting disable false")
        } else {
            disabledADS = settings.getBoolean(TAG_DISABLED_ADS, false)
            Log.d(TAG, "else get boolean false")
        }
    }

    @JvmStatic
    fun isWelcomeDone(context: Context): Boolean {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            return sp.getBoolean(PREF_WELCOME_DONE, false)
        }

    @JvmStatic
    fun markWelcomeDone(context: Context) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            sp.edit().putBoolean(PREF_WELCOME_DONE, true).apply()
        }
}
