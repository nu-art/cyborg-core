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

package com.nu.art.cyborg.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.ClassInstantiationRuntimeException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.exceptions.runtime.WhoCalledThis;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.util.HashMap;

final class ReceiversManager
	extends Logger {

	private final Cyborg cyborg;

	private HashMap<Class<? extends CyborgReceiver<?>>, CyborgReceiver<?>> registeredReceivers = new HashMap<>();

	private HashMap<Class<? extends CyborgReceiver<?>>, Boolean> receiversInManifest = new HashMap<>();

	public ReceiversManager(Cyborg cyborg) {
		super();
		this.cyborg = cyborg;
	}

	/*
	 * ToRefactor the actions should go away?? see Bluetooth module for reason why not to.
	 */
	@SuppressWarnings("unchecked")
	final <Receiver extends CyborgReceiver<?>> void registerReceiver(Class<Receiver> receiverType, String... actions) {
		Boolean isInManifest = receiversInManifest.get(receiverType);
		if (isInManifest == null) {
			isInManifest = checkIfBroadcastReceiverIsRegisteredInManifest(receiverType);
			receiversInManifest.put(receiverType, isInManifest);
		}

		if (isInManifest) {
			logWarning("Receiver type '" + receiverType.getSimpleName() + "' is already declared in the manifest, " + "and would NOT be added again!!!");
			return;
		}

		Receiver receiver = (Receiver) registeredReceivers.get(receiverType);
		if (receiver != null) {
			logWarning("", new WhoCalledThis("Attempt to RE-register a receiver '" + receiverType.getSimpleName() + "'"));
			return;
		}
		try {
			receiver = ReflectiveTools.newInstance(receiverType);
		} catch (Exception e) {
			throw new ClassInstantiationRuntimeException(receiverType, e);
		}

		IntentFilter intentFilter = new IntentFilter();

		if (actions == null || actions.length == 0) {
			actions = receiver.getDefaultActions();
		}

		for (String action : actions) {
			intentFilter.addAction(action);
		}
		logInfo("+++-+ Registering Receiver: '" + receiverType.getName() + "'");
		registeredReceivers.put(receiverType, receiver);
		cyborg.getApplicationContext().registerReceiver(receiver, intentFilter);
	}

	public final void enforceBroadcastReceiverInManifest(Class<? extends BroadcastReceiver> receiverType) {
		PackageManager pm = cyborg.getPackageManager();
		try {
			ActivityInfo info = pm.getReceiverInfo(new ComponentName(cyborg.getApplicationContext(), receiverType), PackageManager.GET_DISABLED_COMPONENTS);
			if (info.enabled)
				return;

			throw ExceptionGenerator.receiverWasFoundButIsDisabled(receiverType);
		} catch (NameNotFoundException e) {
			throw ExceptionGenerator.receiverWasNotInManifest(receiverType);
		}
	}

	private <Receiver extends CyborgReceiver<?>> boolean checkIfBroadcastReceiverIsRegisteredInManifest(Class<Receiver> receiverType) {
		PackageManager pm = cyborg.getPackageManager();
		try {
			ActivityInfo info = pm.getReceiverInfo(new ComponentName(cyborg.getApplicationContext(), receiverType), 0);
			return info.enabled;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	final <Receiver extends CyborgReceiver<?>> void unregisterReceiver(Class<Receiver> receiverType) {
		Boolean isInManifest = receiversInManifest.get(receiverType);
		if (isInManifest == null || isInManifest) {
			return;
		}

		Receiver receiver = (Receiver) registeredReceivers.remove(receiverType);
		if (receiver == null) {
			logWarning("Attempt to UN-register a not existing receiver '" + receiverType.getSimpleName() + "'");
			return;
		}

		logInfo("+++-+ Unregistering Receiver: '" + receiverType.getName() + "'");
		cyborg.getApplicationContext().unregisterReceiver(receiver);
	}
}
