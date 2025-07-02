package com.lksnext.parkingplantilla.utils;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    private SnackbarUtils() {
        // Prevent instantiation
    }

    public static void showVisibleSnackbar(View rootView, String message, int duration) {
        Snackbar snackbar = Snackbar.make(rootView, message, duration);

        View snackbarView = snackbar.getView();
        ViewGroup.LayoutParams params = snackbarView.getLayoutParams();

        if (params instanceof CoordinatorLayout.LayoutParams coordinatorParams) {
            coordinatorParams.gravity = Gravity.TOP;
            coordinatorParams.topMargin = 150;
        } else if (params instanceof FrameLayout.LayoutParams frameParams) {
            frameParams.gravity = Gravity.TOP;
            frameParams.topMargin = 150;
        }

        snackbar.show();
    }
}