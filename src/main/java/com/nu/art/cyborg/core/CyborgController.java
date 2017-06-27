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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.Restorable;
import com.nu.art.cyborg.annotations.ViewIdentifier;
import com.nu.art.cyborg.common.consts.ScreenOrientation;
import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;
import com.nu.art.cyborg.core.KeyboardChangeListener.OnKeyboardVisibilityListener;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.core.more.CyborgStateExtractor;
import com.nu.art.cyborg.core.more.CyborgStateInjector;
import com.nu.art.cyborg.core.more.CyborgViewInjector;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.modular.core.ModuleManager.ModuleInjector;

/**
 * So this is what Cyborg is ALL about... It all comes down to this.<br><br>
 * <ul>
 * <li>The controller manages all your UI views, it handles all their events, and it does it all by dependencies injection!</li>
 * <li>You can declare members of type {@link View} in your controller with the {@link ViewIdentifier} annotation, and Cyborg will do the rest.</li>
 * <li>You can declare members that extends {@link CyborgModule} and Cyborg would resolve these for you.</li>
 * <li>The controller have a super intuitive behaviour... <b>LIKE</b> a Fragment.</li>
 * <li>The controller have most of the apis you would normally use, so you don't need to search for your context.</li>
 * <li> If you have data tat you would like to persist after your activityType had lost its state... declare a member and add the {@link Restorable} annotation
 * to
 * it!</li>
 * </ul>
 * <b>I can elaborate about the controller's magnificence for 50 more lines, but instead just explore the API, and check out the sample project!</b>
 */
@SuppressWarnings( {
											 "unused",
											 "deprecation",
											 "unchecked"
									 })
