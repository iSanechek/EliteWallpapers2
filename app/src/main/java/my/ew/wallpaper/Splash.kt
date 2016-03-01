package my.ew.wallpaper

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import com.crashlytics.android.Crashlytics

import io.fabric.sdk.android.Fabric
import my.elite.wallpapers.R


public class Splash : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.splah_activity)

        initStop()
    }

    private fun initStop() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                val start = Intent(this@Splash, Wallpaper::class.java)
                startActivity(start)
                finish()
            }
        }, SPLASH_OUT.toLong())
    }
    companion object {

        private val SPLASH_OUT = 1500
    }
}
