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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.common.implementors.AnimationListenerImpl;
import com.nu.art.cyborg.core.animations.PredefinedStackTransitionAnimator;
import com.nu.art.cyborg.core.animations.PredefinedTransitions;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by TacB0sS on 25-Jun 2015.
 */
public final class CyborgStackController
		extends CyborgController {

	public interface StackTransitionAnimator {

		void animateIn(StackLayer origin, StackLayer target, int duration, AnimationListener listener);

		void animateOut(StackLayer origin, StackLayer target, int duration, AnimationListener listener);
	}

	public abstract class StackLayer {

		private StackTransitionAnimator[] stackTransitionAnimator;

		protected Processor<?> processor;

		protected String refKey;

		protected CyborgController controller;

		protected CyborgController[] nestedControllers = {};

		protected View rootView;

		private Bundle stateBundle = new Bundle();

		private int duration;

		private boolean saveState;

		private boolean keepBackground;

		private StackLayer() {
			if (defaultTransition != null) {
				PredefinedStackTransitionAnimator transitionAnimator = new PredefinedStackTransitionAnimator(getActivity(), defaultTransition, defaultTransitionOrientation);
				this.stackTransitionAnimator = new StackTransitionAnimator[]{transitionAnimator};
			}
		}

		public void setStackTransitionAnimators(StackTransitionAnimator[] stackTransitionAnimators) {
			this.stackTransitionAnimator = stackTransitionAnimators;
		}

		public void setRefKey(String refKey) {
			this.refKey = refKey;
		}

		protected abstract void create();

		public void restoreState() {
			if (controller == null)
				return;

			if (!saveState)
				return;

			controller.onRestoreInstanceState(stateBundle);
		}

		public void detachView() {
			getFrameRootView().removeView(rootView);
			if (controller == null)
				return;

			controller.dispatchLifeCycleEvent(LifeCycleState.OnPause);
			controller.dispatchLifeCycleEvent(LifeCycleState.OnDestroy);

			controller = null;
		}

		public void saveState() {
			stateBundle.clear();
			if (controller == null)
				return;

			controller.onSaveInstanceState(stateBundle);
		}

		public void preDestroy() {}

		public View getRootView() {
			return rootView;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public int getDuration() {
			return duration;
		}

		public void setSaveState(boolean saveState) {
			this.saveState = saveState;
		}

		protected void setKeepBackground(boolean keepBackground) {
			this.keepBackground = keepBackground;
		}

		public boolean isSaveState() {
			return saveState;
		}

		public CyborgController getController() {
			return controller;
		}

		private void addNestedController(CyborgController controller) {
			nestedControllers = ArrayTools.appendElement(nestedControllers, controller);
		}

		protected void setProcessor(Processor<?> processor) {
			this.processor = processor;
		}
	}

	private class StackLayoutLayer
			extends StackLayer {

		private final int layoutId;

		private StackLayoutLayer(int layoutId) {
			this.layoutId = layoutId;
		}

		@Override
		protected void create() {
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
	}

	private class StackControllerLayer
			extends StackLayer {

		private final Class<? extends CyborgController> controllerType;

		private StackControllerLayer(Class<? extends CyborgController> controllerType) {
			this.controllerType = controllerType;
		}

		@Override
		protected void create() {
			controller = ReflectiveTools.newInstance(controllerType);
			if (refKey == null)
				refKey = controller.getClass().getSimpleName();

			if (getActivity() instanceof CyborgActivity) {
				CyborgActivityBridge activityBridge = ((CyborgActivity) getActivity()).getBridge();
				controller.setActivityBridge(activityBridge);
			}

			controller.setStateTag(refKey);
			controller._createView(inflater, getFrameRootView(), false);
			rootView = controller.getRootView();

			// Always add it as the lowest item to avoid animation hiccups, where the popping a layer actually places its view on top instead of under... is this correct? the logic sure seems reliable, but are there any other cases this might not work?
			getFrameRootView().addView(rootView, 0);

			controller.extractMembersImpl();
			// xml attribute for root controller are handled in the handleAttributes method

			controller.dispatchLifeCycleEvent(LifeCycleState.OnCreate);

			if (processor != null)
				postCreateProcessController(processor, controller);

			if (getState() == LifeCycleState.OnResume)
				controller.dispatchLifeCycleEvent(LifeCycleState.OnResume);
		}
	}

	private LayoutInflater inflater;

	private ArrayList<StackLayer> layersStack = new ArrayList<>();

	private RelativeLayout frameLayout;

	private Class<? extends CyborgController> rootControllerType;

	private int rootLayoutId = -1;

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

	void setRootControllerType(Class<? extends CyborgController> rootControllerType) {
		this.rootControllerType = rootControllerType;
	}

	void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}

	void setRootLayoutId(int rootLayoutId) {
		this.rootLayoutId = rootLayoutId;
	}

	void setPopOnBackPress(boolean popOnBackPress) {
		this.popOnBackPress = popOnBackPress;
	}

	void setDefaultTransition(PredefinedTransitions defaultTransition) {
		this.defaultTransition = defaultTransition;
	}

	void setDefaultTransitionOrientation(int defaultTransitionOrientation) {
		this.defaultTransitionOrientation = defaultTransitionOrientation;
	}

	void setTransitionDuration(int transitionDuration) {
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

		layerBuilder.setSaveState(false);
		withRoot = true;
		layerBuilder.build();
	}

	@Override
	protected View createCustomView(LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
		this.frameLayout = new RelativeLayout(parent.getContext());
		parent.addView(frameLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return frameLayout;
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

	public class StackLayerBuilder {

		private StackTransitionAnimator[] stackTransitionAnimators;

		private String refKey;

		private Class<? extends CyborgController> controllerType;

		private int layoutId = -1;

		private boolean saveState;

		private int duration = transitionDuration;

		private Processor<?> processor;

		private boolean disposable;

		private boolean keepBackground;

		public StackLayerBuilder setRefKey(String refKey) {
			this.refKey = refKey;
			return this;
		}

		// TODO need to find a way to enable two transition simultaneously, e.g. Fade and Cube
		public StackLayerBuilder setStackTransitionAnimators(StackTransitionAnimator... stackTransitionAnimators) {
			this.stackTransitionAnimators = stackTransitionAnimators;
			return this;
		}

		public StackLayerBuilder setSaveState(boolean saveState) {
			this.saveState = saveState;
			return this;
		}

		public StackLayerBuilder setControllerType(Class<? extends CyborgController> controllerType) {
			if (layoutId != -1)
				throw new BadImplementationException("Already set layoutId, cannot also set controllerType");
			this.controllerType = controllerType;
			return this;
		}

		public StackLayerBuilder setLayoutId(int layoutId) {
			if (controllerType != null)
				throw new BadImplementationException("Already set controller type, cannot also set layoutId");
			this.layoutId = layoutId;
			return this;
		}

		public StackLayerBuilder setDuration(int duration) {
			this.duration = duration;
			return this;
		}

		public StackLayerBuilder setKeepBackground(boolean keepBackground) {
			this.keepBackground = keepBackground;
			return this;
		}

		public StackLayerBuilder setProcessor(Processor<?> processor) {
			this.processor = processor;
			return this;
		}

		public StackLayerBuilder setDisposable(boolean disposable) {
			this.disposable = disposable;
			return this;
		}

		public final void build() {
			StackLayer layerToBeAdded = null;

			if (layoutId != -1)
				layerToBeAdded = new StackLayoutLayer(layoutId);

			if (controllerType != null)
				layerToBeAdded = new StackControllerLayer(controllerType);

			if (layerToBeAdded == null)
				throw new ImplementationMissingException("MUST specify a layoutId or a controllerType");

			if (refKey == null)
				if (controllerType != null)
					refKey = controllerType.getSimpleName();
				else
					throw new ImplementationMissingException("MUST specify a refKey when using a layoutId");

			layerToBeAdded.setRefKey(refKey);

			if (stackTransitionAnimators != null)
				layerToBeAdded.setStackTransitionAnimators(stackTransitionAnimators);

			layerToBeAdded.setDuration(duration);
			layerToBeAdded.setSaveState(saveState);
			layerToBeAdded.setKeepBackground(keepBackground);

			layerToBeAdded.setProcessor(processor);
			push(layerToBeAdded);
		}
	}

	public final StackLayerBuilder createLayerBuilder() {
		return new StackLayerBuilder();
	}

	private RelativeLayout getFrameRootView() {
		return frameLayout;
	}

	public void popUntil(String refKey) {
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	private void push(final StackLayer targetLayerToBeAdded) {
		if (animatingTransition) {
			logWarning("NOT PUSHING NEW LAYER... TRANSITION ANIMATION IN PROGRESS!!!");
			return;
		}

		final StackLayer originLayerToBeDisposed = targetLayerToBeAdded.keepBackground ? null : getTopLayer();
		if (originLayerToBeDisposed != null)
			originLayerToBeDisposed.preDestroy();

		targetLayerToBeAdded.create();
		layersStack.add(targetLayerToBeAdded);

		final StackTransitionAnimator[] transitionAnimators = targetLayerToBeAdded.stackTransitionAnimator;
		if (transitionAnimators == null) {
			disposeLayer(originLayerToBeDisposed);
			return;
		}

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
						if (originLayerToBeDisposed != null)
							disposeLayer(originLayerToBeDisposed);

						setInAnimationState(false);
					}
				};

				for (StackTransitionAnimator animator : transitionAnimators) {
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

	public boolean popLast() {
		if (animatingTransition) {
			logWarning("NOT POPPING LAST LAYER... TRANSITION ANIMATION IN PROGRESS!!!");
			return true;
		}

		final StackLayer targetLayerToBeRemove = getAndRemoveTopLayer();
		if (targetLayerToBeRemove == null)
			return false;

		final StackLayer originLayerToBeRestored = targetLayerToBeRemove.keepBackground ? null : getTopLayer();
		if (originLayerToBeRestored != null) {
			originLayerToBeRestored.create();
			originLayerToBeRestored.restoreState();
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

		final Runnable startAnimation = new Runnable() {
			@Override
			public void run() {
				setInAnimationState(true);
				AnimationListenerImpl listener = new AnimationListenerImpl() {
					@Override
					public void onAnimationEnd(Animation animation) {
						targetLayerToBeRemove.detachView();
						setInAnimationState(false);
					}
				};

				for (StackTransitionAnimator animator : transitionAnimators) {
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

		if (layerToBeDisposed.saveState) {
			layerToBeDisposed.saveState();
		}
		layerToBeDisposed.detachView();
	}

	public void clear() {
	}

	@Override
	public boolean onBackPressed() {
		if (!popOnBackPress || !focused)
			return super.onBackPressed();
		return popLast();
	}

	public String[] getStackListTags() {
		return layersStack.toArray(new String[layersStack.size()]);
	}
}
