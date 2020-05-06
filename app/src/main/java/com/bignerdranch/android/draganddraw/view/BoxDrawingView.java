package com.bignerdranch.android.draganddraw.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import androidx.annotation.Nullable;

import com.bignerdranch.android.draganddraw.R;
import com.bignerdranch.android.draganddraw.model.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * Main view for drawing boxes.
 */
public class BoxDrawingView extends View {
    private static final String TAG = BoxDrawingView.class.getSimpleName();

    private static final String ARG_CURRENT_OR = "ARG_CURRENT_OR";
    private static final String ARG_CURRENT_CUR = "ARG_CURRENT_CUR";
    private static final String ARG_PARENT_VIEW = "ARG_PARENT_VIEW";
    private static final String ARG_BOX_ROTATION = "ARG_BOX_ROTATION";

    private int mMaxDisplayHeight;
    private int mMaxDisplayWidth;
    private Integer mCurrentScreenRotation;
    private float mBoxDegreeRotation;

    private Box mCurrent;
    private List<Box> mBoxes = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    /**
     * Constructor for creating View in the code.
     *
     * @param context current context
     */
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    /**
     * Context for creating view from XML layout
     *
     * @param context current context
     * @param attrs   additional view's attributes from XML file
     */
    public BoxDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources resources = getResources();

        mBoxPaint = new Paint();
        mBoxPaint.setColor(resources.getColor(R.color.colorPrimary) & 0x60FF0000);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(resources.getColor(R.color.colorCanvasBack));

        mCurrentScreenRotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();

