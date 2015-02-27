package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class GraphvizDirectlyFollowsGraph {

	@Plugin(name = "Graphviz directly-follows graph visualisation", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Process Tree" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Display directly-follows graph", requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Dfg dfg) {
		
		if (dfg.getDirectlyFollowsGraph().getVertices().length > 50) {
			return new JPanel();
		}
		
		return new DotPanel(dfg2Dot(dfg, true));
	}

	public static Dot dfg2Dot(Dfg dfg, boolean includeParalelEdges) {

		Dot dot = new Dot();

		Sextuple<Long, Long, Long, Long, Integer, Integer> q = getExtremeOccurrences(dfg);
		long startMax = q.getB();
		long endMax = q.getD();
		int dfgMax = q.getE();
		int dfgParallelMax = q.getF();

		//prepare the nodes
		HashMap<XEventClass, DotNode> activityToNode = new HashMap<XEventClass, DotNode>();
		for (XEventClass activity : dfg.getDirectlyFollowsGraph().getVertices()) {
			DotNode node = dot.addNode(activity.toString(), "");
			activityToNode.put(activity, node);

			String options = "shape=\"box\"";

			//determine node colour using start and end activities
			if (dfg.getStartActivities().contains(activity) && dfg.getEndActivities().contains(activity)) {
				options += ", style=\"filled\""
						+ ", fillcolor=\""
						+ ColourMap.toHexString(ColourMaps.colourMapGreen(
								dfg.getStartActivities().getCardinalityOf(activity), startMax))
						+ ":"
						+ ColourMap.toHexString(ColourMaps.colourMapRed(
								dfg.getEndActivities().getCardinalityOf(activity), endMax)) + "\"";
			} else if (dfg.getStartActivities().contains(activity)) {
				options += ", style=\"filled\""
						+ ", fillcolor=\""
						+ ColourMap.toHexString(ColourMaps.colourMapGreen(
								dfg.getStartActivities().getCardinalityOf(activity), startMax)) + ":white\"";
			} else if (dfg.getEndActivities().contains(activity)) {
				options += ", style=\"filled\""
						+ ", fillcolor=\"white:"
						+ ColourMap.toHexString(ColourMaps.colourMapRed(
								dfg.getEndActivities().getCardinalityOf(activity), endMax)) + "\"";
			}

			node.setOptions(options);
		}

		//add the directly-follows edges
		for (long edge : dfg.getDirectlyFollowsGraph().getEdges()) {
			XEventClass from = dfg.getDirectlyFollowsGraph().getEdgeSource(edge);
			XEventClass to = dfg.getDirectlyFollowsGraph().getEdgeTarget(edge);
			int weight = (int) dfg.getDirectlyFollowsGraph().getEdgeWeight(edge);

			DotNode source = activityToNode.get(from);
			DotNode target = activityToNode.get(to);
			String label = String.valueOf(weight);
			String options = "color=\"" + ColourMap.toHexString(ColourMaps.colourMapBlue(weight, dfgMax)) + "\"";

			dot.addEdge(source, target, label, options);
		}

		if (includeParalelEdges) {
			//add the parallel directly-follows edges
			for (long edge : dfg.getParallelGraph().getEdges()) {
				XEventClass from = dfg.getParallelGraph().getEdgeSource((int) edge);
				XEventClass to = dfg.getParallelGraph().getEdgeTarget((int) edge);
				int weight = (int) dfg.getParallelGraph().getEdgeWeight((int) edge);

				DotNode source = activityToNode.get(from);
				DotNode target = activityToNode.get(to);
				String label = String.valueOf(weight);
				String options = "style=\"dashed\", dir=\"none\", constraint=\"false\", color=\""
						+ ColourMap.toHexString(ColourMaps.colourMapBlue(weight, dfgParallelMax)) + "\"";

				dot.addEdge(source, target, label, options);
			}
		}

		return dot;
	}

	public static Sextuple<Long, Long, Long, Long, Integer, Integer> getExtremeOccurrences(Dfg dfg) {
		long startMin = Long.MAX_VALUE;
		long startMax = Long.MIN_VALUE;
		List<XEventClass> starts = dfg.getStartActivities().sortByCardinality();
		if (starts.size() > 0) {
			startMin = dfg.getStartActivities().getCardinalityOf(starts.get(0));
			startMax = dfg.getStartActivities().getCardinalityOf(starts.get(starts.size() - 1));
		}

		long endMin = Long.MAX_VALUE;
		long endMax = Long.MIN_VALUE;
		List<XEventClass> ends = dfg.getEndActivities().sortByCardinality();
		if (ends.size() > 0) {
			endMin = dfg.getEndActivities().getCardinalityOf(ends.get(0));
			endMax = dfg.getEndActivities().getCardinalityOf(ends.get(ends.size() - 1));
		}

		long maxWeight = Long.MIN_VALUE;
		for (long edge : dfg.getDirectlyFollowsGraph().getEdges()) {
			maxWeight = Math.max(maxWeight, dfg.getDirectlyFollowsGraph().getEdgeWeight((int) edge));
		}

		long maxParallel = Long.MIN_VALUE;
		for (long edge : dfg.getParallelGraph().getEdges()) {
			maxParallel = Math.max(maxParallel, dfg.getParallelGraph().getEdgeWeight((int) edge));
		}

		return Sextuple.of(startMin, startMax, endMin, endMax, (int) maxWeight, (int) maxParallel);
	}
}
