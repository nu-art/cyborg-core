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

	protected boolean cyclic;

	public abstract int getItemTypesCount();

	public abstract int getPositionForItem(Item item);

	public abstract int getItemTypeByPosition(int position);

	public abstract Item getItemForPosition(int position);

	public abstract int getItemsCount();

	public abstract int getRealItemsCount();

	public abstract void renderItem(Item item);

	public abstract void renderItemAtPosition(int position);

	protected CyborgAdapter adapter;

	public final void setCyclic() {
		cyclic = true;
	}

	public void setAdapter(CyborgAdapter<?> adapter) {
		this.adapter = adapter;
	}

	protected final void dispatchDataSetChanged() {
		adapter.onDataSetChanged();
	}

	protected final void dispatchItemRangeInserted(int from, int to) {
		adapter.onItemRangeInserted(from, to);
	}

	protected final void dispatchItemAtPositionChanged(int position) {
		adapter.onItemAtPositionChanged(position);
	}
}