package com.example.anurag.customercare.utils;

import android.view.View;

/**
 * Created by anurag on 20/05/17.
 */

public class Utils {

    public static void setViewVisibility(int visibility, View... views) {
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }

}
