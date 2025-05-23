package com.lksnext.parkingplantilla.adapters;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {

    @BindingAdapter("imageResource")
    public static void setImageResource(ImageView imageView, String resourceName) {
        if (resourceName == null) return;

        // Get resource ID dynamically by name
        int resourceId = imageView.getContext().getResources()
                .getIdentifier(resourceName, "drawable",
                        imageView.getContext().getPackageName());

        // Set the resource if found, otherwise use a default
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        } else {
            imageView.setImageResource(
                    imageView.getContext().getResources()
                            .getIdentifier("ic_car", "drawable",
                                    imageView.getContext().getPackageName()));
        }
    }
}