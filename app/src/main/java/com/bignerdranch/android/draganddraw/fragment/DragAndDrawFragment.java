package com.bignerdranch.android.draganddraw.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.draganddraw.R;
import com.bignerdranch.android.draganddraw.view.BoxDrawingView;

/**
 * Main fragment for hosting a canvas for drawing boxes.
 */
public class DragAndDrawFragment extends Fragment {
    private static final String TAG = DragAndDrawFragment.class.getSimpleName();
    private static final boolean IS_FRAGMENT_STATE_RETAIN = true;

    private BoxDrawingView mBoxDrawingView;

    public static Fragment newInstance() {
        return new DragAndDrawFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(IS_FRAGMENT_STATE_RETAIN);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);

        mBoxDrawingView = view.findViewById(R.id.box_drawing_view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mBoxDrawingView.clearCanvas();
                Log.i(TAG, "Canvas is cleared");
                return true;
            case R.id.menu_undo:
                mBoxDrawingView.undoLastDraw();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
