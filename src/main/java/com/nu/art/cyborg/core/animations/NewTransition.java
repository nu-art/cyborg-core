package com.nu.art.cyborg.core.animations;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

public interface NewTransition {

	void animate(StackLayerBuilder layer, float progress, boolean in);
}
