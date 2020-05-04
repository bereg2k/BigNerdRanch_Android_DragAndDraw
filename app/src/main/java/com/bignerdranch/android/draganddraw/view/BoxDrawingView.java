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

public class BoxDrawingView extends View {
    private static final String TAG = BoxDrawingView.class.getSimpleName();

    private static final String ARG_CURRENT_OR = "ARG_CURRENT_OR";
    private static final String ARG_CURRENT_CUR = "ARG_CURRENT_CUR";
    private static final String ARG_PARENT_VIEW = "ARG_PARENT_VIEW";
    private static final String ARG_CURRENT_ROTATION = "ARG_INITIAL_ROTATION";

    public Integer initialRotation;
    public Integer currentRotation;
    private int mMaxDisplayHeight;
    private int mMaxDisplayWidth;

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
        mBoxPaint.setColor(resources.getColor(R.color.colorPrimary));

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(resources.getColor(R.color.colorCanvasBack));

        initialRotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();

        calculateDisplayMetrics();
    }

    private void calculateDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mMaxDisplayWidth = Math.min(metrics.heightPixels, metrics.widthPixels);
        mMaxDisplayHeight = Math.max(metrics.heightPixels, metrics.widthPixels);
    }

    /**
     * Clears up the current canvas
     */
    public void clearCanvas() {
        mCurrent = null;
        mBoxes.clear();
        invalidate();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_PARENT_VIEW, super.onSaveInstanceState());

        for (int i = 0; i < mBoxes.size(); i++) {
            bundle.putParcelable(ARG_CURRENT_OR + "_" + i, calculateZeroBaseCoordinates(mBoxes.get(i).getOrigin()));
            bundle.putParcelable(ARG_CURRENT_CUR + "_" + i, calculateZeroBaseCoordinates(mBoxes.get(i).getCurrent()));
        }

        bundle.putInt(ARG_CURRENT_ROTATION, currentRotation);
        return bundle;
    }

    private PointF calculateZeroBaseCoordinates(PointF pointF) {
        float x = pointF.x;
        float y = pointF.y;
        float x0 = 0;
        float y0 = 0;

        switch (currentRotation) {
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
                x0 = x;
                y0 = y;
        }

        return new PointF(x0, y0);
    }

    private PointF calculateNewCoordinates(PointF pointF) {
        float x0 = pointF.x;
        float y0 = pointF.y;
        float x = 0;
        float y = 0;

        switch (currentRotation) {
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
                x = x0;
                y = y0;
        }

        return new PointF(x, y);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = ((Bundle) state);
        Parcelable parentState = bundle.getParcelable(ARG_PARENT_VIEW);
        super.onRestoreInstanceState(parentState);

        initialRotation = bundle.getInt(ARG_CURRENT_ROTATION);

        int i = 0;
        while (bundle.getParcelable(ARG_CURRENT_OR + "_" + i) != null) {
            PointF origin = bundle.getParcelable(ARG_CURRENT_OR + "_" + i);
            PointF current = bundle.getParcelable(ARG_CURRENT_CUR + "_" + i);

            Box box = new Box(calculateNewCoordinates(origin));
            box.setCurrent(calculateNewCoordinates(current));
            mBoxes.add(box);
            i++;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";

                // Reset drawing state
                mCurrent = new Box(current);
                mBoxes.add(mCurrent);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";

                // refreshing the Box.mCurrent on move action
                if (mCurrent != null) {
                    mCurrent.setCurrent(current);
                    invalidate();
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

        Log.i(TAG, action + " on X = " + current.x +
                "and Y = " + current.y);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill out the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxes) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }
}
