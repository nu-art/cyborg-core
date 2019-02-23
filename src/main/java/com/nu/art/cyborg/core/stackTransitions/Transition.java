package com.nu.art.cyborg.core.stackTransitions;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public interface Transition {

	void animate(StackLayerBuilder layer, float progress, boolean in);

	Transition reverse();
}
