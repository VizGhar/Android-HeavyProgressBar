package xyz.kandrac.progressbar;

import android.content.Context;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = getWidth() / 100 * mProgressActual;
        int modifiedProgress = mProgressActual < 50 ? mProgressActual : 100 - mProgressActual;
        int y = (modifiedProgress * modifiedProgress / getHeight()) + getHeight() / 2;

        canvas.drawLine(0, getHeight() / 2, x, y, mProgressPaint);
        canvas.drawLine(x, y, getWidth(), getHeight() / 2, mProgressPaint);

        canvas.drawText(String.format(Locale.getDefault(), "%d%%", mProgressActual), x, y - 10, mProgressPaint);
    }
}
