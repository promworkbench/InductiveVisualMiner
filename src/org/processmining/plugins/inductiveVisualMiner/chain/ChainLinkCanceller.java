package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.ProMCanceller;

public class ChainLinkCanceller implements Canceller {

	private final ProMCanceller globalCanceller;
	private boolean cancelled;
	
	public ChainLinkCanceller(ProMCanceller globalCanceller) {
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
