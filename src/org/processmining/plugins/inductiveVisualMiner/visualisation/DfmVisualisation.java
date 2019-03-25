package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotCluster;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DfmVisualisation {

	private long maxCardinality;
	private long minCardinality;
	ProcessTreeVisualisationParameters parameters;

	private AlignedLogVisualisationData data;

	private Dot dot;
	private ProcessTreeVisualisationInfo info;
	private TraceViewEventColourMap traceViewColourMap;

	private TIntObjectHashMap<LocalDotNode> node2input;
	private TIntObjectHashMap<LocalDotNode> node2output;

	public Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> fancy(IvMModel model,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters) {
		this.parameters = parameters;
		this.data = data;
		DirectlyFollowsModel dfg = model.getDfg();
		node2input = new TIntObjectHashMap<>(10, 0.5f, -1);
		node2output = new TIntObjectHashMap<>(10, 0.5f, -1);

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
		node2output.put(-1, source);
		LocalDotNode sink = new LocalDotNode(dot, info, NodeType.sink, "", 0, source);
		node2input.put(-1, sink);
		info.setRoot(source, sink);

		/**
		 * Empty traces
		 */
		if (dfg.isEmptyTraces()) {
			addArc2(DFMEdgeType.modelBetweenActivities, -1, -1);
			//addArc(source, sink, -1, -1, true, false, dfmEdgeType.modelBetweenActivities);
		}

		/**
		 * Activities
		 */
		for (int activity : dfg.getActivitiesIndices()) {
			Triple<String, Long, Long> cardinality = data.getNodeLabel(activity, false);
			convertActivity(model.getDfg(), activity, cardinality);
		}

		/**
		 * Edges
		 */
		for (long edge : dfg.getEdges()) {
			int sourceActivity = dfg.getEdgeSource(edge);
			int targetActivity = dfg.getEdgeTarget(edge);

			if (sourceActivity != targetActivity) {
				addArc2(DFMEdgeType.modelBetweenActivities, sourceActivity, targetActivity);
			} else {
				//special case: log move between two instances of the same activity (on a self-loop)
				addArc2(DFMEdgeType.modelSelfLoop, sourceActivity, targetActivity);
			}
		}

		/**
		 * Start activities
		 */
		for (TIntIterator it = dfg.getStartActivities().iterator(); it.hasNext();) {
			int node = it.next();
			addArc2(DFMEdgeType.modelBetweenActivities, -1, node);
		}

		/**
		 * End activities
		 */
		for (TIntIterator it = dfg.getEndActivities().iterator(); it.hasNext();) {
			int node = it.next();
			addArc2(DFMEdgeType.modelBetweenActivities, node, -1);
		}

		return Triple.of(dot, info, traceViewColourMap);
	}

	private LocalDotEdge addMoveArc(LocalDotNode from, LocalDotNode to, int node, EdgeType type, int lookupNode1,
			int lookupNode2, Pair<String, Long> cardinality, DFMEdgeType edgeType) {

		LocalDotEdge edge = new LocalDotEdge(null, dot, info, from, to, "", node, type, edgeType, lookupNode1,
				lookupNode2, true);

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

	private LocalDotNode convertActivity(DirectlyFollowsModel dfg, int msdNode,
			Triple<String, Long, Long> cardinality) {

		boolean hasModelMoves = parameters.isShowModelMoves() && data.getModelMoveEdgeLabel(msdNode).getB() != 0;

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
		traceViewColourMap.set(msdNode, fillColour, fontColour);

		String label = dfg.getActivityOfIndex(msdNode);
		if (label.length() == 0) {
			label = " ";
		}
		if (!cardinality.getA().isEmpty()) {
			label += "&#92;n" + cardinality.getA();
		}

		final LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.activity, label, msdNode, null);

		if (hasModelMoves) {
			//put the node in a cluster
			dot.removeNode(dotNode);
			DotCluster cluster = dot.addCluster();
			cluster.setOption("style", "invis");

			LocalDotNode before = new LocalDotNode(dot, info, NodeType.xor, "", -1, null);
			dot.removeNode(before);
			cluster.addNode(before);
			info.addNode(msdNode, before, null);
			node2input.put(msdNode, before);

			cluster.addNode(dotNode);

			LocalDotNode after = new LocalDotNode(dot, info, NodeType.xor, "", -1, null);
			dot.removeNode(after);
			cluster.addNode(after);
			node2output.put(msdNode, after);

			//connect before -> activity -> after
			addArc2(DFMEdgeType.modelIntraActivity, -1, msdNode);
			//addArc(before, dotNode, node, node, true, false, dfmEdgeType.logMoveInterActivity);
			addArc2(DFMEdgeType.modelIntraActivity, msdNode, -1);

			//add the model-move edge
			Pair<String, Long> modelMoves = data.getModelMoveEdgeLabel(msdNode);
			addMoveArc(before, after, msdNode, EdgeType.modelMove, -1, -1, modelMoves, DFMEdgeType.modelMove);

		} else {
			node2input.put(msdNode, dotNode);
			node2output.put(msdNode, dotNode);
		}

		if (gradientColour == null) {
			dotNode.setOption("fillcolor", ColourMap.toHexString(fillColour));
		} else {
			dotNode.setOption("fillcolor",
					ColourMap.toHexString(fillColour) + ":" + ColourMap.toHexString(gradientColour));
		}
		dotNode.setOption("fontcolor", ColourMap.toHexString(fontColour));

		info.addNode(msdNode, dotNode, null);

		//visualise log moves during activities, e.g. a_start, log move, a_complete
		if (parameters.isShowLogMoves()) {
			LogMovePosition logMovePosition = LogMovePosition.onLeaf(msdNode);
			Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
			Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
			if (t.getB() > 0) {
				addMoveArc(dotNode, dotNode, msdNode, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t, DFMEdgeType.logMoveDuringActivity);
			}
		}

		return dotNode;
	}

	private double getOccurrenceFactor(long cardinality) {
		assert (minCardinality <= cardinality);
		assert (cardinality <= maxCardinality);
		return ProcessTreeVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}

	private void addArc2(DFMEdgeType edgeType, int dfmNodeFrom, int dfmNodeTo) {
		LocalDotNode dotNodeFrom;
		LocalDotNode dotNodeTo;
		Pair<String, Long> cardinality;
		if (edgeType != DFMEdgeType.modelIntraActivity) {
			dotNodeFrom = node2output.get(dfmNodeFrom);
			dotNodeTo = node2input.get(dfmNodeTo);

			cardinality = data.getEdgeLabel(dfmNodeFrom, dfmNodeTo,
					!parameters.isShowModelMoves() || edgeType.isFrequencyIncludesModelMoves());
		} else {
			//special case: model move that has been split up in two parts
			if (dfmNodeFrom != -1) {
				dotNodeFrom = info.getActivityDotNode(dfmNodeFrom);
				dotNodeTo = node2output.get(dfmNodeFrom);
				dfmNodeTo = dfmNodeFrom;
			} else {
				dotNodeFrom = node2input.get(dfmNodeTo);
				dotNodeTo = info.getActivityDotNode(dfmNodeTo);
				dfmNodeFrom = dfmNodeTo;
			}
			cardinality = data.getEdgeLabel(dfmNodeTo,
					!parameters.isShowModelMoves() || edgeType.isFrequencyIncludesModelMoves());
		}

		List<LocalDotEdge> edges = new ArrayList<>();

		//first, there might be log moves on the arc
		if (parameters.isShowLogMoves() && edgeType.canHaveLogMoves()) {
			LogMovePosition logMovePosition = edgeType.getLogMovePosition(dfmNodeFrom, dfmNodeTo);
			Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
			if (logMoves.getB().size() > 0) {
				//add an intermediate note and draw a log move on int
				LocalDotNode intermediateNode = new LocalDotNode(dot, info, NodeType.xor, "", -1, null);
				edges.add(new LocalDotEdge(null, dot, info, dotNodeFrom, intermediateNode, "", -1, EdgeType.model,
						edgeType, dfmNodeFrom, dfmNodeTo, true));
				edges.add(new LocalDotEdge(null, dot, info, intermediateNode, dotNodeTo, "", -1, EdgeType.model,
						edgeType, dfmNodeFrom, dfmNodeTo, true));

				Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
				addMoveArc(intermediateNode, intermediateNode, -1, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t, edgeType.getCorrespondingLogMove());
			} else {
				//no log moves involved here
				edges.add(new LocalDotEdge(null, dot, info, dotNodeFrom, dotNodeTo, "", -1, EdgeType.model, edgeType,
						dfmNodeFrom, dfmNodeTo, true));
			}
		} else {
			//no log moves involved here
			edges.add(new LocalDotEdge(null, dot, info, dotNodeFrom, dotNodeTo, "", -1, EdgeType.model, edgeType,
					dfmNodeFrom, dfmNodeTo, true));
		}

		for (LocalDotEdge edge : edges) {
			if (parameters.isShowFrequenciesOnModelEdges() && !cardinality.getA().isEmpty()) {
				edge.setLabel(cardinality.getA());
			} else {
				edge.setLabel(" ");
			}

			if (parameters.getColourModelEdges() != null) {
				String lineColour = parameters.getColourModelEdges().colourString(cardinality.getB(), minCardinality,
						maxCardinality);
				edge.setOption("color", lineColour);
			}

			edge.setOption("penwidth",
					"" + parameters.getModelEdgesWidth().size(cardinality.getB(), minCardinality, maxCardinality));
		}
	}
}
