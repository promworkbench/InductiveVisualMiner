package org.processmining.plugins.inductiveVisualMiner.configuration;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public class InductiveVisualMinerConfigurationPreSet extends InductiveVisualMinerConfigurationAbstract {

	private InductiveVisualMinerState state;
	private InductiveVisualMinerPanel panel;

	public InductiveVisualMinerConfigurationPreSet(final InductiveVisualMinerPanel panel,
			final InductiveVisualMinerState state) {
		this.panel = panel;
		this.state = state;
	}

	public InductiveVisualMinerConfigurationPreSet() {
	}

	public InductiveVisualMinerState getState() {
		return state;
	}

	public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	public void setState(InductiveVisualMinerState state) {
		this.state = state;
	}

	public void setPanel(InductiveVisualMinerPanel panel) {
		this.panel = panel;
	}

}
