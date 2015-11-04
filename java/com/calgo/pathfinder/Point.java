package com.calgo.pathfinder;

class Point{
    int x;
    int y;
    Point left;
    Point right;
    Point down;
    Point up;
    boolean end; // destination

    Point (int a, int b) {
        x = a;
        y = b;
        left = right = up = down = null;
        end = false;
    }

    void link(Point p) {
        if (p.y < y) {
            up = p;
            p.down = this;
        } else if (p.y > y) {
            down = p;
            p.up = this;
        } else if (p.x < x) {
            left = p;
            p.right = this;
        } else if (p.x > x) {
            right = p;
            p.left = this;
        }
    }
}