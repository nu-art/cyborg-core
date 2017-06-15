package com.nu.art.cyborg.ui.tools;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class CustomScroller
		extends Scroller {

	private static final int DEFAULT_SCROLL_DURATION = 500;

	private int duration = DEFAULT_SCROLL_DURATION;

	public CustomScroller(Context context) {
		super(context);
	}

	public CustomScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	public CustomScroller(Context context, Interpolator interpolator, boolean flywheel) {
		super(context, interpolator, flywheel);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		// Ignore received duration, use fixed one instead
		super.startScroll(startX, startY, dx, dy, this.duration);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy) {
		// Ignore received duration, use fixed one instead
		super.startScroll(startX, startY, dx, dy, duration);
	}

	public void setFixedDuration(int duration) {
		this.duration = duration;
	}
}