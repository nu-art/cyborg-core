/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.cyborg.core;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nu.art.core.utils.DebugFlags;
import com.nu.art.cyborg.core.CyborgAdapter.PositionResolver;

import static com.nu.art.cyborg.core.abs._DebugFlags.Debug_Performance;

/**
 * This is a very important class, it is used to render items in {@link CyborgAdapter}...see example project!
 *
 * @param <ItemType> The item type this renderer can render.
 */
public abstract class ItemRenderer<ItemType>
	extends CyborgController {

	private ItemType item;

	private PositionResolver positionResolver;

	protected ItemRenderer(@LayoutRes int layoutId) {
		super(layoutId);
	}

	final void _setItem(ItemType item) {
		this.item = item;
	}

	protected final ItemType getItem() {
		return item;
	}

	@Override
	public final void render() {
		long startMs = System.currentTimeMillis();
		renderItem(item);
		if (DebugFlags.isDebuggableFlag(Debug_Performance))
			logVerbose("Render duration: " + (System.currentTimeMillis() - startMs) + "ms");
	}

	protected abstract void renderItem(ItemType itemType);

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		if (layoutId == -1)
			return createCustomView(inflater, parent, attachToParent);
		return inflater.inflate(layoutId, parent, false);
	}

	public int getItemPosition() {
		return positionResolver.getItemPosition();
	}

	void setPositionResolver(PositionResolver positionResolver) {
		this.positionResolver = positionResolver;
	}
}
