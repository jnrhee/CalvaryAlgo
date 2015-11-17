package com.calgo.pathfinder;

import java.util.ArrayList;

class Point implements  Comparable<Point>{
    static final int UP = 0;
    static final int DOWN = 1;
    static final int LEFT = 2;
    static final int RIGHT = 3;

    int x;
    int y;
    Point left;
    Point right;
    Point down;
    Point up;

    private int end;

    boolean mTarget;
    private boolean isVisit;

    //Dijkstra
    int minDistance;
    int weight;
    Point previousPoint;

    Point (int a, int b) {
        x = a;
        y = b;
        left = right = up = down = null;
        end = -1;
        mTarget = false;
        isVisit = false;
        //Dijkstra
        minDistance = Integer.MAX_VALUE;
        weight = 1;
    }

    void tryToLinkTo(Point p) {
        if (p.x == x && p.y == y)
            return;
        if (p.x == x) {
            if (p.y == y - 1) {
                up = p;
                p.down = this;
            } else if (p.y == y + 1) {
                down = p;
                p.up = this;
            }
        }

        if (p.y == y) {
            if (p.x == x - 1) {
                left = p;
                p.right = this;
            } else if (p.x == x + 1) {
                right = p;
                p.left = this;
            }
        }
    }

    int isEnd() {
        if (end == -1) {
            int cnt  = 0;
            if (left != null) cnt++;
            if (right != null) cnt++;
            if (up != null) cnt++;
            if (down != null) cnt++;

            if (cnt < 2)
                end = 1;
            else
                end = 0;
        }

        return end;
    }

    double distanceFrom(Point p) {
        int xdist = Math.abs(x - p.x);
        int ydist = Math.abs(y - p.y);

        return Math.sqrt(xdist*xdist + ydist*ydist);
    }

    void setVisit(boolean visit) {
        isVisit = visit;
    }

    boolean getVisit() {
        return isVisit;
    }

    ArrayList<Point> getNeighbors() {
        ArrayList<Point> neighbors = new ArrayList<Point>();

        if (up != null)
            neighbors.add(up);
        if (right != null)
            neighbors.add(right);
        if (down != null)
            neighbors.add(down);
        if (left != null)
            neighbors.add(left);

        return neighbors;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + "x["+x +"] y["+ y+"]";
    }

    @Override
    public int compareTo(Point other)
    {
      // return minDistance.compareTo(other.minDistance);
      return Double.compare(minDistance, other.minDistance);
        //
       // minDistance.compareTo()
    }
}