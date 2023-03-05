package com.tapbi.spark.qrcode.utils.rateapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tapbi.spark.qrcode.R;


/**
 * Created by willy on 2017/6/3.
 */
public class PartialView extends RelativeLayout {

    private ImageView mFilledView;
    private ImageView mEmptyView;

    public PartialView(Context context) {
        super(context);
        init();
    }

    public PartialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PartialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mFilledView = new ImageView(getContext());
        mFilledView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mEmptyView = new ImageView(getContext());
        mEmptyView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(mFilledView);
        addView(mEmptyView);
    }

    public void setFilledDrawable() {
        mFilledView.setImageResource(R.drawable.yellow_star);
    }

    public void setEmptyDrawable() {
        mEmptyView.setImageResource(R.drawable.star_img);
    }

    public void setFilled() {
        mFilledView.setVisibility(VISIBLE);
        mEmptyView.setVisibility(GONE);
    }

    public void setPartialFilled(float rating) {
//        float percentage = rating % 1;
//        int level = (int) (10000 * percentage);
//        level = level == 0 ? 10000 : level;
//        mFilledView.setImageLevel(level);
//        mEmptyView.setImageLevel(10000 - level);

        setFilled();
    }

    public void setEmpty() {
        mFilledView.setVisibility(GONE);
        mEmptyView.setVisibility(VISIBLE);
    }

}
