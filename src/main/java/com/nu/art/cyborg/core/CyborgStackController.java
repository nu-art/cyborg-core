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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.common.implementors.AnimationListenerImpl;
import com.nu.art.cyborg.common.utils.Interpolators;
import com.nu.art.cyborg.core.animations.PredefinedStackTransitionAnimator;
import com.nu.art.cyborg.core.animations.PredefinedTransitions;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.ui.animations.interpulator.ReverseInterpolator;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.nu.art.cyborg.core.consts.DebugFlags.DebugPerformance;
import static com.nu.art.cyborg.core.consts.DebugFlags.DebugStack;

/**
 * Created by TacB0sS on 25-Jun 2015.
 */
public final class CyborgStackController
		extends CyborgController {

	public static abstract class StackTransitionAnimator {

		protected Interpolator interpolator = Interpolators.LinearInterpolator;
		protected Interpolator reverseInterpolator = new ReverseInterpolator(Interpolators.LinearInterpolator);

		protected void setInterpolator(Interpolator interpolator) {
			this.interpolator = interpolator;
			this.reverseInterpolator = new ReverseInterpolator(interpolator);
		}

		protected abstract void animateIn(StackLayer origin, StackLayer target, int duration, AnimationListener listener);

		protected abstract void animateOut(StackLayer origin, StackLayer target, int duration, AnimationListener listener);
	}

	private LayoutInflater inflater;

	private ArrayList<StackLayer> layersStack = new ArrayList<>();

	private RelativeLayout containerLayout;

	private Class<? extends CyborgController> rootControllerType;

	private int rootLayoutId = -1;

	private boolean rootSaveState = false;

	private String rootTag;

	private int transitionDuration = 300;

	private boolean animatingTransition;

	private PredefinedTransitions defaultTransition;

	private int defaultTransitionOrientation;

	private boolean popOnBackPress = true;

	private boolean withRoot;

	private boolean focused = true;

	private CyborgStackController() {
		super(-1);
	}

	final void setRootSaveState(boolean rootSaveState) {
		this.rootSaveState = rootSaveState;
	}

	final void setRootControllerType(Class<? extends CyborgController> rootControllerType) {
		this.rootControllerType = rootControllerType;
	}

	final void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}

	final void setRootLayoutId(int rootLayoutId) {
		this.rootLayoutId = rootLayoutId;
	}

	final void setPopOnBackPress(boolean popOnBackPress) {
		this.popOnBackPress = popOnBackPress;
	}

	final void setDefaultTransition(PredefinedTransitions defaultTransition) {
		this.defaultTransition = defaultTransition;
	}

	final void setDefaultTransitionOrientation(int defaultTransitionOrientation) {
		this.defaultTransitionOrientation = defaultTransitionOrientation;
	}

	final void setTransitionDuration(int transitionDuration) {
		this.transitionDuration = transitionDuration;
	}

	private void assignRootController() {
		if (rootLayoutId == -1 && rootControllerType == null)
			return;

		StackLayerBuilder layerBuilder = createLayerBuilder();
		if (rootLayoutId != -1)
			layerBuilder.setLayoutId(rootLayoutId);

		layerBuilder.setRefKey(rootTag);

		if (rootControllerType != null)
			layerBuilder.setControllerType(rootControllerType);

		layerBuilder.setSaveState(rootSaveState);
		layerBuilder.fromXml = true;
		withRoot = true;
		layerBuilder.build();
	}

	@Override
	protected View createCustomView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		//		this.containerLayout = new RelativeLayout(parent.getContext());
		//		parent.addView(containerLayout);
		return this.containerLayout = (RelativeLayout) parent;
	}

	@Override
	protected void onCreate() {
		inflater = LayoutInflater.from(getActivity());
	}

	@Override
	public void handleAttributes(Context context, AttributeSet attrs) {
		super.handleAttributes(context, attrs);
		assignRootController();

		// If the developer didn't specify a root layer in the xml
		StackLayer topLayer = getTopLayer();
		if (topLayer == null)
			return;

		// If the developer used a root layer with a layoutId and no controller type
		CyborgController controller = topLayer.controller;
		if (controller == null)
			return;

		controller.handleAttributes(context, attrs);
	}

	public abstract class StackLayer {

		private StackTransitionAnimator[] stackTransitionAnimator;

		protected Processor<?> processor;

		protected String refKey;

		protected CyborgController controller;

		protected View rootView;

		private Bundle stateBundle = new Bundle();

		private int duration = transitionDuration;

		private boolean saveState = true;

		private boolean keepBackground;

		protected boolean keepInStack = true;

		private Interpolator interpolator;

		private StackLayer() {
			if (defaultTransition != null) {
				PredefinedStackTransitionAnimator transitionAnimator = new PredefinedStackTransitionAnimator(getActivity(), defaultTransition, defaultTransitionOrientation);
				this.stackTransitionAnimator = new StackTransitionAnimator[]{transitionAnimator};
			}
		}

		public abstract StackLayer setControllerType(Class<? extends CyborgController> controllerType);

		public abstract StackLayer setLayoutId(int layoutId);

		// TODO need to find a way to enable two transition simultaneously, e.g. Fade and Cube
		public final StackLayer setStackTransitionAnimators(StackTransitionAnimator... stackTransitionAnimators) {
			this.stackTransitionAnimator = stackTransitionAnimators;
			return this;
		}

		public final StackLayer setAnimationInterpolator(Interpolator interpolator) {
			this.interpolator = interpolator;
			return this;
		}

		public final StackLayer setKeepInStack(boolean keepInStack) {
			this.keepInStack = keepInStack;
			return this;
		}

		public final StackLayer setRefKey(String refKey) {
			this.refKey = refKey;
			return this;
		}

		public final StackLayer setDuration(int duration) {
			this.duration = duration;
			return this;
		}

		public final StackLayer setSaveState(boolean saveState) {
			this.saveState = saveState;
			return this;
		}

		public final StackLayer setKeepBackground(boolean keepBackground) {
			this.keepBackground = keepBackground;
			return this;
		}

		public final StackLayer setProcessor(Processor<?> processor) {
			this.processor = processor;
			return this;
		}

		protected abstract void resume();

		protected abstract void create();

		public abstract void append();

		public abstract void build();

		void restoreState() {
			if (controller == null)
				return;

			if (!saveState)
				return;

			controller.onRestoreInstanceState(stateBundle);
		}

		void detachView() {
			logWarning("Removing view: " + this + " " + (rootView == null ? "--- NULL" : ""));
			getFrameRootView().removeView(rootView);
			printStateTags();

			rootView = null;
			if (controller == null)
				return;

			controller.dispatchLifeCycleEvent(LifeCycleState.OnPause);
			controller.dispatchLifeCycleEvent(LifeCycleState.OnDestroy);
			removeNestedController(controller);
			controller = null;
		}

		void saveState() {
			if (!saveState)
				return;

			stateBundle.clear();
			if (controller == null)
				return;

			controller.onSaveInstanceState(stateBundle);
		}

		void preDestroy() {}

		public View getRootView() {
			return rootView;
		}

		public int getDuration() {
			return duration;
		}

		public boolean isSaveState() {
			return saveState;
		}

		public CyborgController getController() {
			return controller;
		}

		private void onAnimatedIn() {
			if (controller == null)
				return;

			controller._onAnimatedIn();
		}

		@Override
		public String toString() {
			return refKey;
		}
	}

	private void printStateTags() {
		if (!DebugStack)
			return;

		int childCount = getFrameRootView().getChildCount();
		String[] views = new String[childCount];
		for (int i = 0; i < childCount; i++) {
			views[i] = ((CyborgController) getFrameRootView().getChildAt(i).getTag()).getStateTag();
		}
		logWarning("Tags: " + Arrays.toString(views));
	}

	public class StackLayerBuilder
			extends StackLayer {

		private Class<? extends CyborgController> controllerType;

		private int layoutId = -1;

		private boolean fromXml;

		@Override
		protected void create() {
			if (rootView != null)
				return;

			logWarning("Create: " + this);
			if (layoutId != -1)
				createLayoutLayer();
			else if (controllerType != null)
				createControllerLayer();
			else
				throw new BadImplementationException("Stack Layer was not configured properly");

			printStateTags();
		}

		private void createControllerLayer() {
			controller = ReflectiveTools.newInstance(controllerType);
			if (refKey == null)
				refKey = controller.getClass().getSimpleName();

			//			if(CyborgBuilder.getInEditMode())
			CyborgActivityBridge activityBridge = getActivity().getBridge();
			controller.setActivityBridge(activityBridge);
			controller.setKeepInStack(keepInStack);

			controller.setStateTag(refKey);
			controller._createView(inflater, getFrameRootView(), false);
			rootView = controller.getRootView();

			// Always add it as the lowest item to avoid animation hiccups, where the popping a layer actually places its view on top instead of under... is this correct? the logic sure seems reliable, but are there any other cases this might not work?
			getFrameRootView().addView(rootView);

			controller.extractMembersImpl();
			// xml attribute for root controller are handled in the handleAttributes method

			controller.dispatchLifeCycleEvent(LifeCycleState.OnCreate);

			// ARE THESE TWO ACTIONS DEPEND ON ONE ANOTHER, IN ANY CONFIGURATION???
			if (processor != null)
				postCreateProcessController(processor, controller);

			restoreState();

			// JUST FOR THE RECORD... I HATE THIS CONDITION>> ()
			if (!fromXml)
				controller.onReady();
			// --------------------------------------------------------------------

			resume();
			CyborgStackController.this.addNestedController(controller);
		}

		@Override
		protected void resume() {
			if (getState() == LifeCycleState.OnResume)
				controller.dispatchLifeCycleEvent(LifeCycleState.OnResume);
		}

		private void createLayoutLayer() {
			try {
				rootView = inflater.inflate(this.layoutId, getFrameRootView(), false);
			} catch (Exception e) {
				Throwable t = getCauseError(e);
				if (t instanceof RuntimeException)
					throw (RuntimeException) t;
				else
					throw new RuntimeException("", t);
			}
			getFrameRootView().addView(rootView);

			if (rootView instanceof CyborgView) {
				controller = ((CyborgView) rootView).getController();
				controller.setStateTag(refKey);

				// the dispatchLifeCycleEvent for the onCreate is called from CyborgView
			}
		}

		private Throwable getCauseError(Throwable e) {
			if (e == null)
				return null;

			if (e instanceof InflateException || e instanceof InvocationTargetException)
				return getCauseError(e.getCause());

			return e;
		}

		public StackLayerBuilder setControllerType(Class<? extends CyborgController> controllerType) {
			if (layoutId != -1)
				throw new BadImplementationException("Already set layoutId, cannot also set controllerType");

			this.controllerType = controllerType;
			if (refKey == null)
				refKey = controllerType.getSimpleName();

			return this;
		}

		public StackLayerBuilder setLayoutId(int layoutId) {
			if (controllerType != null)
				throw new BadImplementationException("Already set controller type, cannot also set layoutId");

			this.layoutId = layoutId;
			return this;
		}

		public final void append() {
			layersStack.add(this);
		}

		public final void build() {
			long started = System.currentTimeMillis();

			if (refKey == null)
				throw new ImplementationMissingException("MUST specify a refKey when using a layoutId");

			push(this);

			if (DebugPerformance)
				logDebug("Open Controller (" + controllerType + "): " + (System.currentTimeMillis() - started) + "ms");
		}
	}

	public final StackLayerBuilder createLayerBuilder() {
		return new StackLayerBuilder();
	}

	private RelativeLayout getFrameRootView() {
		return containerLayout;
	}

	public void popUntil(String refKey) {
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	private void push(final StackLayer targetLayerToBeAdded) {
		if (animatingTransition) {
			if (DebugStack)
				logInfo("TRANSITION ANIMATION IN PROGRESS!!!");
		}

		// promote this stack in the hierarchy, so it will receive the events first.
		// SHOULD I MANAGE A STACK OF STACKS, THAT WILL DEFINE THE ORDER OF WHICH EVENTS ARE RECEIVED, ACCORDING TO PUSH RECENCY...?
		//		activityBridge.removeController(this);
		//		activityBridge.addController(this);

		final StackLayer originLayerToBeDisposed = targetLayerToBeAdded.keepBackground ? null : getTopLayer();
		if (originLayerToBeDisposed != null)
			originLayerToBeDisposed.preDestroy();

		layersStack.add(targetLayerToBeAdded);
		targetLayerToBeAdded.create();

		final StackTransitionAnimator[] transitionAnimators = targetLayerToBeAdded.stackTransitionAnimator;

		// we must call clear animation to ensure onAnimationEnd is called.
		targetLayerToBeAdded.getRootView().clearAnimation();

		/* so after long trials, this seems to be the best behavior, if we invoke a second transition pop or push while another
		 * is in progress, and we want the events not to collide with regards to the state of the stack, we need to make sure that
		 * the stack is updated as soon as the interaction begins.
		 */
		if (originLayerToBeDisposed != null) {

			// we must call clear animation to ensure onAnimationEnd is called.
			originLayerToBeDisposed.getRootView().clearAnimation();

			// remove the layer from the stack if at the end of this transition it should not be there.
			if (originLayerToBeDisposed.controller != null && !originLayerToBeDisposed.controller.keepInStack)
				layersStack.remove(originLayerToBeDisposed);
		}

		if (transitionAnimators == null) {
			// if there is no animation transitioning between the two layer, just dispose the older layer
			disposeLayer(originLayerToBeDisposed);
			return;
		}

		if (DebugStack)
			logInfo("push: " + originLayerToBeDisposed + " => " + targetLayerToBeAdded);

		final View targetView = targetLayerToBeAdded.getRootView();
		final ViewTreeObserver treeObserver = targetView.getViewTreeObserver();
		treeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver treeObserver = targetView.getViewTreeObserver();
				if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
					treeObserver.removeOnGlobalLayoutListener(this);
				} else {
					treeObserver.removeGlobalOnLayoutListener(this);
				}
				setInAnimationState(true);

				AnimationListenerImpl listener = new AnimationListenerImpl() {
					@Override
					public void onAnimationEnd(Animation animation) {
						if (originLayerToBeDisposed != null) {
							if (DebugStack)
								logInfo("disposing-push: " + originLayerToBeDisposed);

							disposeLayer(originLayerToBeDisposed);
						}

						setInAnimationState(false);
						targetLayerToBeAdded.onAnimatedIn();
					}
				};

				for (StackTransitionAnimator animator : transitionAnimators) {
					Interpolator interpolator = targetLayerToBeAdded.interpolator;
					if (interpolator != null)
						animator.setInterpolator(interpolator);

					// All Animations are performed together, the listener MUST be called only once
					animator.animateIn(originLayerToBeDisposed, targetLayerToBeAdded, targetLayerToBeAdded.duration,
							animator == transitionAnimators[transitionAnimators.length - 1] ? listener : null);
				}
			}
		});
	}

	private void setInAnimationState(boolean animating) {
		animatingTransition = animating;
	}

	@SuppressWarnings("unchecked")
	private <Type> void postCreateProcessController(Processor<Type> processor, CyborgController controller) {
		processor.process((Type) controller);
	}

	public void setAsPriority() {
		activityBridge.setPriorityStack(this);
	}

	public boolean popLast() {
		if (animatingTransition) {
			logDebug("TRANSITION ANIMATION IN PROGRESS!!!");
		}

		final StackLayer targetLayerToBeRemove = getAndRemoveTopLayer();
		if (targetLayerToBeRemove == null)
			return false;

		final StackLayer originLayerToBeRestored = getTopLayer();

		if (originLayerToBeRestored != null) {
			if (targetLayerToBeRemove.keepBackground)
				originLayerToBeRestored.resume();
			else
				originLayerToBeRestored.create();
		}

		final StackTransitionAnimator[] transitionAnimators = targetLayerToBeRemove.stackTransitionAnimator;
		if (transitionAnimators == null) {
			targetLayerToBeRemove.detachView();
			return true;
		}

		final View viewToBeRestored;
		if (originLayerToBeRestored != null) {
			viewToBeRestored = originLayerToBeRestored.getRootView();
		} else
			viewToBeRestored = null;

		final int duration;
		if (originLayerToBeRestored != null)
			duration = targetLayerToBeRemove.duration;
		else
			duration = transitionDuration;

		targetLayerToBeRemove.getRootView().clearAnimation();
		if (originLayerToBeRestored != null)
			originLayerToBeRestored.getRootView().clearAnimation();

		if (DebugStack)
			logDebug("pop: " + targetLayerToBeRemove + " => " + originLayerToBeRestored);

		final Runnable startAnimation = new Runnable() {
			@Override
			public void run() {
				setInAnimationState(true);
				AnimationListenerImpl listener = new AnimationListenerImpl() {
					@Override
					public void onAnimationEnd(Animation animation) {
						if (DebugStack)
							logDebug("disposing-pop: " + targetLayerToBeRemove);
						disposeLayer(targetLayerToBeRemove);

						if (originLayerToBeRestored != null)
							originLayerToBeRestored.onAnimatedIn();

						setInAnimationState(false);
					}
				};

				for (StackTransitionAnimator animator : transitionAnimators) {
					Interpolator interpolator = targetLayerToBeRemove.interpolator;
					if (interpolator != null)
						animator.setInterpolator(interpolator);

					// All Animations are performed together, the listener MUST be called only once
					animator.animateOut(originLayerToBeRestored, targetLayerToBeRemove, duration,
							animator == transitionAnimators[transitionAnimators.length - 1] ? listener : null);
				}
			}
		};

		if (viewToBeRestored == null) {
			startAnimation.run();
			return true;
		}

		final ViewTreeObserver treeObserver = viewToBeRestored.getViewTreeObserver();
		treeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver treeObserver = viewToBeRestored.getViewTreeObserver();
				if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
					treeObserver.removeOnGlobalLayoutListener(this);
				} else {
					treeObserver.removeGlobalOnLayoutListener(this);
				}
				startAnimation.run();
			}
		});
		return true;
	}

	private StackLayer getTopLayer() {
		return getTopLayer(false);
	}

	private StackLayer getAndRemoveTopLayer() {
		return getTopLayer(true);
	}

	private StackLayer getTopLayer(boolean remove) {
		StackLayer layer = layersStack.size() == (withRoot && remove ? 1 : 0) ? null : layersStack.get(layersStack.size() - 1);

		if (remove)
			layersStack.remove(layer);

		return layer;
	}

	private void disposeLayer(StackLayer layerToBeDisposed) {
		if (layerToBeDisposed == null)
			return;

		/* in the case that we push two screens and press back..
		 * the layer we pop will be disposed when the onAnimationEnd of the last push would be called
		 */
		if (getTopLayer() == layerToBeDisposed)
			return;

		layerToBeDisposed.saveState();
		layerToBeDisposed.detachView();
	}

	public void clear() {
		StackLayer topLayer;
		while ((topLayer = getTopLayer(true)) != null) {
			topLayer.detachView();
		}
	}

	@Override
	public boolean onBackPressed() {
		StackLayer topLayer = getTopLayer();
		if (topLayer != null && topLayer.controller != null)
			if (topLayer.controller.onBackPressed())
				return true;

		if (!popOnBackPress || !focused)
			return super.onBackPressed();

		return popLast();
	}

	public StackLayer[] getStackListTags() {
		return layersStack.toArray(new StackLayer[layersStack.size()]);
	}
}
