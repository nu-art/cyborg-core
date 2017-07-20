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

	private static ChargingSource batteryChargingSource;

	private static ChargingState batteryChargingState;

	private static BatteryHealth batteryHealthState;

	private static int batteryPercentage;

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
		ChargingWireless(BatteryManager.BATTERY_PLUGGED_WIRELESS);

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
		Full(BatteryManager.BATTERY_STATUS_FULL);

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
		Cold(BatteryManager.BATTERY_HEALTH_COLD);

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

	private void processBatteryChanged(Intent batteryStatusIntent) {
		setChargingSource(ChargingSource.getState(batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)));
		setChargingState(ChargingState.getState(batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)));
		setBatteryHealthState(BatteryHealth.getState(batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)));

		int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		if (level > 0 && scale > 0)
			batteryPercentage = (int) ((level / (float) scale) * 100);
		else
			batteryPercentage = -1;

		dispatchGlobalEvent("Battery state changed.", BatteryStateListener.class, new Processor<BatteryStateListener>() {
			@Override
			public void process(BatteryStateListener batteryStateListener) {
				batteryStateListener.onBatteryStateChanged();
			}
		});
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

	private static void setChargingSource(ChargingSource source) {
		batteryChargingSource = source;
	}

	private static void setChargingState(ChargingState chargingState) {
		batteryChargingState = chargingState;
	}

	private static void setBatteryHealthState(BatteryHealth healthState) {
		batteryHealthState = healthState;
	}

	public ChargingSource getChargingSource() {
		return batteryChargingSource;
	}

	public ChargingState getBatteryChargingState() {
		return batteryChargingState;
	}

	public BatteryHealth getBatteryHealthState() {
		return batteryHealthState;
	}

	/**
	 * @return Battery charge percentange, 0 is empty, 100 is full.
	 */
	public int getBatteryPercentage() {
		return batteryPercentage;
	}
}
