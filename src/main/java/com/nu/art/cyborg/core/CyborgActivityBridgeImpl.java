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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.support.ViewServer;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.WhoCalledThis;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgBuilder.LaunchConfiguration;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.abs._SystemServices;
import com.nu.art.cyborg.core.consts.DebugFlags;
import com.nu.art.cyborg.core.consts.IntentKeys;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.core.interfaces.LifeCycleListener;
import com.nu.art.cyborg.modules.PermissionModule;
import com.nu.art.modular.core.EventDispatcher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import static com.nu.art.cyborg.core.consts.DebugFlags.DebugActivityLifeCycle;

/**
 * Created by TacB0sS on 19-Jun 2015.
 */
public class CyborgActivityBridgeImpl
		extends Logger
		implements CyborgActivityBridge, IntentKeys, _SystemServices {

	public static Intent composeIntent(LaunchConfiguration launchConfiguration) {
		return composeIntent(launchConfiguration.activityType, launchConfiguration.screenName, launchConfiguration.layoutId);
	}

	public static Intent composeIntent(String screenName, int layoutId) {
		return composeIntent(CyborgActivity.class, screenName, layoutId);
	}

	public static Intent composeIntent(Class<? extends CyborgActivity> activityType, String screenName, int layoutId) {
		Intent intent = new Intent(CyborgBuilder.getInstance().getApplicationContext(), activityType);
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

	private final CyborgActivity activity;

	private final Cyborg cyborg;

	//	private ArrayList<WeakReference<CyborgController>> controllers = new ArrayList<>();
	private ArrayList<WeakReference<CyborgController>> toBeRemoved = new ArrayList<>();
	@SuppressWarnings("unchecked")
	private WeakReference<CyborgController>[] _controllerList = new WeakReference[0];
	private WeakReference<CyborgStackController> priorityStack;

	private LifeCycleListener[] lifecycleListeners = {};

	private String screenName;

	private boolean addToStack;

	private boolean savedState;

	private boolean destroyed;

	private LifeCycleState state;

	private LayoutInflater layoutInflater;

	private EventDispatcher eventDispatcher = new EventDispatcher("CyborgUI-Dispatcher");

	CyborgActivityBridgeImpl(String screenName, CyborgActivity activity) {
		this.activity = activity;
		this.screenName = screenName;
		cyborg = CyborgBuilder.getInstance();
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
	public CyborgActivity getActivity() {
		return activity;
	}

	@Override
	public Intent getIntent() {
		return activity.getIntent();
	}

	private void createView() {
		Intent intent = activity.getIntent();

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
	public final FrameLayout addContentLayer(int layoutId) {
		return (FrameLayout) activity.findViewById(android.R.id.content);
	}

	@Override
	public final void hideKeyboard(View rootView) {
		InputMethodManager inputServiceManager = getSystemService(InputMethodService);
		inputServiceManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
	}

	@Override
	public final void showKeyboard(View view) {
		InputMethodManager inputServiceManager = getSystemService(InputMethodService);
		inputServiceManager.showSoftInput(activity.getCurrentFocus(), InputMethodManager.SHOW_FORCED);
		inputServiceManager.showSoftInput(activity.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
	}

	/* ********************************
				Activity LifeCycle
		 **********************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		new KeyboardChangeListener(cyborg, activity);
		eventDispatcher.addListener(activity);
		addToStack = activity.getIntent().getBooleanExtra(ShouldAddToStack, true);
		dispatchLifecycleEvent(LifeCycleState.OnCreate);
		logLifeCycle(screenName + ": onCreate");
		createView();
		if (cyborg.isDebugCertificate())
			ViewServer.get(activity).addWindow(activity);
	}

	interface ControllerProcessor {

		boolean process(CyborgController controller);
	}

	private void processControllers(ControllerProcessor processor) {
		for (WeakReference<CyborgController> ref : _controllerList) {
			CyborgController controller = ref.get();
			if (controller == null) {
				toBeRemoved.add(ref);
				continue;
			}
			processor.process(controller);
		}

		ArrayTools.removeElements(_controllerList, toBeRemoved);
		toBeRemoved.clear();
	}

	@Override
	public void onNewIntent(Intent intent) {
		logLifeCycle(screenName + ": onNewIntent");
		handleIntent(intent);
	}

	private void handleIntent(final Intent intent) {
		processControllers(new ControllerProcessor() {

			@Override
			public boolean process(CyborgController cyborgController) {
				cyborgController.handleIntent(intent);
				return false;
			}
		});
	}

	@Override
	public void onRestoreInstanceState(final Bundle inState) {
		logLifeCycle("RestoreState");
		processControllers(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				Bundle controllerBundle = inState.getBundle(controller.getStateTag());
				if (controllerBundle == null) {
					logWarning("Could not find State for controller: " + controller.getClass() + ", with key: " + controller.getStateTag());
					return false;
				}

				controller.onRestoreInstanceState(controllerBundle);
				return false;
			}
		});
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
	public void onSaveInstanceState(final Bundle outState) {
		logLifeCycle(screenName + ": SaveState");
		processControllers(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				Bundle controllerBundle = new Bundle();
				outState.putBundle(controller.getStateTag(), controllerBundle);
				controller.onSaveInstanceState(controllerBundle);
				return false;
			}
		});
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
	public boolean onCreateOptionsMenu(final Menu menu) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.createMenuOptions(menu, activity.getMenuInflater());
			}
		});
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onKeyDown(keyCode, event);
			}
		});
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onKeyUp(keyCode, event);
			}
		});
	}

	@Override
	public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onKeyLongPress(keyCode, event);
			}
		});
	}

	@Override
	public boolean onKeyShortcut(final int keyCode, final KeyEvent event) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onKeyShortcut(keyCode, event);
			}
		});
	}

	@Override
	public boolean onKeyMultiple(final int keyCode, final int repeatCount, final KeyEvent event) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onKeyMultiple(keyCode, repeatCount, event);
			}
		});
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public boolean onBackPressed() {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onBackPressed();
			}
		});
	}

	private boolean processControllersPriority(ControllerProcessor processor) {
		if (priorityStack != null) {
			CyborgStackController stackController = priorityStack.get();
			if (stackController != null && processor.process(stackController))
				return true;
		}
		// First check for stack controllers
		for (int i = _controllerList.length - 1; i >= 0; i--) {
			WeakReference<CyborgController> ref = _controllerList[i];
			CyborgController controller = ref.get();
			if (controller == null) {
				toBeRemoved.add(ref);
				continue;
			}

			if (!(controller instanceof CyborgStackController))
				continue;

			if (processor.process(controller))
				return true;
		}

		// Now check for regular controllers
		for (int i = _controllerList.length - 1; i >= 0; i--) {
			WeakReference<CyborgController> ref = _controllerList[i];
			CyborgController controller = ref.get();
			if (controller == null) {
				toBeRemoved.add(ref);
				continue;
			}

			if (controller instanceof CyborgStackController)
				continue;

			if (processor.process(controller))
				return true;
		}

		_controllerList = ArrayTools.removeElements(_controllerList, toBeRemoved);
		toBeRemoved.clear();

		return false;
	}

	@Override
	public void onUserLeaveHint() {
		processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onUserLeaveHint();
			}
		});
	}

	@Override
	public void onUserInteraction() {
		processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.onUserInteraction();
			}
		});
	}

	@Override
	public boolean onMenuItemSelected(int featureId, final MenuItem item) {
		return processControllersPriority(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				return controller.actionDelegator.onMenuItemClick(item);
			}
		});
	}

	/* ********************************
		Listeners
 	 **********************************/
	public void setPriorityStack(CyborgStackController controller) {
		priorityStack = new WeakReference<>(controller);
	}

	@Override
	public final void addController(CyborgController controller) {
		if (controller instanceof ItemRenderer)
			return;

		if (controller instanceof InRendererCyborgController)
			return;

		//		controllers.add(new WeakReference<>(controller));
		_controllerList = ArrayTools.appendElement(_controllerList, new WeakReference<>(controller));
		eventDispatcher.addListener(controller);
		if (DebugActivityLifeCycle) {
			logDebug("Added controller(" + _controllerList.length + "): " + controller);
		}
	}

	@Override
	public final void removeController(CyborgController controller) {
		for (WeakReference<CyborgController> ref : _controllerList) {
			if (ref.get() == null) {
				toBeRemoved.add(ref);
				continue;
			}

			if (ref.get() == controller) {
				toBeRemoved.add(ref);
			}
		}

		_controllerList = ArrayTools.removeElements(_controllerList, toBeRemoved);
		eventDispatcher.removeListener(controller);
		toBeRemoved.clear();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		cyborg
				.dispatchModuleEvent(screenName + ": onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode, OnActivityResultListener.class, new Processor<OnActivityResultListener>() {
					@Override
					public void process(OnActivityResultListener listener) {
						listener.onActivityResult(requestCode, resultCode, data);
					}
				});
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		logDebug("onRequestPermissionsResult requestCode: " + requestCode + ", permissions: " + Arrays.toString(permissions) + ", grantResults: " + Arrays
				.toString(grantResults));
		getModule(PermissionModule.class).onPermissionsResult(requestCode, permissions, grantResults);
	}

	@SuppressWarnings("unchecked")
	public final <ListenerType> void dispatchEvent(String message, final Class<ListenerType> listenerType, final Processor<ListenerType> processor) {
		logDebug("Dispatching UI Event: " + message);
		final WhoCalledThis whoCalledThis = new WhoCalledThis("Dispatching UI Event: " + message);
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (isDestroyed() || isSavedState())
					return;

				Class<ListenerType> _listenerType = DebugFlags.paramExtractor.extractGenericTypeFromProcessorTest(listenerType, processor);
				eventDispatcher.dispatchEvent(whoCalledThis, _listenerType, processor);
			}
		});
	}

	@Override
	public void reCreateScreen() {

	}

	public Window getWindow() {
		return activity.getWindow();
	}

	@Override
	public void setInputMode(int softInputMode) {
		getWindow().setSoftInputMode(softInputMode);
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
	public final <Type> Type getController(@IdRes int viewId) {
		return (Type) activity.findViewById(viewId).getTag();
	}

	@Override
	@SuppressWarnings( {
												 "rawtypes",
												 "unchecked"
										 })
	public <ModuleType extends CyborgModule> ModuleType getModule(Class<ModuleType> moduleType) {
		return (ModuleType) cyborg.getModule((Class<? extends CyborgModule>) moduleType);
	}

	@Override
	public void postOnUI(long delay, Runnable action) {
		cyborg.postOnUI(delay, action);
	}

	@Override
	public void postOnUI(Runnable action) {
		cyborg.postOnUI(action);
	}

	@Override
	public void removeAndPostOnUI(long delay, Runnable action) {
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

	private synchronized void dispatchLifecycleEvent(final LifeCycleState state) {
		this.state = state;
		processControllers(new ControllerProcessor() {
			@Override
			public boolean process(CyborgController controller) {
				controller.dispatchLifeCycleEvent(state);
				return false;
			}
		});

		for (LifeCycleListener lifecycleListener : lifecycleListeners) {
			state.process(lifecycleListener);
		}
	}

	@Override
	public final <Service> Service getSystemService(ServiceType<Service> service) {
		return cyborg.getSystemService(service);
	}

	/* ********************************
		Logs
 	 **********************************/
	private void logLifeCycle(String log) {
		if (DebugActivityLifeCycle)
			logDebug("Activity State Changed: " + log);
	}
}
