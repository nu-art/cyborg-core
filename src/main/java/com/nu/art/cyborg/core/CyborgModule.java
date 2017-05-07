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

package com.nu.art.cyborg.core;

import android.app.Application;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Handler;
import android.view.animation.Animation;
import android.widget.Toast;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.abs.CyborgDelegator;
import com.nu.art.cyborg.core.modules.ThreadsModule;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModuleItem;
import com.nu.art.modular.core.ValidationResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;

/**
 * The concept behind this object is to encapsulate a feature, I've covered three types of feature <b>groups</b> so far:<br>
 * <ol>
 * <li>Services of the <b>Android framework</b>:</li>
 * <ul>
 * <li>Network</li>
 * <li>Location</li>
 * <li>Sensors</li>
 * <li>Google Maps</li>
 * <li>Other...</li>
 * </ul>
 * <li>Also used to encapsulate <b>3rd party feature</b>:</li>
 * <ul>
 * <li>PayPal</li>
 * <li>Google Analytics</li>
 * <li>AdMob</li>
 * <li>Other...</li>
 * </ul>
 * <li>I've also added/implemented <b>new featured</b> modules:</li>
 * <ul>
 * <li>Crash report</li>
 * <li>Automation</li>
 * <li>Other application level modules...</li>
 * </ul>
 * </ol>
 * <br>
 * Each of these modules deals with its own unique feature, and rarely has the need to interacte with another, although it is possible.<br>
 * <p/>
 * In order to make your own Cyborg module:
 * <ol>
 * <li>Extend this {@link CyborgModule}</li>
 * <li>Add the your new module type to your application's {@link CyborgModulesBuilder}</li>
 * <li>Override the <b>injectMembers()</b> method in your new module</li>
 * <li>Implement your module.</li>
 * </ol>
 * <br>
 * <b>TIP:</b> <br>
 * Let the module lead you as you develop...<br>
 * Try to access it from parts of the code, to see the functions your module requires<br>
 * In the injectMembers method, perform all the initialization required... <br>
 * Everything you'll need dynamically should be available for you from the superclass<br>
 * Any static data you want to set, is done in the module registration level.<br>
 * <br>
 *
 * @author TacB0sS
 */
