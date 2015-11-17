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

    DijkAlgo(Point startPoint) {
        Point target;
        nextMove = startPoint;
        long startTime = System.currentTimeMillis();
        Log.d("Ernest","===================Start=======================================");
        target = computePaths(startPoint);
        Log.d("Ernest","Start:"+startPoint+"target:" +target);

        shortestPathList =  getShortestPathTo(target);
        for (Point p: shortestPathList) {
        //    Log.d("Ernest", String.format("x = %d y = %d", p.x, p.y));
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
    public boolean isFound() {

        if (shortestPathList.isEmpty())
            return true;
        else
            return false;
    }

    Point computePaths(Point source) {
        Point target = null;
        source.minDistance = 0;
        //visit each vertex u, always visiting vertex with smallest minDistance first
        // Visit each edge exiting u
        PriorityQueue<Point> pointQueue = new PriorityQueue<Point>();
        pointQueue.add(source);

        while (!pointQueue.isEmpty()) {
            Point currentPoint = pointQueue.poll();

            if (currentPoint.mTarget == true)
                target = currentPoint;

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

        return target;

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

