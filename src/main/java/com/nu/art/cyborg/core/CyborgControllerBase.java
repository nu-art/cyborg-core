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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.common.beans.ModelEvent;
import com.nu.art.cyborg.common.interfaces.ICyborgController;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.core.more.UserActionsDelegatorImpl;
import com.nu.art.modular.core.Module;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;

/**
 * This is an internal object.
 */
//@SuppressWarnings("WeakerAccess")
abstract class CyborgControllerBase
		extends Logger
		implements ICyborgController {

	public static final Random UtilsRandom = new Random();

	public static short getRandomShort() {
		return (short) UtilsRandom.nextInt(Short.MAX_VALUE);
	}

	public LifeCycleState getActivityState() {
		return activityBridge.getState();
	}

	final class ActionDelegator
			extends UserActionsDelegatorImpl {

		public ActionDelegator(Cyborg cyborg) {
			super(cyborg);
		}

		@Override
		public boolean onLongClick(View v) {
			super.onLongClick(v);
			return CyborgControllerBase.this.onLongClick(v);
		}

		@Override
		public boolean onTouch(View v, MotionEvent me) {
			super.onTouch(v, me);
			return CyborgControllerBase.this.onTouch(v, me);
		}

		@Override
		public void onClick(final View v) {
			super.onClick(v);
			CyborgControllerBase.this.onClick(v);
		}

		@Override
		public void onModelEvent(ModelEvent event) {
			super.onModelEvent(event);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				super.onProgressChanged(seekBar, progress, fromUser);
			}
			CyborgControllerBase.this.onProgressChanged(seekBar, progress, fromUser);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			super.onStartTrackingTouch(seekBar);
			CyborgControllerBase.this.onStartTrackingTouch(seekBar);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			super.onStopTrackingTouch(seekBar);
			CyborgControllerBase.this.onStopTrackingTouch(seekBar);
		}

		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
			super.onItemSelected(parentView, selectedView, position, id);
			CyborgControllerBase.this.onItemSelected(parentView, selectedView, position, id);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			super.onNothingSelected(parentView);
			CyborgControllerBase.this.onNothingSelected(parentView);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			super.onItemClick(parent, view, position, id);
			CyborgControllerBase.this.onItemClick(parent, view, position, id);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			super.onItemLongClick(parent, view, position, id);
			return CyborgControllerBase.this.onItemLongClick(parent, view, position, id);
		}

		@Override
		public void onRecyclerItemClicked(RecyclerView parentView, View view, int position) {
			super.onRecyclerItemClicked(parentView, view, position);
			CyborgControllerBase.this.onRecyclerItemClicked(parentView, view, position);
		}

		@Override
		public boolean onRecyclerItemLongClicked(RecyclerView parentView, View view, int position) {
			super.onRecyclerItemLongClicked(parentView, view, position);
			return CyborgControllerBase.this.onRecyclerItemLongClicked(parentView, view, position);
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			super.onKeyDown(keyCode, event);
			return CyborgControllerBase.this.onKeyDown(keyCode, event);
		}

		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			super.onKeyUp(keyCode, event);
			return CyborgControllerBase.this.onKeyUp(keyCode, event);
		}

		@Override
		public boolean onKeyLongPress(int keyCode, KeyEvent event) {
			super.onKeyLongPress(keyCode, event);
			return CyborgControllerBase.this.onKeyLongPress(keyCode, event);
		}

		@Override
		public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
			super.onRatingChanged(ratingBar, rating, fromUser);
			CyborgControllerBase.this.onRatingChanged(ratingBar, rating, fromUser);
		}

		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			CyborgControllerBase.this.onPageSelected(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			CyborgControllerBase.this.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			super.onPageScrollStateChanged(state);
			CyborgControllerBase.this.onPageScrollStateChanged(state);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			super.onCheckedChanged(buttonView, isChecked);
			CyborgControllerBase.this.onCheckedChanged(buttonView, isChecked);
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			super.onMenuItemClick(item);
			return CyborgControllerBase.this.onMenuItemClick(item);
		}

		@Override
		public void beforeTextChanged(TextView view, CharSequence string, int start, int count, int after) {
			super.beforeTextChanged(view, string, start, count, after);
			CyborgControllerBase.this.beforeTextChanged(view, string, start, count, after);
		}

		@Override
		public void onTextChanged(TextView view, CharSequence string, int start, int before, int count) {
			super.onTextChanged(view, string, start, before, count);
			CyborgControllerBase.this.onTextChanged(view, string, start, before, count);
		}

		@Override
		public void afterTextChanged(TextView view, Editable editableValue) {
			super.afterTextChanged(view, editableValue);
			CyborgControllerBase.this.afterTextChanged(view, editableValue);
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			super.onEditorAction(v, actionId, event);
			return CyborgControllerBase.this.onEditorAction(v, actionId, event);
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			super.onFocusChange(v, hasFocus);
			CyborgControllerBase.this.onFocusChange(v, hasFocus);
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			super.onKey(v, keyCode, event);
			return CyborgControllerBase.this.onKey(v, keyCode, event);
		}
	}

	protected final ActionDelegator actionDelegator;

	protected final Cyborg cyborg;

	protected final void startActivity(Intent intent) {
		activityBridge.startActivity(intent);
	}

	protected CyborgActivityBridge activityBridge;

	final void setActivityBridge(CyborgActivityBridge activityBridge) {
		this.activityBridge = activityBridge;
	}

	protected final CyborgActivity getActivity() {
		return activityBridge.getActivity();
	}

	protected final LayoutInflater getLayoutInflater() {
		return activityBridge.getDefaultLayoutInflater();
	}

	protected final Intent getIntent() {
		return activityBridge.getIntent();
	}

	protected final boolean isDestroyed() {
		return activityBridge.isDestroyed();
	}

	protected final void finishActivity() {
		activityBridge.finish();
	}

	@Override
	public final <ListenerType> void dispatchEvent(String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		logDebug("Dispatching UI Event: " + message);
		activityBridge.dispatchEvent(listenerType, processor);
	}

	CyborgControllerBase() {
		cyborg = CyborgBuilder.getInstance();
		actionDelegator = new ActionDelegator(cyborg);
	}

	@Override
	public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onNothingSelected(AdapterView<?> parentView) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onClick(View v) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public boolean onLongClick(View v) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onModelEvent(ModelEvent event) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View clickedView, int position, long id) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parentView, View clickedView, int position, long id) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public void onRecyclerItemClicked(RecyclerView parentView, View view, int position) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public boolean onRecyclerItemLongClicked(RecyclerView parentView, View view, int position) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onPageSelected(int position) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// Dummy method to be overridden in the inheriting class...
		return false;
	}

	@Override
	public void beforeTextChanged(TextView view, CharSequence string, int start, int count, int after) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void onTextChanged(TextView view, CharSequence string, int start, int before, int count) {
		// Dummy method to be overridden in the inheriting class...
	}

	@Override
	public void afterTextChanged(TextView view, Editable editableValue) {
		// Dummy method to be overridden in the inheriting class...
	}

	/*
	 * Interfaces .....
	 */
	@Override
	public final long elapsedTimeMillis() {
		return cyborg.elapsedTimeMillis();
	}

	protected final boolean isMainThread() {
		return cyborg.isMainThread();
	}

	@Override
	public final void postOnUI(long delay, Runnable action) {
		cyborg.postOnUI(delay, action);
	}

	@Override
	public final void postOnUI(Runnable action) {
		cyborg.postOnUI(action);
	}

	@Override
	public final void removeAndPostOnUI(long delay, Runnable action) {
		cyborg.removeAndPostOnUI(delay, action);
	}

	@Override
	public final void removeAndPostOnUI(Runnable action) {
		cyborg.removeAndPostOnUI(action);
	}

	@Override
	public final void removeActionFromUI(Runnable action) {
		cyborg.removeActionFromUI(action);
	}

	@Override
	public final Handler getUI_Handler() {
		return cyborg.getUI_Handler();
	}

	@Override
	public final void toastDebug(String text) {
		cyborg.toastDebug(text);
	}

	@Override
	public final void toastShort(int stringId, Object... args) {
		cyborg.toastShort(stringId, args);
	}

	@Override
	public final void toastLong(int stringId, Object... args) {
		cyborg.toastLong(stringId, args);
	}

	@Override
	public final void toastShort(StringResourceResolver stringResolver) {
		cyborg.toastShort(stringResolver);
	}

	@Override
	public final void toastLong(StringResourceResolver stringResolver) {
		cyborg.toastLong(stringResolver);
	}

	@Override
	public void sendEvent(String category, String action, String label, long value) {
		cyborg.sendEvent(category, action, label, value);
	}

	@Override
	public void sendException(String description, Throwable t, boolean crash) {
		cyborg.sendException(description, t, crash);
	}

	@Override
	public void sendView(String viewName) {
		cyborg.sendView(viewName);
	}

	public final <Type extends Module> Type getModule(Class<Type> moduleType) {
		return cyborg.getModule(moduleType);
	}

	@Override
	public final void vibrate(int repeat, long... interval) {
		cyborg.vibrate(repeat, interval);
	}

	@Override
	public final void vibrate(long ms) {
		cyborg.vibrate(ms);
	}

	@Override
	public final String convertNumericString(String numericString) {
		return cyborg.convertNumericString(numericString);
	}

	@Override
	public final InputStream getAsset(String assetName)
			throws IOException {
		return cyborg.getAsset(assetName);
	}

	@Override
	public final String getString(int stringId, Object... params) {
		return cyborg.getString(stringId, params);
	}

	@Override
	public final String getString(StringResourceResolver stringResolver) {
		return cyborg.getString(stringResolver);
	}

	@Override
	public final String getPackageName() {
		return cyborg.getPackageName();
	}

	@Override
	public final boolean isDebug() {
		return cyborg.isDebug();
	}

	@Override
	public final boolean isDebugCertificate() {
		return cyborg.isDebugCertificate();
	}

	@Override
	public final void waitForDebugger() {
		cyborg.waitForDebugger();
	}

	public final Animation loadAnimation(int animationId) {
		return cyborg.loadAnimation(animationId);
	}

	@Override
	public Context getApplicationContext() {
		return cyborg.getApplicationContext();
	}

	@Override
	public final Resources getResources() {
		return cyborg.getResources();
	}

	@Override
	public final InputStream getRawResources(int resourceId) {
		return getResources().openRawResource(resourceId);
	}

	@Override
	public final float dimToPx(int type, float size) {
		return cyborg.dimToPx(type, size);
	}

	@Override
	public final Locale getLocale() {
		return cyborg.getLocale();
	}

	@Override
	public final float getDimension(int dimensionId) {
		return cyborg.getDimension(dimensionId);
	}

	@Override
	public final int getColor(int colorId) {
		return cyborg.getColor(colorId);
	}

	@Override
	public final ContentResolver getContentResolver() {
		return cyborg.getContentResolver();
	}

	public final int dpToPx(int dp) {
		return cyborg.dpToPx(dp);
	}

	@Override
	public final void postActivityAction(ActivityStackAction action) {
		cyborg.postActivityAction(action);
	}
}