public abstract class CyborgController
		extends CyborgControllerBase {

	public static final CyborgController[] EmptyControllersArray = new CyborgController[0];

	public ScreenOrientation getScreenOrientation() {
		Display display = getSystemService(WindowService).getDefaultDisplay();
		if (display.getWidth() == display.getHeight()) {
			return ScreenOrientation.Square;
		}
		if (display.getWidth() < display.getHeight()) {
			return ScreenOrientation.Portrait;
		}
		return ScreenOrientation.Landscape;
	}

	private CyborgController[] nestedControllers = {};

	private final SparseArray<View> views = new SparseArray<View>();

	final int layoutId;

	private View rootView;

	private LifeCycleState state;

	private String stateTag;

	public CyborgController(@LayoutRes int layoutId) {
		super();
		this.layoutId = layoutId;
	}

	/**
	 * Override this method and extract your views if you <b>don't want</b> or <b>can't</b>  to use the annotation
	 */
	protected void extractMembers() {}

	private void injectMembers() {
		CyborgViewInjector viewInjector = new CyborgViewInjector(getClass().getSimpleName(), rootView, actionDelegator, isDebug());
		ModuleInjector moduleInjector = cyborg.getModuleInjector();
		viewInjector.injectToInstance(this);
		moduleInjector.injectToInstance(this);
	}

	final void setState(LifeCycleState newState) {
		if (Cyborg.DebugControllerLifeCycle)
			logDebug("State Changed: " + this.state + " ==> " + newState);

		if (state == newState)
			throw new BadImplementationException("States are not managed well");

		this.state = newState;
	}

	protected final LifeCycleState getState() {
		return state;
	}

	protected final View getViewById(int id) {
		return getViewById(View.class, id);
	}

	/**
	 * Get view by id, class type, and parent view.
	 *
	 * @param viewType   The Class type of the view.
	 * @param id         The view id of the desired view.
	 * @param <ViewType> View Class Type
	 *
	 * @return The view.
	 */
	protected final <ViewType extends View> ViewType getViewById(Class<ViewType> viewType, int id) {
		return getViewById(rootView, viewType, id);
	}

	/**
	 * Get view by id, class type, and parent view.
	 *
	 * @param container  The view that is containing the desired view.
	 * @param viewType   The Class type of the view.
	 * @param id         The view id of the desired view.
	 * @param <ViewType> View Class Type
	 *
	 * @return The view.
	 */
	protected final <ViewType extends View> ViewType getViewById(View container, Class<ViewType> viewType, int id) {
		ViewType view = (ViewType) getView(id);
		if (view != null) {
			return view;
		}

		view = (ViewType) container.findViewById(id);
		if (view == null) {
			throw new BadImplementationException("View not found for constant: " + id);
		}

		views.put(id, view);
		return view;
	}

	private View getView(int id) {
		return views.get(id);
	}

	/**
	 * A render api that will call renderImpl on the UI thread.
	 */
	public final void render() {
		postOnUI(new Runnable() {

			@Override
			public void run() {
				renderImpl();
			}
		});
	}

	/**
	 * @return Get the stack this controller is a part of.
	 */
	final CyborgStackController getStack() {
		View rootView = getRootView();

		CyborgController controller;
		while (rootView != null) {
			ViewParent parent = rootView.getParent();
			if (!(parent instanceof View))
				return null;

			rootView = (View) parent;

			if (!(rootView instanceof CyborgView)) {
				continue;
			}

			controller = ((CyborgView) rootView).getController();
			if (controller instanceof CyborgStackController)
				return (CyborgStackController) controller;
		}

		throw new ImplementationMissingException("In order to use the stack, this view must be a contained within a StackController");
	}

	/**
	 * @return The root view of the controller.
	 */
	protected final View getRootView() {
		return rootView;
	}

	/**
	 * A callback to handle the xml attribute for internal items.
	 *
	 * @param context Not sure why this is needed...
	 * @param attrs   The xml attributes
	 */
	public void handleAttributes(Context context, AttributeSet attrs) {
		getModule(AttributeModule.class).setAttributes(context, attrs, this);
	}

	protected void onDestroyView() {}

	final void dispatchLifeCycleEvent(LifeCycleState newState) {
		if (newState == state)
			logWarning("ALREADY IN STATE: " + newState);

		for (CyborgController nestedController : nestedControllers) {
			nestedController.dispatchLifeCycleEvent(newState);
		}

		switch (newState) {
			case OnCreate:
				if (this.state != null)
					return;

				activityBridge.addController(this);
				onCreate();
				break;
			case OnResume:
				if (this.state != LifeCycleState.OnPause && this.state != LifeCycleState.OnCreate)
					return;

				onResume();
				break;
			case OnPause:
				if (this.state != LifeCycleState.OnResume)
					return;

				onPause();
				break;
			case OnDestroy:
				if (this.state != LifeCycleState.OnPause)
					return;

				activityBridge.removeController(this);
				nestedControllers = EmptyControllersArray;
				onDestroy();
				break;
		}

		setState(newState);
	}

	protected void onCreate() {}

	protected void onStart() {}

	protected void onResume() {}

	protected void onPause() {}

	protected void onStop() {}

	protected void onDestroy() {}

	protected void onConfigurationChanged(Configuration newConfig) {}

	protected void handleIntent(Intent intent) {}

	protected void renderImpl() {}

	public boolean onBackPressed() {
		return false;
	}

	protected final void setInputMode(final int inputMode) {
		activityBridge.setInputMode(inputMode);
	}

	protected boolean createMenuOptions(Menu menu, MenuInflater menuInflater) {
		return false;
	}
	/* ******************************************
		STATE HANDLING
	 ********************************************/

	final void onSaveInstanceState(Bundle outState) {
		View rootView = getRootView();
		if (rootView == null)
			return;

		onPreSaveState();
		onSaveComplexObjectState(outState);
		CyborgStateExtractor stateInjector = new CyborgStateExtractor(stateTag, outState);
		stateInjector.extractFromInstance(this);

		SparseArray<Parcelable> viewState = new SparseArray<>();
		rootView.saveHierarchyState(viewState);
		outState.putSparseParcelableArray("rootView", viewState);
	}

	final void onRestoreInstanceState(Bundle inState) {
		CyborgStateInjector stateInjector = new CyborgStateInjector(stateTag, inState);
		stateInjector.injectToInstance(this);
		onPostRestoredState();
		onRestoreComplexObjectState(inState);

		SparseArray<Parcelable> viewState = inState.getSparseParcelableArray("rootView");
		getRootView().restoreHierarchyState(viewState);
	}

	protected void onPreSaveState() {}

	protected void onSaveComplexObjectState(Bundle outState) {}

	protected final void saveStatefulObject(String key, Bundle outState, Object o) {
		if (o == this)
			throw new BadImplementationException("Do not pass the controller as the object to save it!! Cyborg is doing it perfectly on its own.");

		//		if (o instanceof View)
		//			saveViewState(key, outState, (View) o);

		String injectorKey = stateTag + "-" + key;
		CyborgStateExtractor stateInjector = new CyborgStateExtractor(injectorKey, outState);
		stateInjector.extractFromInstance(o);
	}

	//	private void saveViewState(String key, Bundle outState, View view) {
	//		String viewStateKey = stateTag + "-view-" + key;
	//		try {
	//			Method onSaveInstanceState = View.class.getDeclaredMethod("onSaveInstanceState");
	//			onSaveInstanceState.setAccessible(true);
	//			Parcelable p = (Parcelable) onSaveInstanceState.invoke(view);
	//			outState.putParcelable(viewStateKey, p);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//	}

	protected void onPostRestoredState() {}

	protected void onRestoreComplexObjectState(Bundle inState) {}

	protected final void restoreStatefulObject(String key, Bundle inState, Object o) {
		if (o == this)
			throw new BadImplementationException("Do not pass the controller as the object to restore it!! Cyborg is doing it perfectly on its own.");

		String injectorKey = stateTag + "-" + key;
		CyborgStateInjector stateInjector = new CyborgStateInjector(injectorKey, inState);
		stateInjector.injectToInstance(o);

		//		if (o instanceof View)
		//			restoreViewState(key, inState, (View) o);
	}

	//	private void restoreViewState(String key, Bundle inState, View view) {
	//		String viewStateKey = stateTag + "-view-" + key;
	//		try {
	//			Method onRestoreInstanceState = View.class.getDeclaredMethod("onRestoreInstanceState", Parcelable.class);
	//			onRestoreInstanceState.setAccessible(true);
	//			Parcelable p = inState.getParcelable(viewStateKey);
	//			onRestoreInstanceState.invoke(view, p);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//	}


	/* ******************************************
		KEYBOARD
	 ********************************************/

	/**
	 * Hide the soft keyboard
	 */
	protected final void hideKeyboard() {
		View view = activityBridge.getActivity().getCurrentFocus();

		if (view == null)
			view = getRootView();

		activityBridge.hideKeyboard(view);
	}

	/**
	 * Show the soft keyboard.
	 *
	 * @param view The view to show the keyboard for.
	 */
	protected final void showKeyboard(View view) {
		if (view == null)
			view = activityBridge.getActivity().getCurrentFocus();

		if (view == null)
			view = getRootView();

		activityBridge.showKeyboard(view);
	}

	/**
	 * Show the soft keyboard for the current focused view, if none found, then for the root view of this controller
	 */
	protected final void showKeyboard() {
		showKeyboard(null);
	}

	/**
	 * Add a keyboard visibility listener.
	 *
	 * @param listener The listener to be called on.
	 */
	protected final void addKeyboardListener(final OnKeyboardVisibilityListener listener) {
		activityBridge.addKeyboardListener(listener);
	}

	/**
	 * Remove the keyboard visibility listener.
	 *
	 * @param listener The listener to be removed.
	 */
	protected final void removeKeyboardListener(final OnKeyboardVisibilityListener listener) {
		activityBridge.removeKeyboardListener(listener);
	}

	final void setStateTag(String stateTag) {
		if (stateTag.equals(this.stateTag))
			return;

		this.stateTag = stateTag;
	}

	final String getStateTag() {
		return stateTag;
	}

	final void _createView(LayoutInflater inflater, ViewGroup parent) {
		_createView(inflater, parent, true);
	}

	final void _createView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		rootView = createView(inflater, parent, attachToParent);
		rootView.setTag(this);
	}

	final void disposeViews() {
		rootView = null;
	}

	/**
	 * Get a controller instance from the stack matching the parameters
	 *
	 * @param viewId The <b>CyborgView</b> id.
	 * @param <Type> The Type of the expected controller.
	 *
	 * @return An instance of the controller, or null if does not exist.
	 */
	protected final <Type> Type getControllerById(@IdRes int viewId) {
		return (Type) rootView.findViewById(viewId).getTag();
	}

	protected View createView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		if (layoutId == -1)
			return createCustomView(inflater, parent, attachToParent);
		try {
			return inflater.inflate(layoutId, parent, attachToParent);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * In case you don't provide a layoutId, and want to create you layout dynamically you MUST override this method
	 *
	 * @return Expected your controller root view to be returned.
	 */
	protected View createCustomView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		throw ExceptionGenerator.didNotProvideLayoutIdOrCustomView(this);
	}

	final void extractMembersImpl() {
		injectMembers();
		extractMembers();
	}

	protected final StackLayerBuilder createNewLayerBuilder() {
		return getStack().createLayerBuilder();
	}

	final void addNestedController(CyborgController controller) {
		nestedControllers = ArrayTools.appendElement(nestedControllers, controller);
	}
}