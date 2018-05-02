package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.awt.Color;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class DfgVisualisation {

	private long maxCardinality;
	private long minCardinality;
	ProcessTreeVisualisationParameters parameters;

	private AlignedLogVisualisationData data;

	private Dot dot;
	private ProcessTreeVisualisationInfo info;
	private TraceViewEventColourMap traceViewColourMap;

	public Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> fancy(IvMModel model,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters) {
		this.parameters = parameters;
		this.data = data;
		Dfg dfg = model.getDfg();

		//find maximum and minimum occurrences
		Pair<Long, Long> p = data.getExtremeCardinalities();
		minCardinality = p.getLeft();
		maxCardinality = p.getRight();

		dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);

		traceViewColourMap = new TraceViewEventColourMap(model);

		//source & sink
		info = new ProcessTreeVisualisationInfo();
		LocalDotNode source = new LocalDotNode(dot, info, NodeType.source, "", 0, null);
		LocalDotNode sink = new LocalDotNode(dot, info, NodeType.sink, "", 0, source);
		info.setRoot(source, sink);

		/**
		 * Nodes
		 */
		for (int activity : dfg.getActivityIndices()) {
			Triple<String, Long, Long> cardinality = data.getNodeLabel(activity, false);
			LocalDotNode dotNode = convertActivity(model.getDfg(), activity, cardinality);
		}

		/**
		 * Edges
		 */
		for (long edge : dfg.getDirectlyFollowsEdges()) {
			int sourceActivity = dfg.getDirectlyFollowsEdgeSourceIndex(edge);
			int targetActivity = dfg.getDirectlyFollowsEdgeTargetIndex(edge);

			LocalDotNode from = info.getActivityDotNode(sourceActivity);
			LocalDotNode to = info.getActivityDotNode(targetActivity);
			addArc(from, to, targetActivity, true, false);
		}

		/**
		 * Start activities
		 */
		for (int node : dfg.getStartActivityIndices()) {
			addArc(source, info.getActivityDotNode(node), node, true, false);
		}

		/**
		 * End activities
		 */
		for (int node : dfg.getEndActivityIndices()) {
			addArc(info.getActivityDotNode(node), sink, node, true, false);
		}

		return Triple.of(dot, info, traceViewColourMap);
	}

	private LocalDotEdge addArc(final LocalDotNode from, final LocalDotNode to, final int node,
			boolean directionForward, boolean includeModelMoves) throws UnknownTreeNodeException {

		final Pair<String, Long> cardinality = data.getEdgeLabel(node, includeModelMoves);

		final LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(null, dot, info, from, to, "", node, EdgeType.model, -1, -1, directionForward);
		} else {
			edge = new LocalDotEdge(null, dot, info, to, from, "", node, EdgeType.model, -1, -1, directionForward);
			edge.setOption("dir", "back");
		}

		if (parameters.getColourModelEdges() != null) {
			String lineColour = parameters.getColourModelEdges().colourString(cardinality.getB(), minCardinality,
					maxCardinality);
			edge.setOption("color", lineColour);
		}

		edge.setOption("penwidth",
				"" + parameters.getModelEdgesWidth().size(cardinality.getB(), minCardinality, maxCardinality));

		if (parameters.isShowFrequenciesOnModelEdges() && !cardinality.getA().isEmpty()) {
			edge.setLabel(cardinality.getA());
		} else {
			edge.setLabel(" ");
		}

		return edge;
	}

	private LocalDotNode convertActivity(Dfg dfg, int node, Triple<String, Long, Long> cardinality) {
		//style the activity by the occurrences of it
		Color fillColour = Color.white;
		Color gradientColour = null;
		if (cardinality.getB() != 0 && parameters.getColourNodes() != null) {
			fillColour = parameters.getColourNodes().colour((long) (getOccurrenceFactor(cardinality.getB()) * 100), 0,
					100);

			if (cardinality.getC() != 0 && parameters.getColourNodesGradient() != null) {
				gradientColour = parameters.getColourNodesGradient()
						.colour((long) (getOccurrenceFactor(cardinality.getC()) * 100), 0, 100);
			}
		}

		//determine label colour
		Color fontColour = Color.black;
		if (ColourMaps.getLuma(fillColour) < 128) {
			fontColour = Color.white;
		}
		traceViewColourMap.set(node, fillColour, fontColour);

		String label = dfg.getActivityOfIndex(node).getId();
		if (label.length() == 0) {
			label = " ";
		}
		if (!cardinality.getA().isEmpty()) {
			label += "\n" + cardinality.getA();
		}

		final LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.activity, label, node, null);
		if (gradientColour == null) {
			dotNode.setOption("fillcolor", ColourMap.toHexString(fillColour));
		} else {
			dotNode.setOption("fillcolor",
					ColourMap.toHexString(fillColour) + ":" + ColourMap.toHexString(gradientColour));
		}
		dotNode.setOption("fontcolor", ColourMap.toHexString(fontColour));

		info.addNode(node, dotNode, null);
		return dotNode;
	}

	private double getOccurrenceFactor(long cardinality) {
		assert (minCardinality <= cardinality);
		assert (cardinality <= maxCardinality);
		return ProcessTreeVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}
}