@SuppressWarnings("unused")
public abstract class CyborgModule
		extends Module
		implements CyborgDelegator {

	public static final String GSF_Services = "com.google.android.providers.gsf.permission.READ_GSERVICES";

	public static final String PackageNameVariable = "${package-name}";

	public static final Random UtilsRandom = new Random();

	public static final Random RandomUtils = new Random();

	public static short getNextRandomPositiveShort() {
		return (short) UtilsRandom.nextInt(Short.MAX_VALUE);
	}

	private final String TAG = getClass().getSimpleName();

	protected Cyborg cyborg;

	final void setCyborg(Cyborg cyborg) {
		this.cyborg = cyborg;
	}

	protected final void registerReceiver(Class<? extends CyborgReceiver<?>> receiverType, String... actions) {
		cyborg.registerReceiver(receiverType, actions);
	}

	protected final void unregisterReceiver(Class<? extends CyborgReceiver<?>> receiverType) {
		cyborg.unregisterReceiver(receiverType);
	}

	public final boolean isInEditMode() {
		return CyborgImpl.inEditMode;
	}

	protected void validateModule(ValidationResult result) {
		Class<? extends CyborgModule> moduleType = getClass();
		ModuleDescriptor descriptor = moduleType.getAnnotation(ModuleDescriptor.class);
		if (descriptor == null) {
			result.addEntry(this, "MISSING " + ModuleDescriptor.class.getName());
			return;
		}
		Class<? extends CyborgModule>[] dependencies = descriptor.dependencies();
		for (Class<? extends CyborgModule> dependencyModuleType : dependencies) {
			if (getModule(dependencyModuleType) == null) {
				result.addEntry(this, "MISSING Dependency Module of type: '" + dependencyModuleType.getName() + "'");
			}
		}

		String[] usesPermissions = descriptor.usesPermissions();
		for (String permission : usesPermissions) {
			boolean optional = false;
			if (permission.startsWith("?")) {
				optional = true;
				permission = permission.replace("?", "");
			}

			permission = replaceRuntimeVariables(permission);
			if (!checkUsesPermission(permission)) {
				logWarning("Permissions check returned NEGATIVE: " + permission);
				if (!optional)
					result.addEntry(this, "<uses-permission android:name=\"" + permission + "\" />");
			}
		}

		String[] definedPermissions = descriptor.definedPermissions();
		for (String permission : definedPermissions) {
			permission = replaceRuntimeVariables(permission);
			if (!checkDefinedPermission(permission)) {
				result.addEntry(this, "<permission android:name=\"" + permission + "\"android:protectionLevel=${level} />");
			}
		}
	}

	private boolean checkDefinedPermission(String permission) {
		try {
			cyborg.getPackageManager().getPermissionInfo(permission, PackageManager.GET_META_DATA);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	private String replaceRuntimeVariables(String permission) {
		return permission.replace(CyborgModule.PackageNameVariable, cyborg.getPackageName());
	}

	protected final boolean checkUsesPermission(String permission) {
		return cyborg.getPackageManager().checkPermission(permission, cyborg.getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}

	protected final boolean isPackageInstalled(String packageName) {
		try {
			cyborg.getPackageManager().getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	protected final void printDetails() {
		logInfo("----------- " + getClass().getSimpleName() + " ------------");
		printModuleDetails();
		logInfo("-------- End of " + getClass().getSimpleName() + " --------");
	}

	protected final void reCreateScreen() {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(CyborgActivityBridge activity) {
				activity.reCreateScreen();
			}
		});
	}

	@Override
	protected final <Type extends ModuleItem> Type instantiateModuleItem(Class<Type> moduleItemType) {
		Type moduleItem = super.instantiateModuleItem(moduleItemType);
		if (moduleItem instanceof CyborgModuleItem)
			((CyborgModuleItem) moduleItem).setCyborg(cyborg);
		return moduleItem;
	}

	protected void printModuleDetails() {
	}

	@Override
	public final long elapsedTimeMillis() {
		return cyborg.elapsedTimeMillis();
	}

	@Override
	public final InputStream getRawResources(int resourceId) {
		return getResources().openRawResource(resourceId);
	}

	@Override
	public Application getApplication() {
		return cyborg.getApplication();
	}

	@Override
	public final Resources getResources() {
		return cyborg.getResources();
	}

	@Override
	public final ContentResolver getContentResolver() {
		return cyborg.getContentResolver();
	}

	@Override
	public final <Service> Service getSystemService(ServiceType<Service> service) {
		return cyborg.getSystemService(service);
	}

	@Override
	public final Locale getLocale() {
		return cyborg.getLocale();
	}

	@Override
	public final int dpToPx(int dp) {
		return cyborg.dpToPx(dp);
	}

	@Override
	public final float getDimension(int dimensionId) {
		return cyborg.getDimension(dimensionId);
	}

	@Override
	public final float getDynamicDimension(int type, float size) {
		return cyborg.getDynamicDimension(type, size);
	}

	@Override
	public final int getColor(int colorId) {
		return cyborg.getColor(colorId);
	}

	/*
	 * Interfaces .....
	 */
	@Override
	public final void postOnUI(int delay, Runnable action) {
		cyborg.postOnUI(delay, action);
	}

	@Override
	public final void removeAndPostOnUI(Runnable action) {
		cyborg.removeAndPostOnUI(action);
	}

	@Override
	public final void removeAndPostOnUI(int delay, Runnable action) {
		cyborg.removeAndPostOnUI(delay, action);
	}

	@Override
	public final void removeActionFromUI(Runnable action) {
		cyborg.removeActionFromUI(action);
	}

	@Override
	public final void postOnUI(Runnable action) {
		cyborg.postOnUI(action);
	}

	public final void postOnBackground(int delay, Runnable action) {
		getModule(ThreadsModule.class).getDefaultHandler("Background").postDelayed(action, delay);
	}

	public final void removeAndPostOnBackground(Runnable action) {
		removeActionFromBackground(action);
		postOnBackground(action);
	}

	public final void removeAndPostOnBackground(int delay, Runnable action) {
		removeActionFromBackground(action);
		postOnBackground(delay, action);
	}

	public final void removeActionFromBackground(Runnable action) {
		getModule(ThreadsModule.class).getDefaultHandler("Background").removeCallbacks(action);
	}

	public final void postOnBackground(Runnable action) {
		getModule(ThreadsModule.class).getDefaultHandler("Background").post(action);
	}

	@Override
	public Animation loadAnimation(int animationId) {
		return cyborg.loadAnimation(animationId);
	}

	@Override
	public final Handler getUI_Handler() {
		return cyborg.getUI_Handler();
	}

	@Override
	public final void toast(int length, String text) {
		cyborg.toast(length, text);
	}

	@Override
	public final void toastDebug(String text) {
		cyborg.toastDebug(text);
	}

	@Override
	public final void toastShort(int stringId, Object... args) {
		cyborg.toast(Toast.LENGTH_SHORT, stringId, args);
	}

	@Override
	public final void toastLong(int stringId, Object... args) {
		cyborg.toast(Toast.LENGTH_LONG, stringId, args);
	}

	@Override
	public final void toast(int length, int stringId, Object... args) {
		cyborg.toast(length, getString(stringId, args));
	}

	@Override
	public void sendEvent(String category, String action, String label, long value) {
		cyborg.sendEvent(category, action, label, value);
	}

	@Override
	public void sendException(String description, Throwable t, boolean crash) {
		cyborg.sendException(description, t, crash);
	}

	@Override
	public void sendView(String viewName) {
		cyborg.sendView(viewName);
	}

	@Override
	@SuppressWarnings( {
			"rawtypes",
			"unchecked"
	})
	public final <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return (ModuleType) cyborg.getModule((Class<CyborgModule>) moduleType);
	}

	@Override
	public final void vibrate(int repeat, long... interval) {
		cyborg.vibrate(repeat, interval);
	}

	@Override
	public final void vibrate(long ms) {
		cyborg.vibrate(ms);
	}

	@Override
	public final String convertNumericString(String numericString) {
		return cyborg.convertNumericString(numericString);
	}

	@Override
	public final InputStream getAsset(String assetName)
			throws IOException {
		return cyborg.getAsset(assetName);
	}

	@Override
	public final String getString(int stringId, Object... params) {
		return cyborg.getString(stringId, params);
	}

	@Override
	public final String getString(StringResourceResolver stringResolver) {
		return cyborg.getString(stringResolver);
	}

	@Override
	public final String getPackageName() {
		return cyborg.getPackageName();
	}

	@Override
	public final void toast(StringResourceResolver stringResolver, int length) {
		cyborg.toast(stringResolver, length);
	}

	@Override
	public final boolean isDebuggable() {
		return cyborg.isDebuggable();
	}

	@Override
	public final boolean isDebugCertificate() {
		return cyborg.isDebugCertificate();
	}

	@Override
	public final void waitForDebugger() {
		cyborg.waitForDebugger();
	}

	@Override
	public final void postActivityAction(ActivityStackAction action) {
		cyborg.postActivityAction(action);
	}

	@Override
	public final <ListenerType> void dispatchEvent(final Class<ListenerType> listenerType, final Processor<ListenerType> processor) {
		cyborg.dispatchEvent(listenerType, processor);
	}
}
