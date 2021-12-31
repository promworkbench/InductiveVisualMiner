package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

public abstract class IvMFilterTreeNodeLeafAbstract<X> implements IvMFilterTreeNodeLeaf<X> {

	protected abstract boolean staysInLogA(X element);

	@Override
	public boolean staysInLog(X element) {
		if (!couldSomethingBeFiltered()) {
			return true;
		}
		return staysInLogA(element);
	}
}
