package org.processmining.plugins.inductiveVisualMiner.chain;

public abstract class DataChainAbstract<C> implements DataChain<C> {
	protected OnException onException;
	protected Runnable onChange;
	protected OnStatus<C> onStatus;

	@Override
	public OnException getOnException() {
		return onException;
	}

	@Override
	public void setOnException(OnException onException) {
		this.onException = onException;
	}

	@Override
	public OnStatus<C> getOnStatus() {
		return onStatus;
	}

	@Override
	public void setOnStatus(OnStatus<C> onStatus) {
		this.onStatus = onStatus;
	}

	@Override
	public Runnable getOnChange() {
		return onChange;
	}

	@Override
	public void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}
}
