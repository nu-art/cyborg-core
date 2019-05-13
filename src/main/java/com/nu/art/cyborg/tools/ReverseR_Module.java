package com.nu.art.cyborg.tools;

import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.util.HashMap;

public final class ReverseR_Module
	extends CyborgModule {

	@Override
	protected void init() {
		try {
			resourceMap = new ResourcesMap(Class.forName(getPackageName() + ".R"));
		} catch (Throwable e) {
			resourceMap = new ResourcesMap();
		}
	}

	/**
	 * Maps the R class to its ResourcesMap
	 */
	private ResourcesMap resourceMap;

	public synchronized final String getName(ResourceType resourceType, int id) {
		String name = resourceMap.getName(resourceType, id);

		if (name != null)
			return name;

		return "No resource '" + resourceType.type + "' name associated with id: " + id;
		//		throw new IllegalArgumentException("Could not find field name for ResourceType '" + resourceType + "' && Id '" + id + "'");
	}

	public synchronized final int getId(ResourceType resourceType, String name) {
		Integer id = resourceMap.getId(resourceType, name);

		if (id != null)
			return id;

		return -1;
		//		throw new IllegalArgumentException("Could not find field Id for ResourceType '" + resourceType + "' && Field Name '" + name + "'");
	}

	private static final class ValuesMap {

		/**
		 * A constant to class field name mapping.
		 */
		private HashMap<Object, Object> resourcesValues;

		ValuesMap() {
			resourcesValues = new HashMap<>();
		}

		ValuesMap(Class<?> resourceClass) {
			resourcesValues = ReflectiveTools.getFieldsCrossMappings(resourceClass);
		}

		String getName(int id) {
			return (String) resourcesValues.get(id);
		}

		Integer getId(String name) {
			return (Integer) resourcesValues.get(name);
		}
	}

	private static final class ResourcesMap {

		private final Class<?> rClass;
		/**
		 * An inner resources class name to its values map.
		 */
		private HashMap<ResourceType, ValuesMap> resourcesValuesMap = new HashMap<>();

		ResourcesMap() {
			rClass = null;
		}

		ResourcesMap(Class<?> rClass) {
			this.rClass = rClass;
		}

		String getName(ResourceType resourceType, int id) {
			return getOrCreateValuesMap(resourceType).getName(id);
		}

		Integer getId(ResourceType resourceType, String name) {
			return getOrCreateValuesMap(resourceType).getId(name);
		}

		private ValuesMap getOrCreateValuesMap(ResourceType resourceType) {
			ValuesMap valuesMap = resourcesValuesMap.get(resourceType);
			if (valuesMap == null) {
				if (rClass == null)
					valuesMap = new ValuesMap(null);
				else {
					Class<?>[] resourcesClasses = rClass.getDeclaredClasses();
					for (Class<?> resourceClass : resourcesClasses) {
						if (!resourceClass.getSimpleName().equals(resourceType.type))
							continue;

						valuesMap = new ValuesMap(resourceClass);
					}
				}
			}

			resourcesValuesMap.put(resourceType, valuesMap);
			return valuesMap;
		}
	}
}