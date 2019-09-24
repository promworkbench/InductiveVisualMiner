package org.processmining.plugins.inductiveVisualMiner.visualisation;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DfmVisualisationSimple {

	public static Dot fancy(IvMModel model) {
		return fancy(model.getDfg());
	}

	public static Dot fancy(DirectlyFollowsModel dfg) {
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);

		TIntObjectMap<LocalDotNode> activity2dotNode = new TIntObjectHashMap<>(10, 0.5f, -1);

		//source & sink
		LocalDotNode source = new LocalDotNode(dot, null, NodeType.source, "", 0, null);
		LocalDotNode sink = new LocalDotNode(dot, null, NodeType.sink, "", 0, source);

		/**
		 * Empty traces
		 */
		if (dfg.isEmptyTraces()) {
			dot.addEdge(source, sink);
		}

		/**
		 * Nodes
		 */
		for (int activity : dfg.getNodeIndices()) {
			LocalDotNode dotNode = new LocalDotNode(dot, null, NodeType.activity, dfg.getNodeOfIndex(activity),
					activity, null);
			activity2dotNode.put(activity, dotNode);
		}

		/**
		 * Edges
		 */
		for (long edge : dfg.getEdges()) {
			int sourceActivity = dfg.getEdgeSource(edge);
			int targetActivity = dfg.getEdgeTarget(edge);

			LocalDotNode from = activity2dotNode.get(sourceActivity);
			LocalDotNode to = activity2dotNode.get(targetActivity);
			dot.addEdge(from, to);
		}

		/**
		 * Start activities
		 */
		for (TIntIterator it = dfg.getStartNodes().iterator(); it.hasNext();) {
			int node = it.next();
			dot.addEdge(source, activity2dotNode.get(node));
		}

		/**
		 * End activities
		 */
		for (TIntIterator it = dfg.getEndNodes().iterator(); it.hasNext();) {
			int node = it.next();
			dot.addEdge(activity2dotNode.get(node), sink);
		}

		return dot;
	}
}
