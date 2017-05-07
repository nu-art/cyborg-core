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

package com.nu.art.cyborg.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.ui.StartActivityForResultActivity.StartActivityType;
import com.nu.art.reflection.tools.ReflectiveTools;

public class StartActivityForResultActivity
		extends Activity
		implements StartActivityForResultWorkaroundConsts {

	public static void startActivityForResultHack(Context context, Intent originalIntent, Intent startIntent, int requestCode) {
		StartActivityForResultHandler.startActivityForResult(context, originalIntent, startIntent, requestCode);
	}

	public static void startIntentSenderForResultHack(Context context, Intent originalIntent, IntentSender intentSender, int requestCode) {
		startIntentSenderForResultHack(context, originalIntent, intentSender, null, requestCode, 0, 0, 0);
	}

	public static void startIntentSenderForResultHack(Context context, Intent originalIntent, IntentSender intentSender, Intent fillInIntent, int requestCode, int flagsMasks, int flagsValues, int extraFlags) {
		StartIntentSenderForResultHandler
				.startIntentSenderForResult(context, originalIntent, intentSender, fillInIntent, requestCode, flagsMasks, flagsValues, extraFlags);
	}

	public enum StartActivityType {
		StartActivityForResult(StartActivityForResultHandler.class),
		StartIntentSenderForResult(StartIntentSenderForResultHandler.class);

		Class<? extends StartActivityHandler> handlerType;

		StartActivityType(Class<? extends StartActivityHandler> handlerType) {
			this.handlerType = handlerType;
		}

	}

	private StartActivityHandler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String type = intent.getStringExtra(ExtraKey__StartType);

		StartActivityType startType = StartActivityType.valueOf(type);
		handler = ReflectiveTools.newInstance(startType.handlerType);
		handler.handleIntent(this, intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (handler != null)
			handler.onActivityResult(this, requestCode, resultCode, data);
	}
}

abstract class StartActivityHandler
		implements StartActivityForResultWorkaroundConsts {

	protected static Intent createGeneralIntent(Context context, StartActivityType type, Intent originalIntent, int requestCode) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(context, StartActivityForResultActivity.class));
		intent.putExtra(ExtraKey__StartType, type.name());
		intent.putExtra(ExtraKey__OriginIntent, originalIntent);
		intent.putExtra(ExtraKey__RequestCode, requestCode);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	private Intent originIntent;

	protected int requestCode;

	protected void handleIntent(@SuppressWarnings("unused") Activity activity, Intent intent) {
		originIntent = intent.getParcelableExtra(ExtraKey__OriginIntent);
		if (originIntent == null)
			throw new BadImplementationException("originIntent == null");

		requestCode = intent.getIntExtra(ExtraKey__RequestCode, -1);
		if (requestCode == -1)
			throw new BadImplementationException("requestCode not specified");
	}

	protected void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (requestCode != this.requestCode)
			throw new BadImplementationException("Something is off... none matching request codes.");

		originIntent.putExtra(ExtraKey__RequestCode, requestCode);
		originIntent.putExtra(ExtraKey__ResultCode, resultCode);
		originIntent.putExtra(ExtraKey__ResponseData, data);

		activity.startActivity(originIntent);

		activity.finish();
	}

	protected final void handleException(Activity activity, SendIntentException e) {
		originIntent.putExtra(ExtraKey__RequestCode, requestCode);
		originIntent.putExtra(ExtraKey__ResultCode, -10);
		Intent data = new Intent();
		data.putExtra(ExtraKey__Exception, e);
		originIntent.putExtra(ExtraKey__ResponseData, data);

		activity.startActivity(originIntent);

		activity.finish();
	}
}

final class StartActivityForResultHandler
		extends StartActivityHandler {

	@Override
	protected final void handleIntent(Activity activity, Intent intent) {
		super.handleIntent(activity, intent);

		Intent startIntent = intent.getParcelableExtra(ExtraKey__IntentToStart);
		if (startIntent == null)
			throw new BadImplementationException("startIntent == null");

		activity.startActivityForResult(startIntent, requestCode);
	}

	public static void startActivityForResult(Context context, Intent originalIntent, Intent startIntent, int requestCode) {
		Intent intent = createGeneralIntent(context, StartActivityType.StartActivityForResult, originalIntent, requestCode);

		intent.putExtra(ExtraKey__IntentToStart, startIntent);
		context.startActivity(intent);
	}
}

final class StartIntentSenderForResultHandler
		extends StartActivityHandler {

	@Override
	protected final void handleIntent(Activity activity, Intent intent) {
		super.handleIntent(activity, intent);

		IntentSender intentSender = intent.getParcelableExtra(ExtraKey__IntentSender);
		if (intentSender == null)
			throw new BadImplementationException("startIntent == null");

		Intent fillingInIntent = intent.getParcelableExtra(ExtraKey__FillInIntent);
		if (fillingInIntent == null)
			fillingInIntent = new Intent();

		int flagsMask = intent.getIntExtra(ExtraKey__FlagsMask, 0);
		int flagsValues = intent.getIntExtra(ExtraKey__FlagsValues, 0);
		int extraFlags = intent.getIntExtra(ExtraKey__ExtraFlags, 0);

		try {
			activity.startIntentSenderForResult(intentSender, requestCode, fillingInIntent, flagsMask, flagsValues, extraFlags);
		} catch (SendIntentException e) {
			handleException(activity, e);
		}
	}

	public static void startIntentSenderForResult(Context context, Intent originalIntent, IntentSender intentSender, Intent fillInIntent, int requestCode, int flagsMasks, int flagsValues, int extraFlags) {
		Intent intent = createGeneralIntent(context, StartActivityType.StartIntentSenderForResult, originalIntent, requestCode);

		intent.putExtra(ExtraKey__IntentSender, intentSender);
		if (fillInIntent != null)
			intent.putExtra(ExtraKey__FillInIntent, fillInIntent);
		intent.putExtra(ExtraKey__FlagsMask, flagsMasks);
		intent.putExtra(ExtraKey__FlagsValues, flagsValues);
		intent.putExtra(ExtraKey__ExtraFlags, extraFlags);
		context.startActivity(intent);
	}
}

interface StartActivityForResultWorkaroundConsts {

	/**
	 * The type of what to start as the delegating activity
	 */
	String ExtraKey__StartType = "Start Type";

	String ExtraKey__OriginIntent = "Origin Intent";

	String ExtraKey__IntentToStart = "Intent To Start";

	String ExtraKey__RequestCode = "Request Code";

	String ExtraKey__ResultCode = "Result Code";

	String ExtraKey__ResponseData = "Response Data";

	String ExtraKey__IntentSender = "Intent Sender";

	String ExtraKey__FillInIntent = "Fill In Intent";

	String ExtraKey__FlagsMask = "Flags Mask";

	String ExtraKey__FlagsValues = "Flags Values";

	String ExtraKey__ExtraFlags = "Extra Flags";

	String ExtraKey__Exception = "Exception";
}
