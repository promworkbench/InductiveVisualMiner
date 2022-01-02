package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

public abstract class FilterCommunicatorImpl<TI, TO, FI, FO> implements FilterCommunicator<TI, TO, FI, FO> {

	private toFilterChannel<TI, TO> toFilter;
	private toFilterController toFilterController;

	@Override
	public TO toFilter(TI input) {
		if (toFilter != null) {
			return toFilter.toFilter(input);
		}
		return null;
	}

	@Override
	public void setToFilter(toFilterChannel<TI, TO> to) {
		this.toFilter = to;
	}

	@Override
	public void setAndSelectRootFilter(String name) {
		if (toFilterController != null) {
			toFilterController.setAndSelectRootFilter(name);
		}
	}

	@Override
	public void setSetAndSelectRootFilter(toFilterController to) {
		this.toFilterController = to;
	}

}