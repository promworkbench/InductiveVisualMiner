package org.processmining.plugins.inductiveVisualMiner.chain;

/**
 * A carte-blanche gives read-access to the entire state. Used for optional
 * objects on the chain. Please note that thread-safety is not guaranteed.
 */
public class IvMObjectCarteBlanche {

	private final DataState state;

	public IvMObjectCarteBlanche(DataState state) {
		this.state = state;
	}

	public IvMObjectValues getIfPresent(IvMObject<?>... objects) {
		IvMObjectValues result = new IvMObjectValues();
		for (IvMObject<?> object : objects) {
			gatherInput(object, result);
		}
		return result;
	}

	/**
	 * 
	 * @param <C>
	 * @param object
	 * @return the object or null if it does not exist in the state
	 */
	public <C> C getDirectIfPresent(IvMObject<C> object) {
		return state.getObject(object);
	}

	private <C> void gatherInput(IvMObject<C> object, IvMObjectValues values) {
		C value = state.getObject(object);
		if (value != null) {
			values.set(object, value);
		}
	}
}