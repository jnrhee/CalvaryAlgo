package com.calgo.pathfinder;

class Point{
    int x;
    int y;
    Point left;
    Point right;
    Point down;
    Point up;

    Point (int a, int b) {
        x = a;
        y = b;
        left = right = up = down = null;
    }
}