package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Component;
import java.util.Map.Entry;
import java.util.Set;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLink;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkComputation;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ControllerView<State> extends SideWindow {

	private static final long serialVersionUID = -7305901655789589843L;
	private final DotPanel dotPanel;

	public ControllerView(Component parent) {
		super(parent, "Controller view - " + InductiveVisualMinerPanel.title);

		Dot dot = new Dot();
		dot.addNode("Waiting for controller view");
		dotPanel = new DotPanel(dot);
		add(dotPanel);
	}

	public void pushCompleteChainLinks(DataChain chain) {
		if (isVisible()) {
			Dot dot = new Dot();

			//add nodes (objects)
			THashMap<String, DotNode> object2dotNode = new THashMap<>();
			for (String object : chain.object2inputs.keySet()) {
				DotNode dotNode = dot.addNode(object);
				object2dotNode.put(object, dotNode);
				if (chain.state.hasObject(object)) {
					//complete
					dotNode.setOption("style", "filled");
					dotNode.setOption("fillcolor", "cyan");
				} else {
					dotNode.setOption("style", "");
				}
			}

			//add nodes (chain links)
			Set<DataChainLink> chainLinks = new THashSet<>();
			for (Set<DataChainLink> entry : chain.object2inputs.values()) {
				chainLinks.addAll(entry);
			}
			THashMap<DataChainLink, DotNode> link2dotNode = new THashMap<>();
			for (DataChainLink x : chainLinks) {
				DotNode dotNode = dot.addNode(x.getName());
				link2dotNode.put(x, dotNode);
				if (chain.executionCancellers.containsKey(x) && !chain.executionCancellers.get(x).isCancelled()) {
					//busy
					dotNode.setOption("style", "filled");
					dotNode.setOption("fillcolor", "orange");
				} else {
					dotNode.setOption("style", "");
				}
			}

			//edges (outputs)
			for (DataChainLink chainLink : chainLinks) {
				if (chainLink instanceof DataChainLinkComputation) {
					for (String object : ((DataChainLinkComputation) chainLink).getOutputNames()) {
						dot.addEdge(object2dotNode.get(object), link2dotNode.get(chainLink));
					}
				}
			}

			//edges (inputs)
			for (Entry<String, Set<DataChainLink>> entry : chain.object2inputs.entrySet()) {
				String object = entry.getKey();
				for (DataChainLink chainLink : entry.getValue()) {
					dot.addEdge(object2dotNode.get(object), link2dotNode.get(chainLink));
				}
			}

			dotPanel.changeDot(dotPanel.getDot(), false);
		}
	}

}
