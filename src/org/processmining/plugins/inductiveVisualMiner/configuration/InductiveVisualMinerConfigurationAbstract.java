package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;
import java.util.concurrent.Executor;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

/**
 * IvM configuration that contains the chainlink. To extend, please subclass and
 * override the getChain() method.
 * 
 * @author sander
 *
 */
public abstract class InductiveVisualMinerConfigurationAbstract implements InductiveVisualMinerConfiguration {

	private final Chain chain;
	private final InductiveVisualMinerState state;
	private final InductiveVisualMinerPanel panel;
	private final VisualMinerWrapper[] discoveryTechniques;
	private final Mode[] modes;
	private final List<IvMFilter> preMiningFilters;
	private final List<IvMFilter> highlightingFilters;

	public InductiveVisualMinerConfigurationAbstract(XLog log, ProMCanceller canceller, Executor executor) {
		discoveryTechniques = createDiscoveryTechniques();
		preMiningFilters = createPreMiningFilters();
		highlightingFilters = createHighlightingFilters();
		modes = createModes();
		state = createState(log);
		panel = createPanel(canceller);
		chain = createChain(state, panel, canceller, executor, preMiningFilters, highlightingFilters);
	}

	protected abstract List<IvMFilter> createPreMiningFilters();

	protected abstract List<IvMFilter> createHighlightingFilters();

	protected abstract VisualMinerWrapper[] createDiscoveryTechniques();

	protected abstract Mode[] createModes();

	protected abstract InductiveVisualMinerState createState(XLog log);

	protected abstract InductiveVisualMinerPanel createPanel(ProMCanceller canceller);

	protected abstract Chain createChain(InductiveVisualMinerState state, InductiveVisualMinerPanel panel,
			ProMCanceller canceller, Executor executor, List<IvMFilter> preMiningFilters,
			List<IvMFilter> highlightingFilters);

	public Chain getChain() {
		return chain;
	}

	public InductiveVisualMinerState getState() {
		return state;
	}

	public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	public VisualMinerWrapper[] getDiscoveryTechniques() {
		return discoveryTechniques;
	}

	public Mode[] getModes() {
		return modes;
	}

	public List<IvMFilter> getPreMiningFilters() {
		return preMiningFilters;
	}

	public List<IvMFilter> getHighlightingFilters() {
		return highlightingFilters;
	}
}