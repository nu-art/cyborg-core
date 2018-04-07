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

package com.nu.art.cyborg.modules;

import android.content.Intent;
import android.os.BatteryManager;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.Condition;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.reflection.tools.ReflectiveTools;

/**
 * Created by matankoby on 7/18/17.
 */

public class PowerModule
	extends CyborgModule {

	private ChargingSource chargingSource;

	private ChargingState chargingState;

	private BatteryHealth batteryHealth;

	private int batteryLevel;

	private static final String[] DefaultActions = new String[]{
		Intent.ACTION_POWER_CONNECTED,
		Intent.ACTION_POWER_DISCONNECTED,
		Intent.ACTION_BATTERY_CHANGED
	};

	/**
	 * Plugged power source type, or unplugged.
	 */
	public enum ChargingSource {
		Error(-1),
		None(0),
		ChargingUSB(BatteryManager.BATTERY_PLUGGED_USB),
		ChargingAC(BatteryManager.BATTERY_PLUGGED_AC),
		ChargingWireless(4),
		// BatteryManager.BATTERY_PLUGGED_WIRELESS
		//
		;

		private final int status;

		ChargingSource(int status) {this.status = status;}

		static ChargingSource getState(final int status) {
			return ReflectiveTools.findMatchingEnumItem(ChargingSource.class, new Condition<ChargingSource>() {
				@Override
				public boolean checkCondition(ChargingSource batteryChargingSource) {
					return batteryChargingSource.status == status;
				}
			});
		}
	}

	public enum ChargingState {
		Error(-1),
		Unknown(BatteryManager.BATTERY_STATUS_UNKNOWN),
		Charging(BatteryManager.BATTERY_STATUS_CHARGING),
		Discharging(BatteryManager.BATTERY_STATUS_DISCHARGING),
		NotCharging(BatteryManager.BATTERY_STATUS_NOT_CHARGING),
		Full(BatteryManager.BATTERY_STATUS_FULL),
		//
		;

		private final int status;

		ChargingState(int status) {this.status = status;}

		static ChargingState getState(final int status) {
			return ReflectiveTools.findMatchingEnumItem(ChargingState.class, new Condition<ChargingState>() {
				@Override
				public boolean checkCondition(ChargingState batteryChargingState) {
					return batteryChargingState.status == status;
				}
			});
		}
	}

	public enum BatteryHealth {
		Error(-1),
		Unknown(BatteryManager.BATTERY_HEALTH_UNKNOWN),
		Good(BatteryManager.BATTERY_HEALTH_GOOD),
		Overheat(BatteryManager.BATTERY_HEALTH_OVERHEAT),
		Dead(BatteryManager.BATTERY_HEALTH_DEAD),
		OverVoltage(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE),
		UnspecifiedFailure(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE),
		Cold(BatteryManager.BATTERY_HEALTH_COLD),
		//
		;

		private final int status;

		BatteryHealth(int status) {this.status = status;}

		static BatteryHealth getState(final int status) {
			return ReflectiveTools.findMatchingEnumItem(BatteryHealth.class, new Condition<BatteryHealth>() {
				@Override
				public boolean checkCondition(BatteryHealth batteryHealth) {
					return batteryHealth.status == status;
				}
			});
		}
	}

	@Override
	protected void init() {}

	public void initPower_IndicatorReceiver() {
		registerReceiver(Power_IndicatorReceiver.class);
	}

	@SuppressWarnings("ConstantConditions")
	private void processBatteryChanged(Intent batteryStatusIntent) {
		boolean hasChanged = false;

		hasChanged |= setBatteryLevel(batteryStatusIntent);
		hasChanged |= setChargingSource(batteryStatusIntent);
		hasChanged |= setChargingState(batteryStatusIntent);
		hasChanged |= setHealthState(batteryStatusIntent);

		if (!hasChanged)
			return;

		dispatchGlobalEvent("Battery state changed.", new Processor<BatteryStateListener>() {
			@Override
			public void process(BatteryStateListener batteryStateListener) {
				batteryStateListener.onBatteryStateChanged();
			}
		});
	}

	private boolean setHealthState(Intent batteryStatusIntent) {
		BatteryHealth _batteryHealthState = batteryHealth;
		batteryHealth = BatteryHealth.getState(batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1));
		return _batteryHealthState != batteryHealth;
	}

	private boolean setChargingState(Intent batteryStatusIntent) {
		ChargingState _batteryChargingState = chargingState;
		chargingState = ChargingState.getState(batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
		return _batteryChargingState != chargingState;
	}

	private boolean setChargingSource(Intent batteryStatusIntent) {
		ChargingSource _batteryChargingSource = chargingSource;
		chargingSource = ChargingSource.getState(batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1));
		return _batteryChargingSource != chargingSource;
	}

	private boolean setBatteryLevel(Intent batteryStatusIntent) {
		int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		int previousBatteryLevel = batteryLevel;
		if (level > 0 && scale > 0)
			batteryLevel = (int) ((level / (float) scale) * 100);
		else
			batteryLevel = -1;
		return previousBatteryLevel != batteryLevel;
	}

	public interface BatteryStateListener {

		void onBatteryStateChanged();
	}

	private static class Power_IndicatorReceiver
		extends CyborgReceiver<PowerModule> {

		protected Power_IndicatorReceiver() {
			super(PowerModule.class, DefaultActions);
		}

		@Override
		protected void onReceive(Intent batteryStatusIntent, PowerModule module) {
			String action = batteryStatusIntent.getAction();
			switch (action) {
				case Intent.ACTION_BATTERY_CHANGED:
					module.processBatteryChanged(batteryStatusIntent);
					break;
				case Intent.ACTION_POWER_CONNECTED:
					// Raised when the device connects to a power source. Wakes the application.
					break;
				case Intent.ACTION_POWER_DISCONNECTED:
					// Raised when the device disconnectes from a power source. Wakes the application.
					break;
			}
		}
	}

	public ChargingSource getChargingSource() {
		return chargingSource;
	}

	public ChargingState getChargingState() {
		return chargingState;
	}

	public BatteryHealth getBatteryHealth() {
		return batteryHealth;
	}

	/**
	 * @return Battery charge percentange, 0 is empty, 100 is full.
	 */
	public int getBatteryPercentage() {
		return batteryLevel;
	}
}
