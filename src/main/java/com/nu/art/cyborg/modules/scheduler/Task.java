package com.nu.art.cyborg.modules.scheduler;

import com.nu.art.modular.core.ModuleItem;

/**
 * Created by TacB0sS on 13-Nov 2016.
 */

public abstract class Task<DataType>
	extends ModuleItem {

	protected short id;

	final Class<DataType> dataType;

	protected Task(Class<DataType> dataType) {this.dataType = dataType;}

	@Override
	protected void init() {

	}

	protected abstract void execute(DataType dataType);

	final short getId() {
		return 0;
	}
}
