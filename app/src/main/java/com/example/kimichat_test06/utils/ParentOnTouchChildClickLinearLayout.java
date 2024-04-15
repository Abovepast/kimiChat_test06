package com.example.kimichat_test06.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.LinearLayout;

/**
 * Created by  Rex on 2016/10/25.
 * 能兼容子view点击事件和自身onTouch事件的容器-LinearLayout可改
 */
public class ParentOnTouchChildClickLinearLayout extends LinearLayout {


    public ParentOnTouchChildClickLinearLayout(Context context) {
        super(context);
    }

    public ParentOnTouchChildClickLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParentOnTouchChildClickLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int yyy = -1;
    private int xxx = -1;
    private boolean isMove = false;

    /**
     * 核心方法
     *
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                //此处为break所以 onTouch中没有Down
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isMove)
                    return false;
                isMove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMove) {
                    yyy = (int) event.getRawY();
                    xxx = (int) event.getRawX();
                }
                isMove = true;
                //细节优化 短距离移除
                float moveY = event.getRawY();
                float moveX = event.getRawX();
                //如果是非点击事件就拦截 让父布局接手onTouch 否则执行子ViewOnClick
                if (Math.abs(moveY - yyy) > dip2px(getContext(), 20) || Math.abs(moveX - xxx) > dip2px(getContext(), 20)) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                break;

        }
        return false;
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}