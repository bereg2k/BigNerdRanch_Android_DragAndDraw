package com.bignerdranch.android.draganddraw.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bignerdranch.android.draganddraw.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    private static final String TAG = SingleFragmentActivity.class.getSimpleName();

    @LayoutRes
    public static final int ACTIVITY_LAYOUT_RES = R.layout.activity_fragment;
    @IdRes
    public static final int FRAGMENT_CONTAINER_RES = R.id.fragment_container;

    /**
     * Abstract method for mandatory creating of a main fragment that this activity will host.
     *
     * @return Fragment object from the hosting Activity
     */
    public abstract Fragment createFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ACTIVITY_LAYOUT_RES);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(FRAGMENT_CONTAINER_RES);

        if (fragment == null) {
            fragment = createFragment();

            fm.beginTransaction()
                    .add(FRAGMENT_CONTAINER_RES, fragment)
                    .commit();

            Log.i(TAG, "onCreate: fragment has been posted - " + fragment.toString());
        }
    }
}
