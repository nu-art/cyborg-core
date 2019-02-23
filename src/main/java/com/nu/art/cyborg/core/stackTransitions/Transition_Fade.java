package com.nu.art.cyborg.core.stackTransitions;

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public class Transition_Fade
	extends BaseTransition {

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		View view = layer.getRootView();
		view.setAlpha(in ? progress : 1 - progress);
	}
}
