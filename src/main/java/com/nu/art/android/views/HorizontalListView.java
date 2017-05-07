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

package com.nu.art.android.views;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.nu.art.android.gestures.TouchAnalyzer;
import com.nu.art.android.gestures.TouchAnalyzer.OnTouchGestureAdapter;
import com.nu.art.android.gestures.TouchAnalyzer.TouchEventData;

import java.util.ArrayList;

public class HorizontalListView
		extends AdapterView<ListAdapter> {

	private ArrayList<View> recycledViews = new ArrayList<View>();

	private class ViewedItems {

		private int adapterIndex;

		private int showCount;

		private int itemsWidth;

		private int initX;

		private int dx;

		public void setDx(int dx) {
			this.dx = dx;
		}

		public ViewedItems() {
			reset();
		}

		private synchronized void calculateVisibleWidth() {
			if (getChildCount() == 0) {
				itemsWidth = 0;
				return;
			}
			itemsWidth = getChildAt(getChildCount() - 1).getRight() - getChildAt(0).getLeft();
		}

		private void reset() {
			adapterIndex = -1;
			showCount = 0;
			itemsWidth = 0;
		}

		public int getPreviousAdapterIndex() {
			return adapterIndex;
		}

		public int getNextAdapterIndex() {
			return adapterIndex + showCount + 1;
		}

		public int getNextViewIndex() {
			return showCount;
		}

		public void addedNextItem() {
			showCount++;
		}

		public void addedFirstItem() {
			adapterIndex--;
			showCount++;
		}

		public void removedFirstItem() {
			adapterIndex++;
			showCount--;
		}

		public void removedLastItem() {
			showCount--;
		}

		public void onScrollCompleted() {
			initX += dx;
			setDx(0);
		}
	}

	private DataSetObserver dataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			clearItems();
			onInvalidated();
		}

		@Override
		public void onInvalidated() {
			invalidate();
			requestLayout();
		}
	};

	private ListAdapter adapter;

	private ViewedItems viewingItems = new ViewedItems();

	TouchAnalyzer touchAnalyzer;

	private int parentHeightMeasureSpec;

	private int margin = 2;

	private Rect cacheRect = new Rect();

	public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HorizontalListView(Context context) {
		super(context);
		init(context);
	}

	@Override
	public void requestLayout() {
		super.requestLayout();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		touchAnalyzer.onTouch(this, event);
		return true;
	}

	private void init(Context context) {
		setWillNotDraw(false);
		touchAnalyzer = new TouchAnalyzer();
		touchAnalyzer.addOnTouchGesturListener(new OnTouchGestureAdapter() {

			@Override
			public void onDrag(TouchEventData touchEventData) {
				int dx = (int) touchEventData.getDx();
				normalizeDx(dx);
				requestLayout();
			}

			private void normalizeDx(int dx) {
				if (viewingItems.initX + dx > 0)
					dx = -viewingItems.initX;

				if (viewingItems.getNextAdapterIndex() == adapter.getCount()) {
					Log.i("HLV", "dx: " + viewingItems.dx);
					Log.i("HLV", "init: " + viewingItems.initX);
					Log.i("HLV", "final: " + (viewingItems.initX + viewingItems.dx));
					Log.i("HLV", "items-width: " + viewingItems.itemsWidth);
					Log.i("HLV", "list-width: " + getWidth());
					Log.i("HLV", "leftChild: " + getChildAt(0).getLeft());

					if (viewingItems.initX + dx + viewingItems.itemsWidth < getWidth())
						dx = -(viewingItems.initX - (getWidth() - viewingItems.itemsWidth));
				}
				viewingItems.setDx(dx);
			}

			@Override
			public void onClick(int pointerIndex, float x, float y) {
				OnItemClickListener onItemClickListener = getOnItemClickListener();
				if (onItemClickListener == null)
					return;

				final int index = getChildIndex((int) x, (int) y);
				if (index == -1)
					return;

				View child = getChildAt(index);
				int adapterIndex = viewingItems.adapterIndex + 1 + index;

				onItemClickListener.onItemClick(HorizontalListView.this, child, adapterIndex, adapter.getItemId(adapterIndex));
			}

			@Override
			public void onGestureStopped() {
				viewingItems.onScrollCompleted();
				super.onGestureStopped();
			}
		});
	}

	private int getChildIndex(final int x, final int y) {
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).getHitRect(cacheRect);
			if (cacheRect.contains(x, y)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setSelection(int position) {
		// TODO implement this

	}

	@Override
	public ListAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (this.adapter != null && (adapter == null || this.adapter != adapter)) {
			this.adapter.unregisterDataSetObserver(dataObserver);
			clearItems();
		}
		this.adapter = adapter;
		if (adapter != null)
			adapter.registerDataSetObserver(dataObserver);
		dataObserver.onChanged();
	}

	private void clearItems() {
		removeAllViewsInLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		parentHeightMeasureSpec = heightMeasureSpec;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (adapter == null)
			return;

		int rightChildEdge = 0;
		int rightChildWidth = 0;
		if (getChildCount() > 0) {
			View rightMostChild = getRightMostChild();
			rightChildEdge = rightMostChild.getRight();
			rightChildWidth = rightMostChild.getMeasuredWidth();
		}

		// Add Right Item
		if (rightChildEdge < right + rightChildWidth / 2 && viewingItems.getNextAdapterIndex() < adapter.getCount()) {
			View recycledView = null;
			if (recycledViews.size() > 0)
				recycledView = recycledViews.remove(0);

			View child = adapter.getView(viewingItems.getNextAdapterIndex(), recycledView, this);
			LayoutParams params = child.getLayoutParams();
			if (params == null)
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, getHeight());

			addViewInLayout(child, viewingItems.getNextViewIndex(), params, true);
			int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), getHeight());
			child.measure(0, MeasureSpec.EXACTLY | childHeightSpec);

			viewingItems.addedNextItem();
		}

		layoutItems();
		if (getChildCount() == 0) {
			return;
		}

		View firstChild = getChildAt(0);
		int previousAdapterIndex = viewingItems.getPreviousAdapterIndex();

		// Add LEFT Item
		if (previousAdapterIndex >= 0 && firstChild.getLeft() > -firstChild.getMeasuredWidth() / 3) {
			View recycledView = null;
			//			if (recycledViews.size() > 0)
			//				recycledView = recycledViews.remove(0);

			View child = adapter.getView(previousAdapterIndex, recycledView, this);
			LayoutParams params = child.getLayoutParams();
			if (params == null)
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, getHeight());

			addViewInLayout(child, 0, params, true);
			int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), getHeight());
			child.measure(0, MeasureSpec.EXACTLY | childHeightSpec);

			viewingItems.addedFirstItem();

			viewingItems.initX -= child.getMeasuredWidth();
		}

		//remove LEFT most elements
		for (int i = 0; i < viewingItems.showCount; i++) {
			View child = getChildAt(i);
			if (child.getRight() < -child.getMeasuredWidth() * 2) {
				int initX = child.getLeft();
				viewingItems.removedFirstItem();
				removeViewInLayout(child);
				recycledViews.add(child);
				if (i < getChildCount())
					viewingItems.initX -= initX - getChildAt(i).getLeft();
				layoutItems();
			} else
				break;
		}

		//remove RIGHT most elements
		int initX = getChildAt(0).getLeft();
		for (int i = 0; i < viewingItems.showCount; i++) {
			View child = getChildAt(getChildCount() - i - 1);
			int leftEdgeOfRightChild = child.getLeft();
			int childWidth = child.getMeasuredWidth();
			if (leftEdgeOfRightChild + initX - getWidth() > childWidth) {
				removeViewInLayout(child);
				recycledViews.add(child);
				viewingItems.removedLastItem();
			} else
				break;
		}

		viewingItems.calculateVisibleWidth();
	}

	private View getRightMostChild() {
		return getChildAt(getChildCount() - 1);
	}

	private void layoutItems() { // error when scrolling over to the right
		int x = viewingItems.initX + viewingItems.dx;

		for (int i = 0; i < viewingItems.showCount; i++) {
			View child = getChildAt(i);
			child.layout(x + margin, 0, x += child.getMeasuredWidth(), getHeight());
		}
	}
}
