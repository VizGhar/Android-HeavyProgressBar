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

    public static final int DEFAULT_LINE_WIDTH_DP = 2;
    public static final int DEFAULT_TEXT_SIZE_SP = 12;

    @IntRange(from = 0, to = 100)
    private int mProgressActual;

    private Paint mProgressPaint;

    private boolean animating = false;
    private int animateProgress;
    private boolean finishes = false;

    private Paint mTextPaint;
    private Paint mAnimatePaint;
    private Path progressBarPath = new Path();
    private Path animationCheckPath = new Path();

    ValueAnimator progressChangeAnimation;

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

    /**
     * Animates progress change for this Progress bar. Length of animation is computed as
     * {@code progressDelta * 5}. Means animation from 0 to 100 percent lasts 500ms
     *
     * @param toProgress target progress value
     */
    public void animateProgress(@IntRange(from = 0, to = 100) int toProgress) {

        int delta = mProgressActual > toProgress ? mProgressActual - toProgress : toProgress - mProgressActual;

        if (progressChangeAnimation != null) {
            progressChangeAnimation.cancel();
        }

        progressChangeAnimation = ValueAnimator.ofInt(mProgressActual, toProgress);
        progressChangeAnimation.setDuration(delta * 5);
        progressChangeAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        progressChangeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setProgressPercent((int) valueAnimator.getAnimatedValue());
            }
        });
        progressChangeAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressChangeAnimation = null;
            }
        });

        progressChangeAnimation.start();
    }

    /**
     * Set new progress value in percents. The value is automatically mirrored into views canvas.
     *
     * @param progress to be set
     */
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
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HeavyProgressBar, 0, 0);

        try {
            mProgressActual = a.getInt(R.styleable.HeavyProgressBar_progress, 0);
            int tint = a.getColor(R.styleable.HeavyProgressBar_tint, 0);
            int width = a.getDimensionPixelSize(R.styleable.HeavyProgressBar_line_width, dpToPx(DEFAULT_LINE_WIDTH_DP));
            int textSize = a.getDimensionPixelSize(R.styleable.HeavyProgressBar_text_size, dpToPx(DEFAULT_TEXT_SIZE_SP));

            mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mProgressPaint.setStrokeWidth(width);
            mProgressPaint.setStyle(Paint.Style.STROKE);
            mProgressPaint.setColor(tint);

            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setColor(tint);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setTextSize(textSize);

            mAnimatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mAnimatePaint.setColor(Color.WHITE);
            mAnimatePaint.setStyle(Paint.Style.STROKE);
            mAnimatePaint.setStrokeWidth(width / 2);

        } finally {
            a.recycle();
        }
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

            if (animateProgress > 10) {
                animationCheckPath.moveTo(centerX - animateProgress / 2, centerY + animateProgress / 16);
                animationCheckPath.lineTo(centerX - animateProgress / 6, centerY + 3 * animateProgress / 8);
                animationCheckPath.lineTo(centerX + animateProgress / 2, centerY - 3 * animateProgress / 8);
                canvas.drawPath(animationCheckPath, mAnimatePaint);

                animationCheckPath.reset();
            }

        } else {

            int x = paddingLeft + width * mProgressActual / 100;
            int modifiedProgress = mProgressActual < 50 ? mProgressActual : 100 - mProgressActual;
            int y = (4 * modifiedProgress * modifiedProgress / getHeight()) + getHeight() / 2;      // quadratic grow -> x^2

            // connecting line
            progressBarPath.moveTo(paddingLeft, getHeight() / 2);
            progressBarPath.lineTo(x, y);
            progressBarPath.lineTo(paddingLeft + width, getHeight() / 2);

            canvas.drawPath(progressBarPath, mProgressPaint);
            canvas.drawText(String.format(Locale.getDefault(), "%d%%", mProgressActual), x, y - 30, mTextPaint);

            progressBarPath.reset();
        }

        if (finishes) {
            finishes = false;
        }
    }

    public void reset() {
        animating = false;
        animateProgress = 0;
        finishes = false;
        setProgressPercent(0);
    }
}
