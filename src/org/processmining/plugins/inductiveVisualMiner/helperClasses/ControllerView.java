package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Component;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;

public class ControllerView<State> extends SideWindow {

	private static final long serialVersionUID = -7305901655789589843L;
	private final DotPanel dotPanel;
	private boolean initialised = false;

	public ControllerView(Component parent) {
		super(parent, "Controller view - " + InductiveVisualMinerPanel.title);

		Dot dot = new Dot();
		dot.addNode("Waiting for controller view");
		dotPanel = new DotPanel(dot);
		add(dotPanel);
	}

	public <C> void pushCompleteChainLinks(DataChain<C> chain) {
		if (isVisible() || !initialised) {
			initialised = true;
			dotPanel.changeDot(chain.toDot(), false);
		}
	}
}