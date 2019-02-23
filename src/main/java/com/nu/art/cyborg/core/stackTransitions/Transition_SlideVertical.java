package com.nu.art.cyborg.core.stackTransitions;

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public class Transition_SlideVertical
	extends BaseTransition {

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		View view = layer.getRootView();
		View parent = (View) view.getParent();
		int parentHeight = parent.getHeight();

		view.setTranslationY(parentHeight * (in ? -1 + progress : progress));
	}
}
