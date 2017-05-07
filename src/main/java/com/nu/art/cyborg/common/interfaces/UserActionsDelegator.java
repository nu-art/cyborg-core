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

package com.nu.art.cyborg.common.interfaces;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.nu.art.cyborg.common.beans.ModelEvent;
import com.nu.art.cyborg.core.CyborgRecycler.OnRecyclerItemClickListener;
import com.nu.art.cyborg.core.CyborgRecycler.OnRecyclerItemLongClickListener;

/**
 * UI - User Interaction
 *
 * @author TacB0sS
 */
public interface UserActionsDelegator
		extends OnClickListener, OnTouchListener, OnLongClickListener, OnSeekBarChangeListener, OnRecyclerItemClickListener, OnRecyclerItemLongClickListener,
				OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener, OnRatingBarChangeListener, OnPageChangeListener, OnCheckedChangeListener,
				OnMenuItemClickListener, OnEditorActionListener, OnFocusChangeListener, OnKeyListener, OnTextChangedListener {

	boolean onKeyShortcut(int keyCode, KeyEvent event);

	boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

	boolean onKeyDown(int keyCode, KeyEvent event);

	boolean onKeyUp(int keyCode, KeyEvent event);

	boolean onKeyLongPress(int keyCode, KeyEvent event);

	/**
	 * Been called when an application level event has occurred.
	 *
	 * @param event The event details.
	 */
	void onModelEvent(ModelEvent event);

	@Override
	void onClick(View v);

	@Override
	boolean onLongClick(View v);

	@Override
	boolean onTouch(View v, MotionEvent event);

	@Override
	void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

	@Override
	void onStartTrackingTouch(SeekBar seekBar);

	@Override
	void onStopTrackingTouch(SeekBar seekBar);

	@Override
	void onItemClick(AdapterView<?> parentView, View clickedView, int position, long id);

	@Override
	boolean onItemLongClick(AdapterView<?> parentView, View clickedView, int position, long id);

	@Override
	void onItemSelected(AdapterView<?> parentView, View selectedView, int position, long id);

	@Override
	void onNothingSelected(AdapterView<?> parentView);

	@Override
	void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser);

	@Override
	void onPageSelected(int position);

	@Override
	void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

	@Override
	void onPageScrollStateChanged(int state);

	@Override
	void onCheckedChanged(CompoundButton buttonView, boolean isChecked);

	@Override
	boolean onEditorAction(TextView v, int actionId, KeyEvent event);

	@Override
	void onFocusChange(View v, boolean hasFocus);

	@Override
	boolean onKey(View v, int keyCode, KeyEvent event);

	@Override
	boolean onMenuItemClick(MenuItem item);

	@Override
	void beforeTextChanged(TextView view, CharSequence string, int start, int count, int after);

	@Override
	void onTextChanged(TextView view, CharSequence string, int start, int before, int count);

	@Override
	void afterTextChanged(TextView view, Editable editableValue);

	@Override
	void onRecyclerItemClicked(RecyclerView parentView, View view, int position);

	@Override
	boolean onRecyclerItemLongClicked(RecyclerView parentView, View view, int position);
}
