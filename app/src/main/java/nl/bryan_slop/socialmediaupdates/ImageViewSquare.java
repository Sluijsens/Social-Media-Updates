package nl.bryan_slop.socialmediaupdates;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

/**
 * Created by Bryan on 21-8-2014.
 */
public class ImageViewSquare extends ImageView {
    final private Context context;

    public ImageViewSquare(Context context) {
        super(context);
        this.context = context;
    }

    public ImageViewSquare(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ImageViewSquare(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(widthSize, widthSize);
    }
}
