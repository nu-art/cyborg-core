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

package com.nu.art.cyborg.core.modules;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.MediaStore;

import com.nu.art.cyborg.R;
import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.core.tools.ExceptionTools;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.common.beans.FullComponentName;
import com.nu.art.cyborg.common.consts.AnalyticsConstants;
import com.nu.art.cyborg.common.consts.ComponentType;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.common.utils.DynamicStringsResolver;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgModule;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleDescriptor(
		usesPermissions = {})
public final class UtilsModule
		extends CyborgModule
		implements AnalyticsConstants {

	private static final String SCHEME = "package";

	private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";

	private static final String APP_PKG_NAME_22 = "pkg";

	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";

	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	private static final String ANDROID_SETTINGS_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";

	private final String AppPackageNameKey = (Build.VERSION.SDK_INT == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);

	public static final StringResourceResolver CompleteActionWithTitle = new DynamicStringsResolver(R.string.UtilsModule_IntentResolverDefaultTitle);

	@Override
	protected void init() {}

	/**
	 * Creates a custom intent chooser <b>WITH</b> the option to set as default
	 *
	 * @param url The url invoking the intent.
	 */
	public final void openWebBrowser(String url, String... ignorePackages) {
		openWebBrowser(false, null, url, ignorePackages);
	}

	/**
	 * Creates a custom intent chooser <b>WITHOUT</b> the option to set as default
	 *
	 * @param title The title of the app chooser.
	 * @param url   The url invoking the intent.
	 */
	public final void openWebBrowser(StringResourceResolver title, String url, String... ignorePackages) {
		openWebBrowser(title != null, title, url, ignorePackages);
	}

	private final void openWebBrowser(boolean customChooser, StringResourceResolver title, String url, String... ignorePackages) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		invokeIntent(customChooser, title, intent, ignorePackages);
	}

	public final void invokeIntent(final boolean customChooser, final StringResourceResolver title, final Intent intent, final String... ignorePackages) {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(CyborgActivityBridge activity) {
				Intent finalIntent = intent;
				if (customChooser) {
					// if (ignorePackages.length == 0) {
					StringResourceResolver finalTitle = title;
					if (finalTitle == null)
						finalTitle = CompleteActionWithTitle;
					finalIntent = Intent.createChooser(intent, getString(finalTitle));
					// } else {
					// List<ResolveInfo> intents = application.getPackageManager().queryIntentActivities(finalIntent,
					// 0);
					//
					// }
				}
				try {
					activity.startActivity(finalIntent);
				} catch (Exception e) {
					sendException("Unable to start activity: " + ExceptionTools.getStackTrace(e), e, false);
				}
			}
		});
	}

	public final Intent getChooserIntentForMimeTypes(StringResourceResolver title, String... mimeTypes) {
		List<Intent> intents = new ArrayList<>();
		for (int i = 0; i < mimeTypes.length; i++) {
			String mimeType = mimeTypes[i];
			appendExtraIntentsForMimeType(intents, mimeType);
			if (i == 0)
				continue;

			Intent mimeTypePickerIntent = new Intent(Intent.ACTION_PICK);
			mimeTypePickerIntent.setType(mimeType);
			queryIntentsToList(intents, mimeTypePickerIntent);
		}

		Intent mimeTypePickerIntent = new Intent(Intent.ACTION_PICK);
		mimeTypePickerIntent.setType(mimeTypes[0]);

		final Intent chooserIntent = Intent.createChooser(mimeTypePickerIntent, getString(title));

		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Intent[intents.size()]));
		return chooserIntent;
	}

	private void appendExtraIntentsForMimeType(List<Intent> intents, String mimeType) {
		Intent actionIntent;

		if (mimeType.matches("image/.*")) {
			actionIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		} else if (mimeType.matches("video/.*")) {
			actionIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		} else
			return;

		queryIntentsToList(intents, actionIntent);
	}

	private void queryIntentsToList(List<Intent> intents, Intent actionIntent) {
		PackageManager packageManager = cyborg.getPackageManager();
		List<ResolveInfo> resolvedApps = packageManager.queryIntentActivities(actionIntent, 0);
		for (ResolveInfo resolvedApp : resolvedApps) {
			String packageName = resolvedApp.activityInfo.packageName;
			Intent intent = new Intent(actionIntent);
			intent.setComponent(new ComponentName(resolvedApp.activityInfo.packageName, resolvedApp.activityInfo.name));
			intent.setPackage(packageName);
			if (intents.contains(intent))
				continue;
			intents.add(intent);
		}
	}

	public final void openMyApplicationSettingsScreen() {
		openMyApplicationSettingsScreen(cyborg.getApplicationContext());
	}

	public final void openMyApplicationSettingsScreen(Context context) {
		openApplicationDetailsScreen(context, cyborg.getPackageName());
	}

	public final void openApplicationDetailsScreen(Context context, String appPackageName) {
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // above 2.3
			intent.setAction(ANDROID_SETTINGS_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts(SCHEME, appPackageName, null);
			intent.setData(uri);
		} else { // below 2.3
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
			intent.putExtra(AppPackageNameKey, appPackageName);
		}
		if (context == null)
			context = cyborg.getApplicationContext();

		if (context instanceof Application) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}

	/**
	 * Check if any default activity is set to fulfill the supplied intent.<br>
	 * NOTE: That there is also a system activity that fulfill this intent, the application chooser activity, which is not included in the check, e.g. if the
	 * Application Chooser activity is set, this method would return null.
	 *
	 * @param intent The intent to check.
	 *
	 * @return Whether or not a default activity is set for this sort of intent.
	 */
	public final ComponentName getDefaultComponentForIntent(ComponentType type, Intent intent, IntentFilter... intentFilter) {
		PackageManager packageManager = cyborg.getPackageManager();

		List<ComponentName> activities = new ArrayList<>();
		List<IntentFilter> filters = new ArrayList<>(Arrays.asList(intentFilter));

		/**
		 * Returns all the default activities in the system!
		 */
		packageManager.getPreferredActivities(filters, activities, null);
		List<ResolveInfo> fulfillingComponents;
		switch (type) {

			case Activity:
				fulfillingComponents = packageManager.queryIntentActivities(intent, 0);
				break;
			case BroadcastReceiver:
				fulfillingComponents = packageManager.queryBroadcastReceivers(intent, 0);
				break;
			case Service:
				fulfillingComponents = packageManager.queryIntentServices(intent, 0);
				break;
			default:
				throw new MUST_NeverHappenedException("Unknown component type...  MUST be a hack!!");
		}

		String name = "";
		for (ComponentName componentName : activities) {
			for (ResolveInfo resolveInfo : fulfillingComponents) {
				switch (type) {
					case Activity:
						name = resolveInfo.activityInfo.name;
						break;
					case BroadcastReceiver:
						name = resolveInfo.activityInfo.name;
						break;
					case Service:
						name = resolveInfo.serviceInfo.name;
						break;
				}

				if (name.equals(componentName.getClassName())) {
					logInfo("Found default Activity: " + componentName.getPackageName() + ":" + componentName.getClassName());
					return componentName;
				}
			}
		}

		return null;
	}

	/**
	 * @param intent The intent to check.
	 *
	 * @return The actual activity that would be invoked by the supplied intent.
	 */
	public final ComponentName getFinalActivityForIntent(Intent intent) {
		PackageManager packageManager = cyborg.getPackageManager();
		ResolveInfo finalActivity = packageManager.resolveActivity(intent, 0);
		return new ComponentName(finalActivity.activityInfo.packageName, finalActivity.activityInfo.name);
	}

	// /**
	// * @param intent The intent to check.
	// * @param activityType The default activity in question.
	// * @return Whether or not the supplied activity is set for this sort of intent.
	// */
	// public final boolean isActivitySetAsDefaultForIntent(Intent intent, Class<? extends Activity> activityType) {
	// ResolveInfo defaults = getDefaultActivityForIntent(intent);
	// return defaults.activityInfo.name.equals(activityType.getName());
	// }
	//
	// public final ResolveInfo getDefaultActivityForIntent(Intent intent) {
	// PackageManager packageManager = application.getPackageManager();
	// return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
	// }

	public final boolean isComponentActive(FullComponentName fullComponent) {
		Intent intent = new Intent();
		intent.setComponent(fullComponent.getComponentName());
		ComponentName result = getFinalActivityForIntent(intent);
		return fullComponent.equals(result);
	}

	public final void enableComponent(FullComponentName fullComponent) {
		PackageManager pm = cyborg.getPackageManager();
		pm.setComponentEnabledSetting(fullComponent.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	public final void returnComponentToDefaultState(FullComponentName fullComponent) {
		PackageManager pm = cyborg.getPackageManager();
		pm.setComponentEnabledSetting(fullComponent.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
	}

	public final void disableComponent(FullComponentName fullComponent) {
		PackageManager pm = cyborg.getPackageManager();
		pm.setComponentEnabledSetting(fullComponent.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

	public final void openApplicationInPlayStore(final String packageName) {
		openApplicationInPlayStore(packageName, 0);
	}

	public final void openApplicationInPlayStore(final String packageName, final int flags) {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(CyborgActivityBridge activity) {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
					intent.setFlags(flags);
					activity.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					logError("Google Play-Store is not accessible", e);
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
						intent.setFlags(flags);
						activity.startActivity(intent);
					} catch (ActivityNotFoundException e1) {
						logError("Google Play-Store is not accessible", e1);
					}
				}
			}
		});
	}

	public final void searchPlayStoreByPublisherName(final String publisherName) {
		searchPlayStoreByPublisherName(publisherName, 0);
	}

	public final void searchPlayStoreByPublisherName(final String publisherName, final int flags) {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(CyborgActivityBridge activity) {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + publisherName));
					intent.setFlags(flags);
					activity.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					logError("Google Play-Store is not accessible", e);
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/search?q=pub:" + publisherName));
						intent.setFlags(flags);
						activity.startActivity(intent);
					} catch (ActivityNotFoundException e1) {
						logError("Google Play-Store is not accessible", e1);
					}
				}
			}
		});
	}

	/**
	 * @return The system battery level between 0.0f - 1.0f
	 */
	public final float calculateBattery() {
		Intent intent = cyborg.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		float level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		float percent = (level * 100) / scale;
		return percent;
	}

	/**
	 * @return Whether or not the mobile device has internet access.
	 */
	public final boolean hasConnectiity() {
		Socket socket = null;
		try {
			socket = new Socket("google.com", 80);
			return true;
		} catch (Exception e) {
			logError("Ignoring...", e);
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					logWarning("Ignoring...", e);
				}
		}
		return false;
	}
}
