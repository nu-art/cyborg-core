package com.nu.art.cyborg.core.stackTransitions;

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public class Transition_CubeHorizontal
	extends BaseTransition {

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		View view = layer.getRootView();
		View parent = (View) view.getParent();
		int parentWidth = parent.getWidth();

		progress = (in ? -1 : 0) + progress;
		view.setTranslationX(parentWidth * progress);
		view.setPivotX((!in ? 0 : 1) * view.getWidth());
		view.setPivotY(view.getHeight() * 0.5f);
		view.setRotationY(90f * progress);	}
}
