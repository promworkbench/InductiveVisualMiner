package org.processmining.plugins.inductiveVisualMiner.chain;

public abstract class DataChainAbstract implements DataChain {
	protected OnException onException;
	protected Runnable onChange;
	protected OnStatus onStatus;

	@Override
	public OnException getOnException() {
		return onException;
	}

	@Override
	public void setOnException(OnException onException) {
		this.onException = onException;
	}

	@Override
	public OnStatus getOnStatus() {
		return onStatus;
	}

	@Override
	public void setOnStatus(OnStatus onStatus) {
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
