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
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGui;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

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

	public void pushCompleteChainLinks(DataChain chain) {
		if (isVisible() || !initialised) {
			initialised = true;
			Dot dot = new Dot();

			//add nodes (objects)
			THashMap<IvMObject<?>, DotNode> object2dotNode = new THashMap<>();
			for (IvMObject<?> object : chain.object2inputs.keySet()) {
				DotNode dotNode = dot.addNode(object.getName());
				object2dotNode.put(object, dotNode);
				if (chain.state.hasObject(object)) {
					//complete
					dotNode.setOption("style", "filled");
					dotNode.setOption("fillcolor", "aquamarine");
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
			for (DataChainLink chainLink : chainLinks) {
				DotNode dotNode = dot.addNode(chainLink.getName());
				link2dotNode.put(chainLink, dotNode);
				if (chain.executionCancellers.containsKey(chainLink)
						&& !chain.executionCancellers.get(chainLink).isCancelled()) {
					//busy
					dotNode.setOption("style", "filled");
					dotNode.setOption("fillcolor", "orange");
				} else {
					dotNode.setOption("style", "");
				}

				if (chainLink instanceof DataChainLinkGui) {
					dotNode.setOption("shape", "box3d");
				} else {
					dotNode.setOption("shape", "box");
				}
			}

			//add nodes (outputs that are not inputs)
			for (DataChainLink chainLink : chainLinks) {
				if (chainLink instanceof DataChainLinkComputation) {
					for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputNames()) {
						if (!object2dotNode.containsKey(object)) {
							DotNode dotNode = dot.addNode(object.getName());
							object2dotNode.put(object, dotNode);
							if (chain.state.hasObject(object)) {
								//complete
								dotNode.setOption("style", "filled");
								dotNode.setOption("fillcolor", "chartreuse");
							} else {
								dotNode.setOption("style", "");
							}
						}
					}
				}
			}

			//edges (outputs)
			for (DataChainLink chainLink : chainLinks) {
				if (chainLink instanceof DataChainLinkComputation) {
					for (IvMObject<?> object : ((DataChainLinkComputation) chainLink).getOutputNames()) {
						dot.addEdge(link2dotNode.get(chainLink), object2dotNode.get(object));
					}
				}
			}

			//edges (inputs)
			for (Entry<IvMObject<?>, Set<DataChainLink>> entry : chain.object2inputs.entrySet()) {
				IvMObject<?> object = entry.getKey();
				for (DataChainLink chainLink : entry.getValue()) {
					dot.addEdge(object2dotNode.get(object), link2dotNode.get(chainLink));
				}
			}

			dot.toString();

			dotPanel.changeDot(dot, false);
		}
	}

}
