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

package com.nu.art.cyborg.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

@ModuleDescriptor(usesPermissions = {})
public class ClipboardModule
	extends CyborgModule
	implements OnPrimaryClipChangedListener {

	public interface OnClipboardChangedListener {

		void onClipboardChanged(String oldText, String newText);
	}

	private String oldText;
	private ClipboardManager clipboard;

	@Override
	protected void init() {
		clipboard = getSystemService(ClipboardService);
		clipboard.addPrimaryClipChangedListener(this);
	}

	public void onPrimaryClipChanged() {
		String newText = getText();
		if (newText != null && !newText.equals(oldText)) {
			dispatchTextChangedEvent(newText);
		}
	}

	public String getText() {
		if (!clipboard.hasPrimaryClip())
			return null;

		CharSequence text = clipboard.getText();
		return text != null ? text.toString() : null;
	}

	public void copyToClipboard(String text) {
		copyToClipboard(null, text);
	}

	public void copyToClipboard(String label, String text) {
		clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
	}

	private void dispatchTextChangedEvent(final String newText) {
		dispatchModuleEvent("On clipboard changed: " + oldText + " => " + newText, OnClipboardChangedListener.class, new Processor<OnClipboardChangedListener>() {
			@Override
			public void process(OnClipboardChangedListener listener) {
				listener.onClipboardChanged(oldText, newText);
			}
		});
		oldText = newText;
	}
}
