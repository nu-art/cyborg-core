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

package com.nu.art.cyborg.clipboard;

import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.os.Handler;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.ThreadsModule;
import com.nu.art.core.generics.Processor;

@ModuleDescriptor(usesPermissions = {})
public class ClipboardModule
		extends CyborgModule {

	public interface OnClipboardChangedListener {

		void onClipboardChanged(String oldText, String newText);
	}

	private abstract class BaseClipboard<Clipboard> {

		protected abstract void setText(String label, String text);

		protected abstract String getText();

		Clipboard clipboard;

		BaseClipboard(ServiceType<Clipboard> serviceType) {
			super();
			clipboard = getSystemService(serviceType);
			oldText = getText();
		}

		protected abstract boolean hasText();
	}

	@SuppressWarnings("deprecation")
	private class Pre11_Clipboard
			extends BaseClipboard<android.text.ClipboardManager>
			implements Runnable {

		private Handler handler;

		private Pre11_Clipboard() {
			super(ClipboardService_Pre_11);
			handler = getModule(ThreadsModule.class).getDefaultHandler("Clipboard Query Thread");
			run();
		}

		@Override
		public void setText(String label, String text) {
			super.clipboard.setText(text);
		}

		@Override
		public String getText() {
			CharSequence text = super.clipboard.getText();
			return text != null ? text.toString() : null;
		}

		@Override
		protected boolean hasText() {
			return super.clipboard.hasText();
		}

		@Override
		public void run() {
			String newText = getText();
			if (newText != null && !newText.equals(oldText)) {
				dispatchTextChangedEvent(newText);
			}
			handler.postDelayed(this, 500);
		}
	}

	private class Post11_Clipboard
			extends BaseClipboard<android.content.ClipboardManager>
			implements OnPrimaryClipChangedListener {

		private Post11_Clipboard() {
			super(new ServiceType<android.content.ClipboardManager>(Context.CLIPBOARD_SERVICE));
			super.clipboard.addPrimaryClipChangedListener(this);
		}

		@Override
		public void setText(String label, String text) {
			clipboard.setText(text);
		}

		@Override
		public String getText() {
			if (!clipboard.hasPrimaryClip())
				return null;
			CharSequence text = super.clipboard.getText();
			return text != null ? text.toString() : null;
		}

		@Override
		protected boolean hasText() {
			return super.clipboard.hasPrimaryClip();
		}

		@Override
		public void onPrimaryClipChanged() {
			String newText = getText();
			if (newText != null && !newText.equals(oldText)) {
				dispatchTextChangedEvent(newText);
			}
		}
	}

	private String oldText;

	@SuppressWarnings( {"unused", "FieldCanBeLocal"})
	private BaseClipboard<?> clipboard;

	@Override
	protected void init() {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			clipboard = new Pre11_Clipboard();
		} else {
			clipboard = new Post11_Clipboard();
		}
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
