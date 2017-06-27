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
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.consts.LifeCycleState;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.reflection.annotations.ReflectiveInitialization;
import com.nu.art.reflection.tools.ReflectiveTools;

/**
 * I've thought about this for a very long time, and at the end it still took me two days to make up my mind that this is better than Fragments... and now that
 * it is done I believe it is a drastic improvement!
 *
 * So this is a container for your views, which is super intuitive:
 * <pre>
 * {@code
 *
 * <com.nu.art.cyborg.core.CyborgView
 *     android:id="@+id/fragment"
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:custom="http://schemas.android.com/apk/res-auto"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     custom:controller="Your Controller Fully Qualified Name"/>}
 * </pre>
 *
 * Now what is your control65ler?? check out {@link CyborgController}
 */
@SuppressWarnings("unchecked")
public class CyborgView
		extends RelativeLayout {

	protected final String TAG = getClass().getSimpleName();

	protected Cyborg cyborg;

	private CyborgController controller;

	private String stateTag = null;

	public CyborgView(Context context) {
		this(context, null);
	}

	public CyborgView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CyborgView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private Cyborg getCyborg(Context context) {
		if (isInEditMode())
			return CyborgBuilder.getInEditMode(context);

		return CyborgBuilder.getInstance();
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		controller.onConfigurationChanged(newConfig);
	}

	final void init(AttributeSet attrs, int defStyle) {
		if (attrs == null)
			return;

		Context context = getContext();
		cyborg = getCyborg(context);

		// First set attributes to this CyborgView
		cyborg.getModule(AttributeModule.class).setAttributes(context, attrs, this);

		// MUST have a controller at this point
		if (controller == null)
			throw new BadImplementationException("MUST specify a valid controller class name");

		CyborgActivityBridge activityBridge = null;
		// I've been going around this issue for a while... I believe this 'if' is the only real solution
		if (!isInEditMode()) {
			// set the context for inflating and custom views
			activityBridge = ((CyborgActivity) context).getBridge();
			controller.setActivityBridge(activityBridge);
		}

		// set the state tag
		if (stateTag == null)
			stateTag = controller.getClass().getSimpleName();

		// inflating views
		controller._createView(LayoutInflater.from(context), this);
		setTag(controller);

		if (isInEditMode())
			return;

		controller.setStateTag(stateTag);

		// extract members by injection or by find view by id
		controller.extractMembersImpl();

		// set Attributes to the controller, this action can be dependant on the inject members
		cyborg.getModule(AttributeModule.class).setAttributes(context, attrs, controller);

		// handle the attributes for the controller
		controller.handleAttributes(context, attrs);

		if (activityBridge == null)
			throw new MUST_NeverHappenedException("activityBridge is null...???");

		CyborgController parentController = findParentController();
		// this can still be because the controller is set to be the tag of the
		if (parentController != null && parentController != controller)
			parentController.addNestedController(controller);

		LifeCycleState targetState;
		if (parentController == null) {
			targetState = activityBridge.getState();
			for (LifeCycleState lifeCycleState : LifeCycleState.values()) {
				controller.dispatchLifeCycleEvent(lifeCycleState);
				if (lifeCycleState == targetState)
					break;
			}
		}

		// Some view are loaded using findViewById, so after the onCreate,
		// we'll let the controller decide if he wants to delegate some xml attributes to its children views
		controller.handleAttributes(context, attrs);
	}

	private CyborgController findParentController() {
		View v = this;
		while (v.getParent() instanceof View) {
			v = (View) v.getParent();
			if (v instanceof CyborgView)
				return (CyborgController) v.getTag();
		}
		return null;
	}

	final void setController(CyborgController controller) {
		this.controller = controller;
	}

	final void setStateTag(String stateTag) {
		this.stateTag = stateTag;
	}

	public final CyborgController getController() {
		return controller;
	}

	public void dispose() {
		controller.disposeViews();
	}

	@ReflectiveInitialization
	public static class CyborgViewSetter
			extends AttributesSetter<CyborgView> {

		private static final int[] ids = {
				R.styleable.CyborgView_controller,
				R.styleable.CyborgView_tag
		};

		public CyborgViewSetter() {
			super(CyborgView.class, R.styleable.CyborgView, ids);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void setAttribute(CyborgView instance, TypedArray a, int attr) {
			if (attr == R.styleable.CyborgView_controller) {
				String controllerName = a.getString(attr);
				if (controllerName == null || controllerName.length() == 0)
					throw new BadImplementationException("MUST specify a valid a controller class name");

				if (controllerName.startsWith("."))
					controllerName = cyborg.getPackageName() + controllerName;
				try {
					Class<? extends CyborgController> controllerType = (Class<? extends CyborgController>) getClass().getClassLoader().loadClass(controllerName);
					CyborgController controller = ReflectiveTools.newInstance(controllerType);
					instance.setController(controller);
				} catch (ClassNotFoundException e) {
					throw new BadImplementationException("MUST specify a valid controller class name, found: " + controllerName, e);
				}
				return;
			}
			if (attr == R.styleable.CyborgView_tag) {
				String xmlTag = a.getString(attr);
				instance.setStateTag(xmlTag);
			}
		}
	}
}
