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

	public enum dfmEdgeType {
		/**
		 * normal DFM-edge
		 */
		modelBetweenActivities,

		/**
		 * DFM-edge from an activity to itself, e.g. a_start, a_complete,
		 * a_start, a_complete
		 */
		modelSelfLoop,

		/**
		 * log move edge during an activity, e.g. a_start, log move, a_complete
		 */
		logMoveDuringActivity,

		/**
		 * log move edge in between two different activities, e.g. a_start,
		 * a_complete, log move, b_start, b_complete
		 */
		logMoveInterActivity,

		/**
		 * log move edge in between two executions of the same activity, e.g.
		 * a_start, a_complete, log move, a_start, a_complete
		 */
		logMoveOnSelfLoop,

		/**
		 * model move edge
		 */
		modelMove,

		/**
		 * edge from a point where a model move edge splits off to/from an
		 * activity
		 */
		modelIntraActivity;

		private boolean canHaveLogMoves = false;
		static {
			modelBetweenActivities.canHaveLogMoves = true;
			modelSelfLoop.canHaveLogMoves = true;
		}

		public boolean canHaveLogMoves() {
			return canHaveLogMoves;
		}

		private boolean isLogMove = false;
		static {
			logMoveDuringActivity.isLogMove = true;
			logMoveInterActivity.isLogMove = true;
			logMoveOnSelfLoop.isLogMove = true;
		}

		public boolean isLogMove() {
			return isLogMove;
		}

		private boolean frequencyIncludesModelMoves = true;
		static {
			modelIntraActivity.frequencyIncludesModelMoves = false;
		}

		public boolean isFrequencyIncludesModelMoves() {
			return frequencyIncludesModelMoves;
		}

		public LogMovePosition getLogMovePosition(int dfmNodeFrom, int dfmNodeTo) {
			switch (this) {
				case logMoveDuringActivity :
					return null;
				case logMoveInterActivity :
					return null;
				case logMoveOnSelfLoop :
					return null;
				case modelBetweenActivities :
					return LogMovePosition.onEdge(dfmNodeFrom, dfmNodeTo);
				case modelIntraActivity :
					return null;
				case modelMove :
					return null;
				case modelSelfLoop :
					return LogMovePosition.betweenTwoExecutionsOf(dfmNodeTo);
				default :
					return null;
			}
		}
	}

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
			addArc2(dfmEdgeType.modelBetweenActivities, -1, -1);
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
				addArc2(dfmEdgeType.modelBetweenActivities, sourceActivity, targetActivity);
			} else {
				//special case: log move between two instances of the same activity (on a self-loop)
				addArc2(dfmEdgeType.modelSelfLoop, sourceActivity, targetActivity);
			}
		}

		/**
		 * Start activities
		 */
		for (TIntIterator it = dfg.getStartActivities().iterator(); it.hasNext();) {
			int node = it.next();
			//addArc(source, node2input.get(node), -1, node, true, false, dfmEdgeType.modelBetweenActivities);
			addArc2(dfmEdgeType.modelBetweenActivities, -1, node);
		}

		/**
		 * End activities
		 */
		for (TIntIterator it = dfg.getEndActivities().iterator(); it.hasNext();) {
			int node = it.next();
			//addArc(node2output.get(node), sink, node, -1, true, false, dfmEdgeType.modelBetweenActivities);
			addArc2(dfmEdgeType.modelBetweenActivities, node, -1);
		}

		return Triple.of(dot, info, traceViewColourMap);
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
			addArc2(dfmEdgeType.modelIntraActivity, -1, msdNode);
			//addArc(before, dotNode, node, node, true, false, dfmEdgeType.logMoveInterActivity);
			addArc2(dfmEdgeType.modelIntraActivity, msdNode, -1);

			//add the model-move edge
			Pair<String, Long> modelMoves = data.getModelMoveEdgeLabel(msdNode);
			addMoveArc(before, after, msdNode, EdgeType.modelMove, -1, -1, modelMoves);

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

		//visualise log moves
		if (parameters.isShowLogMoves()) {
			LogMovePosition logMovePosition = LogMovePosition.onLeaf(msdNode);
			Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
			Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
			if (t.getB() > 0) {
				LocalDotEdge edge = addMoveArc(dotNode, dotNode, msdNode, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t);
			}
		}

		return dotNode;
	}

	private double getOccurrenceFactor(long cardinality) {
		assert (minCardinality <= cardinality);
		assert (cardinality <= maxCardinality);
		return ProcessTreeVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}

	private void addArc2(dfmEdgeType edgeType, int dfmNodeFrom, int dfmNodeTo) {

		LocalDotNode dotNodeFrom;
		LocalDotNode dotNodeTo;
		if (edgeType != dfmEdgeType.modelIntraActivity) {
			dotNodeFrom = node2output.get(dfmNodeFrom);
			dotNodeTo = node2input.get(dfmNodeTo);
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
		}

		Pair<String, Long> cardinality = data.getEdgeLabel(dfmNodeFrom, dfmNodeTo,
				!parameters.isShowModelMoves() || edgeType.frequencyIncludesModelMoves);

		List<LocalDotEdge> edges = new ArrayList<>();

		//first, there might be log moves on the arc
		if (parameters.isShowLogMoves() && edgeType.canHaveLogMoves()) {
			LogMovePosition logMovePosition = edgeType.getLogMovePosition(dfmNodeFrom, dfmNodeTo);
			Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
			if (logMoves.getB().size() > 0) {
				//add an intermediate note and draw a log move on int
				LocalDotNode intermediateNode = new LocalDotNode(dot, info, NodeType.xor, "", -1, null);
				edges.add(new LocalDotEdge(null, dot, info, dotNodeFrom, intermediateNode, "", -1, EdgeType.model,
						dfmNodeFrom, dfmNodeTo, true));
				edges.add(new LocalDotEdge(null, dot, info, intermediateNode, dotNodeTo, "", -1, EdgeType.model,
						dfmNodeFrom, dfmNodeTo, true));

				Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
				addMoveArc(intermediateNode, intermediateNode, -1, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t);
			} else {
				//no log moves involved here
				edges.add(new LocalDotEdge(null, dot, info, dotNodeFrom, dotNodeTo, "", -1, EdgeType.model, dfmNodeFrom,
						dfmNodeTo, true));
			}
		} else {
			//no log moves involved here
			edges.add(new LocalDotEdge(null, dot, info, dotNodeFrom, dotNodeTo, "", -1, EdgeType.model, dfmNodeFrom,
					dfmNodeTo, true));
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
