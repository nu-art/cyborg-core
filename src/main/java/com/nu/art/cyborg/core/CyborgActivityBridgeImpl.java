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

import android.support.ViewServer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.FrameLayout;

import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgBuilder.LaunchConfiguration;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.abs._SystemServices;
import com.nu.art.cyborg.core.consts.IntentKeys;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.core.interfaces.LifeCycleListener;
import com.nu.art.cyborg.core.interfaces.OnKeyboardVisibilityListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by TacB0sS on 19-Jun 2015.
 */
public class CyborgActivityBridgeImpl
		extends Logger
		implements CyborgActivityBridge, IntentKeys, _SystemServices {

	public static Intent composeIntent(LaunchConfiguration launchConfiguration) {
		return composeIntent(CyborgActivity.class, launchConfiguration.screenName, launchConfiguration.layoutId);
	}

	public static Intent composeIntent(String screenName, int layoutId) {
		return composeIntent(CyborgActivity.class, screenName, layoutId);
	}

	public static Intent composeIntent(Class<? extends CyborgActivity> activityType, String screenName, int layoutId) {
		Intent intent = new Intent(CyborgBuilder.getInstance().getApplication(), activityType);
		intent.putExtra(ScreenName, screenName);
		intent.putExtra(LayoutId, layoutId);
		return intent;
	}

	public static void startActivityInStack(String screenName, int layoutId) {
		Intent intent = composeIntent(CyborgActivity.class, screenName, layoutId);
		startActivityInStack(intent);
	}

	public static void startActivityInStack(Intent intent) {
		CyborgBuilder.getInstance().openActivityInStack(intent);
	}

	private final Activity activity;

	private final Cyborg cyborg;

	private final Vector<OnActivityResultListener> activityResultListeners = new Vector<OnActivityResultListener>();

	private final Vector<OnKeyboardVisibilityListener> keyboardListenerListeners = new Vector<OnKeyboardVisibilityListener>();

	private CyborgController[] controllerList = {};

	protected LifeCycleListener[] lifecycleListeners = {};

	private final HashMap<String, CyborgController> controllersTagMap = new HashMap<String, CyborgController>();

	protected String screenName;

	protected boolean addToStack;

	private boolean savedState;

	private boolean destroyed;

	private LifeCycleState state;

	private LayoutInflater layoutInflater;

	CyborgActivityBridgeImpl(String screenName, Activity activity) {
		this.activity = activity;
		this.screenName = screenName;
		cyborg = CyborgBuilder.getInstance();
		cyborg.setBeLogged(this);
	}

	@Override
	public LifeCycleState getState() {
		return state;
	}

	public LayoutInflater getDefaultLayoutInflater() {
		if (layoutInflater != null)
			return layoutInflater;
		return layoutInflater = LayoutInflater.from(activity);
	}

	@Override
	public Activity getActivity() {
		return activity;
	}

	@Override
	public Intent getIntent() {
		return activity.getIntent();
	}

	private void createView() {
		Intent intent = activity.getIntent();
		int windowFeature = intent.getIntExtra(WindowFeature, Window.FEATURE_NO_TITLE);
		activity.requestWindowFeature(windowFeature);

		if (screenName == null)
			screenName = intent.getStringExtra(ScreenName);

		if (screenName == null)
			throw new BadImplementationException("Nameless Activity");

		int theme = intent.getIntExtra(ActivityTheme, -1);
		if (theme != -1)
			activity.setTheme(theme);

		int layoutId = intent.getIntExtra(LayoutId, -1);
		if (layoutId != -1)
			activity.setContentView(layoutId);
	}

	@Override
	public final FrameLayout addContentLayer(int v2_dialog_controller__upgrade_to_add_site) {
		return (FrameLayout) activity.findViewById(android.R.id.content);
	}

	@Override
	public final void hideKeyboard(IBinder windowToken) {
		cyborg.hideKeyboard(windowToken);
	}

	@Override
	public final void showKeyboard(IBinder windowToken) {
		cyborg.showKeyboard(windowToken);
	}

	/* ********************************
				Activity LifeCycle
		 **********************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		addToStack = activity.getIntent().getBooleanExtra(ShouldAddToStack, true);
		dispatchLifecycleEvent(LifeCycleState.OnCreate);
		logLifeCycle(screenName + ": onCreate");
		createView();
		if (cyborg.isDebugCertificate())
			ViewServer.get(activity).addWindow(activity);
	}

	@Override
	public void onNewIntent(Intent intent) {
		logLifeCycle(screenName + ": onNewIntent");
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		for (CyborgController controller : controllerList) {
			controller.handleIntent(intent);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle inState) {
		logLifeCycle("RestoreState");
		for (CyborgController controller : controllerList) {
			Bundle controllerBundle = inState.getBundle(controller.getStateTag());
			controller.onRestoreInstanceState(controllerBundle);
		}
	}

	@Override
	public void onResume() {
		logLifeCycle(screenName + ": onResume");
		savedState = false;
		if (addToStack)
			cyborg.setActivityInForeground(this);

		cyborg.sendView(screenName);
		dispatchLifecycleEvent(LifeCycleState.OnResume);
		ViewServer.get(activity).setFocusedWindow(activity);
	}

	@Override
	public void onPause() {
		logLifeCycle(screenName + ": onPause");
		if (addToStack)
			cyborg.setActivityInForeground(null);
		dispatchLifecycleEvent(LifeCycleState.OnPause);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		logLifeCycle(screenName + ": SaveState");
		for (CyborgController controller : controllerList) {
			Bundle controllerBundle = new Bundle();
			outState.putBundle(controller.getStateTag(), controllerBundle);
			controller.onSaveInstanceState(controllerBundle);
		}
		savedState = true;
	}

	@Override
	public void onDestroy() {
		logLifeCycle(screenName + ": onDestroy");
		dispatchLifecycleEvent(LifeCycleState.OnDestroy);
		ViewServer.get(activity).removeWindow(activity);
		destroyed = true;
	}

	@Override
	public final boolean isSavedState() {
		return savedState;
	}

	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void finish() {
		activity.finish();
	}

	/* ********************************
			UI Events Callbacks
	 **********************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean toRet = false;
		for (CyborgController controller : controllerList) {
			toRet |= controller.createMenuOptions(menu, activity.getMenuInflater());
		}
		return toRet;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean toRet = false;
		for (CyborgController controller : controllerList) {
			toRet |= controller.onKeyDown(keyCode, event);
		}
		return toRet;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean toRet = false;
		for (CyborgController controller : controllerList) {
			toRet |= controller.onKeyUp(keyCode, event);
		}
		return toRet;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean toRet = false;
		for (CyborgController controller : controllerList) {
			toRet |= controller.onKeyLongPress(keyCode, event);
		}
		return toRet;
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		boolean toRet = false;
		for (CyborgController controller : controllerList) {
			toRet |= controller.onKeyShortcut(keyCode, event);
		}
		return toRet;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		boolean toRet = false;
		for (CyborgController controller : controllerList) {
			toRet |= controller.onKeyMultiple(keyCode, repeatCount, event);
		}
		return toRet;
	}

	@Override
	public boolean onBackPressed() {
		boolean toRet = false;
		CyborgController[] controllers = controllerList;
		for (int i = controllers.length - 1; i >= 0; i--) {
			CyborgController controller = controllers[i];
			toRet |= controller.onBackPressed();
			if (toRet)
				return true;
		}
		return toRet;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		for (CyborgController controller : controllerList) {
			if (controller.actionDelegator.onMenuItemClick(item))
				return true;
		}
		return false;
	}

	/* ********************************
		Listeners
 	 **********************************/
	@Override
	public void removeResultListener(OnActivityResultListener onActivityResultListener) {
		activityResultListeners.remove(onActivityResultListener);
	}

	@Override
	public void addResultListener(OnActivityResultListener onActivityResultListener) {
		activityResultListeners.add(onActivityResultListener);
	}

	@Override
	public final void addController(String newStateTag, CyborgController controller) {
		if (Arrays.asList(controllerList).contains(controller)) {
			removeController(controller.getStateTag());
		}
		controllerList = ArrayTools.appendElement(controllerList, controller);
		CyborgController previousController = controllersTagMap.put(newStateTag, controller);

		if (previousController != null) {
			if (previousController.getClass().getSimpleName().equals(previousController.getStateTag()))
				throw new BadImplementationException("Too Many controllers of same type: '" + previousController
						.getStateTag() + "'\n --- If using two or more controllers of the same type in the same CyborgActivity, you MUST define a different tag for each controller!");
			else
				throw new BadImplementationException("More than one controller declared with tag '" + controller
						.getStateTag() + "'  --- TAG must be unique for each controller in the same CyborgActivity!");
		}
	}

	@Override
	public final void removeController(String stateTag) {
		CyborgController controller = controllersTagMap.remove(stateTag);
		controllerList = ArrayTools.removeElement(controllerList, controller);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		logLifeCycle(screenName + ": onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode);
		OnActivityResultListener[] listeners = ArrayTools.asArray(activityResultListeners, OnActivityResultListener.class);
		for (OnActivityResultListener listener : listeners) {
			if (listener.onActivityResult(requestCode, resultCode, data)) {
				removeResultListener(listener);
				return;
			}
		}
	}
	// need to make sure the only lifecycles called on the controllers are the same ones as in the activity lifecycle state
	//	including onCreate, which is now has an abnormal behavior

	@SuppressWarnings("unchecked")
	public final <ListenerType> void dispatchEvent(final Class<ListenerType> listenerType, final Processor<ListenerType> action) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (isDestroyed() || isSavedState())
					return;

				if (listenerType.isAssignableFrom(activity.getClass())) {
					action.process((ListenerType) activity);
				}

				ListenerType[] listeners = ArrayTools.asFilteredArray(Arrays.asList(controllerList), listenerType);
				for (ListenerType listener : listeners) {
					if (((CyborgController) listener).getState().ordinal() >= LifeCycleState.OnDestroy.ordinal())
						continue;
					action.process(listener);
				}
			}
		});
	}

	@Override
	public void reCreateScreen() {

	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		activity.startActivityForResult(intent, requestCode);
	}

	@Override
	public void startActivity(Intent intent) {
		activity.startActivity(intent);
	}

	@SuppressWarnings("unchecked")
	public final <Type> Type getController(Class<Type> type, String tag) {
		CyborgController controller = controllersTagMap.get(tag);
		if (!type.isAssignableFrom(controller.getClass()))
			throw new BadImplementationException("Controller of type: " + controller.getClass().getSimpleName() + " does NOT implements requested type: " + type
					.getSimpleName());
		return (Type) controller;
	}

	@Override
	@SuppressWarnings( {"rawtypes",
												 "unchecked"
										 })
	public <ModuleType extends CyborgModule> ModuleType getModule(Class<ModuleType> moduleType) {
		return (ModuleType) cyborg.getModule((Class<? extends CyborgModule>) moduleType);
	}

	@Override
	public void postOnUI(int delay, Runnable action) {
		cyborg.postOnUI(delay, action);
	}

	@Override
	public void postOnUI(Runnable action) {
		cyborg.postOnUI(action);
	}

	@Override
	public void removeAndPostOnUI(int delay, Runnable action) {
		cyborg.removeAndPostOnUI(delay, action);
	}

	@Override
	public void removeAndPostOnUI(Runnable action) {
		cyborg.removeAndPostOnUI(action);
	}

	@Override
	public void removeActionFromUI(Runnable action) {
		cyborg.removeActionFromUI(action);
	}

	@Override
	public Handler getUI_Handler() {
		return cyborg.getUI_Handler();
	}

	public final void addLifeCycleListener(LifeCycleListener listener) {
		lifecycleListeners = ArrayTools.appendElement(lifecycleListeners, listener);
	}

	public final void removeLifeCycleListener(LifeCycleListener listener) {
		lifecycleListeners = ArrayTools.removeElement(lifecycleListeners, listener);
	}

	private synchronized void dispatchLifecycleEvent(LifeCycleState state) {
		this.state = state;
		for (CyborgController controller : controllerList) {
			controller.dispatchLifeCycleEvent(state);
		}

		for (LifeCycleListener lifecycleListener : lifecycleListeners) {
			state.process(lifecycleListener);
		}
	}

	@Override
	public final <Service> Service getSystemService(ServiceType<Service> service) {
		return cyborg.getSystemService(service);
	}

	private class KeyboardListener
			implements OnGlobalLayoutListener {

		private final View activityRootView;

		private boolean wasOpened;

		private final int DefaultKeyboardDP = 100;

		// From @nathanielwolf answer...  Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
		private final int EstimatedKeyboardDP = DefaultKeyboardDP + (VERSION.SDK_INT >= 20 ? 48 : 0);

		private final Rect r = new Rect();

		public KeyboardListener(View activityRootView) {
			this.activityRootView = activityRootView;
		}

		@Override
		public void onGlobalLayout() {
			// Convert the dp to pixels.
			int estimatedKeyboardHeight = cyborg.dpToPx(EstimatedKeyboardDP);

			// Conclude whether the keyboard is shown or not.
			activityRootView.getWindowVisibleDisplayFrame(r);
			int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
			boolean isShown = heightDiff >= estimatedKeyboardHeight;

			if (isShown == wasOpened) {
				return;
			}

			wasOpened = isShown;
			synchronized (keyboardListenerListeners) {
				for (OnKeyboardVisibilityListener listener : keyboardListenerListeners) {
					listener.onVisibilityChanged(isShown);
				}
			}
		}
	}

	private KeyboardListener keyboardListener;

	public synchronized void addKeyboardVisibilityListener(OnKeyboardVisibilityListener listener) {
		if (keyboardListenerListeners.size() == 0) {
			final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
			activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener = new KeyboardListener(activityRootView));
		}
		synchronized (keyboardListenerListeners) {
			keyboardListenerListeners.add(listener);
		}
	}

	public synchronized void removeKeyboardVisibilityListener(OnKeyboardVisibilityListener listener) {
		synchronized (keyboardListenerListeners) {
			keyboardListenerListeners.remove(listener);
		}
		if (keyboardListenerListeners.size() == 0) {
			final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

			if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
				activityRootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
			else
				activityRootView.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardListener);
			keyboardListener = null;
		}
	}

	/* ********************************
		Logs
 	 **********************************/
	private void logLifeCycle(String log) {
		if (Cyborg.DebugActivityLifeCycle)
			logDebug("Activity State Changed: " + log);
	}
}
