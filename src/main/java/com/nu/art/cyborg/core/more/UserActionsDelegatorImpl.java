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

package com.nu.art.cyborg.core.more;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nu.art.cyborg.common.beans.ModelEvent;
import com.nu.art.cyborg.common.interfaces.UserActionsDelegator;
import com.nu.art.cyborg.core.abs.Cyborg;

public class UserActionsDelegatorImpl
	implements UserActionsDelegator {

	private final Cyborg cyborg;
	private UserActionsDelegator[] modulesAssignableFrom;

	public UserActionsDelegatorImpl(Cyborg cyborg) {
		super();
		this.cyborg = cyborg;
	}

	private UserActionsDelegator[] getModulesAssignableFrom() {
		if (modulesAssignableFrom == null)
			modulesAssignableFrom = cyborg.getModulesAssignableFrom(UserActionsDelegator.class);

		return modulesAssignableFrom;
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onKeyShortcut(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onKeyMultiple(keyCode, repeatCount, event);
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onKeyDown(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onKeyUp(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onKeyLongPress(keyCode, event);
		}
		return false;
	}

	@Override
	public void onModelEvent(ModelEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onModelEvent(event);
		}
	}

	@Override
	public void onClick(View v) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onClick(v);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onTouch(v, me);
		}
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onLongClick(v);
		}
		return false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onProgressChanged(seekBar, progress, fromUser);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onStartTrackingTouch(seekBar);
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onStopTrackingTouch(seekBar);
		}
	}

	@Override
	public void onRecyclerItemClicked(RecyclerView parentView, View view, int position) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onRecyclerItemClicked(parentView, view, position);
		}
	}

	@Override
	public boolean onRecyclerItemLongClicked(RecyclerView parentView, View view, int position) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onRecyclerItemLongClicked(parentView, view, position);
		}
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onItemSelected(parent, view, position, id);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onNothingSelected(parent);
		}
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onRatingChanged(ratingBar, rating, fromUser);
		}
	}

	@Override
	public void onPageSelected(int position) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onPageSelected(position);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onPageScrollStateChanged(state);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onMenuItemClick(item);
		}
		return false;
	}

	@Override
	public void beforeTextChanged(TextView view, CharSequence string, int start, int count, int after) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.beforeTextChanged(view, string, start, count, after);
		}
	}

	@Override
	public void onTextChanged(TextView view, CharSequence string, int start, int before, int count) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onTextChanged(view, string, start, before, count);
		}
	}

	@Override
	public void afterTextChanged(TextView view, Editable editableValue) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.afterTextChanged(view, editableValue);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onEditorAction(v, actionId, event);
		}
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onFocusChange(v, hasFocus);
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		for (UserActionsDelegator userActionDelegator : getModulesAssignableFrom()) {
			userActionDelegator.onKey(v, keyCode, event);
		}
		return false;
	}
}
