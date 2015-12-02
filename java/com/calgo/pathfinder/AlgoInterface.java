package com.calgo.pathfinder;

/**
 * Created by hyunchan on 11/10/2015.
 */
public interface AlgoInterface {
    Point getNextMove();
    Point getNextMove(Point start, Point target);
    boolean isFound();
}
