package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public abstract class IvMFilterBuilderAbstract<X, Y, G extends IvMFilterGui> implements IvMFilterBuilder<X, Y, G> {

	@Override
	public int compareTo(IvMFilterBuilder<?, ?, ?> o) {
		return this.toString().toLowerCase().compareTo(o.toString().toLowerCase());
	}

	@Override
	public <TI, TO, FI, FO> void setCommunicationChannel(FilterCommunicator<TI, TO, FI, FO> channel, G gui) {

	}
}