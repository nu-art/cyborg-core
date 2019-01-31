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

package com.nu.art.cyborg.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.core.consts.LifecycleState;

public interface CyborgActivityBridge {

	/* Callbacks */
	boolean onCreateOptionsMenu(Menu menu);

	boolean onMenuItemSelected(int featureId, MenuItem item);

	boolean onKeyDown(int keyCode, KeyEvent event);

	boolean onKeyUp(int keyCode, KeyEvent event);

	boolean onKeyLongPress(int keyCode, KeyEvent event);

	boolean onKeyShortcut(int keyCode, KeyEvent event);

	boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

	boolean onBackPressed();

	void onUserLeaveHint();

	void onUserInteraction();

	void onActivityResult(int requestCode, int resultCode, Intent data);

	void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

	/* Controllers */

	void setPriorityStack(CyborgStackController controller);

	void addController(CyborgController controller);

	void removeController(CyborgController controller);

	/* LifeCycle */
	void onCreate(Bundle savedInstanceState);

	void onNewIntent(Intent intent);

	void onRestoreInstanceState(Bundle inState);

	void onResume();

	void onPause();

	void onSaveInstanceState(Bundle outState);

	void onDestroy();

	boolean isSavedState();

	boolean isDestroyed();

	void finish();

	<ListenerType> void dispatchEvent(ILogger originator, String message, Class<ListenerType> listenerType, Processor<ListenerType> processor);

	void reCreateScreen();

	void startActivityForResult(Intent intent, int requestCode);

	void startActivity(Intent intent);

	LayoutInflater getDefaultLayoutInflater();

	Intent getIntent();

	CyborgActivity getActivity();

	<Type> Type getController(@IdRes int viewId);

	<ModuleType extends CyborgModule> ModuleType getModule(Class<ModuleType> moduleType);

	void postOnUI(long delay, Runnable action);

	void postOnUI(Runnable action);

	void removeAndPostOnUI(long delay, Runnable action);

	void removeAndPostOnUI(Runnable action);

	void removeActionFromUI(Runnable action);

	Handler getUI_Handler();

	LifecycleState getState();

	FrameLayout addContentLayer(int contentLayer);

	boolean isKeyboardVisible();

	void hideKeyboard(View view);

	void showKeyboard(View view);

	void setInputMode(int softInputMode);
}
