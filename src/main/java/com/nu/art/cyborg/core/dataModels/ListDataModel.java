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

import com.nu.art.core.tools.ArrayTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by TacB0sS on 22-Jun 2015.
 */
@SuppressWarnings("unchecked")
public class ListDataModel<Item>
	extends DataModel<Item> {

	private ArrayList<Item> items = new ArrayList<>();

	public ListDataModel(Class<? extends Item> firstItem, Class<? extends Item>... itemTypes) {
		super(ArrayTools.insertElement(itemTypes, firstItem, 0));
	}

	public ListDataModel(Class<? extends Item>[] itemTypes) {
		super(itemTypes);
	}

	public final void add(Item... items) {
		if (items == null)
			return;

		addAll(Arrays.asList(items));
	}

	public final void addAll(Collection<Item> items) {
		int size = this.items.size();
		this.items.addAll(items);
		if (autoNotifyChanges)
			notifyItemRangeInserted(size, items.size());
	}

	public final void setItems(Item... items) {
		this.items.clear();
		if (items == null)
			return;

		this.items.addAll(Arrays.asList(items));
		if (autoNotifyChanges)
			notifyDataSetChanged();
	}

	public final void clear() {
		items.clear();
		if (autoNotifyChanges)
			notifyDataSetChanged();
		//		if (adapter != null)
		//			adapter.onItemRangeInserted(size, items.size());

	}

	public void swapItemsByPosition(int fromPosition, int toPosition) {
		Collections.swap(items, fromPosition, toPosition);
		notifyItemMoved(fromPosition, toPosition);
	}

	public final void removeItems(int index) {
		this.items.remove(index);
		if (autoNotifyChanges)
			notifyItemRemoved(index);
	}

	public final void removeItems(Item... items) {
		if (items == null)
			return;

		removeItems(Arrays.asList(items));
	}

	public final void removeItems(List<Item> items) {
		int position = -1;
		if (items.size() == 1)
			position = getPositionByItem(items.get(0));

		boolean removed = this.items.removeAll(items);
		if (position >= 0) {
			if (autoNotifyChanges)
				notifyItemRemoved(position);
			return;
		}

		if (!removed)
			return;

		// TODO: can add a calculation of minimal range..
		if (autoNotifyChanges)
			notifyDataSetChanged();
	}

	@Override
	public int getPositionForItem(Item item) {
		return items.indexOf(item);
	}

	@Override
	public Item getItemForPosition(int position) {
		return items.get(position % items.size());
	}

	@Override
	public int getRealItemsCount() {
		return items.size();
	}

	private int getPositionByItem(Item item) {
		return items.indexOf(item);
	}
}
