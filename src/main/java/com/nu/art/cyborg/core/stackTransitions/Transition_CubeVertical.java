package com.nu.art.cyborg.core.stackTransitions;

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public class Transition_CubeVertical
	extends BaseTransition {

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		View view = layer.getRootView();
		View parent = (View) view.getParent();
		int parentHeight = parent.getHeight();

		progress = 1 - progress;
		progress = (!in ? -1 : 0) + progress;
		view.setTranslationY(-parentHeight * progress);
		view.setPivotY((!in ? 0 : 1) * view.getHeight());
		view.setPivotX(view.getWidth() * 0.5f);
		view.setRotationX(90f * progress);
	}
}
