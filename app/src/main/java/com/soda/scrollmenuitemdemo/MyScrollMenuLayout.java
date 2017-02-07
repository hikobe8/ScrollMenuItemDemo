package com.soda.scrollmenuitemdemo;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.Calendar;

/**
 * Created by Ray on 2017/2/7.
 */

public class MyScrollMenuLayout extends ViewGroup {

    /**
     * 系统认为的最小滑动距离
     */
    private final int mTouchSlop;
    private Scroller mScroller;
    private int mLeftBorder;
    private int mRightBorder;

    private float mDownX; //手指按下的x坐标
    private float mLastMoveX; //上次滑动的最后点x坐标
    private float mMoveX;
    private int mMenuWidth; //菜单栏的宽度


    public MyScrollMenuLayout(Context context) {
        this(context, null, 0);
    }

    public MyScrollMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScrollMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int childCount = getChildCount();
        if (childCount > 2) {
            throw new RuntimeException("MyScrollMenuLayout can't has more than 2 children!");
        }
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(new ViewConfiguration());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View childAt0 = getChildAt(0); //内容布局
        View childAt1 = getChildAt(1); //菜单布局
        measureChild(childAt0, widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = (int) (MeasureSpec.getSize(widthMeasureSpec)*1.0f / 3);
        measureChild(childAt1, MeasureSpec.makeMeasureSpec(size, mode), heightMeasureSpec); // 设置菜单为三分之一

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View childAt0 = getChildAt(0); //内容布局
        View childAt1 = getChildAt(1); //菜单布局
        childAt0.layout(0, 0, childAt0.getMeasuredWidth(), childAt0.getMeasuredHeight());
        childAt1.layout(getMeasuredWidth(), 0, getMeasuredWidth()+childAt1.getMeasuredWidth(), getMeasuredHeight());
        //获取左右边界值
        mLeftBorder = childAt0.getLeft();
        mRightBorder = childAt1.getRight();
        mMenuWidth = childAt1.getMeasuredWidth();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getRawX();
                int scolledX = (int) (mLastMoveX - mMoveX);
                if (getScrollX() + scolledX < mLeftBorder) {
                    //滑动距离小于左边界，滑动到左边界就不再滑动
                    scrollTo(mLeftBorder, 0);
                    return true;
                }
                if (getScrollX() + getWidth() + scolledX > mRightBorder) {
                    //滑动距离大于左边界，滑动到右边界就不再滑动
                    scrollTo(mRightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy(scolledX, 0);
                mLastMoveX = mMoveX;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(getScrollX()) > mMenuWidth/3) {
                    //展开菜单
                    int dx = mMenuWidth - getScrollX();
                    mScroller.startScroll(getScrollX(), 0, dx, 0);
                } else {
                    //关闭菜单
                    int dx = 0 - getScrollX();
                    mScroller.startScroll(getScrollX(), 0, dx, 0);
                }
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getRawX();
                mLastMoveX = mDownX; // 更新滑动点为按下点
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = ev.getRawX();
                int offsetX = (int) Math.abs(mMoveX - mDownX); // 计算滑动距离
                mLastMoveX = mMoveX;
                if (offsetX > mTouchSlop) {
                    return true; //大于系统认为的最小滑动距离 则拦截事件
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
