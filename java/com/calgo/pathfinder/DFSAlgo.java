package com.calgo.pathfinder;

import java.util.Stack;

/**
 * Created by hyunchan on 11/10/2015.
 */
public class DFSAlgo implements AlgoInterface {

    private Point curPoint;
    private Stack theStack;

    DFSAlgo(Point startPoint) {
        curPoint = startPoint;
        startPoint.setVisit(true);

        theStack = new Stack();
        theStack.push(startPoint);

    }

    @Override
    public Point getNextMove() {
        Point returnMove;

        Point nextMouse = getAdjUnvisitedPoint((Point)(theStack.peek()));
        if (nextMouse == null) {
            returnMove = (Point) theStack.pop();
        } else {
            returnMove = nextMouse;
            returnMove.setVisit(true);

            theStack.push(returnMove);
        }
        curPoint = returnMove;
        return returnMove;
    }

    @Override
    public boolean isFound() {
        if (curPoint == null)
            return false;
        if (!theStack.isEmpty() && !curPoint.mTarget)
            return false;
        else
            return true;

    }


    Point getAdjUnvisitedPoint(Point currentPt) {
        Point returnPt = null;

        if (currentPt == null)
            return null;

        if ( currentPt.left != null && currentPt.left.getVisit() == false )
            returnPt = currentPt.left;
        else if ( currentPt.right != null && currentPt.right.getVisit() == false )
            returnPt = currentPt.right;
        else if ( currentPt.down != null && currentPt.down.getVisit() == false )
            returnPt = currentPt.down;
        else if ( currentPt.up != null && currentPt.up.getVisit() == false )
            returnPt = currentPt.up;
        else
            returnPt = null;

        return returnPt;

    }
}

