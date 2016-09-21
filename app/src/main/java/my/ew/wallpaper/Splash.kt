package my.ew.wallpaper

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import my.elite.wallpapers.R


class Splash : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.splah_activity)

        initStop()
    }

    private fun initStop() {
        Handler().postDelayed({
            val start = Intent(this@Splash, Wallpaper::class.java)
            startActivity(start)
            finish()
        }, SPLASH_OUT.toLong())
    }
    companion object {

        private val SPLASH_OUT = 1500
    }
}
