package org.processmining.plugins.inductiveVisualMiner.resourcePerspective;

import gnu.trove.map.hash.THashMap;

import java.awt.Component;
import java.util.Map;

import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMapGreen;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMap;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.plugins.inductiveVisualMiner.resourcePerspective.ProcessTree2DfgUnfoldedNode.DfgUnfoldedNode;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class ResourceView extends SideWindow {

	DotPanel graphPanel;

	public ResourceView(Component parent) {
		super(parent, "Inductive visual Miner - resource perspective");

		//initialise the splash screen
		Dot dot = new Dot();
		dot.addNode("Inductive visual Miner");
		dot.addNode("resource view");
		dot.addNode("initialising..");
		graphPanel = new DotPanel(dot);
		add(graphPanel);
	}

	public void set(ProcessTree tree) {
		DfgUnfoldedNode dfg = ProcessTree2DfgUnfoldedNode.makeDfg(new UnfoldedNode(tree.getRoot()), true);

		Dot dot = new Dot();
		Map<UnfoldedNode, DotNode> nodeMap = new THashMap<>();
		for (UnfoldedNode unode : dfg.directlyFollowsGraph.getVertices()) {
			String label = unode.getNode().toString();
			if (unode.getNode() instanceof And) {
				label = "parallel abstraction";
			}
			
			DotNode dNode = dot.addNode(label);
			nodeMap.put(unode, dNode);
			
			if (unode.getNode() instanceof And) {
				dNode.setOption("style", "rounded, filled");
			} else {
				dNode.setOption("style", "rounded");
			}
			dNode.setOption("shape", "box");
			dNode.setOption("fontsize", "9");
		}
		for (long edgeIndex : dfg.directlyFollowsGraph.getEdges()) {
			dot.addEdge(nodeMap.get(dfg.directlyFollowsGraph.getEdgeSource(edgeIndex)),
					nodeMap.get(dfg.directlyFollowsGraph.getEdgeTarget(edgeIndex)));
		}
		
		DotNode source = dot.addNode("");
		source.setOption("width", "0.2");
		source.setOption("shape", "circle");
		source.setOption("style", "filled");
		source.setOption("fillcolor", "green");
		for (UnfoldedNode unode : dfg.startActivities) {
			dot.addEdge(source, nodeMap.get(unode));
		}
		
		DotNode sink = dot.addNode("");
		sink.setOption("width", "0.2");
		sink.setOption("shape", "circle");
		sink.setOption("style", "filled");
		sink.setOption("fillcolor", "red");
		for (UnfoldedNode unode : dfg.endActivities) {
			dot.addEdge(nodeMap.get(unode), sink);
		}
		
		graphPanel.changeDot(dot, true);
	}

	public void set(Pair<MultiSet<Pair<String, String>>, MultiSet<String>> input) {
		Dot dot = new Dot();
		ColourMap colourMap = new ColourMapGreen();
		Map<String, DotNode> nodeMap = new THashMap<>();

		{
			MultiSet<String> graph = input.getB();
			long maxCardinality = graph.getCardinalityOf(graph.getElementWithHighestCardinality());
			for (String e : graph) {
				long cardinality = graph.getCardinalityOf(e);

				DotNode node = dot.addNode(e.toString() + "\n" + cardinality);
				nodeMap.put(e, node);

				//fill colour
				String fillColour = ColourMap.toHexString(colourMap.colour(cardinality, maxCardinality));
				node.setOption("fillcolor", fillColour);
				node.setOption("style", "filled");
			}
		}

		{
			MultiSet<Pair<String, String>> graph = input.getA();
			long maxCardinality = graph.getCardinalityOf(graph.getElementWithHighestCardinality());
			SizeMap sizeMap = new SizeMapLinear(1, 4);
			for (Pair<String, String> p : input.getA()) {
				long cardinality = graph.getCardinalityOf(p);

				DotEdge edge = dot.addEdge(nodeMap.get(p.getLeft()), nodeMap.get(p.getRight()), cardinality + "");

				//line colour
				String lineColour = ColourMap.toHexString(colourMap.colour(cardinality, maxCardinality));
				edge.setOption("color", lineColour);

				//line thickness
				edge.setOption("penwidth", sizeMap.size(cardinality, maxCardinality) + "");
			}
		}

		graphPanel.changeDot(dot, true);
	}
}