        calculateDisplayMetrics();
    }

    /**
     * Clears up the current canvas
     */
    public void clearCanvas() {
        mCurrent = null;
        mBoxes.clear();
        mBoxDegreeRotation = 0;
        invalidate();
    }

    /**
     * Cancel the last drawn rectangle.
     */
    public void undoLastDraw() {
        if (mBoxes.isEmpty()) {
            return;
        }
        mBoxes.remove(mBoxes.size() - 1);
        invalidate();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_PARENT_VIEW, super.onSaveInstanceState());

        for (int i = 0; i < mBoxes.size(); i++) {
            bundle.putParcelable(ARG_CURRENT_OR + "_" + i, calculateZeroBaseCoordinates(mBoxes.get(i).getOriginPoint()));
            bundle.putParcelable(ARG_CURRENT_CUR + "_" + i, calculateZeroBaseCoordinates(mBoxes.get(i).getCurrentPoint()));
            bundle.putFloat(ARG_BOX_ROTATION + "_" + i, mBoxes.get(i).getRotation());
        }

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = ((Bundle) state);
        Parcelable parentState = bundle.getParcelable(ARG_PARENT_VIEW);
        super.onRestoreInstanceState(parentState);

        mCurrentScreenRotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();

        int i = 0;
        while (bundle.getParcelable(ARG_CURRENT_OR + "_" + i) != null) {
            PointF originPoint = bundle.getParcelable(ARG_CURRENT_OR + "_" + i);
            PointF currentPoint = bundle.getParcelable(ARG_CURRENT_CUR + "_" + i);
            float boxRotation = bundle.getFloat(ARG_BOX_ROTATION + "_" + i);

            Box box = new Box(calculateNewCoordinates(originPoint));
            box.setCurrentPoint(calculateNewCoordinates(currentPoint));
            box.setRotation(boxRotation);
            mBoxes.add(box);
            i++;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        int actionPointerId = event.getPointerId(actionIndex);
        float pointerX = event.getX(actionIndex);
        float pointerY = event.getY(actionIndex);
        PointF currentIndexed = new PointF(pointerX, pointerY);

        String action = "";

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";

                // Reset drawing state, start drawing new box
                mCurrent = new Box(currentIndexed);
                mBoxes.add(mCurrent);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN";

                // only for logging purposes
                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "ACTION_POINTER_UP";

                // only for logging purposes
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";

                int pointers = event.getPointerCount();
                for (int i = 0; i < pointers; i++) {
                    int pointerId = event.getPointerId(i);
                    if (pointerId == 0 && mCurrent != null) {
                        // refreshing the Box.mCurrent on move action
                        // this is done with first finger and it varies the box's "current" point
                        mCurrent.setCurrentPoint(currentIndexed);
                        invalidate();
                    }
                    if (pointerId == 1 && pointers > 1 && mCurrent != null) {
                        // when user lifts up the first finger, keeping the second,
                        // this method must not set rotation (to zero) for the last box.
                        // only when two fingers are down, rotation is being calculated and set!

                        double deltaX = event.getX(i) - event.getX(0);
                        double deltaY = event.getY(i) - event.getY(0);
                        mBoxDegreeRotation = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
                        mCurrent.setRotation(mBoxDegreeRotation);

                        Log.i(TAG, action + ": actionIndex = " + actionIndex +
                                ", actionPointerId = " + actionPointerId +
                                ", at x = " + pointerX + " and y = " + pointerY);
                        Log.i(TAG, "mDegree =  " + mBoxDegreeRotation);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";

                mCurrent = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";

                mCurrent = null;
                break;
            default:
                mCurrent = null;
        }

        Log.i(TAG, action + ": actionIndex = " + actionIndex +
                ", actionPointerId = " + actionPointerId +
                ", at x = " + pointerX + " and y = " + pointerY);
        if (mBoxDegreeRotation != 0) {
            Log.i(TAG, "!!!!! mDegree =  " + mBoxDegreeRotation);
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill out the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxes) {
            canvas.save();
            float left = Math.min(box.getOriginPoint().x, box.getCurrentPoint().x);
            float right = Math.max(box.getOriginPoint().x, box.getCurrentPoint().x);
            float top = Math.min(box.getOriginPoint().y, box.getCurrentPoint().y);
            float bottom = Math.max(box.getOriginPoint().y, box.getCurrentPoint().y);

            canvas.rotate(box.getRotation(), box.getCurrentPoint().x, box.getCurrentPoint().y);
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.restore();
        }
    }

    /**
     * Calculate current display metrics for a later usage in coordinates calculations for
     * various screen's rotations.
     */
    private void calculateDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mMaxDisplayWidth = Math.min(metrics.heightPixels, metrics.widthPixels);
        mMaxDisplayHeight = Math.max(metrics.heightPixels, metrics.widthPixels);
    }

    /**
     * Calculate so-called "zero-based" coordinates: coordinates for a point, as if
     * the device is held vertically with 0 degrees rotation angle (default rotation).
     * This information is needed to correctly calculate coordinates for other screen's rotations.
     *
     * @param pointF point with coordinates in current rotation
     * @return new point with coordinates on the vertical screen with 0 degrees rotation
     */
    private PointF calculateZeroBaseCoordinates(PointF pointF) {
        float x = pointF.x;
        float y = pointF.y;
        float x0 = 0;
        float y0 = 0;

        switch (mCurrentScreenRotation) {
            case Surface.ROTATION_0:
                x0 = x;
                y0 = y;
                break;
            case Surface.ROTATION_90:
                x0 = mMaxDisplayWidth - y;
                y0 = x;
                break;
            case Surface.ROTATION_180:
                x0 = mMaxDisplayWidth - x;
                y0 = mMaxDisplayHeight - y;
                break;
            case Surface.ROTATION_270:
                x0 = y;
                y0 = mMaxDisplayHeight - x;
                break;
            default:
        }

        return new PointF(x0, y0);
    }

    /**
     * Calculate new coordinates using "zero-based" coordinates.
     * See {@link BoxDrawingView#calculateZeroBaseCoordinates(PointF)}.
     *
     * @param pointF point with coordinates in current rotation
     * @return new point with coordinates on the screen with current rotation
     */
    private PointF calculateNewCoordinates(PointF pointF) {
        float x0 = pointF.x;
        float y0 = pointF.y;
        float x = 0;
        float y = 0;

        switch (mCurrentScreenRotation) {
            case Surface.ROTATION_0:
                x = x0;
                y = y0;
                break;
            case Surface.ROTATION_90:
                x = y0;
                y = mMaxDisplayWidth - x0;
                break;
            case Surface.ROTATION_180:
                x = mMaxDisplayWidth - x0;
                y = mMaxDisplayHeight - y0;
                break;
            case Surface.ROTATION_270:
                x = mMaxDisplayHeight - y0;
                y = x0;
                break;
            default:
        }

        return new PointF(x, y);
    }
}
