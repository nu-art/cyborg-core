package com.nu.art.cyborg.core.stackTransitions;

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public class Transition_ScaleVertical
	extends BaseTransition {

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		View view = layer.getRootView();
		View parent = (View) view.getParent();
		int parentHeight = parent.getHeight();
		int parentWidth = parent.getWidth();

		view.setPivotY((in ? 0 : 1) * parentHeight);
		view.setPivotX(parentWidth * 0.5f);
		view.setScaleY(in ? progress : 1 - progress);
	}
}
