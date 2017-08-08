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

package com.nu.art.android.gestures;

import java.util.Vector;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public final class ZoomGesture
		implements OnTouchListener, ZoomListener {

	private float distanceX;

	private float distanceY;

	private int minZoomX_Level = 0;

	private int minZoomY_Level = 0;

	private int maxZoomX_Level = 10;

	private int maxZoomY_Level = 10;

	private int zoomX;

	private int zoomY;

	private int zoomStepSizeX = 40;

	private int zoomStepSizeY = 40;

	private double vectorLength;

	private Vector<ZoomListener> zoomListeners = new Vector<>();

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			distanceX = -1;
			distanceY = -1;
			this.vectorLength = -1;
			onZoomGestureEnded(v, zoomX, zoomY);
			return true;
		}

		if (event.getPointerCount() != 2) {
			return true;
		}

		float distanceX = Math.abs(event.getX(1) - event.getX(0));
		float distanceY = Math.abs(event.getY(1) - event.getY(0));
		double vectorLength = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		double zoomStepMargin = Math.sqrt(zoomStepSizeX * zoomStepSizeY);

		if (this.distanceX == -1) {
			this.distanceX = distanceX;
			this.distanceY = distanceY;
			this.vectorLength = -2;
			onZoomGestureStarted(v);
			return true;
		}
		if (this.vectorLength == -2) {
			this.vectorLength = vectorLength;
		}

		if (vectorLength - this.vectorLength < -zoomStepMargin || vectorLength - this.vectorLength > zoomStepMargin) {
			if (distanceX - this.distanceX > zoomStepSizeX) {
				this.vectorLength = vectorLength;
				this.distanceX = distanceX;
				if (zoomX < maxZoomX_Level) {
					++zoomX;
				}
				onZoom(v, zoomX, zoomY);
			} else if (distanceX - this.distanceX < -zoomStepSizeX) {
				this.distanceX = distanceX;
				this.vectorLength = vectorLength;
				if (zoomX > minZoomX_Level) {
					--zoomX;
				}
				onZoom(v, zoomX, zoomY);
			}
			if (distanceY - this.distanceY > zoomStepSizeY) {
				this.distanceY = distanceY;
				this.vectorLength = vectorLength;
				if (zoomY < maxZoomY_Level) {
					++zoomY;
				}
				onZoom(v, zoomX, zoomY);
			} else if (distanceY - this.distanceY < -zoomStepSizeY) {
				this.distanceY = distanceY;
				this.vectorLength = vectorLength;
				if (zoomY > minZoomY_Level) {
					--zoomY;
				}
				onZoom(v, zoomX, zoomY);
			}
		}
		return true;
	}

	public int getMinZoomX_Level() {
		return minZoomX_Level;
	}

	public void setMinZoomX_Level(int minZoomX_Level) {
		this.minZoomX_Level = minZoomX_Level;
	}

	public int getMinZoomY_Level() {
		return minZoomY_Level;
	}

	public void setMinZoomY_Level(int minZoomY_Level) {
		this.minZoomY_Level = minZoomY_Level;
	}

	public int getMaxZoomX_Level() {
		return maxZoomX_Level;
	}

	public void setMaxZoomX_Level(int maxZoomX_Level) {
		this.maxZoomX_Level = maxZoomX_Level;
	}

	public int getMaxZoomY_Level() {
		return maxZoomY_Level;
	}

	public void setMaxZoomY_Level(int maxZoomY_Level) {
		this.maxZoomY_Level = maxZoomY_Level;
	}

	public int getZoomX() {
		return zoomX;
	}

	public void setZoomX(int zoomX) {
		this.zoomX = zoomX;
	}

	public int getZoomY() {
		return zoomY;
	}

	public void setZoomY(int zoomY) {
		this.zoomY = zoomY;
	}

	public int getZoomStepSizeX() {
		return zoomStepSizeX;
	}

	public void setZoomStepSizeX(int zoomStepSizeX) {
		this.zoomStepSizeX = zoomStepSizeX;
	}

	public int getZoomStepSizeY() {
		return zoomStepSizeY;
	}

	public void setZoomStepSizeY(int zoomStepSizeY) {
		this.zoomStepSizeY = zoomStepSizeY;
	}

	public void addZoomListener(ZoomListener zoomListener) {
		zoomListeners.add(zoomListener);
	}

	@Override
	public void onZoomGestureStarted(View v) {
		for (int i = 0; i < zoomListeners.size(); i++) {
			zoomListeners.get(i).onZoomGestureStarted(v);
		}
	}

	@Override
	public void onZoom(View v, int zoomX, int zoomY) {
		for (int i = 0; i < zoomListeners.size(); i++) {
			zoomListeners.get(i).onZoom(v, zoomX, zoomY);
		}
	}

	@Override
	public void onZoomGestureEnded(View v, int zoomX, int zoomY) {
		for (int i = 0; i < zoomListeners.size(); i++) {
			zoomListeners.get(i).onZoomGestureEnded(v, zoomX, zoomY);
		}
	}
}
