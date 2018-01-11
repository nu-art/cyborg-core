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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.Restorable;
import com.nu.art.cyborg.annotations.ViewIdentifier;
import com.nu.art.cyborg.common.beans.ModelEvent;
import com.nu.art.cyborg.common.consts.ScreenOrientation;
import com.nu.art.cyborg.common.interfaces.ICyborgController;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.common.utils.BusyState;
import com.nu.art.cyborg.common.utils.Tools;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;
import com.nu.art.cyborg.core.CyborgStackController.StackTransitionAnimator;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.animations.PredefinedStackTransitionAnimator;
import com.nu.art.cyborg.core.animations.PredefinedTransitions;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition.TransitionOrientation;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.core.modules.DeviceDetailsModule;
import com.nu.art.cyborg.core.more.CyborgStateExtractor;
import com.nu.art.cyborg.core.more.CyborgStateInjector;
import com.nu.art.cyborg.core.more.CyborgViewInjector;
import com.nu.art.cyborg.core.more.UserActionsDelegatorImpl;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModuleManager.ModuleInjector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;

import static com.nu.art.cyborg.core.consts.DebugFlags.DebugControllerLifeCycle;
import static com.nu.art.cyborg.core.consts.DebugFlags.DebugPerformance;

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
											 "unchecked",
											 "WeakerAccess"
									 })
