package xyz.kandrac.progressbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Locale;

/**
 * Custom progress bar implementation.
 * <p>
 * Created by jan on 25.1.2017.
 */
public class HeavyProgressBar extends View {

    @IntRange(from = 0, to = 100)
    private int mProgressActual;

    private Paint mProgressPaint;

    private boolean animating = false;
    private int animateProgress;
    private boolean finishes = false;

    @ColorInt
    private int mTint;
    private Paint mTextPaint;
    Path path = new Path();

    public HeavyProgressBar(Context context) {
        this(context, null, 0);
    }

    public HeavyProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeavyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void animateProgress(@IntRange(from = 0, to = 100) int toProgress) {

        int delta = mProgressActual > toProgress ? mProgressActual - toProgress : toProgress - mProgressActual;

        ValueAnimator animation = ValueAnimator.ofInt(mProgressActual, toProgress);
        animation.setDuration(delta * 5);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setProgressPercent((int) valueAnimator.getAnimatedValue());
            }
        });
        animation.start();
    }

    public void setProgressPercent(@IntRange(from = 0, to = 100) int progress) {
        mProgressActual = progress;
        if (mProgressActual != 100) {
            invalidate();
            requestLayout();
        } else {

            ValueAnimator animation = ValueAnimator.ofInt(0, 50);
            animation.setDuration(300);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    animateProgress = (int) valueAnimator.getAnimatedValue();
                    invalidate();
                    requestLayout();
                }
            });
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animating = false;
                    finishes = true;
                }
            });

            animating = true;
            animation.start();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HeavyProgressBar,
                0, 0);

        try {
            mProgressActual = a.getInt(R.styleable.HeavyProgressBar_progress, 0);
            mTint = a.getColor(R.styleable.HeavyProgressBar_tint, Color.YELLOW);
        } finally {
            a.recycle();
        }

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStrokeWidth(15);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(mTint);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTint);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(30);

    }

    /**
     * Transforms dips to pixels
     *
     * @param dp to transform
     * @return pixels
     */
    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int minWidth = dpToPx(32);
        int minHeight = dpToPx(64);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(minWidth, widthSize);
        } else {
            width = minWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(minHeight, heightSize);
        } else {
            height = minHeight;
        }

        setMeasuredDimension(width, height + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int width = getWidth() - paddingLeft - paddingRight;

        if (animating || finishes) {

            // cut from both ends of line
            int cutLine = width * animateProgress / 100;
            int centerX = paddingLeft + width / 2;
            int centerY = getHeight() / 2;

            canvas.drawLine(paddingLeft + cutLine, centerY, getWidth() - paddingRight - cutLine, centerY, mProgressPaint);
            canvas.drawCircle(centerX, centerY, animateProgress, mTextPaint);

        } else {

            int x = paddingLeft + width * mProgressActual / 100;
            int modifiedProgress = mProgressActual < 50 ? mProgressActual : 100 - mProgressActual;
            int y = (4 * modifiedProgress * modifiedProgress / getHeight()) + getHeight() / 2;      // quadratic grow -> x^2

            // connecting line
            path.moveTo(paddingLeft, getHeight() / 2);
            path.lineTo(x, y);
            path.lineTo(paddingLeft + width, getHeight() / 2);

            canvas.drawPath(path, mProgressPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%d%%", mProgressActual), x, y - 30, mTextPaint);

            path.reset();
        }

        if (finishes) {
            finishes = false;
        }
    }
}
