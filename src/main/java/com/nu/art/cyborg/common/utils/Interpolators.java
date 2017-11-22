package com.nu.art.cyborg.common.utils;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by tacb0ss on 23/11/2017.
 */

public interface Interpolators {

	LinearInterpolator LinearInterpolator = new LinearInterpolator();

	AccelerateDecelerateInterpolator AccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

	AccelerateInterpolator AccelerateInterpolator = new AccelerateInterpolator();

	DecelerateInterpolator DecelerateInterpolator = new DecelerateInterpolator();
}
