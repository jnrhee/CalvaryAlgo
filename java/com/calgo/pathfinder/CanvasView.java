package com.calgo.pathfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;

public class CanvasView extends View {
    public int width;
    public int height;
    Context context;

    private int gridStep;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;

    private PointGroup pg;
    private Point[] points;
    private Point mouse;

    private Handler handler = new Handler();

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);

        handler.postDelayed(runnable, 100);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mouse != null) {
                if (mouse.down != null)
                    mouse = mouse.down;
                else if (mouse.right != null)
                    mouse = mouse.right;
                else if (mouse.up != null)
                    mouse = mouse.up;
                else if (mouse.left != null)
                    mouse = mouse.left;

                invalidate();
            }

            if (!mouse.end)
                handler.postDelayed(this, 500);
        }
    };

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        width = w;
        height = h;

        gridStep = width/10;
        if (width > h) {
            gridStep = h/10;
        }

        pg = new PointGroup(0,0, width/gridStep, height/gridStep);
        while (pg.addNextPoint());
        points = pg.getAllPoints();
        mouse = pg.getStartingPoint();
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (points == null)
            return;

        /* clear background */
        canvas.drawARGB(0xff, 0x84, 0xde, 0xff);

        /* draw grid lines */
        for (int i=gridStep;i<width;i+=gridStep) {
            canvas.drawLine(i,0,i,height,mPaint);
        }
        for (int i=gridStep;i<height;i+=gridStep) {
            canvas.drawLine(0,i,width,i,mPaint);
        }

        // and we set a new Paint with the desired attributes
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeWidth(24f);

        for (int i=0;i<points.length;i++)
            canvas.drawPoint(points[i].x*gridStep, points[i].y*gridStep, p);

        if (mouse != null) {
            p.setColor(Color.YELLOW);
            canvas.drawPoint(mouse.x * gridStep, mouse.y * gridStep, p);
        }

        // draw the mPath with the mPaint on the canvas when onDraw
        canvas.drawPath(mPath, mPaint);
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        mPath.lineTo(mX, mY);
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean updateScreen = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                updateScreen = true;
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                updateScreen = true;
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                updateScreen = true;
                break;
        }

        if (updateScreen)
            invalidate();

        return true;
    }
}