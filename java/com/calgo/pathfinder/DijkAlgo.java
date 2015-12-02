package com.calgo.pathfinder;

import android.database.sqlite.SQLiteDiskIOException;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by hyunchan on 11/10/2015.
 */
public class DijkAlgo implements AlgoInterface {

    List<Point> shortestPathList = null;
    Point nextMove;
    private Point[] allPoints;
    private Point startPoint;
    private Point targetPoint;


    DijkAlgo(Point[] points, Point start, Point target) {

        allPoints = points;
        nextMove = startPoint =start;
        targetPoint = target;

    }

    private void compute() {
        for (Point p: allPoints) {
            p.weight = 1;
            p.minDistance = Integer.MAX_VALUE;
            p.previousPoint = null;
        }
        long startTime = System.currentTimeMillis();
        Log.d("Ernest","===================Start=======================================");
        computePaths(startPoint);
        Log.d("Ernest", "Start:" + startPoint + "target:" + targetPoint);

        shortestPathList =  getShortestPathTo(targetPoint);
        //remmove fist elemement. same as current location.
        shortestPathList.remove(0);
        for (Point p: shortestPathList) {
            Log.d("Ernest", String.format("x = %d y = %d", p.x, p.y));
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        Log.d("Ernest", "==End: elapsed time is : "+ elapsedTime /1000.0+" secs");


    }
    @Override
    public Point getNextMove() {
        Iterator<Point> iter = shortestPathList.listIterator();
        if (iter.hasNext()) {
            nextMove = iter.next();
            iter.remove();
        }
        return nextMove;
    }

    @Override
    public Point getNextMove(Point start, Point target) {

        startPoint = start;
        targetPoint = target;
        compute();
        Iterator<Point> iter = shortestPathList.listIterator();

        if (iter.hasNext()) {
            nextMove = iter.next();
            iter.remove();
        }
        Log.d("Ernest","DjkAlgo next move is :" +nextMove);

        return nextMove;
    }
    @Override
    public boolean isFound() {

        if (shortestPathList.isEmpty())
            return true;
        else
            return false;
    }

    void computePaths(Point source) {
        source.minDistance = 0;
        //visit each vertex u, always visiting vertex with smallest minDistance first
        // Visit each edge exiting u
        PriorityQueue<Point> pointQueue = new PriorityQueue<Point>();
        pointQueue.add(source);

        while (!pointQueue.isEmpty()) {
            Point currentPoint = pointQueue.poll();

            // Visit each edge exiting u
            for (Point nextPoint : currentPoint.getNeighbors()) {
                int weight = nextPoint.weight;
                // relax the edge (u,v)
                int distanceThroughCurrentPoint = currentPoint.minDistance + weight;
                if (distanceThroughCurrentPoint < nextPoint.minDistance) {
                    pointQueue.remove(nextPoint);
                    nextPoint.minDistance = distanceThroughCurrentPoint;
                    nextPoint.previousPoint = currentPoint;
                    pointQueue.add(nextPoint);
                }
            }
        }
    }

    public static List<Point> getShortestPathTo(Point target)
    {
        List<Point> path = new ArrayList<Point>();
        for (Point p = target; p != null; p = p.previousPoint)
            path.add(p);

        Collections.reverse(path);
        return path;
    }
}

