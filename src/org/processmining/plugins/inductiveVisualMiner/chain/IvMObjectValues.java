package org.processmining.plugins.inductiveVisualMiner.chain;

import gnu.trove.map.hash.THashMap;

public class IvMObjectValues {

	private THashMap<IvMObject<?>, Object> object2value = new THashMap<>();

	/**
	 * Ensure that the object is available before calling.
	 * 
	 * @param <C>
	 * @param name
	 * @return the requested object
	 */
	@SuppressWarnings("unchecked")
	public <C> C get(IvMObject<C> name) {
		assert object2value.containsKey(name); //check whether the required object is present
		return (C) object2value.get(name);
	}

	public <C> void set(IvMObject<C> name, C object) {
		object2value.put(name, object);
	}

	public <C> IvMObjectValues s(IvMObject<C> name, C object) {
		set(name, object);
		return this;
	}

	/**
	 * 
	 * @param objects
	 * @return whether all objects are available
	 */
	public boolean has(IvMObject<?>... objects) {
		for (IvMObject<?> object : objects) {
			if (!object2value.containsKey(object)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param inputObjects
	 * @return A sub-set of this object, that has the requested objects. The
	 *         objects are not required to be present.
	 */
	public IvMObjectValues getIfPresent(IvMObject<?>... objects) {
		IvMObjectValues result = new IvMObjectValues();
		for (IvMObject<?> object : objects) {
			if (has(object)) {
				getIfPresent2(result, object);
			}
		}
		return result;
	}

	private <C> void getIfPresent2(IvMObjectValues result, IvMObject<C> object) {
		result.set(object, get(object));
	}

	public IvMObjectValues getIfPresent(IvMObject<?>[] inputObjects, IvMObject<?>... triggerObjects) {
		IvMObjectValues result = new IvMObjectValues();
		for (IvMObject<?> object : inputObjects) {
			getIfPresent2(result, object);
		}
		for (IvMObject<?> object : triggerObjects) {
			if (has(object)) {
				getIfPresent2(result, object);
			}
		}
		return result;
	}
}
