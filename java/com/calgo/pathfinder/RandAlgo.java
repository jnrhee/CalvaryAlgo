package com.calgo.pathfinder;

/**
 * Created by hyunchan on 11/10/2015.
 */
public class RandAlgo implements AlgoInterface {
    private Point curPoint;
    private int mouseDir;

    RandAlgo(Point startPoint) {
        curPoint = startPoint;
        mouseDir = -1;
    }


    @Override
    public Point getNextMove() {
        Point nextMove = null;

        // if can't go in the same direciton, move to the next random dir
        boolean found = false;

        int dir;
        if (curPoint.isEnd() == 1 && mouseDir != -1) {
            dir = PointGroup.getOppositeDir(mouseDir);
            nextMove = tryMoveMouse(dir);

        } else {
            do {
                dir = PointGroup.randomDir();
                if (!PointGroup.isOppositeDir(mouseDir, dir)) {
                    nextMove = tryMoveMouse(dir);
                }
            } while (nextMove == null);
        }
        mouseDir = dir;
        curPoint = nextMove;
        return nextMove;
    }

    @Override
    public Point getNextMove(Point start, Point p1) {
        Point nextMove = null;

        // if can't go in the same direciton, move to the next random dir
        boolean found = false;

        int dir;
        if (curPoint.isEnd() == 1 && mouseDir != -1) {
            dir = PointGroup.getOppositeDir(mouseDir);
            nextMove = tryMoveMouse(dir);

        } else {
            do {
                dir = PointGroup.randomDir();
                if (!PointGroup.isOppositeDir(mouseDir, dir)) {
                    nextMove = tryMoveMouse(dir);
                }
            } while (nextMove == null);
        }
        mouseDir = dir;
        curPoint = nextMove;
        return nextMove;
    }
    @Override
    public boolean isFound() {
        if (curPoint == null)
            return false;
        if (!curPoint.mTarget)
            return false;
        else
            return true;

    }

    private Point tryMoveMouse(int dir) {

        Point nextPoint = null;

        switch (dir) {
            case Point.UP:
                if (curPoint.up != null) {
                    nextPoint = curPoint.up;
                }
                break;
            case Point.DOWN:
                if (curPoint.down != null) {
                    nextPoint = curPoint.down;
                }
                break;
            case Point.LEFT:
                if (curPoint.left != null) {
                    nextPoint = curPoint.left;
                }
                break;
            case Point.RIGHT:
                if (curPoint.right != null) {
                    nextPoint = curPoint.right;
                }
                break;
        }
        return nextPoint;
    }


}
