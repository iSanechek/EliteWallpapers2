package my.ew.wallpaper

import android.app.Application
import com.yandex.metrica.YandexMetrica

/**
 * Created by isanechek on 1/31/18.
 */
class WallApp : Application() {

    override fun onCreate() {
        super.onCreate()
        YandexMetrica.activate(getApplicationContext(), "43614695-4bad-431c-9e14-fa588179b756") // test key
    }
}