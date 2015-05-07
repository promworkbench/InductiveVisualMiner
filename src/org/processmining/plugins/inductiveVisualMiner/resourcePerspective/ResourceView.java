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
