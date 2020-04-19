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

package com.nu.art.cyborg.core.modules;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.common.consts.AnalyticsConstants;
import com.nu.art.cyborg.core.CyborgModule;

import java.util.List;

public final class AndroidComponentModule
	extends CyborgModule
	implements AnalyticsConstants {

	public enum ComponentType {
		Activity,
		BroadcastReceiver,
		Service,
		//		Provider,
	}

	private PackageManager packageManager;

	@Override
	protected void init() {
		this.packageManager = cyborg.getPackageManager();
	}

	public AndroidComponentBuilder createComponentBuilder() {
		return new AndroidComponentBuilder();
	}

	public class AndroidComponentBuilder {

		private ComponentType type;
		private String className;
		private String packageName;

		public AndroidComponentBuilder setPackageName(String packageName) {
			this.packageName = packageName;
			return this;
		}

		public AndroidComponentBuilder setClassName(ComponentType type, String className) {
			this.type = type;
			this.className = className;
			return this;
		}

		public AndroidComponentBuilder setClassName(Class<?> className) {
			if (Activity.class.isAssignableFrom(className))
				this.type = ComponentType.Activity;
			else if (BroadcastReceiver.class.isAssignableFrom(className))
				this.type = ComponentType.BroadcastReceiver;
			else if (Service.class.isAssignableFrom(className))
				this.type = ComponentType.Service;
				//			else if (ContentProvider.class.isAssignableFrom(className))
				//				this.type = ComponentType.Provider;
			else
				throw new BadImplementationException("class can only be of Activity || BroadcastReceiver || Service");

			this.className = className.getName();
			return this;
		}

		public final AndroidComponentWrapper build() {
			switch (type) {
				case Activity:
					return new ActivityWrapper(packageName, className);

				case BroadcastReceiver:
					return new ReceiverWrapper(packageName, className);

				case Service:
					return new ServiceWrapper(packageName, className);

				//				case Provider:
				//					return new ProviderWrapper(packageName, className);
				default:
					throw new BadImplementationException("did not specify component or component type...");
			}
		}
	}

	public abstract class AndroidComponentWrapper {

		private String className;
		private String packageName;
		protected final Intent intent;

		public final ComponentName componentName;

		private AndroidComponentWrapper(String packageName, String className) {
			this.packageName = packageName;
			this.className = className;
			componentName = new ComponentName(packageName, className);

			intent = new Intent();
			intent.setComponent(componentName);
		}

		public boolean isEnabled() {
			ResolveInfo result = resolveComponent(intent, 0);
			return compare(result);
		}

		protected boolean compare(ResolveInfo result) {
			if (result == null)
				return false;

			ActivityInfo activityInfo = result.activityInfo;
			if (activityInfo == null)
				return false;

			return compare(activityInfo.packageName, activityInfo.name);
		}

		protected final boolean compare(String packageName, String className) {
			return packageName.equals(this.packageName) && className.equals(this.className);
		}

		protected abstract ResolveInfo resolveComponent(Intent intent, int flags);

		private void changeComponentState(int state) {
			packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
		}

		public void enabled() {
			changeComponentState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
		}

		public void disable() {
			changeComponentState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
		}

		public void reset() {
			changeComponentState(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		}

		protected abstract List<ResolveInfo> queryIntent(Intent intent, int flags);

		public final boolean isQueryableBy(Intent _intent, int flags) {
			Intent intent = new Intent(_intent);

			List<ResolveInfo> fulfillingComponents = queryIntent(intent, flags);
			if (fulfillingComponents == null)
				return false;

			for (ResolveInfo result : fulfillingComponents) {
				if (!compare(result))
					continue;

				return true;
			}

			return false;
		}
	}

	public final class ActivityWrapper
		extends AndroidComponentWrapper {

		private ActivityWrapper(String packageName, String className) {
			super(packageName, className);
		}

		@Override
		protected ResolveInfo resolveComponent(Intent intent, int flags) {
			return packageManager.resolveActivity(intent, flags);
		}

		@Override
		protected List<ResolveInfo> queryIntent(Intent intent, int flags) {
			return packageManager.queryIntentActivities(intent, flags);
		}

		public final boolean isDefault() {
			ResolveInfo result = resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
			return compare(result);
		}

		public final ResolveInfo resolveActivity(Intent intent, int flags) {
			return packageManager.resolveActivity(intent, flags);
		}
	}

	public final class ReceiverWrapper
		extends AndroidComponentWrapper {

		private ReceiverWrapper(String packageName, String className) {
			super(packageName, className);
		}

		@Override
		protected ResolveInfo resolveComponent(Intent intent, int flags) {
			return packageManager.resolveActivity(intent, flags);
		}

		@Override
		protected List<ResolveInfo> queryIntent(Intent intent, int flags) {
			return packageManager.queryBroadcastReceivers(intent, flags);
		}
	}

	public final class ServiceWrapper
		extends AndroidComponentWrapper {

		private ServiceWrapper(String packageName, String className) {
			super(packageName, className);
		}

		protected boolean compare(ResolveInfo result) {
			if (result == null)
				return false;

			ServiceInfo serviceInfo = result.serviceInfo;
			if (serviceInfo == null)
				return false;

			return compare(serviceInfo.packageName, serviceInfo.name);
		}

		@Override
		protected ResolveInfo resolveComponent(Intent intent, int flags) {
			return packageManager.resolveService(intent, flags);
		}

		@Override
		protected List<ResolveInfo> queryIntent(Intent intent, int flags) {
			return packageManager.queryIntentServices(intent, flags);
		}
	}

	//  NOT MATCHING THE PATTERN... FOR NOW!
	//
	//	public final class ProviderWrapper
	//		extends AndroidComponentWrapper {
	//
	//		private ProviderWrapper(String packageName, String className) {
	//			super(packageName, className);
	//		}
	//
	//		protected boolean compare(ResolveInfo result) {
	//			if (result == null)
	//				return false;
	//
	//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
	//				return false;
	//
	//			ProviderInfo providerInfo = result.providerInfo;
	//			if (providerInfo == null)
	//				return false;
	//
	//			return compare(providerInfo.packageName, providerInfo.name);
	//		}
	//
	//		@Override
	//		protected ResolveInfo resolveComponent(Intent intent, int flags) {
	//			return packageManager.resolveContentProvider(intent, flags);
	//		}
	//
	//		@Override
	//		protected List<ResolveInfo> queryIntent(Intent intent, int flags) {
	//			return packageManager.queryContentProviders(intent, flags);
	//		}
	//	}
}
