package com.calgo.pathfinder;

import android.util.Log;

import java.util.Stack;

/**
 * Created by hyunchan on 11/24/2015.
 */
public class ChaseAlgo implements AlgoInterface {

    private Point[] allPoints;
    private Point curPoint;
    private Point targetPoint;
    private Point lastPoint;
    private Stack theStack;
    AlgoInterface chaseAlgo;

    ChaseAlgo(Point[] points, Point start, Point target) {
        lastPoint = curPoint = start;
        targetPoint = target;
        allPoints = points;

    }

    @Override
    public Point getNextMove() {
        Point returnMove;

        returnMove = curPoint;
        return returnMove;
    }


    public Point getNextMove(Point start, Point p1) {
        Point nextPoint, returnMove;
        int newX, newY;


    //    Log.e("CHC", "P1 is: " + p1 + "MyPoint is:" +curPoint);

        nextPoint = curPoint;
  //      while (nextPoint == curPoint) {
            if (p1.x > curPoint.x) {
                Log.e("CHC", "P1  is on my right" + p1.x + ":" + curPoint.x);
                if (curPoint.right != null) {
                    nextPoint = curPoint.right;
                    Log.e("CHC", "Move to rigtt");
//                    continue;
                }
            } else  if (p1.x < curPoint.x) {
                Log.e("CHC", "P1  is on my left" + p1.x + ":" + curPoint.x);
                if (curPoint.left != null) {
                    nextPoint = curPoint.left;
                    Log.e("CHC", "Move to left");
  //                  continue;
                }
            } else if (p1.y > curPoint.y) {
                Log.e("CHC", "P1  is on my down" + p1.y + ":" + curPoint.y);
                if (curPoint.down != null) {
                    nextPoint = curPoint.down;
                    Log.e("CHC", "Move to down");
    //                continue;
                }
            } else  if (p1.y < curPoint.y) {
                Log.e("CHC", "P1  is on my up" + p1.y + ":" + curPoint.y);
                if (curPoint.up != null) {
                    nextPoint = curPoint.up;
                    Log.e("CHC", "Move to up");
      //              continue;
                }
            }
            if (curPoint == nextPoint) {
                Log.e("CHC", "Didn't get direction");
                for(Point p : curPoint.getNeighbors()) {
                    if (p != lastPoint) {
                        nextPoint = p;
                        break;
                    }

                }

            }
     //   }
        lastPoint = curPoint;
        curPoint= nextPoint;
        return nextPoint;

    }

    @Override
    public boolean isFound() {
        return false;
    }
}
