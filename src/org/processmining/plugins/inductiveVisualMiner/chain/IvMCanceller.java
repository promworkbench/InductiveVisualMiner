package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.plugin.ProMCanceller;

public class IvMCanceller implements ProMCanceller {

	public static final IvMCanceller neverCancel = new IvMCanceller(null) {
		@Override
		public boolean isCancelled() {
			return false;
		}
	};

	private final ProMCanceller globalCanceller;
	private boolean cancelled;

	public IvMCanceller(ProMCanceller globalCanceller) {
		this.globalCanceller = globalCanceller;
		cancelled = false;
	}

	public boolean isCancelled() {
		return globalCanceller.isCancelled() || cancelled;
	}

	public void cancel() {
		cancelled = true;
	}
}
