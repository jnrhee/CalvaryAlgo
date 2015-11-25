package com.calgo.pathfinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CanvasOverlayView extends View {
    private MainActivity context;
    private Paint mPaint;
    private Matrix mat;
    private Path mPath;
    private float padding = 16f;
    private static final float PADDING_SCALE = 0.9f;

    public CanvasOverlayView(Context c, AttributeSet attrs) {
        super(c, attrs);

        context = (MainActivity) c;
        mPaint = new Paint();
        mPaint.setStrokeWidth(5);
        mPaint.setPathEffect(null);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);

        mat = new Matrix();
        mat.setTranslate(padding, padding);
        mat.postScale(PADDING_SCALE, PADDING_SCALE);
        //mat.setTranslate(padding, padding);

        mPath = new Path();

    }

    private final float ovScale = 0.3f;
    private final float paddingPct = 0.01f;
    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        padding = (float)w*paddingPct;

        float ref = (1f-ovScale)/2f;

        setX(((float)w * ref)- paddingPct*(float)w);
        setY(-((float) h * ref) + paddingPct * (float) w);
        setScaleX(ovScale);
        setScaleY(ovScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawARGB(0x80, 0x0, 0, 0);

        CanvasView mainCv = (CanvasView) findViewById(R.id.myCanvas);
        if (mainCv == null) {
            mainCv = (CanvasView) context.findViewById(R.id.myCanvas);
        }

        if (mainCv != null) {
            Path p = mainCv.getPath(1);
            p.transform(mat, mPath);
            canvas.drawPath(mPath, mPaint);
        }

        int size = mainCv.getObjPointSize();
        for (int i=0;i<size;i++) {
            Point obj = mainCv.getObjPoint(i);
            Paint paint = mainCv.getPaint(i);
            int gridStep = mainCv.getGridStep();
            canvas.drawCircle(
                    padding + PADDING_SCALE * (float) (obj.x * gridStep),
                    padding + PADDING_SCALE * (float) (obj.y * gridStep),
                    mainCv.MOUSE_OVAL_SIZE / 2,
                    paint);
        }
    }

}