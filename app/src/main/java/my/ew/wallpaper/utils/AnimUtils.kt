package my.ew.wallpaper.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout

/**
 * Created by isanechek on 09.08.15.
 */
class AnimUtils(val vv: View, duration: Int, val mType: Int) : Animation() {
    private var endHeight: Int = 0
    private val lp: LinearLayout.LayoutParams

    init {
        setDuration(duration.toLong())
        endHeight = vv.getHeight()
        lp = (vv.getLayoutParams() as LinearLayout.LayoutParams)
        if (mType == EXPAND) {
            lp.height = 0
        } else {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        vv.setVisibility(View.VISIBLE)
    }

    fun getHeight(): Int {
        return vv.getHeight()
    }

    fun setHeight(height: Int) {
        endHeight = height
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        if (interpolatedTime < 1.0f) {
            if (mType == EXPAND) {
                lp.height = (endHeight * interpolatedTime).toInt()
            } else {
                lp.height = (endHeight * (1 - interpolatedTime)).toInt()
            }
            vv.requestLayout()
        } else {
            if (mType == EXPAND) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                vv.requestLayout()
            } else {
                vv.setVisibility(View.GONE)
            }
        }
    }

    companion object {

        public val COLLAPSED: Int = 1
        public val EXPAND: Int = 0
    }
}
