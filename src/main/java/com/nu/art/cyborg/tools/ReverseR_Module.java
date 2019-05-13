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
		String name;
		name = resourceMap.getName(resourceType, id);

		if (name != null)
			return name;

		return "No name associated with id: " + id;
		//		throw new IllegalArgumentException("Could not find field name for ResourceType '" + resourceType + "' && Id '" + id + "'");
	}

	public synchronized final int getId(ResourceType resourceType, String name) {
		Integer id;
		id = resourceMap.getId(resourceType, name);

		if (id != null)
			return id;

		return -1;
		//		throw new IllegalArgumentException("Could not find field Id for ResourceType '" + resourceType + "' && Field Name '" + name + "'");
	}

	private static final class ValuesMap {

		private final Class<?> resourceClass;

		/**
		 * A constant to class field name mapping.
		 */
		private HashMap<Object, Object> resourcesValues;

		ValuesMap(Class<?> resourceClass) {
			this.resourceClass = resourceClass;
			resourcesValues = ReflectiveTools.getFieldsCrossMappings(resourceClass);
		}

		@SuppressWarnings("unused")
		public Class<?> getResourceClass() {
			return resourceClass;
		}

		String getName(int id) {
			return (String) resourcesValues.get(id);
		}

		Integer getId(String name) {
			return (Integer) resourcesValues.get(name);
		}
	}

	private static final class ResourcesMap {

		/**
		 * An inner resources class name to its values map.
		 */
		private HashMap<String, ValuesMap> resourcesValuesMap = new HashMap<>();

		ResourcesMap() {
		}

		ResourcesMap(Class<?> rClass) {
			Class<?>[] resourcesClasses = rClass.getDeclaredClasses();
			for (Class<?> resourceClass : resourcesClasses) {
				if (resourceClass.getSimpleName().equals("id")) {
					resourcesValuesMap.put(resourceClass.getSimpleName(), new ValuesMap(resourceClass));
				}
			}
		}

		String getName(ResourceType resourceType, int id) {
			return resourcesValuesMap.get(resourceType.getClassName()).getName(id);
		}

		Integer getId(ResourceType resourceType, String name) {
			return resourcesValuesMap.get(resourceType.getClassName()).getId(name);
		}
	}
}