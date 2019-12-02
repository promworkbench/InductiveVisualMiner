package org.processmining.plugins.inductiveVisualMiner.configuration;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public class InductiveVisualMinerConfigurationDefault extends InductiveVisualMinerConfigurationAbstract {

	private final InductiveVisualMinerState state;
	private final PluginContext context;
	private final ProMCanceller canceller;

	public InductiveVisualMinerConfigurationDefault(PluginContext context, XLog log, ProMCanceller canceller) {
		this.state = new InductiveVisualMinerState(log);
		this.context = context;
		this.canceller = canceller;
	}

	public InductiveVisualMinerState getState() {
		return state;
	}

	public InductiveVisualMinerPanel getPanel() {
		return InductiveVisualMinerPanel.panel(context, state, discoveryTechniques, canceller);
	}

}
