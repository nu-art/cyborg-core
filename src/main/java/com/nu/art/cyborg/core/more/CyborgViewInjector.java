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

package com.nu.art.cyborg.core.more;

import android.view.View;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.ViewIdentifier;
import com.nu.art.cyborg.common.consts.ViewListener;
import com.nu.art.cyborg.common.interfaces.UserActionsDelegator;
import com.nu.art.cyborg.core.CyborgAdapter;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.cyborg.core.CyborgView;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.reflection.injector.AnnotatbleInjector;
import com.nu.art.reflection.tools.ART_Tools;

import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings( {
											 "rawtypes",
											 "unchecked"
									 })
public final class CyborgViewInjector
		extends AnnotatbleInjector<ViewIdentifier, View, CyborgController>
		implements ILogger {

	private static final HashMap<Class<? extends CyborgController>, Field[]> cache = new HashMap<>();
	/**
	 * A map of rootView Id, to its rootView instance.
	 */
	private View rootView;

	private UserActionsDelegator modelDelegator;

	private boolean debuggable;

	private ILogger logger;

	/**
	 * This constructor is for a single rootView for a single controller approach as the {@link CyborgAdapter} and {@link
	 * com.nu.art.cyborg.core.ItemRenderer}
	 *
	 * @param rootView       The rootView containing the rootView to inject to the controllers.
	 * @param modelDelegator The listener delegator that would catch the events registered for the injected members.
	 * @param debuggable     A debug flag.
	 */
	public CyborgViewInjector(View rootView, UserActionsDelegator modelDelegator, boolean debuggable) {
		super(ViewIdentifier.class);

		this.rootView = rootView;
		this.modelDelegator = modelDelegator;
		this.debuggable = debuggable;
		logger = CyborgBuilder.getInstance().getLogger(this);
	}

	@Override
	protected final Field[] extractFieldsFromInstance(Class<? extends CyborgController> controllerType) {
		Field[] fields = cache.get(controllerType);
		if (fields == null) {
			fields = ART_Tools
					.getFieldsWithAnnotationAndTypeFromClassHierarchy(controllerType, CyborgController.class, null, ViewIdentifier.class, View.class, View[].class, CyborgController.class);
			cache.put(controllerType, fields);
		}
		return fields;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object getValueFromAnnotationAndField(Object fieldValue, ViewIdentifier annotation, Field viewField) {
		Class<?> fieldType = viewField.getType();
		ViewIdentifier viewIdentifier = viewField.getAnnotation(ViewIdentifier.class);
		if (!fieldType.isArray()) {
			int parentViewId = viewIdentifier.parentViewId();
			int viewId = viewIdentifier.viewId();
			if (viewId == -1)
				throw ExceptionGenerator.developerDidNotSetViewIdForViewInjector(viewField);

			return setupItem(fieldValue, viewField, fieldType, viewIdentifier, parentViewId, viewId);
		}

		Class<?> componentType = fieldType.getComponentType();

		int parentViewId = viewIdentifier.parentViewId();
		int viewId = viewIdentifier.viewId();
		if (viewId != -1)
			throw ExceptionGenerator.developerSetViewIdForViewArrayInjector(viewField);

		int[] ids = viewIdentifier.viewIds();
		if (ids.length == 0)
			throw ExceptionGenerator.developerDidNotSetViewIdsForViewArrayInjector(viewField);

		return getArrayValueFromAnnotationAndField(fieldValue, viewField, viewIdentifier, componentType, parentViewId, ids);
	}

	private <ComponentType> ComponentType[] getArrayValueFromAnnotationAndField(Object fieldValue, Field viewField, ViewIdentifier viewIdentifier, Class<ComponentType> componentType, int parentViewId, int[] ids) {
		ComponentType[] items;
		if (fieldValue != null)
			items = (ComponentType[]) fieldValue;
		else
			items = ArrayTools.newInstance(componentType, ids.length);

		for (int i = 0; i < items.length; i++) {
			items[i] = (ComponentType) setupItem(items[i], viewField, componentType, viewIdentifier, parentViewId, ids[i]);
		}

		return items;
	}

	private Object setupItem(Object fieldValue, Field viewField, Class<?> fieldType, ViewIdentifier viewIdentifier, int parentViewId, int viewId) {
		View view = (View) fieldValue;
		View parentView = rootView;

		if (view == null) {
			if (parentViewId != -1)
				parentView = rootView.findViewById(parentViewId);

			view = parentView.findViewById(viewId);

			if (view == null)
				throw ExceptionGenerator.couldNotFindViewForViewIdInLayout(viewField);
		}

		if (View.class.isAssignableFrom(fieldType)) {
			return setupView(view, viewIdentifier.forDev(), viewIdentifier.listeners());
		}

		if (CyborgController.class.isAssignableFrom(fieldType)) {
			return setupController(view, viewField, viewIdentifier.forDev());
		}

		throw ExceptionGenerator.developerSetViewIdentifierAnnotationToMemberWithUnsupportedType(viewField);
	}

	private CyborgController setupController(View view, Field viewField, boolean forDev) {
		if (!(view instanceof CyborgView))
			throw ExceptionGenerator.developerSetViewIdOfIncompatibleViewForController(viewField);

		CyborgController controller = ((CyborgView) view).getController();
		controller.setVisibility(forDev ? View.GONE : View.VISIBLE);

		return controller;
	}

	private View setupView(View view, boolean forDev, ViewListener[] listeners) {
		if (modelDelegator == null)
			throw new BadImplementationException("modelDelegator == null");

		for (ViewListener listener : listeners) {
			if (!listener.getMethodOwnerType().isAssignableFrom(view.getClass()))
				throw ExceptionGenerator.wrongListenerToViewAssignment(view, listener);

			try {
				listener.assign(view, modelDelegator);
			} catch (Exception e) {
				throw ExceptionGenerator.errorWhileAssigningListenerToView(e);
			}
		}

		if (forDev && !debuggable)
			view.setVisibility(View.GONE);

		return view;
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
