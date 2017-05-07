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

package com.nu.art.cyborg.annotations;

import android.view.View;

import com.nu.art.cyborg.common.consts.ViewListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * My finest hour... This annotation defines some properties to one or multiple {@link View}s and some extra details about them.
 *
 * @author TacB0sS
 */
@Target( {FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewIdentifier {

	/**
	 * Use this method in case you have duplicated containers with different ids, and you would like to extract their
	 * children.<br>
	 *
	 * @return The parent rootView Id as defined in your layout xmls.
	 *
	 * @code class <b>ClassName</b> {<br>
	 * private &lt;ViewType extends View&gt; <b>ViewType</b> singleView;} <br>
	 */
	int parentViewId() default -1;

	/**
	 * Use this method only in case of a single rootView, eg. <br>
	 * <p/>
	 * <pre>
	 *
	 * @return The rootView Id as defined in your layout xmls.
	 *
	 * @code class <b>ClassName</b> {<br>
	 * private &lt;ViewType extends View&gt; <b>ViewType</b> singleView; <br>
	 * }<br>
	 * </pre>
	 */
	int viewId() default -1;

	/**
	 * Use this method only in case of a multiple views, eg. <br>
	 * <p/>
	 * <pre>
	 *
	 * @return An array of rootView ids. The size of the rootView array would correspond with size of the return array of this
	 * method!!
	 *
	 * @code class <b>ClassName</b> {<br>
	 * private <ViewType extends View> <b>ViewType</b>[] multipleViews;
	 * }<br>
	 * </pre>
	 */
	int[] viewIds() default {};

	/**
	 * @return An array of {@link ViewListener} to setup for this rootView.
	 */
	ViewListener[] listeners() default {};

	/**
	 * According to this flag <b>Cyborg</b> would know if the rootView should be visible or not, depending on the
	 * certificate the apk is signed with.
	 * <br><br>
	 * This gets useful when you want to debug your app while in debug, and not be able to perform these action in production!
	 * (I use it ALL the time!)
	 *
	 * @return whether this rootView is a development utility rootView.
	 */
	boolean forDev() default false;
}
