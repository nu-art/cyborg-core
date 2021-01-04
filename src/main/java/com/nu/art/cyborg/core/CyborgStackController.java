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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.exceptions.runtime.MUST_NeverHappenException;
import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.core.utils.DebugFlags;
import com.nu.art.core.utils.DebugFlags.DebugFlag;
import com.nu.art.cyborg.R;
import com.nu.art.cyborg.common.implementors.AnimatorListenerImpl;
import com.nu.art.cyborg.core.consts.LifecycleState;
import com.nu.art.cyborg.core.modules.ThreadsModule;
import com.nu.art.cyborg.core.stackTransitions.StackTransitions;
import com.nu.art.cyborg.core.stackTransitions.Transition;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.cyborg.ui.animations.SimpleAnimator;
import com.nu.art.cyborg.ui.animations.SimpleAnimator.AnimatorProgressListener;
import com.nu.art.reflection.annotations.ReflectiveInitialization;
import com.nu.art.reflection.tools.ReflectiveTools;
import com.nu.art.storage.PreferencesModule.JsonSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.nu.art.cyborg.core.abs._DebugFlags.Debug_Performance;
import static com.nu.art.cyborg.core.consts.LifecycleState.OnPause;
import static com.nu.art.cyborg.core.consts.LifecycleState.OnResume;

/**
 * Created by TacB0sS on 25-Jun 2015.
 */
@SuppressWarnings( {
	                   "WeakerAccess",
	                   "unused"
                   })
