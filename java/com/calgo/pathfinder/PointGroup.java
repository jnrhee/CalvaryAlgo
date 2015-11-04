package com.calgo.pathfinder;

import java.util.ArrayList;

class PointGroup {
    private int x; // next point to add
    private int y;
    private int maxX;
    private int maxY;
    private ArrayList<Point> al = new ArrayList<Point>();
    private int idx;
    private Point prevP;

    PointGroup (int x1, int y1, int mx, int my) {
        x = x1;
        y = y1;
        maxX = mx;
        maxY = my;
        idx=0;
        prevP = null;
    }

    boolean addNextPoint() {
        Point p = new Point(x, y);
        if (prevP != null) {
            p.link(prevP);
        }
        prevP = p;
        al.add(p);

        if (Math.random()*10 < 5)
            y++;
        else
            x++;

        if (x > maxX || y > maxY) {
            p.end = true;
            return false;
        } else
            return true;
    }

    Point[] getAllPoints() {
        return al.toArray(new Point[al.size()]);
    }

    Point getStartingPoint() {
        return al.get(0);
    }
}