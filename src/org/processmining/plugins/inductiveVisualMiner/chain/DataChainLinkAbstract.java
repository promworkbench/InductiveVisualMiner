package org.processmining.plugins.inductiveVisualMiner.chain;

public abstract class DataChainLinkAbstract implements DataChainLink {

	private IvMObject<?>[] requiredObjects;
	private IvMObject<?>[] optionalObjects;
	private IvMObject<?>[] nonTriggerObjects;

	public IvMObject<?>[] getRequiredObjects() {
		if (requiredObjects == null) {
			requiredObjects = createInputObjects();
		}
		return requiredObjects;
	}

	public IvMObject<?>[] getOptionalObjects() {
		if (optionalObjects == null) {
			optionalObjects = createOptionalObjects();
		}
		return optionalObjects;
	}

	public IvMObject<?>[] getNonTriggerObjects() {
		if (nonTriggerObjects == null) {
			nonTriggerObjects = createNonTriggerObjects();
		}
		return nonTriggerObjects;
	}

	public abstract IvMObject<?>[] createInputObjects();

	public IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] {};
	}

	public IvMObject<?>[] createNonTriggerObjects() {
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
