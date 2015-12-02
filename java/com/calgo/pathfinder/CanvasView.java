package com.calgo.pathfinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    static final int MOUSE_STEP_IN_MS_INIT = 450;
    static final int GRID_STEP_DIV = 20;
    static final boolean MOUSE_CENTER = false;
    static final int VP_SCALE = 3;
    /**/

    static final float SFACTOR = (float)40/GRID_STEP_DIV;
    static final float MOUSE_OVAL_SIZE = 10f*((float)1+SFACTOR*(float)Math.log((float)VP_SCALE));
    static final float POINT_OVAL_SIZE = 7f*((float)1+SFACTOR*(float)Math.log((float)VP_SCALE));
    static final float TARGET_OVAL_SIZE = 12f*((float)1+SFACTOR*(float)Math.log((float)VP_SCALE));

    static final int MOUSE = 0;
    static final int PLAYER_1 = 1;

    public int width;
    public int height;
    public Paint p1Paint;

    private MainActivity context;
    private int gridStep;
    private Path p1Path;
    private Paint linePaint;

    private PointGroup pg;
    private Point[] points;

    private Point[] mouse;
    private Paint mousePaint;
    private Paint pointPaint;
    private Paint targetPaint;
    private Paint scorePaint;
    private Point p1;

    private boolean reInit = false;
    private AlgoInterface[] pathAlgo;

    private int mouseWin = 0;
    private int p1Win = 0;
    private int mouse_step_in_ms = MOUSE_STEP_IN_MS_INIT;
    private long mouseLastMovedTimeStamp;

    private int lastWinnder = -1;

    private int mLevel = 4;

    private Button[] mButtons = new Button[4];
    private Thread gameThread;
    private Object mPauseLock = new Object();
    private boolean mPaused;
    private boolean mFinished;

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
        p1Paint.setAlpha(255 * 60 / 100);

        scorePaint = new Paint();
        scorePaint.setAntiAlias(true);
        scorePaint.setColor(Color.RED);
        scorePaint.setStyle(Paint.Style.STROKE);
        scorePaint.setTextSize(26);

        gameThread = new Thread(gameRunner);
    }

    synchronized private void initMaze() {
        if (lastWinnder != MOUSE) {
            pg = new PointGroup(0, 0, width / gridStep, height / gridStep);
            pg.addRandomPoints();
        }

        points = pg.getAllPoints();

        mouse = new Point[mLevel];

        for (int i=0;i<mouse.length;i++)
            mouse[i] = pg.targetPoint;

        p1 = pg.getStartingPoint();

        inAnimation = false;
        lastDx = lastDy = ANIM_INIT_XY_VAL;

        p1Path.reset();
        prevP1 = null;
        lastWinnder = -1;
        lastP1dir = -1;
        
 //     pathAlgo = new DFSAlgo(mouse);
        pathAlgo = new AlgoInterface[mLevel];



        pathAlgo[0] = new DijkAlgo(points, mouse[0],p1);
        if (mouse.length >= 2)
           pathAlgo[1] = new ChaseAlgo(points, mouse[1],p1);
        for (int i=2;i<mouse.length ;i++) {
            pathAlgo[i] = new RandAlgo(mouse[i]);
        }

    }

    private void checkMouseWithPlayerPoints() {
        for (int i = 0; i < mouse.length; i++) {
            if (mouse[i] == p1) {
                reInit = true;
                mouseWin++;
                lastWinnder = MOUSE;
                return;
            }
        }
    }

    private Runnable gameRunner = new Runnable() {
        @Override
        public void run() {
            while (!mFinished) {
                if (reInit) {
                    initMaze();
                    reInit = false;
                }

                /* move player points */
                moveP1();
                checkMouseWithPlayerPoints();

                /* move mouse points */
                if (!reInit && (System.currentTimeMillis() - mouseLastMovedTimeStamp > mouse_step_in_ms) && mouse != null) {
                    mouseLastMovedTimeStamp = System.currentTimeMillis();
                    for (int i = 0; i < mouse.length; i++) {
                        mouse[i] = pathAlgo[i].getNextMove(mouse[i],p1);
                    }
                    checkMouseWithPlayerPoints();
                }

                /* stop for sometime when game ends */
                if (reInit) {
                    /* draw the last frames */
                    reInit = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reInit = true;
                }

                /* if being paused */
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    };


    void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        gridStep = width/GRID_STEP_DIV;
        if (width > h) {
            gridStep = h/GRID_STEP_DIV;
        }

        reInit = true;
        gameThread.start();
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
                captureButton(Point.RIGHT);
            }
        });

        mButtons[Point.LEFT].setLayoutParams(lp);
        mButtons[Point.LEFT].setX(width / 2 - bSize / 2 - bSize);
        mButtons[Point.LEFT].setY(height - bSize * 2);
        mButtons[Point.LEFT].setRotation(180);
        mButtons[Point.LEFT].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                captureButton(Point.LEFT);
            }
        });

        mButtons[Point.UP].setLayoutParams(lp);
        mButtons[Point.UP].setX(width / 2 - bSize / 2);
        mButtons[Point.UP].setY(height - bSize * 3);
        mButtons[Point.UP].setRotation(-90);
        mButtons[Point.UP].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                captureButton(Point.UP);
            }
        });

        mButtons[Point.DOWN].setLayoutParams(lp);
        mButtons[Point.DOWN].setX(width / 2 - bSize / 2);
        mButtons[Point.DOWN].setY(height - bSize);
        mButtons[Point.DOWN].setRotation(90);
        mButtons[Point.DOWN].setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                captureButton(Point.DOWN);
            }
        });
    }

    private final int ANIM_FPS = 4;
    private final int ANIM_INIT_XY_VAL = -999999;

    private int lastDx = ANIM_INIT_XY_VAL;
    private int lastDy = ANIM_INIT_XY_VAL;
    private boolean inAnimation;
    private int animDxStep;
    private int animDyStep;
    private int prevAxis;

    private CanvasOverlayView cov;

    @Override
    synchronized protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (reInit || p1 == null || mouse == null || points == null) {
            invalidate();
            return;
        }

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
        int dx = (mx - p1.x * viewStep);
        int dy = (my - p1.y * viewStep);

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

        /* clear background */
        if (!inAnimation && lastWinnder != -1) {
            if (lastWinnder == MOUSE)
                canvas.drawARGB(0xff, 0xff, 0, 0);
            else if (lastWinnder == PLAYER_1)
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

        for (int i=0;mouse!=null && i<mouse.length;i++) {
            if (mouse[i] != null) {
                canvas.drawCircle(mouse[i].x * viewStep + dx, mouse[i].y * viewStep + dy, MOUSE_OVAL_SIZE, mousePaint);
            }
        }

        if (p1 != null) {
            if (MOUSE_CENTER)
                canvas.drawCircle(p1.x * viewStep + dx, p1.y * viewStep + dy, MOUSE_OVAL_SIZE, p1Paint);
            else
                canvas.drawCircle(mx, my, MOUSE_OVAL_SIZE, p1Paint);
        }

        canvas.drawText("Level = "+mLevel, 15, 20, scorePaint);

        invalidate();
    }

    private Point prevP1;

    // when ACTION_DOWN start touch according to the x,y values
    private int lastP1dir = -1;
    private void captureButton(int dir) {
        lastP1dir = dir;
    }

    private void moveP1() {
        if (reInit)
            return;

        if (lastP1dir == -1)
            return;

        int dir = lastP1dir;
        lastP1dir = -1;

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
            if (!reInit) {
                reInit = true;
                p1Win++;
                mLevel++;
                lastWinnder = PLAYER_1;
            }
        }
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

        return true;
    }

    Path getPath(int who) {
        return p1Path;
    }

    int getObjPointSize() {
        // p1 + targetPoint + all mouse points
        return 2 + mouse.length;
    }

    Point getObjPoint(int who) {
        if (who == 0)
            return p1;
        else if (who == 1)
            return pg.targetPoint;

        int idx = who - 2;

        if (idx >= mouse.length)
            return null;
        else
            return mouse[idx];
    }

    Paint getPaint(int who) {
        switch (who) {
            case 0: return p1Paint;
            case 1: return targetPaint;
            default: return mousePaint;
        }
    }

    int getGridStep() {
        return gridStep;
    }
}