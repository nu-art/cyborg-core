package com.nu.art.cyborg.core.stackTransitions;

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public class Transition_ScaleHorizontal
	extends BaseTransition {

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		View view = layer.getRootView();
		View parent = (View) view.getParent();
		int parentHeight = parent.getHeight();
		int parentWidth = parent.getWidth();

		view.setPivotX((!in ? 1 : 0) * parentWidth);
		view.setPivotY(parentHeight * 0.5f);

		view.setScaleX(in ? progress : 1 - progress);
	}
}
