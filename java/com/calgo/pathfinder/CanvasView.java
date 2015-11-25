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
import android.widget.Button;
import android.widget.RelativeLayout;

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
    public Paint p1Paint;

    private MainActivity context;
    private int gridStep;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path p1Path;
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
    private Paint scorePaint;
    private Point p1;

    private int mouseDir = -1;


    private boolean reInit = false;
    private Handler handler = new Handler();
    private AlgoInterface pathAlgo;

    private int mouseWin = 0;
    private int p1Win = 0;
    private int mouse_step_in_ms = 700;

    private int lastWinnder;

    private Button[] mButtons = new Button[4];

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
        context = (MainActivity)c;

        hideSystemUI();

        // we set a new Path
        p1Path = new Path();

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

        dGuideX  = width * 1/3;

        mouseDir = -1;

        inAnimation = false;
        lastDx = lastDy = ANIM_INIT_XY_VAL;

        p1Path.reset();
        prevP1 = null;
        if (cov != null)
            cov.invalidate();

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
                            lastWinnder = 0;
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

    private void setupPostView() {
        if (mButtons[Point.RIGHT] != null)
            return;

        int bSize = width/5;
        mButtons[Point.RIGHT] = (Button) context.findViewById(R.id.btnR);

        int origSize = mButtons[Point.RIGHT].getWidth();
        if (origSize == 0) {
            mButtons[Point.RIGHT] = null;
            return;
        }

        mButtons[Point.LEFT] = (Button) context.findViewById(R.id.btnL);
        mButtons[Point.UP] = (Button) context.findViewById(R.id.btnU);
        mButtons[Point.DOWN] = (Button) context.findViewById(R.id.btnD);


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(bSize, bSize);
        mButtons[Point.RIGHT].setLayoutParams(lp);
        mButtons[Point.RIGHT].setX(width / 2 + bSize / 2);
        mButtons[Point.RIGHT].setY(height - bSize * 2);
        mButtons[Point.RIGHT].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                handleButton(Point.RIGHT);
            }
        });

        mButtons[Point.LEFT].setLayoutParams(lp);
        mButtons[Point.LEFT].setX(width / 2 - bSize / 2 - bSize);
        mButtons[Point.LEFT].setY(height - bSize * 2);
        mButtons[Point.LEFT].setRotation(180);
        mButtons[Point.LEFT].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                handleButton(Point.LEFT);
            }
        });

        mButtons[Point.UP].setLayoutParams(lp);
        mButtons[Point.UP].setX(width / 2 - bSize / 2);
        mButtons[Point.UP].setY(height - bSize * 3);
        mButtons[Point.UP].setRotation(-90);
        mButtons[Point.UP].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                handleButton(Point.UP);
            }
        });

        mButtons[Point.DOWN].setLayoutParams(lp);
        mButtons[Point.DOWN].setX(width / 2 - bSize / 2);
        mButtons[Point.DOWN].setY(height - bSize);
        mButtons[Point.DOWN].setRotation(90);
        mButtons[Point.DOWN].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                handleButton(Point.DOWN);
            }
        });
    }

    private final int ANIM_FPS = 5;
    private final int ANIM_INIT_XY_VAL = -999999;

    private int lastDx = ANIM_INIT_XY_VAL;
    private int lastDy = ANIM_INIT_XY_VAL;
    private boolean inAnimation;
    private int animDxStep;
    private int animDyStep;
    private int prevAxis;

    private CanvasOverlayView cov;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setupPostView();

        /* overlay coordinates update */
        if(prevP1 != null && prevP1 != p1) {
            p1Path.moveTo(p1.x*gridStep, p1.y*gridStep);
            p1Path.lineTo(prevP1.x*gridStep, prevP1.y*gridStep);
        }
        if (cov == null) {
            cov = (CanvasOverlayView) context.findViewById(R.id.myCanvasOverlay);
        }
        if (cov != null)
            cov.invalidate();

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

        if (lastDx == ANIM_INIT_XY_VAL) {
            lastDx = dx;
            lastDy = dy;
            inAnimation = false;
        } else if ((dx != lastDx) || (dy != lastDy)) {
            inAnimation = true;

            animDxStep = (dx-lastDx)/ANIM_FPS;
            animDyStep = (dy-lastDy)/ANIM_FPS;

            /* check for corner animation and speed it up to finish
             * the previous directional animation first.
             */
            /*
            if (animDxStep != 0 && animDyStep != 0) {
                if (prevAxis == 0) {
                    animDyStep = 0;
                    animDxStep *= 2;
                } else {
                    animDxStep = 0;
                    animDyStep *= 2;
                }
            }
            */

            lastDx += animDxStep;
            lastDy += animDyStep;

            /* check for close-by */
            int d = Math.abs(dx);
            int r = Math.abs((dx-lastDx)%5);
            int i = Math.abs(lastDx);

            if ((i >= (d-r)) && (i <= (d+r)))
                lastDx = dx;

            d = Math.abs(dy);
            r = Math.abs((dy-lastDy)%5);
            i = Math.abs(lastDy);

            if ((i >= (d-r)) && (i <= (d+r)))
                lastDy = dy;

            if (animDxStep != 0)
                prevAxis = 0;
            else
                prevAxis = 1;

            if ((dx == lastDx) && (dy == lastDy))
                inAnimation = false;

            dx = lastDx;
            dy = lastDy;
        }

        if (points == null)
            return;

        /* clear background */
        if (reInit & !inAnimation) {
            if (lastWinnder == 0)
                canvas.drawARGB(0xff, 0xff, 0, 0);
            else if (lastWinnder == 1)
                canvas.drawARGB(0xff, 0x10, 0x10, 0xff);
        } else
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

        if (inAnimation)
            invalidate();

            /* handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                    }
                }, 16); */

    }

    private Point prevP1;

    // when ACTION_DOWN start touch according to the x,y values
    private void handleButton(int dir) {
        if (reInit)
            return;

        prevP1 = p1;
        switch (dir) {
            case Point.UP:
                if (p1.up != null)
                    p1 = p1.up;
                break;

            case Point.DOWN:
                if (p1.down != null)
                    p1 = p1.down;
                break;

            case Point.RIGHT:
                if (p1.right != null)
                    p1 = p1.right;
                break;

            case Point.LEFT:
                if (p1.left != null)
                    p1 = p1.left;
                break;
        }

        if (p1.mTarget) {
            synchronized (sync) {
                if (!reInit) {
                    reInit = true;
                    p1Win++;
                    lastWinnder = 1;

                    if (mouse_step_in_ms > 100)
                        mouse_step_in_ms -= 100;
                    else if (mouse_step_in_ms > 33)
                        mouse_step_in_ms -= 10;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 2000);
                }
            }
        }

        invalidate();
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean updateScreen = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //startTouch(x, y);
                //updateScreen = true;
                break;
        }

        if (updateScreen)
            invalidate();

        return true;
    }

    Path getPath(int who) {
        return p1Path;
    }

    Point getPoint(int who) {
        switch (who) {
            case 0: return mouse;
            case 1: return p1;
        }
        return null;
    }

    Paint getPaint(int who) {
        switch (who) {
            case 0: return mousePaint;
            case 1: return p1Paint;
        }
        return null;
    }

    int getGridStep() {
        return gridStep;
    }
}