package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Component;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.chain.ChainLink;

public class ControllerView<State> extends SideWindow {

	private static final long serialVersionUID = -7305901655789589843L;
	private final DotPanel dotPanel;
	private Map<ChainLink<State, ?, ?>, DotNode> map;

	public ControllerView(Component parent) {
		super(parent, "Controller view - " + InductiveVisualMinerPanel.title);

		Dot dot = new Dot();
		dot.addNode("waiting for controller view");
		dotPanel = new DotPanel(dot);
		add(dotPanel);
	}

	public void setChain(Chain<State> chain) {
		Pair<Dot, Map<ChainLink<State, ?, ?>, DotNode>> x = chain.toDot();
		dotPanel.changeDot(x.getLeft(), true);
		map = x.getB();
	}

	public void pushCompleteChainLinks(Set<ChainLink<State, ?, ?>> complete) {
		if (isVisible()) {
			for (Entry<ChainLink<State, ?, ?>, DotNode> entry : map.entrySet()) {
				if (complete.contains(entry.getKey())) {
					entry.getValue().setOption("style", "filled");
					entry.getValue().setOption("fillcolor", "cyan");
				} else {
					entry.getValue().setOption("style", "");
				}
			}
			dotPanel.changeDot(dotPanel.getDot(), false);
		}
	}

}
