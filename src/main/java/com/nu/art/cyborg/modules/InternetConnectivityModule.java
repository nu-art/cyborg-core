package com.nu.art.cyborg.modules;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.net.InetAddress;

public class InternetConnectivityModule
		extends CyborgModule {

	private static final String DEFAULT_HOST = "google.com";

	private Handler handler;

	public interface InternetConnectivityListener {

		void onInternetConnectivityLost();

		void onInternetConnectivityGained();
	}

	@Override
	protected void init() {
		handler = getModule(ThreadsModule.class).getDefaultHandler("Internet Check");
		registerReceiver(ConnectivityCheckReceiver.class);
	}

	private void checkInternetConnectionAsync() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress.getByName(DEFAULT_HOST);
					dispatchConnectivityGained();
				} catch (Exception e) {
					logError("Couldn't ping google.com", e);
					dispatchConnectivityLost();
				}
			}
		});
	}

	private static class ConnectivityCheckReceiver
			extends CyborgReceiver<InternetConnectivityModule> {

		protected ConnectivityCheckReceiver() {
			super(InternetConnectivityModule.class, ConnectivityManager.CONNECTIVITY_ACTION);
		}

		@Override
		protected void onReceive(Intent intent, InternetConnectivityModule module) {
			module.checkInternetConnectionAsync();
		}
	}

	private void dispatchConnectivityGained() {
		dispatchEvent("Internet Check - Has Internet", new Processor<InternetConnectivityListener>() {
			@Override
			public void process(InternetConnectivityListener listener) {
				listener.onInternetConnectivityGained();
			}
		});
	}

	private void dispatchConnectivityLost() {
		dispatchEvent("Internet Check - No Internet", new Processor<InternetConnectivityListener>() {
			@Override
			public void process(InternetConnectivityListener listener) {
				listener.onInternetConnectivityLost();
			}
		});
	}
}