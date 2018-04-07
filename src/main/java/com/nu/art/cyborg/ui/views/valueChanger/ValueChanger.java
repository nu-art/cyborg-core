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

package com.nu.art.cyborg.ui.views.valueChanger;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.modules.AttributeModule;

/**
 * Created by TacB0sS on 25-Jul 2015.
 */
public class ValueChanger
	extends LinearLayout
	implements OnClickListener, TextWatcher, OnTouchListener {

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		String text = s.toString().replaceAll("f", "");
		if (text.length() == 0)
			return;
		try {
			value = Float.parseFloat(text);
		} catch (NumberFormatException e) {
			setValueToField();
		}
	}

	private boolean stop;

	private class ValueIncrementer
		implements Runnable {

		private final int factor;

		private int initialDelay = 1000;

		private int iteration = 1;

		private ValueIncrementer(int factor) {this.factor = factor;}

		@Override
		public void run() {
			if (stop)
				return;
			value += factor * deltaValue;
			onValueChanged();
			postDelayed(this, Math.max(initialDelay / iteration++, 25));
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!stop && event.getPointerCount() > 1) {
			stop = true;
			return true;
		}

		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			stop = false;
			ValueIncrementer incrementer = new ValueIncrementer(v.getId() == R.id.IncrementButton ? 1 : -1);
			post(incrementer);
		}

		if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			stop = true;
		}
		return true;
	}

	public interface OnValueChangedListener {

		void onValueChanged();
	}

	float value;

	float deltaValue = -1f;

	EditText valueField;

	OnValueChangedListener listener;

	public ValueChanger(Context context) {
		super(context);
		init(null);
	}

	public ValueChanger(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	public ValueChanger(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		inflate(getContext(), R.layout.v1_custom_view__value_incrementor, this);
		valueField = (EditText) findViewById(R.id.value);
		getCyborg(getContext()).getModule(AttributeModule.class).setAttributes(getContext(), attrs, this);
		if (isInEditMode())
			return;
		valueField.addTextChangedListener(this);
		findViewById(R.id.DecrementButton).setOnTouchListener(this);
		findViewById(R.id.IncrementButton).setOnTouchListener(this);
	}

	private Cyborg getCyborg(Context context) {
		if (isInEditMode()) {
			return CyborgBuilder.getInEditMode(context);
		}
		return CyborgBuilder.getInstance();
	}

	public final void setLabel(String label) {
		((TextView) findViewById(R.id.label)).setText(label);
	}

	public void setValue(float value) {
		this.value = value;
		setValueToField();
	}

	public void setDeltaValue(float deltaValue) {
		this.deltaValue = deltaValue;
	}

	public void setValueChangedListener(OnValueChangedListener valueChangedListener) {
		this.listener = valueChangedListener;
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		if (viewId == R.id.DecrementButton) {
			value -= deltaValue;
		} else if (viewId == R.id.IncrementButton) {
			value += deltaValue;
		} else {
			return;
		}

		onValueChanged();
	}

	private void setValueToField() {
		valueField.setText(String.format("%.1ff", value));
	}

	public float getValue() {
		return value;
	}

	private void onValueChanged() {
		setValueToField();
		if (listener == null)
			return;

		listener.onValueChanged();
	}
}
