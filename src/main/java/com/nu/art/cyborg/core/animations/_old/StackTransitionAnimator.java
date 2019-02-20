package com.nu.art.cyborg.core.animations._old;

import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;

import com.nu.art.cyborg.common.utils.Interpolators;
import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;
import com.nu.art.cyborg.ui.animations.interpulator.ReverseInterpolator;

public abstract class StackTransitionAnimator {

	protected Interpolator interpolator = Interpolators.LinearInterpolator;
	protected Interpolator reverseInterpolator = new ReverseInterpolator(Interpolators.LinearInterpolator);

	protected void setInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
		this.reverseInterpolator = new ReverseInterpolator(interpolator);
	}

	protected abstract void animateIn(StackLayerBuilder origin, StackLayerBuilder target, int duration, AnimationListener listener);

	protected abstract void animateOut(StackLayerBuilder origin, StackLayerBuilder target, int duration, AnimationListener listener);
}
