package com.bignerdranch.android.draganddraw.model;

import android.graphics.PointF;

/**
 * Model class for keeping information about drawn boxes.
 */
public class Box {
    private PointF mOriginPoint;
    private PointF mCurrentPoint;
    private float mRotation;

    public Box(PointF originPoint) {
        mOriginPoint = originPoint;
        mCurrentPoint = originPoint;
    }

    public PointF getOriginPoint() {
        return mOriginPoint;
    }

    public PointF getCurrentPoint() {
        return mCurrentPoint;
    }

    public float getRotation() {
        return mRotation;
    }

    public void setOriginPoint(PointF originPoint) {
        mOriginPoint = originPoint;
    }

    public void setCurrentPoint(PointF currentPoint) {
        mCurrentPoint = currentPoint;
    }

    public void setRotation(float rotation) {
        mRotation = rotation;
    }
}
