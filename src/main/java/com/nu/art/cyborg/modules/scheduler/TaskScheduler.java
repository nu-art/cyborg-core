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

package com.nu.art.cyborg.modules.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.interfaces.Serializer;
import com.nu.art.cyborg.core.CyborgModule;

/**
 * In Order for this to work you need to enable the {@link TasksReceiver} in your manifest
 */
public class TaskScheduler
	extends CyborgModule {

	private Serializer<Object, String> serializer;
	private static final String Key_TaskType = "TaskType";
	private static final String Key_TaskData = "TaskData";
	private AlarmManager alarmManager;

	public void setSerializer(Serializer<Object, String> serializer) {
		this.serializer = serializer;
	}

	@Override
	protected void init() {
		alarmManager = getSystemService(AlarmService);
	}

	public final <DataType> PendingIntent scheduleTask(Class<? extends Task<DataType>> taskType, DataType data, short id, long utcWakeupTime) {
		Intent taskIntent = new Intent(getApplicationContext(), TasksReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putString(Key_TaskType, taskType.getName());
		if (data != null)
			bundle.putString(Key_TaskData, serializer.serialize(data));

		taskIntent.putExtras(bundle);

		PendingIntent taskPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, taskIntent, PendingIntent.FLAG_ONE_SHOT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, utcWakeupTime, taskPendingIntent);

		return taskPendingIntent;
	}

	public final void cancel(PendingIntent taskPendingIntent) {
		alarmManager.cancel(taskPendingIntent);
	}

	@SuppressWarnings("unchecked")
	public <DataType> void process(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			logError("Unexpected behaviour, bundle is null..");
			return;
		}

		String taskTypeClassName = bundle.getString(Key_TaskType);
		Task<DataType> task;
		try {
			Class<? extends Task<DataType>> taskType = (Class<? extends Task<DataType>>) Class.forName(taskTypeClassName);
			task = instantiateModuleItem(taskType);
		} catch (Exception e1) {
			throw new BadImplementationException("Cannot find class type: " + taskTypeClassName, e1);
		}

		DataType data = null;
		String dataAsString = bundle.getString(Key_TaskData, null);

		logInfo("processing action: " + taskTypeClassName + " with data: " + dataAsString);

		if (task.dataType != Void.class && dataAsString != null) {
			if (task.dataType == String.class)
				data = (DataType) dataAsString;
			else
				data = (DataType) serializer.deserialize(dataAsString, task.dataType);
		}

		task.execute(data);
	}
}
