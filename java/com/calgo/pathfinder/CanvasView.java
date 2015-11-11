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
    static final float MOUSE_OVAL_SIZE = 20f;
    static final float POINT_OVAL_SIZE = 14f;
    static final float TARGET_OVAL_SIZE = 24f;

    static final int MOUSE_STEP_IN_MS = 100;

    public int width;
    public int height;
    Context context;

    private int gridStep;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint linePaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;

    private PointGroup pg;
    private Point[] points;
    private Point mouse;
    private Paint mousePaint;
    private Paint pointPaint;
    private Paint targetPaint;

    private int mouseDir = -1;
    private boolean reInit = false;
    private Handler handler = new Handler();
    private AlgoInterface pathAlgo;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(4f);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);

        targetPaint = new Paint();
        targetPaint.setAntiAlias(true);
        targetPaint.setColor(Color.RED);
        targetPaint.setStyle(Paint.Style.FILL);

        mousePaint = new Paint();
        mousePaint.setAntiAlias(true);
        mousePaint.setColor(Color.YELLOW);
        mousePaint.setStyle(Paint.Style.FILL);

        handler.postDelayed(runnable, 100);
    }

    private void initMaze() {
        pg = new PointGroup(0,0, width/gridStep, height/gridStep);
        pg.addRandomPoints();
        points = pg.getAllPoints();
        mouse = pg.getStartingPoint();
        mouseDir = -1;

      pathAlgo = new DFSAlgo(mouse);
 //        pathAlgo = new RandAlgo(mouse);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (reInit) {
                reInit = false;
                initMaze();
            }

            if (mouse != null) {
                mouse = pathAlgo.getNextMove();
                invalidate();
                if (!pathAlgo.isFound())
                    handler.postDelayed(this, MOUSE_STEP_IN_MS);
                else {
                    reInit = true;
                    handler.postDelayed(this, 1000);
                }
            } else
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

        gridStep = width/20;
        if (width > h) {
            gridStep = h/20;
        }

        initMaze();
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
        /*
        for (int i=gridStep;i<width;i+=gridStep) {
            canvas.drawLine(i,0,i,height,linePaint);
        }
        for (int i=gridStep;i<height;i+=gridStep) {
            canvas.drawLine(0,i,width,i,linePaint);
        }
        */
        
        for (int i=0;i<points.length;i++) {
            Point pt = points[i];
            int x = pt.x * gridStep;
            int y = pt.y * gridStep;

            if (pt.left != null)
                canvas.drawLine(x, y, x - gridStep, y,linePaint);
            if (pt.right != null)
                canvas.drawLine(x, y, x + gridStep, y,linePaint);
            if (pt.up != null)
                canvas.drawLine(x, y, x, y-gridStep,linePaint);
            if (pt.down != null)
                canvas.drawLine(x, y, x, y+gridStep,linePaint);
        }

        for (int i=0;i<points.length;i++) {
            Point pt = points[i];
            int x = pt.x * gridStep;
            int y = pt.y * gridStep;

            Paint p = null;
            float ov_size = 0;
            if (pt.mTarget) {
                p = targetPaint;
                ov_size = TARGET_OVAL_SIZE;
            } else {
                p = pointPaint;
                ov_size = POINT_OVAL_SIZE;
            }

            canvas.drawCircle(x, y, ov_size, p);
        }

        if (mouse != null) {
            canvas.drawCircle(mouse.x * gridStep, mouse.y * gridStep, MOUSE_OVAL_SIZE, mousePaint);
        }
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