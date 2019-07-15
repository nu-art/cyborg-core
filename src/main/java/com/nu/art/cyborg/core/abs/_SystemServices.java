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

package com.nu.art.cyborg.core.abs;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.app.job.JobScheduler;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStatsManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.pm.LauncherApps;
import android.hardware.ConsumerIrManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.midi.MidiManager;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaSessionManager;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.DropBoxManager;
import android.os.HardwarePropertiesManager;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.health.SystemHealthManager;
import android.os.storage.StorageManager;
import android.print.PrintManager;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textservice.TextServicesManager;

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

	ServiceType<WindowManager> WindowService = new ServiceType<>(Context.WINDOW_SERVICE);

	ServiceType<LayoutInflater> LayoutInflaterService = new ServiceType<>(Context.LAYOUT_INFLATER_SERVICE);

	ServiceType<ActivityManager> ActivityService = new ServiceType<>(Context.ACTIVITY_SERVICE);

	ServiceType<PowerManager> PowerService = new ServiceType<>(Context.POWER_SERVICE);

	ServiceType<AlarmManager> AlarmService = new ServiceType<>(Context.ALARM_SERVICE);

	ServiceType<NotificationManager> NotificationService = new ServiceType<>(Context.NOTIFICATION_SERVICE);

	ServiceType<KeyguardManager> KeyguardService = new ServiceType<>(Context.KEYGUARD_SERVICE);

	ServiceType<LocationManager> LocationService = new ServiceType<>(Context.LOCATION_SERVICE);

	ServiceType<SearchManager> SearchService = new ServiceType<>(Context.SEARCH_SERVICE);

	ServiceType<SensorManager> SensorService = new ServiceType<>(Context.SENSOR_SERVICE);

	ServiceType<StorageManager> StorageService = new ServiceType<>(Context.STORAGE_SERVICE);

	ServiceType<Vibrator> VibratorService = new ServiceType<>(Context.VIBRATOR_SERVICE);

	ServiceType<ConnectivityManager> ConnectivityService = new ServiceType<>(Context.CONNECTIVITY_SERVICE);

	ServiceType<WifiManager> WifiService = new ServiceType<>(Context.WIFI_SERVICE);

	ServiceType<AudioManager> AudioService = new ServiceType<>(Context.AUDIO_SERVICE);

	ServiceType<MediaRouter> MediaRouterService = new ServiceType<>(Context.MEDIA_ROUTER_SERVICE);

	ServiceType<TelephonyManager> TelephonyService = new ServiceType<>(Context.TELEPHONY_SERVICE);

	ServiceType<SubscriptionManager> SubscriptionService = new ServiceType<>(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

	ServiceType<CarrierConfigManager> CarrierConfigService = new ServiceType<>(Context.CARRIER_CONFIG_SERVICE);

	ServiceType<InputMethodManager> InputMethodService = new ServiceType<>(Context.INPUT_METHOD_SERVICE);

	ServiceType<UiModeManager> UiModeService = new ServiceType<>(Context.UI_MODE_SERVICE);

	ServiceType<DownloadManager> DownloadService = new ServiceType<>(Context.DOWNLOAD_SERVICE);

	ServiceType<BatteryManager> BatteryService = new ServiceType<>(Context.BATTERY_SERVICE);

	ServiceType<JobScheduler> JobSchedulerService = new ServiceType<>(Context.JOB_SCHEDULER_SERVICE);

	ServiceType<NetworkStatsManager> NetworkStatsService = new ServiceType<>(Context.NETWORK_STATS_SERVICE);

	ServiceType<HardwarePropertiesManager> HardwarePropertiesService = new ServiceType<>(Context.HARDWARE_PROPERTIES_SERVICE);

	ServiceType<WifiP2pManager> WifiP2pService = new ServiceType<>(Context.WIFI_P2P_SERVICE);

	ServiceType<ClipboardManager> ClipboardService = new ServiceType<>(Context.CLIPBOARD_SERVICE);

	ServiceType<BluetoothManager> BluetoothService = new ServiceType<>(Context.BLUETOOTH_SERVICE);

	ServiceType<CameraManager> CameraService = new ServiceType<>(Context.CAMERA_SERVICE);

	ServiceType<DevicePolicyManager> PolicyManagerService = new ServiceType<>(Context.DEVICE_POLICY_SERVICE);

	ServiceType<AccountManager> AccountService = new ServiceType<>(Context.ACCOUNT_SERVICE);

	ServiceType<AccessibilityManager> AccessibilityService = new ServiceType<>(Context.ACCESSIBILITY_SERVICE);

	ServiceType<CaptioningManager> CaptioningService = new ServiceType<>(Context.CAPTIONING_SERVICE);

	ServiceType<android.service.wallpaper.WallpaperService> WallpaperService = new ServiceType<>(Context.WALLPAPER_SERVICE);

	ServiceType<NsdManager> NsdDService = new ServiceType<>(Context.NSD_SERVICE);

	ServiceType<FingerprintManager> FingerprintService = new ServiceType<>(Context.FINGERPRINT_SERVICE);

	ServiceType<TelecomManager> TelecomService = new ServiceType<>(Context.TELECOM_SERVICE);

	ServiceType<TextServicesManager> TextServicesService = new ServiceType<>(Context.TEXT_SERVICES_MANAGER_SERVICE);

	ServiceType<AppWidgetManager> AppWidgetService = new ServiceType<>(Context.APPWIDGET_SERVICE);

	ServiceType<DropBoxManager> DropboxService = new ServiceType<>(Context.DROPBOX_SERVICE);

	ServiceType<NfcManager> NfcService = new ServiceType<>(Context.NFC_SERVICE);

	ServiceType<UsbManager> UsbService = new ServiceType<>(Context.USB_SERVICE);

	ServiceType<LauncherApps> LauncherAppsService = new ServiceType<>(Context.LAUNCHER_APPS_SERVICE);

	ServiceType<InputManager> InputService = new ServiceType<>(Context.INPUT_SERVICE);

	ServiceType<DisplayManager> DisplayService = new ServiceType<>(Context.DISPLAY_SERVICE);

	ServiceType<UserManager> UserService = new ServiceType<>(Context.USER_SERVICE);

	ServiceType<RestrictionsManager> RestrictionsService = new ServiceType<>(Context.RESTRICTIONS_SERVICE);

	ServiceType<AppOpsManager> AppOpsService = new ServiceType<>(Context.APP_OPS_SERVICE);

	ServiceType<PrintManager> PrintService = new ServiceType<>(Context.PRINT_SERVICE);

	ServiceType<ConsumerIrManager> ConsumerIrService = new ServiceType<>(Context.CONSUMER_IR_SERVICE);

	ServiceType<TvInputManager> TvInputService = new ServiceType<>(Context.TV_INPUT_SERVICE);

	ServiceType<UsageStatsManager> UsageStatsService = new ServiceType<>(Context.USAGE_STATS_SERVICE);

	ServiceType<MediaSessionManager> MediaSessionService = new ServiceType<>(Context.MEDIA_SESSION_SERVICE);

	ServiceType<MediaProjectionManager> MediaProjectionService = new ServiceType<>(Context.MEDIA_PROJECTION_SERVICE);

	ServiceType<MidiManager> MidiService = new ServiceType<>(Context.MIDI_SERVICE);

	ServiceType<SystemHealthManager> SystemHealthService = new ServiceType<>(Context.SYSTEM_HEALTH_SERVICE);

	//	ServiceType<ShortcutManager> ShortcutService = new ServiceType<>(Context.SHORTCUT_SERVICE); // API 25
	//	ServiceType<StorageStatsManager> StorageStatsService = new ServiceType<>(Context.STORAGE_STATS_SERVICE); // API 26
	//	ServiceType<CompanionDeviceManager> CompanionDeviceService = new ServiceType<>(Context.COMPANION_DEVICE_SERVICE); // API 26
	//	ServiceType<TextClassificationManager> TextClassificationService = new ServiceType<>(Context.TEXT_CLASSIFICATION_SERVICE); // API 26
	//	ServiceType<WifiRttManager> WifiRttRangingService = new ServiceType<>(Context.WIFI_RTT_RANGING_SERVICE); // API 28
	//	ServiceType<CrossProfileApps> CrossProfileAppService = new ServiceType<>(Context.CROSS_PROFILE_APPS_SERVICE); // API 28

	/**
	 * Get Android's Service without casting.
	 *
	 * @param service   The {@link ServiceType} to get.
	 * @param <Service> A generic boundary to the service class type
	 *
	 * @return The instance of Android's service.
	 */
	<Service> Service getSystemService(ServiceType<Service> service);
}
