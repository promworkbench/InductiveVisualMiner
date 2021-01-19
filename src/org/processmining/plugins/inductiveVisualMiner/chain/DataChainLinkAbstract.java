package org.processmining.plugins.inductiveVisualMiner.chain;

public abstract class DataChainLinkAbstract implements DataChainLink {

	private IvMObject<?>[] inputObjects;
	private IvMObject<?>[] triggerObjects;

	public IvMObject<?>[] getInputObjects() {
		if (inputObjects == null) {
			inputObjects = createInputObjects();
		}
		return inputObjects;
	}

	public IvMObject<?>[] getTriggerObjects() {
		if (triggerObjects == null) {
			triggerObjects = createTriggerObjects();
		}
		return triggerObjects;
	}

	public abstract IvMObject<?>[] createInputObjects();

	public IvMObject<?>[] createTriggerObjects() {
		return new IvMObject<?>[] {};
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DataChainLinkAbstract other = (DataChainLinkAbstract) obj;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

}
