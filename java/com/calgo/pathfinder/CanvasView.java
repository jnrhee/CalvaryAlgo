package com.calgo.pathfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    /*
     * ALGO TESTING

    static final int MOUSE_STEP_IN_MS_INIT = 33;
    static final int GRID_STEP_DIV = 40;
    static final boolean MOUSE_CENTER = true;
    static final int VP_SCALE = 1;
    */

    static final int MOUSE_STEP_IN_MS_INIT = 500;
    static final int GRID_STEP_DIV = 20;
    static final boolean MOUSE_CENTER = false;
    static final int VP_SCALE = 4;
    /**/

    static final float SFACTOR = (float)40/GRID_STEP_DIV;
    static final float MOUSE_OVAL_SIZE = 10f*((float)1+SFACTOR*(float)Math.log((float)VP_SCALE));
    static final float POINT_OVAL_SIZE = 7f*((float)1+SFACTOR*(float)Math.log((float)VP_SCALE));
    static final float TARGET_OVAL_SIZE = 12f*((float)1+SFACTOR*(float)Math.log((float)VP_SCALE));

    public int width;
    public int height;
    Context context;

    private int gridStep;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint linePaint;
    private float mX, mY;

    private float dGuideY1, dGuideY2, dGuideX;

    private static final float TOLERANCE = 5;

    private PointGroup pg;
    private Point[] points;

    private Point mouse;
    private Paint mousePaint;
    private Paint pointPaint;
    private Paint targetPaint;
    private Paint p1Paint;
    private Paint scorePaint;
    private Point p1;

    private int mouseDir = -1;


    private boolean reInit = false;
    private Handler handler = new Handler();
    private AlgoInterface pathAlgo;

    private int mouseWin = 0;
    private int p1Win = 0;
    private int mouse_step_in_ms = 700;

    private void hideSystemUI() {
        this.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        hideSystemUI();

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
        mousePaint.setAlpha(255 * 60 / 100);

        p1Paint = new Paint();
        p1Paint.setAntiAlias(true);
        p1Paint.setColor(Color.BLUE);
        p1Paint.setStyle(Paint.Style.FILL);
        p1Paint.setAlpha(255*60/100);

        scorePaint = new Paint();
        scorePaint.setAntiAlias(true);
        scorePaint.setColor(Color.RED);
        scorePaint.setStyle(Paint.Style.STROKE);
        scorePaint.setTextSize(20);

        handler.postDelayed(runnable, 100);
    }

    private void initMaze() {
        pg = new PointGroup(0,0, width/gridStep, height/gridStep);
        pg.addRandomPoints();
        points = pg.getAllPoints();

        mouse = pg.getStartingPoint();
        p1    = pg.getStartingPoint();

        dGuideY1 = height * 1/3;
        dGuideY2 = height * 2/3;

        dGuideX  = width * 1/2;

        mouseDir = -1;

 //     pathAlgo = new DFSAlgo(mouse);
 //        pathAlgo = new RandAlgo(mouse);
        pathAlgo = new DijkAlgo(mouse);
    }

    private Object sync = new Object();

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
                    handler.postDelayed(this, mouse_step_in_ms);
                else {
                    synchronized (sync) {
                        if (!reInit) {
                            reInit = true;
                            mouseWin++;
                        }
                    }
                    handler.postDelayed(this, 2000);
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

        gridStep = width/GRID_STEP_DIV;
        if (width > h) {
            gridStep = h/GRID_STEP_DIV;
        }

        mouse_step_in_ms = MOUSE_STEP_IN_MS_INIT;
        initMaze();
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int scale = VP_SCALE;
        int viewStep = gridStep*scale;
        int mx = width/2;
        int my = height/2;
        int dx = (mx - mouse.x * viewStep);
        int dy = (my - mouse.y * viewStep);
        if (!MOUSE_CENTER) {
            dx = (mx - p1.x * viewStep);
            dy = (my - p1.y * viewStep);
        }

        if (points == null)
            return;

        /* clear background */
        if (reInit)
            canvas.drawARGB(0xff, 0xff, 0, 0);
        else
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

        // and we set a new Paint with the desired attributes

        for (int i=0;i<points.length;i++) {
            Point pt = points[i];
            int x = pt.x * viewStep+dx;
            int y = pt.y * viewStep+dy;

            if (pt.left != null)
                canvas.drawLine(x, y, x - viewStep, y,linePaint);
            if (pt.right != null)
                canvas.drawLine(x, y, x + viewStep, y,linePaint);
            if (pt.up != null)
                canvas.drawLine(x, y, x, y-viewStep,linePaint);
            if (pt.down != null)
                canvas.drawLine(x, y, x, y+viewStep,linePaint);
        }

        for (int i=0;i<points.length;i++) {
            Point pt = points[i];
            int x = pt.x * viewStep+dx;
            int y = pt.y * viewStep+dy;

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
            if (MOUSE_CENTER)
                canvas.drawCircle(mx, my, MOUSE_OVAL_SIZE, mousePaint);
            else
                canvas.drawCircle(mouse.x * viewStep + dx, mouse.y * viewStep + dy, MOUSE_OVAL_SIZE, mousePaint);
        }

        if (p1 != null) {
            if (MOUSE_CENTER)
                canvas.drawCircle(p1.x * viewStep + dx, p1.y * viewStep + dy, MOUSE_OVAL_SIZE, p1Paint);
            else
                canvas.drawCircle(mx, my, MOUSE_OVAL_SIZE, p1Paint);
        }

        canvas.drawText("P1:"+p1Win+"  Mouse:"+mouseWin, 15, 15, scorePaint);
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        if (reInit)
            return;

        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        if (y <= dGuideY1 && p1.up != null) {
            p1 = p1.up;
        } else if (y >= dGuideY2 && p1.down != null) {
            p1 = p1.down;
        } else if (x >= dGuideX && p1.right != null) {
            p1 = p1.right;
        } else if (x < dGuideX && p1.left != null) {
            p1 = p1.left;
        }

        if (p1.mTarget) {
            synchronized (sync) {
                if (!reInit) {
                    reInit = true;
                    p1Win++;
                    if (mouse_step_in_ms > 100)
                        mouse_step_in_ms -= 100;
                    else if (mouse_step_in_ms > 33)
                        mouse_step_in_ms -= 10;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 2000);
                }
            }
        }
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