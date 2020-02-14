package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

public class InductiveVisualMinerConfigurationFake implements InductiveVisualMinerConfiguration {

	private InductiveVisualMinerConfigurationDefault phantom;
	private Chain chain;
	private InductiveVisualMinerState state;
	private InductiveVisualMinerPanel panel;
	private ProMCanceller canceller;
	private Executor executor;
	private VisualMinerWrapper[] discoveryTechniques;

	public InductiveVisualMinerConfigurationFake(InductiveVisualMinerState state, InductiveVisualMinerPanel panel,
			ProMCanceller canceller, Executor executor) {
		this.executor = executor;
		this.canceller = canceller;
		this.state = state;
		this.panel = panel;
		phantom = new InductiveVisualMinerConfigurationDefault(state.getXLog(), canceller, executor);
		this.discoveryTechniques = phantom.getDiscoveryTechniques();
	}

	public VisualMinerWrapper[] getDiscoveryTechniques() {
		return phantom.getDiscoveryTechniques();
	}

	public List<IvMFilter> getPreMiningFilters() {
		return phantom.getPreMiningFilters();
	}

	public List<IvMFilter> getHighlightingFilters() {
		return phantom.getHighlightingFilters();
	}

	public Mode[] getModes() {
		return phantom.getModes();
	}

	public InductiveVisualMinerState getState() {
		return state;
	}

	public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	public Chain getChain() {
		if (chain == null) {
			chain = phantom.createChain(state, panel, canceller, executor, getPreMiningFilters(),
					getHighlightingFilters());
		}
		return chain;
	}

	public void setDiscoveryTechniques(VisualMinerWrapper[] discoveryTechniques) {
		this.discoveryTechniques = discoveryTechniques;
	}

	public PopupItemActivity[] getPopupItemsActivity() {
		return phantom.getPopupItemsActivity();
	}
}