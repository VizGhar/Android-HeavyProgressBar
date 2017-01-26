package xyz.kandrac.progressbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.View;

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
    private Paint other;

    @ColorInt
    private int mTint;

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

    public void setProgressPercent(@IntRange(from = 0, to = 100) int progress) {
        mProgressActual = progress;
        invalidate();
        requestLayout();
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
        mProgressPaint.setStrokeWidth(5);

        mProgressPaint.setColor(mTint);
        mProgressPaint.setTextAlign(Paint.Align.CENTER);
        mProgressPaint.setTextSize(20);

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

        int x = paddingLeft + width * mProgressActual / 100;
        int modifiedProgress = mProgressActual < 50 ? mProgressActual : 100 - mProgressActual;
        int y = (4 * modifiedProgress * modifiedProgress / getHeight()) + getHeight() / 2;

        canvas.drawLine(paddingLeft, getHeight() / 2, x, y, mProgressPaint);
        canvas.drawLine(x, y, paddingLeft + width, getHeight() / 2, mProgressPaint);

        canvas.drawText(String.format(Locale.getDefault(), "%d%%", mProgressActual), x, y - 10, mProgressPaint);
    }
}