public class CyborgStackController
	extends CyborgController {

	public static final DebugFlag DebugFlag = DebugFlags.createFlag(CyborgStackController.class);
	private static final HashMap<String, Transition> transitions = new HashMap<>();

	static {
		for (StackTransitions value : StackTransitions.values()) {
			addTransition(value.name(), value, true);
		}
	}

	private static Transition getTransition(String transition) {
		return transitions.get(transition);
	}

	public static void addTransition(String key, Transition transition, boolean override) {
		if (transitions.put(key, transition) != null && !override)
			throw ExceptionGenerator.tryingToOverrideExistingStackTransitionForKey(key);
	}

	private LayoutInflater inflater;

	private ArrayList<StackLayerBuilder> layersStack = new ArrayList<>();

	private RelativeLayout containerLayout;

	private final StackConfig config = new StackConfig();

	private boolean animatingTransition;

	private boolean focused = true;

	private StackLayerBuilder rootLayerBuilder;
	private boolean processedRoot;

	private StatefulAnimatorProgressor previousListener;

	protected CyborgStackController() {
		super(-1);
	}

	public StackConfig getConfig() {
		return config;
	}

	@Override
	final ViewGroup getRootViewImpl() {
		return containerLayout;
	}

	protected StackLayerBuilder getRootLayerBuilder() {
		if (rootLayerBuilder == null)
			rootLayerBuilder = createLayerBuilder();

		return rootLayerBuilder;
	}

	private boolean hasRoot() {
		return rootLayerBuilder != null && rootLayerBuilder.controllerType != null;
	}

	private void assignRootController() {
		if (!hasRoot())
			return;

		if (processedRoot)
			return;

		processedRoot = true;
		rootLayerBuilder.push();
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
	protected void onResume() {
		assignRootController();
	}

	@Override
	protected void onSaveComplexObjectState(Bundle outState) {
		StackLayerBuilder[] visibleLayers = getVisibleLayers();
		for (StackLayerBuilder visibleLayer : visibleLayers) {
			visibleLayer.saveState();
		}

		LayerData[] layers = ArrayTools.map(LayerData.class, new Function<StackLayerBuilder, LayerData>() {
			@Override
			public LayerData map(StackLayerBuilder stackLayerBuilders) {
				return stackLayerBuilders.serializableData;
			}
		}, layersStack);

		for (LayerData layer : layers) {
			outState.putBundle("layer-" + layer.stateTag, layer.stateBundle);
		}

		outState.putString("layers", JsonSerializer.gson.toJson(layers));
	}

	@Override
	protected void onRestoreComplexObjectState(Bundle inState) {
		String layersAsString = inState.getString("layers", "[]");
		if (layersAsString == null)
			return;

		LayerData[] layers = JsonSerializer.gson.fromJson(layersAsString, LayerData[].class);
		if (layers.length == 0) {
			assignRootController();
			return;
		}

		processedRoot = true;
		for (LayerData layer : layers) {
			layer.stateBundle = inState.getBundle("layer-" + layer.stateTag);
		}

		ArrayList<LayerData> visibleLayers = new ArrayList<>();
		for (int i = layers.length - 1; i >= 0; i--) {
			visibleLayers.add(layers[i]);
			if (!layers[i].keepBackground)
				break;
		}

		for (LayerData layer : layers) {
			StackLayerBuilder stackLayerBuilder = new StackLayerBuilder(layer);
			if (!visibleLayers.contains(layer)) {
				stackLayerBuilder.append();
			} else {
				stackLayerBuilder.push();
			}
		}
	}

	private class StatefulAnimatorProgressor
		implements AnimatorProgressListener {

		private SimpleAnimator animator;
		private AnimatorListenerImpl listener;
		private final Transition[] transitionAnimators;
		private final boolean in;
		private final StackLayerBuilder toLayer;
		private final StackLayerBuilder fromLayer;

		public StatefulAnimatorProgressor(SimpleAnimator animator,
		                                  AnimatorListenerImpl listener,
		                                  Transition[] transitionAnimators,
		                                  boolean in,
		                                  StackLayerBuilder toLayer,
		                                  StackLayerBuilder fromLayer) {
			this.animator = animator;
			this.listener = listener;
			this.transitionAnimators = transitionAnimators;
			this.in = in;
			this.toLayer = toLayer;
			this.fromLayer = fromLayer;
		}

		@Override
		public void onAnimationProgressed(float currentProgress) {
			for (Transition transition : transitionAnimators) {
				animateLayer(transition, currentProgress, toLayer, in);
				animateLayer(transition, currentProgress, fromLayer, !in);
			}
		}

		private void animateLayer(Transition transition, float progress, StackLayerBuilder layer, boolean in) {
			if (layer == null)
				return;

			if (layer.getRootView() == null)
				return;

			if (layer.getRootView().getParent() == null)
				return;

			transition.animate(layer, progress, in);
		}
	}

	private static class LayerData {

		protected StackTransitions[] transitions = {};
		protected String controllerClassAsString;
		protected String stateTag;
		protected int transitionDuration = -1;
		protected boolean keepBackground;
		private transient Bundle stateBundle = new Bundle();

		public void addTransition(StackTransitions transition) {
			transitions = ArrayTools.appendElement(transitions, transition);
		}
	}

	@SuppressWarnings( {
		                   "WeakerAccess",
		                   "UnusedReturnValue"
		                   ,
		                   "unused"
	                   })
	public class StackLayerBuilder {

		private transient Transition[] transitions;
		private transient Processor<?> processor;
		private transient Interpolator interpolator;
		private transient Class<? extends CyborgController> controllerType;

		private transient boolean toBeDisposed;
		private transient boolean keepInStack = true;
		private transient CyborgController controller;
		private LayerData serializableData;

		private StackLayerBuilder() {
			serializableData = new LayerData();
		}

		@SuppressWarnings("unchecked")
		private StackLayerBuilder(LayerData deserializeData) {
			serializableData = deserializeData;
			this.transitions = deserializeData.transitions;
			try {
				this.controllerType = (Class<? extends CyborgController>) Class.forName(deserializeData.controllerClassAsString);
			} catch (ClassNotFoundException e) {
				throw new MUST_NeverHappenException("error while restoring state with controller: " + deserializeData.controllerClassAsString, e);
			}
		}

		public final StackLayerBuilder setTransitions(Transition... transitions) {
			this.transitions = transitions;
			for (Transition transition : transitions) {
				if (transition instanceof StackTransitions)
					serializableData.addTransition((StackTransitions) transition);
			}
			return this;
		}

		protected void setToBeDisposed(boolean toBeDisposed) {
			this.toBeDisposed = toBeDisposed;
		}

		public StackLayerBuilder setControllerType(Class<? extends CyborgController> controllerType) {
			this.controllerType = controllerType;
			if (getStateTag() == null)
				setStateTag(controllerType.getSimpleName());

			serializableData.controllerClassAsString = controllerType.getName();
			return this;
		}

		public final StackLayerBuilder setAnimationInterpolator(Interpolator interpolator) {
			this.interpolator = interpolator;
			// TBD save into serializableData?
			return this;
		}

		public final StackLayerBuilder setKeepInStack(boolean keepInStack) {
			this.keepInStack = keepInStack;
			return this;
		}

		public final StackLayerBuilder setStateTag(String stateTag) {
			if (stateTag == null && this.serializableData.stateTag != null)
				return this;

			this.serializableData.stateTag = stateTag;
			return this;
		}

		public final StackLayerBuilder setTransitionDuration(int transitionDuration) {
			this.serializableData.transitionDuration = transitionDuration;
			return this;
		}

		/**
		 * Use {@link #setTransitionDuration(int)}
		 */
		@Deprecated
		public final StackLayerBuilder setDuration(int duration) {
			return setTransitionDuration(duration);
		}

		public final StackLayerBuilder setKeepBackground(boolean keepBackground) {
			this.serializableData.keepBackground = keepBackground;
			return this;
		}

		public final StackLayerBuilder setProcessor(Processor<?> processor) {
			this.processor = processor;
			return this;
		}

		protected void pause() {
			if (controller == null) {
				if (DebugFlag.isEnabled())
					logWarning("cannot pause... no controller for layer: " + this);
				return;
			}

			if (isState(OnResume))
				controller.dispatchLifeCycleEvent(OnPause);
		}

		protected void resume() {
			if (controller == null) {
				if (DebugFlag.isEnabled())
					logWarning("cannot resume... no controller for layer: " + this);
				return;
			}

			if (isState(OnResume))
				controller.dispatchLifeCycleEvent(OnResume);
		}

		protected void create() {
			if (DebugFlag.isEnabled())
				logWarning("Create: " + this);

			if (controllerType == null)
				throw ExceptionGenerator.stackLayerHasNoControllerType();

			controller = ReflectiveTools.newInstance(controllerType);
			//			if(CyborgBuilder.getInEditMode())
			CyborgActivityBridge activityBridge = getActivity().getBridge();
			controller.setActivityBridge(activityBridge);
			controller.setKeepInStack(keepInStack);

			controller.setStateTag(getStateTag());
			controller._createView(inflater, getRootViewImpl(), false);

			// Always add it as the lowest item to avoid animation hiccups, where the popping a layer actually places its view on top instead of under... is this correct? the logic sure seems reliable, but are there any other cases this might not work?
			getRootViewImpl().addView(controller.getRootView());

			controller.extractMembersImpl();
			// xml attribute for root controller are handled in the handleAttributes method

			controller.dispatchLifeCycleEvent(LifecycleState.OnCreate);

			// ARE THESE TWO ACTIONS DEPEND ON ONE ANOTHER, IN ANY CONFIGURATION???
			if (processor != null)
				postCreateProcessController(processor, controller);

			restoreState();

			// --------------------------------------------------------------------

			resume();
			CyborgStackController.this.addNestedController(controller);
		}

		/**
		 * Use this API to add potential screen to your stack to allow your user to navigate back.
		 * for example when a user presses on a notification.. in most cases you'd like to take them deep into the application,
		 * this API allows you to defined the breadcrumbs of the back navigation.
		 */
		public final void append() {
			addStackLayer(this);
		}

		/**
		 * Use {@link #push()} instead
		 */
		@Deprecated
		public final void build() {
			push();
		}

		public final void push() {
			long started = System.currentTimeMillis();

			if (getStateTag() == null)
				throw new ImplementationMissingException("MUST specify a stateTag when using a layoutId");

			CyborgStackController.this.push(this);

			if (Debug_Performance.isEnabled())
				logDebug("Open Controller (" + controllerType + "): " + (System.currentTimeMillis() - started) + "ms");
		}

		void restoreState() {
			if (controller == null)
				return;

			controller.onRestoreInstanceState(serializableData.stateBundle);
		}

		void detachView() {
			if (DebugFlag.isEnabled())
				logWarning("detachView: " + this);

			if (controller == null) {
				logWarning("cannot detach view... no controller for layer: " + this);
				return;
			}

			getRootViewImpl().removeView(controller.getRootViewImpl());
			printStateTags();

			if (controller == null)
				return;

			controller.dispatchLifeCycleEvent(OnPause);
			controller.dispatchLifeCycleEvent(LifecycleState.OnDestroy);
			removeNestedController(controller);
			controller = null;
		}

		void saveState() {
			serializableData.stateBundle.clear();
			if (controller == null)
				return;

			controller.onSaveInstanceState(serializableData.stateBundle);
		}

		public View getRootView() {
			return controller == null ? null : controller.getRootView();
		}

		public int getDuration() {
			return this.serializableData.transitionDuration;
		}

		public CyborgController getController() {
			return controller;
		}

		protected void onAnimatedIn() {
			if (controller == null)
				return;

			controller._onAnimatedIn();
		}

		@Override
		public String toString() {
			return getStateTag();
		}

		public String getStateTag() {
			return serializableData.stateTag;
		}

		public boolean isKeepBackground() {
			return serializableData.keepBackground;
		}
	}

	private void printStateTags() {
		if (!DebugFlag.isEnabled())
			return;

		logWarning(" Views Tags: " + Arrays.toString(getViewsTags()));
		logWarning("Layers Tags: " + Arrays.toString(getStackLayersTags()));
	}

	public final String[] getViewsTags() {
		int childCount = getRootViewImpl().getChildCount();
		String[] viewsTags = new String[childCount];
		for (int i = 0; i < childCount; i++) {
			View childAt = getRootViewImpl().getChildAt(i);
			CyborgController controller = (CyborgController) childAt.getTag();
			if (controller != null)
				viewsTags[i] = controller.getStateTag();
			else
				viewsTags[i] = childAt.getId() + "-" + childAt.getClass().getSimpleName();
		}
		return viewsTags;
	}

	public final String[] getStackLayersTags() {
		return ArrayTools.map(String.class, new Function<StackLayerBuilder, String>() {

			@Override
			public String map(StackLayerBuilder stackLayerBuilder) {
				return stackLayerBuilder.getStateTag();
			}
		}, ArrayTools.asArray(layersStack, StackLayerBuilder.class));
	}

	private StackLayerBuilder getTopLayer() {
		return getTopLayer(false);
	}

	protected StackLayerBuilder[] getVisibleLayers() {
		ArrayList<StackLayerBuilder> visibleLayers = new ArrayList<>();
		StackLayerBuilder topLayer;
		while ((topLayer = getTopLayer(visibleLayers.size())) != null) {
			visibleLayers.add(0, topLayer);
			if (!topLayer.isKeepBackground())
				break;
		}

		return ArrayTools.asArray(visibleLayers, StackLayerBuilder.class);
	}

	private StackLayerBuilder getAndRemoveTopLayer() {
		return getTopLayer(true);
	}

	private StackLayerBuilder getTopLayer(int offset) {
		return getTopLayer(offset, false);
	}

	protected StackLayerBuilder getTopLayer(boolean remove) {
		return getTopLayer(0, remove);
	}

	private StackLayerBuilder getTopLayer(int offset, boolean remove) {
		int size = layersStack.size() - offset;
		int i = hasRoot() && remove ? 1 : 0;
		int index = size == i ? -1 : size - 1;
		if (index == -1)
			return null;

		StackLayerBuilder layer = layersStack.get(index);

		if (remove)
			removeStackLayer(layer);

		return layer;
	}

	protected void addStackLayer(StackLayerBuilder stackLayerBuilder) {
		layersStack.add(stackLayerBuilder);
	}

	private void removeStackLayer(StackLayerBuilder stackLayerBuilder) {
		layersStack.remove(stackLayerBuilder);
	}

	public final StackLayerBuilder createLayerBuilder() {
		return new StackLayerBuilder();
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	// promote this stack in the hierarchy, so it will receive the events first.
	// SHOULD I MANAGE A STACK OF STACKS, THAT WILL DEFINE THE ORDER OF WHICH EVENTS ARE RECEIVED, ACCORDING TO PUSH RECENCY...?
	//		activityBridge.removeController(this);
	//		activityBridge.addController(this);

	/**
	 *
	 */

	public void clear() {
		ThreadsModule.assertMainThread();
		if (!hasRoot())
			clearWithoutRoot();
		else
			clearWithRoot();
	}

	public void removeAllLowerLayers() {
		if (layersStack.size() == 1 && getRootViewImpl().getChildCount() == 0)
			return;
		while (layersStack.size() > 1) {
			StackLayerBuilder stackLayerBuilder = layersStack.get(0);
			logError("Removing controller: " + (stackLayerBuilder.controller != null ? stackLayerBuilder.controller.getClass() : null));
			stackLayerBuilder.setKeepInStack(false);
			stackLayerBuilder.setToBeDisposed(true);
			disposeLayer(stackLayerBuilder, false);
			stackLayerBuilder.detachView();
			layersStack.remove(0);
		}
		int childCount = getRootViewImpl().getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childAt = getRootViewImpl().getChildAt(i);
			if (childAt == null)
				continue;
			CyborgController controller = (CyborgController) childAt.getTag();
			if (controller == null)
				continue;
			for (StackLayerBuilder builder : layersStack)
				if (builder.controller != null && builder.controller != controller) {
					getRootViewImpl().removeView(childAt);
				}
		}
	}

	private void clearWithRoot() {
		if (layersStack.size() == 1)
			return;

		for (StackLayerBuilder layerBuilder : layersStack) {
			layerBuilder.setKeepInStack(false);
		}

		rootLayerBuilder.setKeepInStack(true);
		rootLayerBuilder.push();
	}

	private void clearWithoutRoot() {
		StackLayerBuilder topLayer;
		while ((topLayer = getTopLayer(true)) != null) {
			topLayer.detachView();
		}

		layersStack.clear();
	}

	public final void popLast() {
		ThreadsModule.assertMainThread();
		onBackPressed();
	}

	public void popUntil(Class<? extends CyborgController> controllerType) {
		popUntil(controllerType.getSimpleName());
	}

	public void popUntil(String tag) {
		ThreadsModule.assertMainThread();

		ArrayList<StackLayerBuilder> topLayers = new ArrayList<>();
		for (int i = layersStack.size() - 1; i >= 0; i--) {
			StackLayerBuilder layer = layersStack.get(i);
			if (layer.getStateTag().equals(tag))
				break;

			topLayers.add(layer);
		}

		for (int i = 0; i < topLayers.size(); i++) {
			StackLayerBuilder topLayer = topLayers.get(i);
			if (!topLayer.isKeepBackground())
				break;

			topLayers.remove(topLayer);
			i--;
		}

		List<StackLayerBuilder> visibleLayers = Arrays.asList(getVisibleLayers());
		for (StackLayerBuilder topLayer : topLayers) {
			if (visibleLayers.contains(topLayer))
				continue;

			layersStack.remove(topLayer);
		}

		popLast();
	}

	protected void push(final StackLayerBuilder targetLayerToBeAdded) {
		ThreadsModule.assertMainThread();
		final StackLayerBuilder[] visibleLayers = getVisibleLayers();

		for (StackLayerBuilder layer : visibleLayers) {
			layer.pause();
		}

		final StackLayerBuilder originLayerToBeDisposed =
			targetLayerToBeAdded.isKeepBackground() || visibleLayers.length == 0 ? null : visibleLayers[visibleLayers.length - 1];

		final Runnable animationEnded = new Runnable() {
			@Override
			public void run() {
				if (!targetLayerToBeAdded.isKeepBackground()) {
					for (StackLayerBuilder layer : visibleLayers) {
						// remove the layer from the stack if at the end of this transition it should not be there.
						layer.toBeDisposed = true;
					}
				}

				alignChildViewsToStack();
				targetLayerToBeAdded.onAnimatedIn();
			}
		};

		///

		addStackLayer(targetLayerToBeAdded);
		targetLayerToBeAdded.create();

		if (DebugFlag.isEnabled())
			logInfo("push: " + Arrays.toString(visibleLayers) + " => " + targetLayerToBeAdded);

		animate(true, true, originLayerToBeDisposed, targetLayerToBeAdded, animationEnded);
	}

	private void popLast(final StackLayerBuilder targetLayerToBeRemove) {
		StackLayerBuilder[] visibleLayers = getVisibleLayers();
		boolean waitForLayoutChanges = true;

		for (StackLayerBuilder layer : visibleLayers) {
			if (targetLayerToBeRemove.isKeepBackground())
				layer.resume();
			else if (layer.controller == null)
				layer.create();
			else {
				waitForLayoutChanges = false;
				layer.resume();
			}
		}

		// background is already visible do not animate it
		final StackLayerBuilder originLayerToBeRestored = targetLayerToBeRemove.isKeepBackground() ? null : getTopLayer();

		final Runnable animationEnded = new Runnable() {
			@Override
			public void run() {
				disposeLayer(targetLayerToBeRemove, false);

				if (originLayerToBeRestored != null)
					originLayerToBeRestored.onAnimatedIn();
			}
		};

		if (DebugFlag.isEnabled())
			logInfo("popping: " + targetLayerToBeRemove + " => " + Arrays.toString(visibleLayers));

		animate(false, waitForLayoutChanges, targetLayerToBeRemove, originLayerToBeRestored, animationEnded);
	}

	@SuppressLint("WrongConstant")
	protected void animate(final boolean in,
	                       final boolean waitForLayoutChanges,
	                       final StackLayerBuilder fromLayer,
	                       final StackLayerBuilder toLayer,
	                       final Runnable animationEnded) {

		final StackLayerBuilder animatingLayer = (in ? toLayer : fromLayer);

		if (animatingTransition && DebugFlag.isEnabled())
			logInfo("TRANSITION ANIMATION IN PROGRESS!!!");

		Transition[] transitions = animatingLayer.transitions;
		final Transition[] transitionAnimators = transitions == null || transitions.length == 0 ? config.transitions : transitions;

		final Interpolator interpolator = animatingLayer.interpolator;
		final int transitionDuration = animatingLayer.getDuration() > 0 ? animatingLayer.getDuration() : config.transitionDuration;

		final View toView = toLayer == null ? null : toLayer.getRootView();

		final int previousVisibility;
		if (toView == null)
			previousVisibility = -1;
		else {
			previousVisibility = toView.getVisibility();
			toView.setVisibility(View.INVISIBLE);
		}

		final Runnable animate = new Runnable() {
			StatefulAnimatorProgressor _listener;

			@Override
			public void run() {
				setInAnimationState(true);
				if (toView != null)
					toView.setVisibility(previousVisibility);

				AnimatorListenerImpl listener = new AnimatorListenerImpl() {
					@Override
					public void onAnimationEnd(Animator animator) {
						super.onAnimationEnd(animator);
						if (_listener != null && _listener.fromLayer != null && DebugFlag.isEnabled())
							logInfo("disposing: " + _listener.fromLayer);

						if (previousListener == _listener)
							previousListener = null;

						animationEnded.run();
						setInAnimationState(false);
					}
				};

				if (transitionAnimators == null) {
					// if there is no animation transitioning between the two layer, just dispose the older layer
					listener.onAnimationEnd(null);
					return;
				}

				if (previousListener != null) {
					if (previousListener.in && in) {
						previousListener.animator.setListener((AnimatorListener) null);
					}

					if (previousListener.in && !in) {
						previousListener.animator.setListener((AnimatorListener) null);
						previousListener.animator.setListener(listener);
						previousListener.animator.animateTo(0);
						return;
					}
				}

				if (DebugFlag.isEnabled())
					logWarning("starting animation");
				SimpleAnimator animator = new SimpleAnimator();

				animator.init(in ? 0 : 1);
				animator.setDuration(transitionDuration);
				animator.setInterpolator(interpolator);
				animator.setListener(listener);
				animator.setListener(_listener = previousListener = new StatefulAnimatorProgressor(animator, listener, transitionAnimators, in, toLayer, fromLayer));
				animator.animateTo(in ? 1 : 0);
			}
		};

		// when animating out and when the stack is empty.. the animating toView would be null and we will not receive a layout change event
		// therefore we skip the listener and invoke the animate runnable directly
		if (!waitForLayoutChanges || toView == null) {
			postOnUI(animate);
			return;
		}

		final ViewTreeObserver treeObserver = toView.getViewTreeObserver();
		treeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			private boolean processed;

			@Override
			@SuppressLint("ObsoleteSdkInt")
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {
				ViewTreeObserver _treeObserver = treeObserver;

				if (!treeObserver.isAlive())
					_treeObserver = toView.getViewTreeObserver();

				if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
					_treeObserver.removeOnGlobalLayoutListener(this);
				} else {
					_treeObserver.removeGlobalOnLayoutListener(this);
				}
				if (processed)
					return;

				processed = true;
				postOnUI(animate);
			}
		});
	}

	private final ArrayList<StackLayerBuilder> toBeDisposed = new ArrayList<>();

	protected void alignChildViewsToStack() {
		for (StackLayerBuilder stackLayerBuilder : layersStack) {
			if (!stackLayerBuilder.toBeDisposed)
				continue;

			toBeDisposed.add(stackLayerBuilder);
		}

		boolean keepInStack;

		for (StackLayerBuilder stackLayerBuilder : toBeDisposed) {
			if (stackLayerBuilder.controller == null)
				// if there is no controller for this layer, take the boolean set in the layer
				keepInStack = stackLayerBuilder.keepInStack;
			else
				// in case there is a controller, use the boolean within that controller
				keepInStack = stackLayerBuilder.controller.keepInStack;

			disposeLayer(stackLayerBuilder, true);
			stackLayerBuilder.toBeDisposed = false;

			if (!keepInStack)
				layersStack.remove(stackLayerBuilder);
		}

		toBeDisposed.clear();
		printStateTags();
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

	private void disposeLayer(StackLayerBuilder layerToBeDisposed, boolean saveState) {
		if (layerToBeDisposed == null) {
			logError("Will not dispose - layer is null");
			return;
		}

		/* in the case that we push two screens and press back..
		 * the layer we pop will be disposed when the onAnimationEnd of the last push would be called
		 */
		if (getTopLayer() == layerToBeDisposed) {
			logError("Will not dispose: " + layerToBeDisposed);
			return;
		}

		if (saveState)
			layerToBeDisposed.saveState();

		layerToBeDisposed.detachView();
	}

	@Override
	public boolean onBackPressed() {
		StackLayerBuilder topLayer = getTopLayer();
		if (topLayer != null && topLayer.controller != null)
			if (topLayer.controller.onBackPressed())
				return true;

		if (!config.popOnBackPress || !focused)
			return super.onBackPressed();

		if (topLayer == null)
			return super.onBackPressed();

		topLayer = getAndRemoveTopLayer();
		if (topLayer != null) {
			popLast(topLayer);
			return true;
		}

		return super.onBackPressed();
	}

	/**
	 * Setting the xml attributes onto a {@link CyborgStackController} instance.
	 */
	@ReflectiveInitialization
	public static class CyborgStackSetter
		extends AttributesSetter<CyborgStackController> {

		private static int[] ids = {
			R.styleable.StackController_transition,
			R.styleable.StackController_transitionDuration,
			R.styleable.StackController_popOnBackPress,
			R.styleable.StackController_rootController,
			R.styleable.StackController_rootTag,
			R.styleable.StackController_rootKeep,
		};

		private CyborgStackSetter() {
			super(CyborgStackController.class, R.styleable.StackController, ids);
		}

		@SuppressWarnings("UnnecessaryReturnStatement")
		@Override
		protected void setAttribute(CyborgStackController instance, TypedArray a, int attr) {
			if (attr == R.styleable.StackController_transition) {
				String transitionKey = a.getString(attr);
				Transition transition = CyborgStackController.getTransition(transitionKey);
				if (transition == null) {
					logWarning("Error resolving transition animation from key: " + transitionKey);
					transition = StackTransitions.Fade;
				}

				instance.getConfig().setTransitions(transition);
				return;
			}

			if (attr == R.styleable.StackController_popOnBackPress) {
				boolean popOnBackPress = a.getBoolean(attr, true);
				instance.getConfig().setPopOnBackPress(popOnBackPress);
				return;
			}

			if (attr == R.styleable.StackController_transitionDuration) {
				int duration = a.getInt(attr, -1);
				if (duration == -1)
					return;

				instance.getConfig().setTransitionDuration(duration);
				return;
			}

			if (attr == R.styleable.StackController_rootController) {
				String controllerName = a.getString(attr);
				if (controllerName == null)
					return;

				Class<? extends CyborgController> rootControllerType = resolveClassType(CyborgController.class, controllerName);
				instance.getRootLayerBuilder().setControllerType(rootControllerType);

				return;
			}

			if (attr == R.styleable.StackController_rootKeep) {
				boolean keepRoot = a.getBoolean(attr, true);
				instance.getRootLayerBuilder().setKeepInStack(keepRoot);
				return;
			}

			if (attr == R.styleable.StackController_rootTag) {
				String rootTag = a.getString(attr);
				instance.getRootLayerBuilder().setStateTag(rootTag);
				return;
			}
		}
	}

	public static class StackConfig {

		int transitionDuration = 300;
		Transition[] transitions = {StackTransitions.Slide};
		boolean popOnBackPress = true;

		void setTransitions(Transition... transitions) {
			this.transitions = transitions;
		}

		void setTransitionDuration(int transitionDuration) {
			this.transitionDuration = transitionDuration;
		}

		void setPopOnBackPress(boolean popOnBackPress) {
			this.popOnBackPress = popOnBackPress;
		}
	}
}