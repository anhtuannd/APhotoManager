package de.k3b.android.osmdroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Creates Icon with text.
 *
 * Created by k3b on 16.07.2015.
 */
public class IconFactory {

    private final DefaultResourceProxyImplEx mResourceProxy;

    /** cluster icon anchor */
    /** anchor point to draw the number of markers inside the cluster icon */
    private float mTextAnchorU = MarkerBase.ANCHOR_CENTER, mTextAnchorV = MarkerBase.ANCHOR_CENTER;

    private final Bitmap mBackground;
    private Paint mTextPaint;

    /** must be called from the gui-Thread */
    public IconFactory(DefaultResourceProxyImplEx resourceProxy, Drawable background) {
        this(resourceProxy, ((BitmapDrawable) background).getBitmap());
    }

    /** must be called from the gui-Thread */
    public IconFactory(DefaultResourceProxyImplEx resourceProxy, Bitmap background) {
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(15.0f);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        mBackground = background;
        this.mResourceProxy = resourceProxy;
    }

    // inspired by org.osmdroid.bonuspack.clustering.MarkerClusterer.
    public BitmapDrawable createIcon(int id, String text) {
        Bitmap finalIcon = Bitmap.createBitmap(mBackground.getWidth(), mBackground.getHeight(), mBackground.getConfig());
        Canvas iconCanvas = new Canvas(finalIcon);
        iconCanvas.drawBitmap(mBackground, 0, 0, null);
        int textHeight = (int) (mTextPaint.descent() + mTextPaint.ascent());
        iconCanvas.drawText(text,
                mTextAnchorU * finalIcon.getWidth(),
                mTextAnchorV * finalIcon.getHeight() - textHeight / 2,
                mTextPaint);
        return new BitmapDrawable(mResourceProxy.getResources(),  finalIcon);
    }
}