public abstract class CyborgController
		extends Logger
		implements ICyborgController {

	public static final CyborgController[] EmptyControllersArray = new CyborgController[0];

	protected final Runnable render = new Runnable() {

		@Override
		public void run() {
			render();
		}
	};

	public ScreenOrientation getScreenOrientation() {
		return getModule(DeviceDetailsModule.class).getOrientation();
	}

	private CyborgController[] nestedControllers = {};

	private final SparseArray<View> views = new SparseArray<>();

	final int layoutId;

	private View rootView;

	private String stateTag;

	private BusyState busyState = new BusyState();

	boolean keepInStack;

	private LifeCycleState state;

	protected final ActionDelegator actionDelegator;

	protected final Cyborg cyborg;

	protected CyborgActivityBridge activityBridge;

	private boolean animatedIn;

	public CyborgController(@LayoutRes int layoutId) {
		super();
		if (DebugPerformance)
			logVerbose("Instantiated");
		this.layoutId = layoutId;
		cyborg = CyborgBuilder.getInstance();
		actionDelegator = new ActionDelegator(cyborg);
	}

	protected final void setBusyState(BusyState busyState) {
		this.busyState = busyState;
		for (CyborgController nestedController : nestedControllers) {
			nestedController.setBusyState(busyState);
		}
	}

	/**
	 * Will cause the root view of the controller to capture all the touch events, and will not propagate the event to the parent views in the view
	 * hierarchy.
	 *
	 * Common use case would be the background of dialogs, once user clicks the shaded area you can block the click events from propagating to the under visible
	 * view.
	 */
	protected final void blockClickEvents() {
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	protected boolean canExecute() {
		return busyState.canExecute();
	}

	/**
	 * Override this method and extract your views if you <b>don't want</b> or <b>can't</b>  to use the annotation
	 */
	protected void extractMembers() {}

	private void injectMembers() {
		if (DebugPerformance)
			logVerbose("injectMembers");
		CyborgViewInjector viewInjector = new CyborgViewInjector(rootView, actionDelegator, isDebug());
		ModuleInjector moduleInjector = cyborg.getModuleInjector();

		if (DebugPerformance)
			logVerbose("viewInjector");
		viewInjector.injectToInstance(this);

		if (DebugPerformance)
			logVerbose("moduleInjector");
		moduleInjector.injectToInstance(this);

		if (DebugPerformance)
			logVerbose("done");
	}

	final void setState(LifeCycleState newState) {
		if (DebugControllerLifeCycle)
			logDebug("State Changed: " + this.state + " ==> " + newState);

		if (state == newState)
			throw new BadImplementationException("States are not managed well");

		this.state = newState;
	}

	protected final LifeCycleState getState() {
		return state;
	}

	protected final <ViewType extends View> ViewType getViewById(int id) {
		return (ViewType) getViewById(View.class, id);
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
	 * A render UI api that will call render on the UI thread.
	 */
	public final void renderUI() {
		if (!isMainThread()) {
			postOnUI(render);
			return;
		}

		render.run();
	}

	/**
	 * @return Get the stack this controller is a part of.
	 */
	public final CyborgStackController getStack() {
		View rootView = getRootView();

		CyborgController controller;
		while (rootView != null) {
			ViewParent parent = rootView.getParent();
			if (!(parent instanceof View))
				break;

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

	protected final <ControllerType extends CyborgController> ControllerType injectController(@IdRes int viewId, Class<ControllerType> controller) {
		CyborgView viewToInject = new CyborgView(getActivity(), controller);

		int id = Tools.generateValidViewId(getActivity());
		viewToInject.setId(id);

		View view = getViewById(viewId);
		if (view == null)
			throw new BadImplementationException("The provided viewId does not exists in this controller");

		if (!(view instanceof ViewGroup))
			throw new BadImplementationException("The provided viewId is to a " + view.getClass()
																																								.getSimpleName() + ".\n  --  When injecting a controller you must specify a valid ViewGroup id");

		((ViewGroup) view).removeAllViews();
		((ViewGroup) view).addView(viewToInject);
		return (ControllerType) viewToInject.getController();
	}

	protected final void injectLayout(@IdRes int parentViewId, @LayoutRes int layoutId) {
		ViewGroup parentView = getViewById(parentViewId);
		parentView.removeAllViews();
		try {
			getLayoutInflater().inflate(layoutId, parentView, true);
		} catch (Throwable e) {
			while (e.getCause() != null) {
				e = e.getCause();
			}
			Log.e("CYBORG", "As Android's exception handling is crappy, here is the reason for the failure: ", e);
			//noinspection ConstantConditions
			throw (RuntimeException) e;
		}
	}

	/**
	 * @return The root view of the controller.
	 */
	public final View getRootView() {
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

	protected boolean isInEditMode() {
		return rootView.isInEditMode();
	}

	protected void onReady() {}

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

	protected boolean isAnimatedIn() {
		return animatedIn;
	}

	final void _onAnimatedIn() {
		animatedIn = true;
		onAnimatedIn();
	}

	protected void onAnimatedIn() {}

	protected void onCreate() {}

	protected void onResume() {}

	protected void onPause() {}

	protected void onDestroy() {}

	protected void onConfigurationChanged(Configuration newConfig) {}

	protected void handleIntent(Intent intent) {}

	protected void render() {}

	public boolean onBackPressed() {
		return false;
	}

	protected boolean onUserLeaveHint() {
		return false;
	}

	protected boolean onUserInteraction() {
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
		CyborgStateExtractor stateExtractor = new CyborgStateExtractor(stateTag, outState);
		stateExtractor.extractFromInstance(this);

		SparseArray<Parcelable> viewState = new SparseArray<>();
		rootView.saveHierarchyState(viewState);
		outState.putSparseParcelableArray("rootView", viewState);
	}

	final void onRestoreInstanceState(Bundle inState) {
		// TODO: 01/10/2017 This method caused a crash in the app after changing the device language while the app was open in the bg. 'viewState' was null.
		CyborgStateInjector stateInjector = new CyborgStateInjector(stateTag, inState);
		stateInjector.injectToInstance(this);
		onPostRestoredState();
		onRestoreComplexObjectState(inState);
		if (inState == null)
			return;
		SparseArray<Parcelable> viewState = inState.getSparseParcelableArray("rootView");
		if (viewState == null) {
			return;
		}

		getRootView().restoreHierarchyState(viewState);
		onControllerRestored();
	}

	protected void onControllerRestored() {}

	protected void onPreSaveState() {}

	protected void onSaveComplexObjectState(Bundle outState) {}

	protected final void saveStatefulObject(String key, Bundle outState, Object o) {
		if (o == this)
			throw new BadImplementationException("Do not pass the controller as the object to save it!! Cyborg is doing it perfectly on its own.");

		String injectorKey = stateTag + "-" + key;
		CyborgStateExtractor stateInjector = new CyborgStateExtractor(injectorKey, outState);
		stateInjector.extractFromInstance(o);
	}

	protected void onPostRestoredState() {}

	protected void onRestoreComplexObjectState(Bundle inState) {}

	protected final void restoreStatefulObject(String key, Bundle inState, Object o) {
		if (o == this)
			throw new BadImplementationException("Do not pass the controller as the object to restore it!! Cyborg is doing it perfectly on its own.");

		String injectorKey = stateTag + "-" + key;
		CyborgStateInjector stateInjector = new CyborgStateInjector(injectorKey, inState);
		stateInjector.injectToInstance(o);
	}

	protected final void startActivity(Intent intent) {
		activityBridge.startActivity(intent);
	}

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
		/* When constructing a controller in a library, it would be simpler (I think) to define listeners in the field's annotation
		 * and force the view injector to use whatever instance already set manually in the extract member method.*/
		extractMembers();
		injectMembers();
	}

	protected final StackLayerBuilder createNewLayerBuilder() {
		return getStack().createLayerBuilder();
	}

	protected final StackTransitionAnimator createLayerTransition(PredefinedTransitions transition) {
		return new PredefinedStackTransitionAnimator(getActivity(), transition, BaseTransition.ORIENTATION_VERTICAL);
	}

	protected final StackTransitionAnimator createLayerTransition(PredefinedTransitions transition, @TransitionOrientation int orientation) {
		return new PredefinedStackTransitionAnimator(getActivity(), transition, orientation);
	}

	final void addNestedController(CyborgController controller) {
		nestedControllers = ArrayTools.appendElement(nestedControllers, controller);
	}

	final void removeNestedController(CyborgController controller) {
		nestedControllers = ArrayTools.removeElement(nestedControllers, controller);
	}

	public void setVisibility(int visibility) {
		rootView.setVisibility(visibility);
	}

	public int getVisibility() {
		return rootView.getVisibility();
	}

	public void invalidateView() {
		rootView.invalidate();
	}

	public static final Random UtilsRandom = new Random();

	public static short getRandomShort() {
		return (short) UtilsRandom.nextInt(Short.MAX_VALUE);
	}

	public final void setKeepInStack(boolean keepInStack) {
		this.keepInStack = keepInStack;
	}

	final class ActionDelegator
			extends UserActionsDelegatorImpl {

		public ActionDelegator(Cyborg cyborg) {
			super(cyborg);
		}

		boolean canReceiveEvents() {
			return getState() == LifeCycleState.OnResume;
		}

		@Override
		public boolean onLongClick(View v) {
			if (!canReceiveEvents())
				return false;

			super.onLongClick(v);
			return CyborgController.this.onLongClick(v);
		}

		@Override
		public boolean onTouch(View v, MotionEvent me) {
			if (!canReceiveEvents())
				return false;

			super.onTouch(v, me);
			return CyborgController.this.onTouch(v, me);
		}

		@Override
		public void onClick(final View v) {
			if (!canReceiveEvents())
				return;

			super.onClick(v);
			CyborgController.this.onClick(v);
		}

		@Override
		public void onModelEvent(ModelEvent event) {
			if (!canReceiveEvents())
				return;

			super.onModelEvent(event);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (!canReceiveEvents())
				return;

			if (fromUser) {
				super.onProgressChanged(seekBar, progress, true);
			}
			CyborgController.this.onProgressChanged(seekBar, progress, fromUser);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if (!canReceiveEvents())
				return;

			super.onStartTrackingTouch(seekBar);
			CyborgController.this.onStartTrackingTouch(seekBar);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (!canReceiveEvents())
				return;

			super.onStopTrackingTouch(seekBar);
			CyborgController.this.onStopTrackingTouch(seekBar);
		}

		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id) {
			if (!canReceiveEvents())
				return;

			super.onItemSelected(parentView, selectedView, position, id);
			CyborgController.this.onItemSelected(parentView, selectedView, position, id);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			if (!canReceiveEvents())
				return;

			super.onNothingSelected(parentView);
			CyborgController.this.onNothingSelected(parentView);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (!canReceiveEvents())
				return;

			super.onItemClick(parent, view, position, id);
			CyborgController.this.onItemClick(parent, view, position, id);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (!canReceiveEvents())
				return false;

			super.onItemLongClick(parent, view, position, id);
			return CyborgController.this.onItemLongClick(parent, view, position, id);
		}

		@Override
		public void onRecyclerItemClicked(RecyclerView parentView, View view, int position) {
			if (!canReceiveEvents())
				return;

			super.onRecyclerItemClicked(parentView, view, position);
			CyborgController.this.onRecyclerItemClicked(parentView, view, position);
		}

		@Override
		public boolean onRecyclerItemLongClicked(RecyclerView parentView, View view, int position) {
			if (!canReceiveEvents())
				return false;

			super.onRecyclerItemLongClicked(parentView, view, position);
			return CyborgController.this.onRecyclerItemLongClicked(parentView, view, position);
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (!canReceiveEvents())
				return false;

			super.onKeyDown(keyCode, event);
			return CyborgController.this.onKeyDown(keyCode, event);
		}

		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			if (!canReceiveEvents())
				return false;

			super.onKeyUp(keyCode, event);
			return CyborgController.this.onKeyUp(keyCode, event);
		}

		@Override
		public boolean onKeyLongPress(int keyCode, KeyEvent event) {
			if (!canReceiveEvents())
				return false;

			super.onKeyLongPress(keyCode, event);
			return CyborgController.this.onKeyLongPress(keyCode, event);
		}

		@Override
		public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
			if (!canReceiveEvents())
				return;

			super.onRatingChanged(ratingBar, rating, fromUser);
			CyborgController.this.onRatingChanged(ratingBar, rating, fromUser);
		}

		@Override
		public void onPageSelected(int position) {
			if (!canReceiveEvents())
				return;

			super.onPageSelected(position);
			CyborgController.this.onPageSelected(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (!canReceiveEvents())
				return;

			super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			CyborgController.this.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (!canReceiveEvents())
				return;

			super.onPageScrollStateChanged(state);
			CyborgController.this.onPageScrollStateChanged(state);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (!canReceiveEvents())
				return;

			super.onCheckedChanged(buttonView, isChecked);
			CyborgController.this.onCheckedChanged(buttonView, isChecked);
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if (!canReceiveEvents())
				return false;

			super.onMenuItemClick(item);
			return CyborgController.this.onMenuItemClick(item);
		}

		@Override
		public void beforeTextChanged(TextView view, CharSequence string, int start, int count, int after) {
			if (!canReceiveEvents())
				return;

			super.beforeTextChanged(view, string, start, count, after);
			CyborgController.this.beforeTextChanged(view, string, start, count, after);
		}

		@Override
		public void onTextChanged(TextView view, CharSequence string, int start, int before, int count) {
			if (!canReceiveEvents())
				return;

			super.onTextChanged(view, string, start, before, count);
			CyborgController.this.onTextChanged(view, string, start, before, count);
		}

		@Override
		public void afterTextChanged(TextView view, Editable editableValue) {
			if (!canReceiveEvents())
				return;

			super.afterTextChanged(view, editableValue);
			CyborgController.this.afterTextChanged(view, editableValue);
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (!canReceiveEvents())
				return false;

			super.onEditorAction(v, actionId, event);
			return CyborgController.this.onEditorAction(v, actionId, event);
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!canReceiveEvents())
				return;

			super.onFocusChange(v, hasFocus);
			CyborgController.this.onFocusChange(v, hasFocus);
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (!canReceiveEvents())
				return false;

			super.onKey(v, keyCode, event);
			return CyborgController.this.onKey(v, keyCode, event);
		}
	}

	/**
	 * Use the one without the listenerType parameter
	 */
	@Deprecated
	public final <ListenerType> void dispatchEvent(String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		dispatchEvent(message, processor);
	}

	@Override
	public final <ListenerType> void dispatchEvent(String message, Processor<ListenerType> processor) {
		activityBridge.dispatchEvent(message, processor);
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

	public Drawable getDrawable(@DrawableRes int drawableId) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			return getResources().getDrawable(drawableId, getActivity().getTheme());
		}
		return getResources().getDrawable(drawableId);
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