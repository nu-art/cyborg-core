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

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.nu.art.belog.Logger;
import com.nu.art.core.tools.ArrayTools;

public final class TouchAnalyzer
		extends Logger
		implements OnTouchListener {

	private static final boolean DebugAnalyzer = true;

	private static final int DoubleClickInterval = 400;

	private static final float RotateClockwiseDeltaAngle = 20f;

	private static final int LongClickMaxInterval = 4000;

	private static final int LongClickRadius = 30;

	private static final int LongClickInterval = 700;

	private static final int PreDragInterval = 100;

	private static final int DragMinRadius = 10;

	private static final int ClickRadius = 10;

	private static final int ClickInterval = 220;

	/**
	 * The minimal radius required to determine that a Zoom Gesture occurs.
	 */
	private static final int ZoomMinimalRadius = 40;

	private static final float ZoomMaxAngle = 20f;

	private static final int ZoomRadius = 600;

	public interface TouchAnalyzerListener {

		void onTouchEvent(TouchEventData[] data, int pointerCount);
	}

	public interface OnTouchGestureListener {

		void onZoomChanged(float zoomFactor, float midX, float midY);

		void onRotationAngleChanged(double degrees);

		void onFling(TouchEventData touchEventData);

		void onDrag(TouchEventData touchEventData);

		void onDoubleClick(int pointerIndex, float x, float y);

		void onLongClick(int pointerIndex, float x, float y);

		void onClick(int pointerIndex, float x, float y);

		void onGestureStopped();
	}

	public static abstract class OnTouchGestureAdapter
			implements OnTouchGestureListener {

		@Override
		public void onGestureStopped() {
		}

		@Override
		public void onZoomChanged(float zoomFactor, float midX, float midY) {
		}

		@Override
		public void onRotationAngleChanged(double degrees) {
		}

		@Override
		public void onFling(TouchEventData touchEventData) {
		}

		@Override
		public void onDrag(TouchEventData touchEventData) {
		}

		@Override
		public void onDoubleClick(int pointerIndex, float x, float y) {
		}

		@Override
		public void onLongClick(int pointerIndex, float x, float y) {
		}

		@Override
		public void onClick(int pointerIndex, float x, float y) {
		}
	}

	public final class TouchEventData {

		private TouchEventData() {
		}

		int index;

		long initAt;

		float initX, initY;

		float currentX, currentY;

		float endX = -1, endY = -1;

		long endAt = -1;

		public long clickedAt;

		public boolean inDragEvent;

		public final float getDistance() {
			float dx = getDx();
			float dy = getDy();
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

		public final float getDy() {
			return currentY - initY;
		}

		public final float getDx() {
			return currentX - initX;
		}

		@Override
		public String toString() {
			return "[" + index + "--" + initX + ", " + initY + "--" + currentX + ", " + currentY + "--" + endX + ", " + endY;
		}
	}

	TouchEventData[] eventsData = new TouchEventData[10];

	private TouchAnalyzerListener analyzingListener;

	private int maxPointerInSession = 0;

	private OnTouchGestureListener[] listeners;

	public TouchAnalyzer() {
		for (int i = 0; i < eventsData.length; i++) {
			eventsData[i] = new TouchEventData();
		}
	}

	public final void addOnTouchGesturListener(OnTouchGestureListener listener) {
		listeners = ArrayTools.appendElement(listeners, listener);
	}

	public final void removeOnTouchGesturListener(OnTouchGestureListener listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	@Override
	public final boolean onTouch(View v, MotionEvent event) {
		int actionMasked = event.getActionMasked();
		int actionIndex = event.getActionIndex();
		int action = event.getAction();
		int pointers = event.getPointerCount();

		TouchEventData eventData = eventsData[actionIndex];
		switch (actionMasked) {
			case MotionEvent.ACTION_DOWN:
				maxPointerInSession = 1;
				for (int i = 0; i < eventsData.length; i++) {
					eventData.inDragEvent = false;
				}
			case MotionEvent.ACTION_POINTER_1_DOWN:
				eventData.initX = event.getX(actionIndex);
				eventData.initY = event.getY(actionIndex);
				eventData.index = actionIndex;
				eventData.currentX = eventData.initX;
				eventData.currentY = eventData.initY;
				eventData.initAt = System.currentTimeMillis();
				if (pointers > maxPointerInSession)
					maxPointerInSession = pointers;
				break;

			case MotionEvent.ACTION_MOVE:
				for (int i = 0; i < pointers; i++) {
					eventData = eventsData[i];
					eventData.currentX = event.getX(i);
					eventData.currentY = event.getY(i);
					float dx = eventData.currentX - eventData.initX;
					float dy = eventData.currentY - eventData.initY;
					float distance = (float) Math.sqrt(dx * dx + dy * dy);
					long pressInterval = System.currentTimeMillis() - eventData.initAt;
					if (DebugAnalyzer)
						logDebug("pointer: %d, distance: %.3f px, interval: " + pressInterval + "ms", eventData.index, distance);

					eventData.inDragEvent = distance > DragMinRadius && pressInterval > PreDragInterval;
				}
				checkDragEvent(pointers);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_1_UP:
				eventData.endX = event.getX(actionIndex);
				eventData.endY = event.getY(actionIndex);
				eventData.currentX = eventData.endX;
				eventData.currentY = eventData.endY;
				eventData.endAt = System.currentTimeMillis();
				for (int i = actionIndex + 1; i < maxPointerInSession; i++) {
					eventsData[i - 1] = eventsData[i];
				}
				eventsData[maxPointerInSession - 1] = eventData;
				checkClickEvent(eventData);
				if (pointers == 1)
					dispatchOnGestureStoppedEvent();
				break;
		}
		if (action != 2) {
			if (DebugAnalyzer)
				logDebug("Action: " + action + ", Action Masked: " + actionMasked + ", Index: " + actionIndex + ", Pointers: " + event
						.getPointerCount() + ", MaxPointers: " + maxPointerInSession);
		}
		if (analyzingListener != null)
			analyzingListener.onTouchEvent(eventsData, maxPointerInSession);

		return true;
	}

	private float initialAngle = 0;

	private int count;

	private float previousAngle;

	//	private float initialZoom = -1;

	private void checkDragEvent(int pointers) {
		int dragging = 0;
		TouchEventData[] events = new TouchEventData[2];
		for (int i = 0; i < pointers; i++) {
			if (eventsData[i].inDragEvent) {
				if (dragging == 2)
					return;
				events[dragging++] = eventsData[i];
			}
		}
		if (dragging == 0) {
			initialAngle = 0;
			previousAngle = 0;
			count = 0;
			//			initialZoom = -1;
			if (DebugAnalyzer)
				logDebug("Resetting angle");
			return;
		}
		if (dragging == 1) {
			if (isFling(events[0])) {
				dispatchFlingEvent(events[0]);
				return;
			}
			if (pointers == 1 && maxPointerInSession == 1)
				dispatchDragEvent(events[0]);
			return;
		}

		/*
		 * Dragging two pointers...
		 */
		float dx = events[0].currentX - events[1].currentX;
		float dy = events[0].currentY - events[1].currentY;
		float angle;
		if (dy != 0)
			angle = (float) Math.atan(dx / dy);
		else
			angle = (float) Math.atan(Float.MAX_VALUE);
		if (initialAngle == 0)
			initialAngle = angle;

		if (previousAngle > 1 && angle < -1)
			count++;
		else if (previousAngle < -1 && angle > 1)
			count--;
		previousAngle = angle;

		float da = (float) (count * Math.PI + angle - initialAngle);
		while (true) {
			if (da > 2 * Math.PI)
				da -= 2 * Math.PI;
			else if (da < -2 * Math.PI)
				da += 2 * Math.PI;
			else
				break;
		}
		double daInDegrees = da * 180 / Math.PI;
		if (DebugAnalyzer)
			logDebug("Initial Angle: " + initialAngle + ", Drag Angle: " + angle + ", da: " + da + ", daInDegrees: " + daInDegrees);

		if (Math.abs(daInDegrees) > RotateClockwiseDeltaAngle) {
			dispatchRotateEvent(daInDegrees);
			return;
		}

		//		if (initialZoom == -1)
		//			initialZoom = dZoom;

		float dx1 = events[0].initX - events[1].initX;
		float dy1 = events[0].initY - events[1].initY;
		float r1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
		float r = (float) Math.sqrt(dx * dx + dy * dy);

		float dr = r - r1;
		float factor = dr < 0 ? -1 : 1;
		dr = Math.abs(dr);
		if (dr < ZoomMinimalRadius)
			return;

		if (Math.abs(da) < ZoomMaxAngle) {
			dr -= ZoomMinimalRadius;
			float dZoom = factor * dr / ZoomRadius;
			dispatchZoomEvent(dZoom, events[1].currentX + dx / 2, events[1].currentY + dy / 2);
		}
	}

	private boolean isFling(TouchEventData touchEventData) {
		return false;
	}

	private void checkClickEvent(TouchEventData eventData) {
		if (eventData.inDragEvent)
			return;
		float dx = eventData.endX - eventData.initX;
		float dy = eventData.endY - eventData.initY;
		float distance = (float) Math.sqrt(dx * dx + dy * dy);
		long pressInterval = eventData.endAt - eventData.initAt;
		boolean clicked = distance < ClickRadius && pressInterval < ClickInterval;
		boolean longClicked = distance < LongClickRadius && pressInterval > LongClickInterval && pressInterval < LongClickMaxInterval;
		boolean doubleClicked = eventData.initAt - eventData.clickedAt < DoubleClickInterval && clicked;

		if (DebugAnalyzer) {
			String label = clicked ? "Clicked, " : "";
			label = longClicked ? "Long Clicked, " : label;
			label = doubleClicked ? "Double Clicked" : label;
			logDebug("pointer: %d, distance: %.3f px^2, interval: " + pressInterval + "ms => " + label, eventData.index, distance);
		}

		if (doubleClicked) {
			dispatchOnDoubleClick(eventData.index, eventData.initX + dx / 2, eventData.initY + dy / 2);
		} else if (clicked) {
			eventData.clickedAt = eventData.initAt;
			dispatchOnClick(eventData.index, eventData.initX + dx / 2, eventData.initY + dy / 2);
		} else if (longClicked) {
			dispatchOnLongClick(eventData.index, eventData.initX + dx / 2, eventData.initY + dy / 2);
		}
	}

	private void dispatchOnGestureStoppedEvent() {
		if (DebugAnalyzer)
			logInfo("Gesture stopped");
		for (OnTouchGestureListener l : listeners) {
			l.onGestureStopped();
		}
	}

	private void dispatchZoomEvent(float zoomFactor, float midX, float midY) {
		if (DebugAnalyzer)
			logInfo("Zoom gesture recognized, dZoom = %.2f, midX = %.2f, midY = %.2f", zoomFactor, midX, midY);
		for (OnTouchGestureListener l : listeners) {
			l.onZoomChanged(zoomFactor, midX, midY);
		}
	}

	private void dispatchRotateEvent(double daInDegrees) {
		if (DebugAnalyzer)
			logInfo("Rotate gesture recognized deg: " + daInDegrees);
		for (OnTouchGestureListener l : listeners) {
			l.onRotationAngleChanged(daInDegrees);
		}
	}

	private void dispatchFlingEvent(TouchEventData touchEventData) {
		if (DebugAnalyzer)
			logInfo("Fling gesture recognized for pointer: " + touchEventData.index);
		for (OnTouchGestureListener l : listeners) {
			l.onFling(touchEventData);
		}
	}

	private void dispatchDragEvent(TouchEventData touchEventData) {
		if (DebugAnalyzer)
			logInfo("Drag gesture recognized for pointer: " + touchEventData.index);
		for (OnTouchGestureListener l : listeners) {
			l.onDrag(touchEventData);
		}
	}

	private void dispatchOnDoubleClick(int pointerIndex, float x, float y) {
		if (DebugAnalyzer)
			logInfo("Double Click gesture recognized for pointer: " + pointerIndex);
		for (OnTouchGestureListener l : listeners) {
			l.onDoubleClick(pointerIndex, x, y);
		}
	}

	private void dispatchOnLongClick(int pointerIndex, float x, float y) {
		if (DebugAnalyzer)
			logInfo("Long Click gesture recognized for pointer: " + pointerIndex);
		for (OnTouchGestureListener l : listeners) {
			l.onLongClick(pointerIndex, x, y);
		}
	}

	private void dispatchOnClick(int pointerIndex, float x, float y) {
		if (DebugAnalyzer)
			logInfo("Click gesture recognized for pointer: " + pointerIndex);
		for (OnTouchGestureListener l : listeners) {
			l.onClick(pointerIndex, x, y);
		}
	}

	public void addAnalyzingListener(TouchAnalyzerListener l) {
		this.analyzingListener = l;
	}
}
