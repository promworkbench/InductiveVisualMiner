package org.processmining.plugins.inductiveVisualMiner.chain;

import gnu.trove.map.hash.THashMap;

public class DataState {

	private THashMap<IvMObject<?>, Object> object2value = new THashMap<>();

	public boolean hasObject(IvMObject<?> name) {
		return object2value.containsKey(name);
	}

	@SuppressWarnings("unchecked")
	public <C> C getObject(IvMObject<C> name) {
		return (C) object2value.get(name);
	}

	public <C> void putObject(IvMObject<C> name, C object) {
		object2value.put(name, object);
	}

	//	public void putObjectCheck(IvMObject<?> name, Object object) {
	//		assert name.getClazz().isInstance(object);
	//		object2value.put(name, object);
	//	}

	public void removeObject(IvMObject<?> name) {
		object2value.remove(name);
	}
}
