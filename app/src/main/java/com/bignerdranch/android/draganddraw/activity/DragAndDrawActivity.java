package com.bignerdranch.android.draganddraw.activity;

import androidx.fragment.app.Fragment;

import com.bignerdranch.android.draganddraw.fragment.DragAndDrawFragment;

public class DragAndDrawActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return DragAndDrawFragment.newInstance();
    }
}
