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

package com.nu.art.cyborg.common.email;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

public final class AndroidEMailer {

	private final Context context;

	private boolean showServiceChooser = true;

	private String serviceChooserDialogTitle = "Send an EMail..";

	public AndroidEMailer(Context context) {
		this.context = context;
	}

	public final String getServiceChooserDialogTitle() {
		return serviceChooserDialogTitle;
	}

	public final void setServiceChooserDialogTitle(String serviceChooserDialogTitle) {
		this.serviceChooserDialogTitle = serviceChooserDialogTitle;
	}

	public final boolean isShowServiceChooser() {
		return showServiceChooser;
	}

	public final void setShowServiceChooser(boolean showServiceChooser) {
		this.showServiceChooser = showServiceChooser;
	}

	public final void sendEmail(EMail mail) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, getAsStringArray(mail.getTo()));
		emailIntent.putExtra(android.content.Intent.EXTRA_CC, getAsStringArray(mail.getCc()));
		emailIntent.putExtra(android.content.Intent.EXTRA_BCC, getAsStringArray(mail.getBcc()));
		emailIntent.putExtra(Intent.EXTRA_TEXT, mail.getMessageBody());
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mail.getSubject());

		ArrayList<Uri> uris = new ArrayList<>();
		for (File file : mail.getAttachments()) {
			Uri u = Uri.fromFile(file);
			uris.add(u);
		}
		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

		if (showServiceChooser)
			emailIntent = Intent.createChooser(emailIntent, serviceChooserDialogTitle);

		if (!(context instanceof Activity))
			emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(emailIntent);
	}

	public String[] getAsStringArray(Recipient[] recipients) {
		String[] addresses = new String[recipients.length];

		for (int i = 0; i < recipients.length; i++) {
			addresses[i] = recipients[i].geteMail();
		}
		return addresses;
	}
}
