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

package com.nu.art.cyborg.core.abs;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

@SuppressWarnings("unused")
public interface _SystemServices {

	final class ServiceType<ServiceClass> {

		private final String key;

		public ServiceType(String key) {
			super();
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	ServiceType<NotificationManager> NotificationService = new ServiceType<NotificationManager>(Context.NOTIFICATION_SERVICE);

	ServiceType<KeyguardManager> KeyguardService = new ServiceType<KeyguardManager>(Context.KEYGUARD_SERVICE);

	ServiceType<InputMethodManager> InputMethodService = new ServiceType<InputMethodManager>(Context.INPUT_METHOD_SERVICE);

	ServiceType<WindowManager> WindowService = new ServiceType<WindowManager>(Context.WINDOW_SERVICE);

	ServiceType<ActivityManager> ActivityService = new ServiceType<ActivityManager>(Context.ACTIVITY_SERVICE);

	ServiceType<ClipboardManager> ClipboardService_Pre_11 = new ServiceType<ClipboardManager>(Context.CLIPBOARD_SERVICE);

	ServiceType<PowerManager> PowerService = new ServiceType<PowerManager>(Context.POWER_SERVICE);

	ServiceType<LocationManager> LocationService = new ServiceType<LocationManager>(Context.LOCATION_SERVICE);

	ServiceType<TelephonyManager> TelephonyService = new ServiceType<TelephonyManager>(Context.TELEPHONY_SERVICE);

	ServiceType<WifiManager> WifiService = new ServiceType<WifiManager>(Context.WIFI_SERVICE);

	ServiceType<ConnectivityManager> ConnectivityService = new ServiceType<ConnectivityManager>(Context.CONNECTIVITY_SERVICE);

	ServiceType<AudioManager> AudioService = new ServiceType<AudioManager>(Context.AUDIO_SERVICE);

	ServiceType<Vibrator> VibratorService = new ServiceType<Vibrator>(Context.VIBRATOR_SERVICE);

	ServiceType<LayoutInflater> LayoutInflaterService = new ServiceType<LayoutInflater>(Context.LAYOUT_INFLATER_SERVICE);

	ServiceType<AlarmManager> AlarmService = new ServiceType<AlarmManager>(Context.ALARM_SERVICE);

	<Service> Service getSystemService(ServiceType<Service> service);
}
