package ru.gkpromtech.exhibition.schema;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SchemaCallout extends RelativeLayout {

    private TextView mTextTitle;
    private final float mDensity;

    @SuppressWarnings("deprecation")
    public SchemaCallout(Context context) {
        super(context);

        mDensity = context.getResources().getDisplayMetrics().density;

        LinearLayout bubble = new LinearLayout(context);
        bubble.setOrientation(LinearLayout.HORIZONTAL);
        int[] colors = {0xE6888888, 0xFF000000};
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        drawable.setCornerRadius(6);
        drawable.setStroke(2, 0xDD000000);
        bubble.setBackgroundDrawable(drawable);
        bubble.setId(android.R.id.button1);
        bubble.setGravity(Gravity.CENTER_VERTICAL);
        bubble.setPadding(10, 10, 10, 10);
        LayoutParams bubbleLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(bubble, bubbleLayout);

        Nub nub = new Nub(context);
        LayoutParams nubLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        nubLayout.addRule(RelativeLayout.BELOW, bubble.getId());
        nubLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(nub, nubLayout);

        mTextTitle = new TextView(getContext());
        mTextTitle.setTextColor(0xFFFFFFFF);
        mTextTitle.setTextSize(12);
        mTextTitle.setMaxWidth((int) (160 * mDensity));
        mTextTitle.setMaxLines(4);
        mTextTitle.setEllipsize(TextUtils.TruncateAt.END);
        bubble.addView(mTextTitle);

    }

    public void setTitle(String title) {
        mTextTitle.setText(title);
    }

    public void transitionIn() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 1f);
        scaleAnimation.setInterpolator(new OvershootInterpolator(1.2f));
        scaleAnimation.setDuration(250);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1f);
        alphaAnimation.setDuration(200);

        AnimationSet animationSet = new AnimationSet(false);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        startAnimation(animationSet);
    }

    private class Nub extends View {

        private Paint paint = new Paint();
        private Path path = new Path();

        public Nub(Context context) {

            super(context);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF000000);
            paint.setAntiAlias(true);

            path.lineTo(20 * mDensity, 0);
            path.lineTo(10 * mDensity, 15 * mDensity);
            path.close();

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension((int) (20 * mDensity), (int) (15 * mDensity));
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
            super.onDraw(canvas);
        }
    }

}
