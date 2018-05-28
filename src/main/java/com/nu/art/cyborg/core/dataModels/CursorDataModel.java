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

import android.database.Cursor;
import android.os.Handler;

import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.cyborg.core.consts.LifecycleState;
import com.nu.art.cyborg.core.interfaces.LifecycleListener;

import static com.nu.art.cyborg.core.consts.LifecycleState.OnDestroy;
import static com.nu.art.cyborg.core.modules.ThreadsModule.assertMainThread;

@SuppressWarnings("WeakerAccess")
public abstract class CursorDataModel<ItemType>
	extends DataModel<ItemType>
	implements LifecycleListener {

	private Runnable getCursorAction = new Runnable() {

		@Override
		public void run() {
			final Cursor _cursor = getCursor();

			// This is to prevent the counting of items in the cursor on the ui thread
			if (_cursor != null)
				_cursor.moveToFirst();

			controller.postOnUI(new Runnable() {

				@Override
				public void run() {
					if (cursor != null && !cursor.isClosed())
						cursor.close();

					if (closed)
						return;

					cursor = _cursor;

					postRefresh.run();
				}
			});
		}
	};

	private final CyborgController controller;
	private final Handler backgroundHandler;

	private Runnable postRefresh = null;
	private Cursor cursor;
	private boolean closed;

	@SafeVarargs
	protected CursorDataModel(CyborgController controller, Handler backgroundHandler, Class<ItemType>... itemsTypes) {
		super(itemsTypes);
		this.controller = controller;
		this.backgroundHandler = backgroundHandler;
		controller.addLifecycleListener(this);
	}

	protected abstract ItemType convert(Cursor cursor);

	protected abstract Cursor getCursor();

	public final void refresh() {
		notifyDataSetChanged();
	}

	private void refresh(Runnable postRefresh) {
		this.postRefresh = postRefresh;
		backgroundHandler.post(getCursorAction);
	}

	@Override
	public int getRealItemsCount() {
		if (cursor == null)
			return 0;

		return cursor.getCount();
	}

	@Override
	public int getPositionForItem(ItemType itemType) {
		return -1;
	}

	@Override
	public ItemType getItemForPosition(int position) {
		cursor.moveToPosition(position);
		return convert(cursor);
	}

	public void close() {
		assertMainThread();

		closed = true;
		if (cursor == null)
			return;

		if (cursor.isClosed())
			return;

		cursor.close();

		cursor = null;
	}

	@Override
	public void onLifecycleChanged(LifecycleState state) {
		if (state != OnDestroy)
			return;

		close();
	}

	public final void notifyItemRemoved(final int index) {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyItemRemoved(index);
			}
		});
	}

	public void notifyItemRangeRemoved(final int from, final int to) {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyItemRangeRemoved(from, to);
			}
		});
	}

	public void notifyItemInserted(final int position) {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyItemInserted(position);
			}
		});
	}

	public void notifyItemRangeInserted(final int from, final int to) {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyItemRangeInserted(from, to);
			}
		});
	}

	public void notifyItemMoved(final int from, final int to) {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyItemMoved(from, to);
			}
		});
	}

	public void notifyDataSetChanged() {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyDataSetChanged();
			}
		});
	}

	public void notifyItemAtPositionChanged(final int position) {
		refresh(new Runnable() {
			@Override
			public void run() {
				if (autoNotifyChanges)
					CursorDataModel.super.notifyItemAtPositionChanged(position);
			}
		});
	}
}
