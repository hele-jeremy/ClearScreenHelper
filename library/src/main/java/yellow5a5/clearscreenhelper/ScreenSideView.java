package yellow5a5.clearscreenhelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Yellow5A5 on 16/10/21.
 */

public class ScreenSideView extends LinearLayout {

    private final int MIN_SCROLL_SIZE = 130;
    private final int LEFT_SIDE_X = MIN_SCROLL_SIZE;
    private final int RIGHT_SIDE_X = getResources().getDisplayMetrics().widthPixels - MIN_SCROLL_SIZE;


    private int mDownX;
    private int mEndX;
    private ValueAnimator mEndAnimator;

    private boolean isCanSrcoll;

    private Constants.Orientation mOrientation;

    private IPositionCallBack mIPositionCallBack;

    public void setIPositionCallBack(IPositionCallBack l) {
        mIPositionCallBack = l;
    }

    public ScreenSideView(Context context) {
        this(context, null);
    }

    public ScreenSideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScreenSideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mEndAnimator = ValueAnimator.ofFloat(0, 1.0f).setDuration(200);
        mEndAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float factor = (float) valueAnimator.getAnimatedValue();
                int diffX = mEndX - (mDownX - MIN_SCROLL_SIZE);
                Log.e(ScreenSideView.class.getName(), "onAnimationUpdate: " + (mDownX + diffX * factor));
                mIPositionCallBack.onPositionChange((int) (mDownX + diffX * factor), 0);
            }
        });
        mEndAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOrientation.equals(Constants.Orientation.LEFT) && mEndX == RIGHT_SIDE_X) {
                    mIPositionCallBack.onClearEnd();
                    mOrientation = Constants.Orientation.RIGHT;
                } else if (mOrientation.equals(Constants.Orientation.RIGHT) && mEndX == LEFT_SIDE_X) {
                    mIPositionCallBack.onRecovery();
                    mOrientation = Constants.Orientation.LEFT;
                }
                mDownX = mEndX;
                isCanSrcoll = false;
            }
        });
    }


    public void setClearSide(Constants.Orientation orientation) {
        mOrientation = orientation;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isScrollFromSide(x)) {
                    isCanSrcoll = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (isGreaterThanMinSize(x) && isCanSrcoll) {
                    mIPositionCallBack.onPositionChange(x - MIN_SCROLL_SIZE, 0);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isCanSrcoll) {
                    mDownX = x;
                    fixPostion();
                    mEndAnimator.start();
                }
        }
        return super.onTouchEvent(event);
    }

    private void fixPostion() {
        if (mOrientation.equals(Constants.Orientation.LEFT) && mDownX > RIGHT_SIDE_X / 3) {
            mEndX = RIGHT_SIDE_X;
        } else if (mOrientation.equals(Constants.Orientation.RIGHT) && (mDownX < RIGHT_SIDE_X * 2 / 3)) {
            mEndX = LEFT_SIDE_X;
        }
    }

    public boolean isGreaterThanMinSize(int x) {
        int absX = Math.abs(mDownX - x);
        return absX > MIN_SCROLL_SIZE;
    }

    public boolean isScrollFromSide(int x) {
        if (x <= LEFT_SIDE_X && mOrientation.equals(Constants.Orientation.LEFT)
                || (x > RIGHT_SIDE_X && mOrientation.equals(Constants.Orientation.RIGHT))) {
            return true;
        } else {
            return false;
        }
    }
}
