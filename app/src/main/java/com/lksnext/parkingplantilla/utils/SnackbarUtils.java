package com.lksnext.parkingplantilla.utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

    public static void showVisibleSnackbar(Activity activity, View rootView, String message, int duration) {
        Snackbar snackbar = Snackbar.make(rootView, message, duration);

        View snackbarView = snackbar.getView();
        ViewGroup.LayoutParams params = snackbarView.getLayoutParams();

        if (params instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams coordinatorParams = (CoordinatorLayout.LayoutParams) params;
            coordinatorParams.gravity = Gravity.TOP;
            coordinatorParams.topMargin = 150;
        } else if (params instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
            frameParams.gravity = Gravity.TOP;
            frameParams.topMargin = 150;
        }

        snackbar.show();
    }
}