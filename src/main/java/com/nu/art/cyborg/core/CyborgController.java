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
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.cyborg.annotations.Restorable;
import com.nu.art.cyborg.annotations.ViewIdentifier;
import com.nu.art.cyborg.common.consts.ScreenOrientation;
import com.nu.art.cyborg.core.CyborgModuleManager.CyborgModuleInjector;
import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.core.interfaces.OnKeyboardVisibilityListener;
import com.nu.art.cyborg.core.more.CyborgStateExtractor;
import com.nu.art.cyborg.core.more.CyborgStateInjector;
import com.nu.art.cyborg.core.more.CyborgViewInjector;

/**
 * So this is what Cyborg is ALL about... It all comes down to this.<br><br>
 * <ul>
 * <li>The controller manages all your UI views, it handles all their events, and it does it all by dependencies injection!</li>
 * <li>You can declare members of type {@link View} in your controller with the {@link ViewIdentifier} annotation, and Cyborg will do the rest.</li>
 * <li>You can declare members that extends {@link CyborgModule} and Cyborg would resolve these for you.</li>
 * <li>The controller have a super intuitive behaviour... <b>LIKE</b> a Fragment.</li>
 * <li>The controller have most of the apis you would normally use, so you don't need to search for your context.</li>
 * <li> If you have data tat you would like to persist after your activity had lost its state... declare a member and add the {@link Restorable} annotation to
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

	/*
	 * A map of rootView Id, to its rootView instance.
     */
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

	final void injectMembers() {
		CyborgViewInjector viewInjector = new CyborgViewInjector(getClass().getSimpleName(), rootView, actionDelegator, isDebuggable());
		CyborgModuleInjector moduleInjector = cyborg.getModuleInjector();
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

	/*/**
	 * @param dialogLayoutId The layout you want to dress the dialog with.
     * @param dialogCode     The code identifying the dialog, MUST be greater than 0.
     */
	protected final void showDialog(String dialogName, Class<? extends CyborgController> dialogController) {
	}

	protected final View getViewById(int id) {
		return getViewById(View.class, id);
	}

	protected final <ViewType extends View> ViewType getViewById(Class<ViewType> viewType, int id) {
		return getViewById(rootView, viewType, id);
	}

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

	protected final View getView(int id) {
		return views.get(id);
	}

	public final void render() {
		postOnUI(new Runnable() {

			@Override
			public void run() {
				renderImpl();
			}
		});
	}

	public final CyborgStackController getStack() {
		View rootView = getRootView();
		CyborgController controller;
		while ((rootView = (View) rootView.getParent()) != null) {
			if (!(rootView instanceof CyborgView))
				continue;

			controller = ((CyborgView) rootView).getController();
			if (controller instanceof CyborgStackController)
				return (CyborgStackController) controller;
		}

		throw new ImplementationMissingException("In order to use the stack, this view must be a contained within a StackController");
	}

	protected final View getRootView() {
		return rootView;
	}

	public void handleAttributes(Context context, AttributeSet attrs) {}

	protected void onPreCreate() {}

	protected void onDestroyView() {}

	final void dispatchLifeCycleEvent(LifeCycleState newState) {
		if (getActivityState().ordinal() < newState.ordinal())
			return;

		if (state != null && state.ordinal() >= newState.ordinal())
			return;

		switch (newState) {
			case OnCreate:
				onCreate();
				break;
			case OnResume:
				onResume();
				break;
			case OnPause:
				onPause();
				break;
			case OnDestroy:
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

	protected boolean createMenuOptions(Menu menu, MenuInflater menuInflater) {
		return false;
	}
	/* ******************************************
		STATE HANDLING
	 ********************************************/

	final void onSaveInstanceState(Bundle outState) {
		onPreSaveState();
		onSaveComplexObjectState(outState);
		CyborgStateExtractor stateInjector = new CyborgStateExtractor(stateTag, outState);
		stateInjector.extractFromInstance(this);

		SparseArray<Parcelable> viewState = new SparseArray<>();
		getRootView().saveHierarchyState(viewState);
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

	protected final void hideKeyboard() {
		activityBridge.hideKeyboard();
	}

	protected final void showKeyboard() {
		activityBridge.showKeyboard();
	}

	protected final void addKeyboardListener(final OnKeyboardVisibilityListener listener) {
		activityBridge.addKeyboardListener(listener);
	}

	protected final void removeKeyboardListener(final OnKeyboardVisibilityListener listener) {
		activityBridge.removeKeyboardListener(listener);
	}

	final void setStateTag(String stateTag) {
		if (stateTag.equals(this.stateTag))
			return;

		if (this.stateTag != null)
			activityBridge.removeController(this.stateTag);

		this.stateTag = stateTag;
		activityBridge.addController(this.stateTag, this);
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

	public void disposeViews() {
		rootView = null;
	}

	protected final <Type> Type getController(Class<Type> type, String tag) {
		return activityBridge.getController(type, tag);
	}

	protected final <Type> Type getController(Class<Type> type) {
		return activityBridge.getController(type, type.getSimpleName());
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

	protected View createCustomView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		throw new BadImplementationException("MUST specify a valid layoutId in the controller constructor or override this method!");
	}

	final void extractMembersImpl() {
		injectMembers();
		extractMembers();
	}

	protected final StackLayerBuilder createNewLayerBuilder() {
		return getStack().createLayerBuilder();
	}
}