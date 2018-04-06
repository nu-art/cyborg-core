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

import android.database.Cursor;

import com.nu.art.core.generics.Function;

/**
 * Created by TacB0sS on 22-Jun 2015.
 */
public class CursorDataModel<Item>
	extends DataModel<Item> {

	private final Function<Cursor, Item> cursorToItem;

	private Cursor cursor;

	public CursorDataModel(Function<Cursor, Item> cursorToItem) {this.cursorToItem = cursorToItem;}

	@Override
	public int getItemTypeByPosition(int position) {
		return 0;
	}

	@Override
	public Item getItemForPosition(int position) {
		return null;
	}

	@Override
	public int getRealItemsCount() {
		return getItemsCount();
	}

	@Override
	public int getItemsCount() {
		return cursor == null ? 0 : cursor.getCount();
	}

	@Override
	public int getItemTypesCount() {
		return 1;
	}

	@Override
	public int getPositionForItem(Item item) {
		throw new UnsupportedOperationException("cannot get a position for an item on a cursor data model!!");
	}

	@Override
	public void renderItem(Item item) {
		// TODO: Need to think about this!
	}

	@Override
	public void renderItemAtPosition(int position) {
		dispatchItemAtPositionChanged(position);
	}
}
