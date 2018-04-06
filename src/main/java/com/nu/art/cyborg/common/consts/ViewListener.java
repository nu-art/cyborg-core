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

package com.nu.art.cyborg.common.consts;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.cyborg.common.interfaces.UserActionsDelegator;
import com.nu.art.cyborg.core.CyborgRecycler;
import com.nu.art.cyborg.core.CyborgRecycler.OnRecyclerItemClickListener;
import com.nu.art.cyborg.core.CyborgRecycler.OnRecyclerItemLongClickListener;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum ViewListener {
	OnClick(ListenersMethods._SetOnClickListener, View.class, OnClickListener.class),
	OnLongClick(ListenersMethods._SetOnLongClickListener, View.class, OnLongClickListener.class),
	OnTouch(ListenersMethods._SetOnTouchListener, View.class, OnTouchListener.class),
	/**
	 * Don't Ever use this... use the on click and get the view check state.
	 */
	@Deprecated OnCheckChanged(ListenersMethods._SetOnCheckChangedListener, CompoundButton.class, OnCheckedChangeListener.class),

	/**
	 * Adds an {@link OnSeekBarChangeListener} to a {@link SeekBar} rootView instance.
	 */
	SeekBar(ListenersMethods._SetOnSeekBarChangeListener, SeekBar.class, OnSeekBarChangeListener.class),

	/**
	 * Adds an {@link OnItemSelectedListener} to an {@link Spinner} rootView instance.
	 */
	OnItemSelected(ListenersMethods._SetOnItemSelectedListener, AdapterView.class, OnItemSelectedListener.class),

	/**
	 * Adds an {@link OnItemClickListener} to an {@link AdapterView} rootView instance.
	 */
	OnItemClicked(ListenersMethods._SetOnItemClickListener, AdapterView.class, OnItemClickListener.class),

	/**
	 * Adds an {@link OnItemLongClickListener} to an {@link AdapterView} rootView instance.
	 */
	OnItemLongClicked(ListenersMethods._SetOnItemLongClickListener, AdapterView.class, OnItemLongClickListener.class),
	/**
	 * Adds an {@link OnRecyclerItemClickListener} to an {@link CyborgRecycler} rootView instance.
	 */
	OnRecyclerItemClicked(ListenersMethods._SetOnRecyclerItemClickListener, CyborgRecycler.class, OnRecyclerItemClickListener.class),
	/**
	 * Adds an {@link OnRecyclerItemLongClickListener} to an {@link CyborgRecycler} rootView instance.
	 */
	OnRecyclerItemLongClicked(ListenersMethods._SetOnRecyclerItemLongClickListener, CyborgRecycler.class, OnRecyclerItemLongClickListener.class),

	/**
	 * Adds an {@link OnRatingBarChangeListener} to the Rating bar instance.
	 */
	OnRatingChanged(ListenersMethods._SetOnRatingBarChangeListener, RatingBar.class, OnRatingBarChangeListener.class),
	/**
	 * Adds an {@link OnPageChangeListener} to the Rating bar instance.
	 */
	OnPageChange(ListenersMethods._OnPageChangeListener, ViewPager.class, OnPageChangeListener.class), /**/
	OnEditorAction(ListenersMethods._OnEditorActionListener, TextView.class, OnEditorActionListener.class), /**/
	OnFocusChange(ListenersMethods._OnFocusChangeListener, View.class, OnFocusChangeListener.class), /**/
	OnKeyListener(ListenersMethods._OnKeyListener, View.class, OnKeyListener.class), /**/
	OnTextChangedListener(ListenersMethods._OnTextChangedListener, TextView.class, TextWatcher.class) {
		@Override
		public void assign(final View view, final UserActionsDelegator modelDelegator)
			throws InvocationTargetException, IllegalAccessException {
			listenerMethod.invoke(view, new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
					modelDelegator.beforeTextChanged(((TextView) view), charSequence, i, i1, i2);
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
					modelDelegator.onTextChanged(((TextView) view), charSequence, i, i1, i2);
				}

				@Override
				public void afterTextChanged(Editable editable) {
					modelDelegator.afterTextChanged(((TextView) view), editable);
				}
			});
		}
	}, /**/
	/**/;

	protected final Method listenerMethod;

	private final Class<? extends View> methodOwnerType;

	ViewListener(String listenerMethodName, Class<? extends View> methodOwnerType, Class<?>... methodArguments) {
		try {
			this.methodOwnerType = methodOwnerType;
			listenerMethod = methodOwnerType.getDeclaredMethod(listenerMethodName, methodArguments);
			listenerMethod.setAccessible(true);
		} catch (Throwable e) {
			String parameterType = ReflectiveTools.parseParametersType(methodArguments);
			String msg = "Failed to get method: " + listenerMethodName + "(" + parameterType + "), for type: " + methodOwnerType;
			Log.e("ViewListener", msg, e);
			throw new MUST_NeverHappenedException(msg, e);
		}
	}

	public final Class<? extends View> getMethodOwnerType() {
		return methodOwnerType;
	}

	public void assign(View view, UserActionsDelegator modelDelegator)
		throws InvocationTargetException, IllegalAccessException {
		listenerMethod.invoke(view, modelDelegator);
	}
}

interface ListenersMethods {

	String _SetOnClickListener = "setOnClickListener";

	String _SetOnTouchListener = "setOnTouchListener";

	String _SetOnCheckChangedListener = "setOnCheckedChangeListener";

	String _SetOnLongClickListener = "setOnLongClickListener";

	String _SetOnSeekBarChangeListener = "setOnSeekBarChangeListener";

	String _SetOnItemSelectedListener = "setOnItemSelectedListener";

	String _SetOnRecyclerItemClickListener = "setRecyclerItemClickListener";

	String _SetOnRecyclerItemLongClickListener = "setRecyclerItemLongClickListener";

	String _SetOnItemClickListener = "setOnItemClickListener";

	String _SetOnItemLongClickListener = "setOnItemLongClickListener";

	String _SetOnRatingBarChangeListener = "setOnRatingBarChangeListener";

	String _OnPageChangeListener = "setOnPageChangeListener";

	String _OnEditorActionListener = "setOnEditorActionListener";

	String _OnFocusChangeListener = "setOnFocusChangeListener";

	String _OnKeyListener = "setOnKeyListener";

	String _OnTextChangedListener = "addTextChangedListener";
}
