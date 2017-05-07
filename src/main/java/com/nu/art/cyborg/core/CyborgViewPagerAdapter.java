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

package com.nu.art.cyborg.core;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nu.art.software.reflection.tools.ReflectiveTools;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public abstract class CyborgViewPagerAdapter<ItemType, RendererType extends ItemRenderer<ItemType>>
		extends PagerAdapter {

	protected final String TAG = getClass().getSimpleName();

	protected final LayoutInflater inflater;

	private final Vector<ItemType> items = new Vector<ItemType>();

	private final Class<ItemType> itemType;

	private final HashMap<ItemType, RendererType> renderers = new HashMap<ItemType, RendererType>();

	private final Class<RendererType> rendererType;

	private final CyborgActivityBridge activityBridge;

	private Runnable notifyDataSetChanged = new Runnable() {
		@Override
		public void run() {
			notifyDataSetChanged();
		}
	};

	public CyborgViewPagerAdapter(CyborgActivityBridge activity, Class<ItemType> itemType, Class<RendererType> rendererType) {
		super();
		this.activityBridge = activity;
		this.inflater = activityBridge.getDefaultLayoutInflater();
		this.rendererType = rendererType;
		this.itemType = itemType;
	}

	public void renderItem(int position) {
		ItemType item = getItem(position);
		if (item == null) {
			logWarning("Item not found for position: " + position);
			return;
		}
		renderItem(item);
	}

	public void renderItem(ItemType item) {
		RendererType renderer = getRenderer(item);
		if (renderer == null) {
			logDebug("No renderer for Item: " + item);
			return;
		}
		logDebug("render Item: " + item);
		renderer.renderImpl();
	}

	@Override
	public final View instantiateItem(ViewGroup parent, int position) {
		ViewGroup convertView = null;
		return _getView(position, convertView, parent);
	}

	@SuppressWarnings("unchecked")
	private View _getView(int position, ViewGroup convertView, ViewGroup parent) {
		View itemView = convertView;
		logDebug("instantiateItem(" + position + ")");
		RendererType renderer;
		ItemType item = getItem(position);
		if (itemView == null) {

			logDebug("Creating new renderer for item: " + item);
			renderer = createNewRenderer(item);
			renderer.setActivityBridge(activityBridge);
			renderer._createView(inflater, parent);
			itemView = renderer.getRootView();
		} else {
			renderer = (RendererType) itemView.getTag();
			if (renderer.getItem() != null && renderer.getItem() != item) {
				disposeItem(item);
			}
		}
		renderers.put(item, renderer);

		logDebug("Rendering Item: " + item);
		renderer._setItem(item);
		renderer.renderImpl();

		parent.addView(itemView);
		return itemView;
	}

	protected final RendererType getRenderer(int position) {
		return getRenderer(getItem(position));
	}

	protected final RendererType getRenderer(ItemType item) {
		return renderers.get(item);
	}

	@Override
	public final void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}

	@Override
	public final void destroyItem(View collection, int position, Object _view) {
		ItemType item = getItem(position);
		logDebug("Destroy Item: " + item);
		View view = (View) _view;
		if (view.getParent() != null) {
			((ViewGroup) view.getParent()).removeView(view);
		}
		renderers.remove(item);
		disposeItem(item);
	}

	protected void disposeItem(ItemType item) {}

	@Override
	public final void notifyDataSetChanged() {
		if (!CyborgBuilder.getInstance().isMainThread()) {
			CyborgBuilder.getInstance().postOnUI(notifyDataSetChanged);
			return;
		}

		logDebug("notifyDataSetChanged()");
		super.notifyDataSetChanged();
	}

	protected RendererType createNewRenderer(ItemType item) {
		logDebug("createNewRenderer(" + item + ")");
		return ReflectiveTools.newInstance(rendererType);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int getItemPosition(Object object) {
		View view = (View) object;
		ItemType item = (ItemType) view.getTag();
		int index = items.indexOf(item);

		if (index == -1)
			return POSITION_NONE;
		return index;
	}

	public ItemType getItem(int position) {
		if (items.size() == 0)
			return null;
		return items.get(position);
	}

	public final int getPositionForItem(ItemType item) {
		return items.indexOf(item);
	}

	public final void clear() {
		items.clear();
	}

	public final void remove(ItemType item) {
		items.remove(item);
	}

	public void insert(int position, ItemType item) {
		items.add(position, item);
	}

	public final void add(ItemType item) {
		items.add(item);
	}

	public final void addAll(ItemType... items) {
		for (ItemType item : items) {
			add(item);
		}
	}

	public final void addAll(List<ItemType> newItems) {
		items.addAll(newItems);
	}

	public final ItemType[] getItems() {
		return items.toArray(getArray(itemType, items.size()));
	}

	@SuppressWarnings("unchecked")
	protected final <ArrayType> ArrayType[] getArray(Class<ArrayType> arrayType, int size) {
		return (ArrayType[]) Array.newInstance(arrayType, size);
	}

	public final void logDebug(String debug) {
		Log.d(TAG, debug);
	}

	public final void logError(String error) {
		Log.e(TAG, error);
	}

	public final void logError(String error, Throwable e) {
		Log.e(TAG, error, e);
	}

	public final void logError(Throwable e) {
		Log.e(TAG, "", e);
	}

	public final void logInfo(String info) {
		Log.i(TAG, info);
	}

	public final void logVerbose(String verbose) {
		Log.v(TAG, verbose);
	}

	public final void logWarning(String warning) {
		Log.w(TAG, warning);
	}

	public final void logWarning(String warning, Throwable e) {
		Log.w(TAG, warning, e);
	}
}
