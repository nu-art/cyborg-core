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

import com.nu.art.core.tools.ArrayTools;

/**
 * Created by TacB0sS on 22-Jun 2015.
 */
public abstract class DataModel<Item> {

	public abstract int getItemTypesCount();

	public abstract int getPositionForItem(Item item);

	public abstract int getItemTypeByPosition(int position);

	public abstract Item getItemForPosition(int position);

	public abstract int getItemsCount();

	public abstract void renderItem(Item item);

	public abstract void renderItemAtPosition(int position);

	private DataModelListener[] listeners = {};

	public final void addDataModelListener(DataModelListener listener) {
		listeners = ArrayTools.appendElement(listeners, listener);
	}

	public final void removeDataModelListener(DataModelListener listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	protected final void dispatchDataSetChanged() {
		for (DataModelListener listener : listeners) {
			listener.onDataSetChanged();
		}
	}

	protected final void dispatchItemAtPositionChanged(int position) {
		for (DataModelListener listener : listeners) {
			listener.onItemAtPositionChanged(position);
		}
	}

	public interface DataModelListener {

		void onDataSetChanged();

		void onItemAtPositionChanged(int position);
	}
}