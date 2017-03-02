package com.example.siqidong.stocksearch;

import android.util.AttributeSet;
import android.content.Context;
import android.view.View;
import android.support.v4.view.ViewPager;

public class MyViewPager extends ViewPager {

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {

        return true;
    }
}
