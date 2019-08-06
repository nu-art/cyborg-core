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

package com.nu.art.cyborg.core.dataModels;

import com.nu.art.cyborg.core.CyborgAdapter;

/**
 * Created by TacB0sS on 22-Jun 2015.
 */
public abstract class DataModel<Item> {

	private final Class<? extends Item>[] itemTypes;
	private CyborgAdapter adapter;
	private boolean cyclic;
	protected boolean autoNotifyChanges = true;
	/**
	 * Determines the max number of items for 'cyclic' usage.
	 * The cyclic datamodel will cycle items until it reaches this max limit.
	 */
	private int defaultMaxItems = Integer.MAX_VALUE;

	public DataModel(Class<? extends Item>[] itemTypes) {
		this.itemTypes = itemTypes;
	}

	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}

	public final void setAutoNotifyChanges(boolean autoNotifyChanges) {
		this.autoNotifyChanges = autoNotifyChanges;
	}

	protected void setDefaultMaxItems(int defaultMaxItems) {
		this.defaultMaxItems = defaultMaxItems;
	}

	public final void setAdapter(CyborgAdapter<?> adapter) {
		this.adapter = adapter;
	}

	protected int getDefaultMaxItems() {
		return defaultMaxItems;
	}

	public final int getItemTypesCount() {
		return itemTypes.length;
	}

	public final Class<? extends Item>[] getItemTypes() {
		return itemTypes;
	}

	public final int getItemTypeByPosition(int position) {
		Item item = getItemForPosition(position);
		if (item == null)
			return 0;

		return getItemTypeByItem(item);
	}

	private int getItemTypeByItem(Item item) {
		for (int i = 0; i < itemTypes.length; i++) {
			if (item.getClass() == itemTypes[i])
				return i;
		}

		for (int i = 0; i < itemTypes.length; i++) {
			if (itemTypes[i].isAssignableFrom(item.getClass()))
				return i;
		}

		return 0;
	}

	public abstract int getRealItemsCount();

	public abstract int getPositionForItem(Item item);

	public abstract Item getItemForPosition(int position);

	public final void renderItem(Item item) {
		int position = getPositionForItem(item);
		if (position == -1)
			return;

		notifyItemAtPositionChanged(position);
	}

	public final int getItemsCount() {
		int realItemsCount = getRealItemsCount();
		return cyclic && realItemsCount > 0 ? defaultMaxItems : realItemsCount;
	}

	// ----------- NOTIFIERS --------------

	public void notifyItemRemoved(int position) {
		if (adapter == null)
			return;

		adapter.onItemRemoved(position);
	}

	public void notifyItemRangeRemoved(int from, int to) {
		if (adapter == null)
			return;

		adapter.onItemRangeRemoved(from, to);
	}

	public void notifyItemInserted(int position) {
		if (adapter == null)
			return;

		adapter.onItemRangeInserted(position, position);
	}

	public void notifyItemRangeInserted(int from, int to) {
		if (adapter == null)
			return;

		adapter.onItemRangeInserted(from, to);
	}

	public void notifyItemMoved(int from, int to) {
		if (adapter == null)
			return;

		adapter.onItemMoved(from, to);
	}

	public void notifyDataSetChanged() {
		if (adapter == null)
			return;

		adapter.onDataSetChanged();
	}

	public void notifyItemAtPositionChanged(int position) {
		if (adapter == null)
			return;

		adapter.onItemAtPositionChanged(position);
	}
}