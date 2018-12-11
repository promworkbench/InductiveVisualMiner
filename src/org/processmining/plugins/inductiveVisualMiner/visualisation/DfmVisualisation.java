package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.awt.Color;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class DfmVisualisation {

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
		DirectlyFollowsModel dfg = model.getDfg();

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
		 * Empty traces
		 */
		if (dfg.getNumberOfEmptyTraces() > 0) {
			addArc(source, sink, -1, -1, true, false);
		}

		/**
		 * Nodes
		 */
		for (int activity : dfg.getActivities()) {
			Triple<String, Long, Long> cardinality = data.getNodeLabel(activity, false);
			convertActivity(model.getDfg(), activity, cardinality);
		}

		/**
		 * Edges
		 */
		for (long edge : dfg.getDirectlyFollowsGraph().getEdges()) {
			int sourceActivity = dfg.getDirectlyFollowsGraph().getEdgeSource(edge);
			int targetActivity = dfg.getDirectlyFollowsGraph().getEdgeTarget(edge);

			LocalDotNode from = info.getActivityDotNode(sourceActivity);
			LocalDotNode to = info.getActivityDotNode(targetActivity);
			addArc(from, to, sourceActivity, targetActivity, true, false);
		}

		/**
		 * Start activities
		 */
		for (int node : dfg.getStartActivities()) {
			addArc(source, info.getActivityDotNode(node), -1, node, true, false);
		}

		/**
		 * End activities
		 */
		for (int node : dfg.getEndActivities()) {
			addArc(info.getActivityDotNode(node), sink, node, -1, true, false);
		}

		return Triple.of(dot, info, traceViewColourMap);
	}

	private void addArc(final LocalDotNode from, final LocalDotNode to, final int fromNode, int toNode,
			boolean directionForward, boolean includeModelMoves) throws UnknownTreeNodeException {

		LogMovePosition logMovePosition = LogMovePosition.onEdge(fromNode, toNode);
		Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
		if (!parameters.isShowLogMoves() || logMoves.getB().isEmpty()) {
			//do not show deviations
			final Pair<String, Long> cardinality = data.getEdgeLabel(fromNode, toNode, includeModelMoves);

			final LocalDotEdge edge;
			if (directionForward) {
				edge = new LocalDotEdge(null, dot, info, from, to, "", -1, EdgeType.model, fromNode, toNode,
						directionForward);
			} else {
				edge = new LocalDotEdge(null, dot, info, to, from, "", -1, EdgeType.model, fromNode, toNode,
						directionForward);
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
		} else {
			//log move
			//draw an intermediate node with a self-loop on it
			LocalDotNode intermediateNode = new LocalDotNode(dot, info, NodeType.xor, "", -1, null);
			LocalDotEdge edge1 = new LocalDotEdge(null, dot, info, from, intermediateNode, " ", -1, EdgeType.model,
					fromNode, toNode, directionForward);
			LocalDotEdge edge2 = new LocalDotEdge(null, dot, info, intermediateNode, to, " ", -1, EdgeType.model,
					fromNode, toNode, directionForward);

			Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
			addMoveArc(intermediateNode, intermediateNode, -1, EdgeType.logMove, logMovePosition.getOn(),
					logMovePosition.getBeforeChild(), t);
		}
	}

	private LocalDotEdge addMoveArc(LocalDotNode from, LocalDotNode to, int node, EdgeType type, int lookupNode1,
			int lookupNode2, Pair<String, Long> cardinality) {

		LocalDotEdge edge = new LocalDotEdge(null, dot, info, from, to, "", node, type, lookupNode1, lookupNode2, true);

		edge.setOption("style", "dashed");
		edge.setOption("arrowsize", ".5");

		if (parameters.getColourMoves() != null) {
			String lineColour = parameters.getColourMoves().colourString(cardinality.getB(), minCardinality,
					maxCardinality);
			edge.setOption("color", lineColour);
			edge.setOption("fontcolor", lineColour);
		}

		edge.setOption("penwidth",
				"" + parameters.getMoveEdgesWidth().size(cardinality.getB(), minCardinality, maxCardinality));

		if (parameters.isShowFrequenciesOnMoveEdges()) {
			edge.setLabel(cardinality.getA());
		} else {
			edge.setLabel(" ");
		}

		return edge;
	}

	private LocalDotNode convertActivity(DirectlyFollowsModel dfg, int node, Triple<String, Long, Long> cardinality) {
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

		String label = dfg.getActivityOfIndex(node);
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

		//visualise log moves
		if (parameters.isShowLogMoves()) {
			LogMovePosition logMovePosition = LogMovePosition.onLeaf(node);
			Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
			Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
			if (t.getB() > 0) {
				addMoveArc(dotNode, dotNode, node, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t);
			}
		}

		//visualise model moves
		if (parameters.isShowModelMoves()) {
			Pair<String, Long> modelMoves = data.getModelMoveEdgeLabel(node);
			if (modelMoves.getB() != 0) {
				addMoveArc(dotNode, dotNode, node, EdgeType.modelMove, -1, -1, modelMoves);
			}
		}

		return dotNode;
	}

	private double getOccurrenceFactor(long cardinality) {
		assert (minCardinality <= cardinality);
		assert (cardinality <= maxCardinality);
		return ProcessTreeVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}
}
