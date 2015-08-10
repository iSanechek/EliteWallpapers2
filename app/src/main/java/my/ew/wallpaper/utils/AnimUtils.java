package my.ew.wallpaper.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by isanechek on 09.08.15.
 */
public class AnimUtils extends Animation {

    public final static int COLLAPSED = 1;
    public final static int EXPAND = 0;

    private View vv;
    private int endHeight;
    private int mType;
    private LinearLayout.LayoutParams lp;

    public AnimUtils(View view, int duration, int type) {
        setDuration(duration);
        vv = view;
        endHeight = vv.getHeight();
        lp = ((LinearLayout.LayoutParams) vv.getLayoutParams());
        mType = type;
        if (mType == EXPAND) {
            lp.height = 0;
        } else {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        vv.setVisibility(View.VISIBLE);
    }

    public int getHeight() {
        return vv.getHeight();
    }

    public void setHeight(int height) {
        endHeight = height;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            if (mType == EXPAND) {
                lp.height = (int)(endHeight * interpolatedTime);
            } else {
                lp.height = (int)(endHeight * (1 - interpolatedTime));
            }
            vv.requestLayout();
        } else {
            if (mType == EXPAND) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                vv.requestLayout();
            } else {
                vv.setVisibility(View.GONE);
            }
        }
    }
}
