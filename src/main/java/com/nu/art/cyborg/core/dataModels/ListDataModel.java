/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by TacB0sS on 22-Jun 2015.
 */
@SuppressWarnings("unchecked")
public class ListDataModel<Item>
		extends DataModel<Item> {

	private final Class<? extends Item>[] itemsType;

	private ArrayList<Item> items = new ArrayList<Item>();

	public ListDataModel(Class<? extends Item>... itemsType) {
		this.itemsType = itemsType;
	}

	public final int indexOf(Item item) {
		return items.indexOf(item);
	}

	public final void addItems(Item... items) {
		addItems(Arrays.asList(items));
	}

	public final void addItems(List<Item> items) {
		this.items.addAll(items);
		dispatchDataSetChanged();
	}

	public final void removeItems(Item... items) {
		removeItems(Arrays.asList(items));
	}

	public final void removeItems(List<Item> items) {
		this.items.removeAll(items);
		dispatchDataSetChanged();
	}

	@Override
	public int getItemTypesCount() {
		return itemsType.length;
	}

	@Override
	public int getPositionForItem(Item item) {
		return items.indexOf(item);
	}

	@Override
	public int getItemTypeByPosition(int position) {
		Item item = getItemForPosition(position);
		if (item == null)
			return 0;

		return getItemTypeByItem(item);
	}

	protected int getItemTypeByItem(Item item) {
		for (int i = 0; i < itemsType.length; i++) {
			if (item.getClass() == itemsType[i])
				return i;
		}
		return 0;
	}

	@Override
	public Item getItemForPosition(int position) {
		return items.get(position);
	}

	@Override
	public int getItemsCount() {
		return items.size();
	}

	@Override
	public void renderItem(Item item) {
		int position = getPositionByItem(item);
		if (position == -1)
			return;
		renderItemAtPosition(position);
	}

	private int getPositionByItem(Item item) {
		return items.indexOf(item);
	}

	@Override
	public void renderItemAtPosition(int position) {
		dispatchItemAtPositionChanged(position);
	}

	public final void notifyDataSetChanged() {
		dispatchDataSetChanged();
	}

	public final void setItems(Item... items) {
		this.items.clear();
		addItems(items);
	}

	public final void clear() {
		items.clear();
		notifyDataSetChanged();
	}
}
