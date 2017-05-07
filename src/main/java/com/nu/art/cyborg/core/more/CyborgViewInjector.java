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

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ThisShouldNotHappenedException;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.ViewIdentifier;
import com.nu.art.cyborg.common.consts.ViewListener;
import com.nu.art.cyborg.common.interfaces.UserActionsDelegator;
import com.nu.art.cyborg.core.CyborgAdapter;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.reflection.injector.AnnotatbleInjector;
import com.nu.art.reflection.tools.ART_Tools;

import java.lang.reflect.Field;

@SuppressWarnings( {
		"rawtypes",
		"unchecked"
})
public final class CyborgViewInjector
		extends AnnotatbleInjector<ViewIdentifier, View, CyborgController>
		implements ILogger {

	/**
	 * A map of rootView Id, to its rootView instance.
	 */
	private final SparseArray<View> views = new SparseArray<View>();

	private String tag;

	private View rootView;

	private UserActionsDelegator modelDelegator;

	private boolean debuggable;

	private ILogger logger;

	/**
	 * This constructor is for a single rootView for a single controller approach as the {@link CyborgAdapter} and {@link
	 * com.nu.art.cyborg.core.ItemRenderer}
	 *
	 * @param tag            For log.
	 * @param rootView       The rootView containing the rootView to inject to the controllers.
	 * @param modelDelegator The listener delegator that would catch the events registered for the injected members.
	 * @param debuggable     A debug flag.
	 */
	public CyborgViewInjector(String tag, View rootView, UserActionsDelegator modelDelegator, boolean debuggable) {
		super(ViewIdentifier.class);
		this.tag = tag;
		this.rootView = rootView;
		this.modelDelegator = modelDelegator;
		this.debuggable = debuggable;
		logger = CyborgBuilder.getInstance().getLogger(this);
	}

	@Override
	protected final Field[] extractFieldsFromInstance(Class<? extends CyborgController> controllerType) {
		return ART_Tools
				.getFieldsWithAnnotationAndTypeFromClassHierarchy(controllerType, CyborgController.class, null, ViewIdentifier.class, View.class, View[].class);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object getValueFromAnnotationAndField(ViewIdentifier annotation, Field viewField) {
		Class<?> fieldType = viewField.getType();
		ViewIdentifier viewIdentifier = viewField.getAnnotation(ViewIdentifier.class);
		if (View.class.isAssignableFrom(fieldType)) {
			// if (viewIdentifier == null)
			// return;
			/*
			 * If rootView field
			 */
			int parentViewId = viewIdentifier.parentViewId();
			int viewId = viewIdentifier.viewId();
			if (viewId == -1) {
				throw new BadImplementationException("You MUST supply a valid rootView id for:\n		 " + viewField);
			}
			return setupView(viewField, parentViewId, viewId, viewIdentifier.forDev(), viewIdentifier.listeners());
		} else if (fieldType.isArray()) {
			Class<? extends View> viewType = (Class<? extends View>) fieldType.getComponentType();

			int parentViewId = viewIdentifier.parentViewId();
			int viewId = viewIdentifier.viewId();
			if (viewId != -1) {
				throw new BadImplementationException("You MUST NOT supply any value rootView id for:\n		 " + viewField);
			}

			int[] ids = viewIdentifier.viewIds();
			if (ids.length == 0) {
				throw new BadImplementationException("There is no point adding an annotation for an array of views,\n without specifying any rootView id in the viewIds() method,\n add ids, or comment out the deceleration of: " + viewField);
			}
			View[] views = ArrayTools.newInstance(viewType, ids.length);
			for (int i = 0; i < ids.length; i++) {
				views[i] = setupView(viewField, parentViewId, ids[i], viewIdentifier.forDev(), viewIdentifier.listeners());
			}
			return views;
		} else {
			throw new ThisShouldNotHappenedException("Extracting fields using: '" + ART_Tools.class.getName() + "' from '" + getClass().getName() + "'");
		}
	}

	private View setupView(Field viewField, int parentViewId, int viewId, boolean forDev, ViewListener[] listeners) {
		View view;
		if (parentViewId != -1) {
			ViewGroup parentView = (ViewGroup) rootView.findViewById(parentViewId);
			view = parentView.findViewById(viewId);
		} else {
			view = rootView.findViewById(viewId);
		}

		if (view == null) {
			throw new BadImplementationException("You must supply a valid rootView id for rootView:\n		 '" + viewField);
		}

		if (modelDelegator == null)
			throw new BadImplementationException("modelDelegator == null");

		for (ViewListener listener : listeners) {
			if (!listener.getMethodOwnerType().isAssignableFrom(view.getClass())) {
				throw new BadImplementationException("Cannot assign '" + listener + "' listener to type: " + view.getClass()
						.getSimpleName() + ", it does not inherit from super type: " + listener.getMethodOwnerType().getSimpleName());
			}
			try {
				listener.assign(view, modelDelegator);
			} catch (Exception e) {
				throw new BadImplementationException("Error while assigning listener to view", e);
			}
		}
		if (forDev && !debuggable) {
			view.setVisibility(View.GONE);
		}
		views.put(viewId, view);
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
