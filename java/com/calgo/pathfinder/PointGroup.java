package com.calgo.pathfinder;

import java.util.ArrayList;

class PointGroup {
    private int mX; // next point to add
    private int mY;
    private int maxX;
    private int maxY;
    private ArrayList<Point> mAl = new ArrayList<Point>();
    private int idx;
    private int mDir;

    PointGroup (int x1, int y1, int mx, int my) {
        mX = x1;
        mY = y1;
        maxX = mx;
        maxY = my;
        idx=0;
    }

    void addRandomPoints() {
        int dir;

        /* set initial direction */
        addCurrentPoint();

        boolean found = false;
        do {
            dir = randomDir();
            found = move(dir);
        } while (!found);

        addCurrentPoint();

        for (int i=0;i<(maxX*maxY)/4;i++) {
            if (move(mDir))
                addCurrentPoint();

            found = false;
            int favorCurrentDirCnt = 2;
            do {
                dir = randomDir();
                if (favorCurrentDirCnt > 0 && dir != mDir) {
                    favorCurrentDirCnt--;
                } else {
                    if (!isOppositeDir(mDir, dir)) {
                        found = move(dir);
                    }
                }
            } while (!found);

            addCurrentPoint();
        }

        Point[] pts = mAl.toArray(new Point[mAl.size()]);
        Point startPt = mAl.get(0);
        Point endPt = null;

        for (int i=0;i<mAl.size()-1;i++) {
            Point p = pts[i];
            for (int j = i+1; j < mAl.size(); j++) {
                p.tryToLinkTo(pts[j]);
            }

            if (endPt == null)
                endPt = p;
            else if (p.distanceFrom(startPt) > endPt.distanceFrom(startPt))
                endPt = p;
        }

        endPt.mTarget = true;
    }

    static boolean isOppositeDir(int d1, int d2) {
        switch (d1) {
            case Point.UP: if (d2 == Point.DOWN) return true; break;
            case Point.DOWN: if (d2 == Point.UP) return true; break;
            case Point.LEFT: if (d2 == Point.RIGHT) return true; break;
            case Point.RIGHT: if (d2 == Point.LEFT) return true; break;
        }
        return false;
    }

    static int getOppositeDir(int d1) {
        switch (d1) {
            case Point.UP: return Point.DOWN;
            case Point.DOWN: return Point.UP;
            case Point.LEFT: return Point.RIGHT;
            case Point.RIGHT: return Point.LEFT;
        }

        return -1;
    }

    private boolean move(int dir) {
        int x = mX;
        int y = mY;
        switch (dir) {
            case Point.UP: y--; break;
            case Point.DOWN: y++; break;
            case Point.LEFT: x--; break;
            case Point.RIGHT: x++; break;
        }

        if (x < 0 || x > maxX || y < 0 || y > maxY)
            return false;
        else {
            mX = x;
            mY = y;
            mDir = dir;
            return true;
        }
    }

    static int randomDir() {
        return (int)(Math.random()*4)%4;
    }

    private void addCurrentPoint() {
        Point p = new Point(mX, mY);
        mAl.add(p);
    }

    Point[] getAllPoints() {
        return mAl.toArray(new Point[mAl.size()]);
    }

    Point getStartingPoint() {
        return mAl.get(0);
    }
}