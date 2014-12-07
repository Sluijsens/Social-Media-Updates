package nl.bryan_slop.socialmediaupdates;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Bryan on 21-8-2014.
 */
public class ImageView16x9 extends ImageView {
    final private Context context;
    final private SharedPreferences mOAuth2;

    public ImageView16x9(Context context) {
        super(context);
        this.context = context;
        this.mOAuth2 = SMU_Activity.mOAuth2;
    }

    public ImageView16x9(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.mOAuth2 = SMU_Activity.mOAuth2;
    }

    public ImageView16x9(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.mOAuth2 = SMU_Activity.mOAuth2;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float resolution = 16f/9f;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        Drawable drawable = getDrawable();
        int drawableWidth = 0;
        int drawableHeight = 0;
        if(drawable != null) {
            drawableWidth = drawable.getIntrinsicWidth();
            drawableHeight = drawable.getIntrinsicHeight();
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int width;
        width = widthSize;
        //width = (int) Math.ceil((float) widthSize);

        int height;
        // Measure height
        height = (int) Math.ceil((float) width / resolution);

//        if(drawableWidth < width && drawableHeight < height) {
//            if(mOAuth2.getBoolean(SMU_Activity.APP_SETTINGS_STRETCH_IMAGES, true) == false || drawableWidth <= 0 || drawableHeight <= 0) {
//                this.setVisibility(View.GONE);
//            }
//        }

//        Log.d("SMU On measure", "-> Width: " + width + ", Height: " + height + ", imgWidth: " + drawableWidth + ", imgHeight: " + drawableHeight);
        // MUST CALL THIS
        setMeasuredDimension(width, height);
    }
}
