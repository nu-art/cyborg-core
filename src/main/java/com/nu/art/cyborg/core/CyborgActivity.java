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
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.nu.art.belog.Logger;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;
import com.nu.art.cyborg.core.abs.Cyborg;

import static com.nu.art.cyborg.core.consts.IntentKeys.WindowFeature;

/**
 * If I could completely remove Activities I would, but Android needs a some rubbish to govern the views, so this a one type activityType for the entire
 * application.
 *
 * The only reason I can see to extend this object is to create a Cyborg activityType with another theme that would be defined in the manifest, but this can
 * also be done programmatically.
 *
 * Update:
 * As I truly hate Activities and Fragments, I tried to force CyborgActivity to extend Activity, but as luck (at the moment) is not on my side, some 3rd party
 * (like Google's YouTube SDK) still uses Fragments, so in order to keep users in same activities and against my gut...
 *
 * CyborgActivity will extend FragmentActivity :(
 */
@SuppressWarnings("unused")
public class CyborgActivity
	extends FragmentActivity
	implements ILogger {

	protected final String TAG = getClass().getSimpleName();

	private final CyborgActivityBridge bridge;

	protected final Cyborg cyborg;

	private LayoutInflater layoutInflater;

	private final Logger logger;

	public final CyborgActivityBridge getBridge() {
		return bridge;
	}

	protected CyborgActivity(String screenName) {
		super();
		cyborg = CyborgBuilder.getInstance();
		logger = cyborg.getLogger(this);
		bridge = new CyborgActivityBridgeImpl(screenName, this);
	}

	public CyborgActivity() {
		this("NoName");
	}

	public void reCreateScreen() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean toRet = bridge.onCreateOptionsMenu(menu);
		return toRet | super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean toRet = bridge.onKeyDown(keyCode, event);
		return toRet | super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean toRet = bridge.onKeyUp(keyCode, event);
		return toRet | super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean toRet = bridge.onKeyLongPress(keyCode, event);
		return toRet | super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		boolean toRet = bridge.onKeyShortcut(keyCode, event);
		return toRet | super.onKeyShortcut(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		boolean toRet = bridge.onKeyMultiple(keyCode, repeatCount, event);
		return toRet | super.onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public void onBackPressed() {
		boolean toRet = bridge.onBackPressed();
		if (toRet)
			return;
		super.onBackPressed();
	}

	@Override
	protected void onUserLeaveHint() {
		bridge.onUserLeaveHint();
		super.onUserLeaveHint();
	}

	@Override
	public void onUserInteraction() {
		//		bridge.onUserInteraction();
		super.onUserInteraction();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		boolean toRet = bridge.onMenuItemSelected(featureId, item);
		return toRet | super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		bridge.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
		bridge.onRestoreInstanceState(inState);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int windowFeature = getIntent().getIntExtra(WindowFeature, Window.FEATURE_NO_TITLE);
		requestWindowFeature(windowFeature);

		onCreateImpl();
		bridge.onCreate(savedInstanceState);
	}

	protected void onCreateImpl() {}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		bridge.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bridge.onResume();
	}

	@Override
	protected void onPause() {
		bridge.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		bridge.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		bridge.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		bridge.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@SuppressWarnings( {
		                   "rawtypes",
		                   "unchecked"
	                   })
	public final <ModuleType extends CyborgModule> ModuleType getModule(Class<ModuleType> moduleType) {
		return bridge.getModule(moduleType);
	}

	public final void postOnUI(long delay, Runnable action) {
		bridge.postOnUI(delay, action);
	}

	public final void postOnUI(Runnable action) {
		bridge.postOnUI(action);
	}

	public final void removeAndPostOnUI(long delay, Runnable action) {
		bridge.removeAndPostOnUI(delay, action);
	}

	public final void removeAndPostOnUI(Runnable action) {
		bridge.removeAndPostOnUI(action);
	}

	public final void removeActionFromUI(Runnable action) {
		bridge.removeActionFromUI(action);
	}

	public final Handler getUI_Handler() {
		return bridge.getUI_Handler();
	}

	protected final StackLayerBuilder createStackLayer(CyborgStackController controller) {
		return controller.createLayerBuilder();
	}

	protected final <Type> Type getViewById(@IdRes int viewId) {
		return (Type) findViewById(viewId);
	}

	@Override
	public void logVerbose(String verbose) {
		if (logger != null)
			logger.logVerbose(verbose);
	}

	@Override
	public void logVerbose(String verbose, Object... params) {
		if (logger != null)
			logger.logVerbose(verbose, params);
	}

	@Override
	public void logVerbose(Throwable e) {
		if (logger != null)
			logger.logVerbose(e);
	}

	@Override
	public void logVerbose(String verbose, Throwable e) {
		if (logger != null)
			logger.logVerbose(verbose, e);
	}

	@Override
	public void logDebug(String debug) {
		if (logger != null)
			logger.logDebug(debug);
	}

	@Override
	public void logDebug(String debug, Object... params) {
		if (logger != null)
			logger.logDebug(debug, params);
	}

	@Override
	public void logDebug(Throwable e) {
		if (logger != null)
			logger.logDebug(e);
	}

	@Override
	public void logDebug(String debug, Throwable e) {
		if (logger != null)
			logger.logDebug(debug, e);
	}

	@Override
	public void logInfo(String info) {
		if (logger != null)
			logger.logInfo(info);
	}

	@Override
	public void logInfo(String info, Object... params) {
		if (logger != null)
			logger.logInfo(info, params);
	}

	@Override
	public void logInfo(Throwable e) {
		if (logger != null)
			logger.logInfo(e);
	}

	@Override
	public void logInfo(String info, Throwable e) {
		if (logger != null)
			logger.logInfo(info, e);
	}

	@Override
	public void logWarning(String warning) {
		if (logger != null)
			logger.logWarning(warning);
	}

	@Override
	public void logWarning(String warning, Object... params) {
		if (logger != null)
			logger.logWarning(warning, params);
	}

	@Override
	public void logWarning(Throwable e) {
		if (logger != null)
			logger.logWarning(e);
	}

	@Override
	public void logWarning(String warning, Throwable e) {
		if (logger != null)
			logger.logWarning(warning, e);
	}

	@Override
	public void logError(String error) {
		if (logger != null)
			logger.logError(error);
	}

	@Override
	public void logError(String error, Object... params) {
		if (logger != null)
			logger.logError(error, params);
	}

	@Override
	public void logError(Throwable e) {
		if (logger != null)
			logger.logError(e);
	}

	@Override
	public void logError(String error, Throwable e) {
		if (logger != null)
			logger.logError(error, e);
	}
}
