package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.awt.Color;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class ProcessTreeVisualisation {

	private long maxCardinality;
	private long minCardinality;
	ProcessTreeVisualisationParameters parameters;

	private AlignedLogVisualisationData data;

	private Dot dot;
	private ProcessTreeVisualisationInfo info;
	private TraceViewEventColourMap traceViewColourMap;

	public Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> fancy(IvMEfficientTree tree,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters)
			throws UnknownTreeNodeException {
		this.parameters = parameters;
		this.data = data;

		//find maximum and mimimum occurrences
		Pair<Long, Long> p = data.getExtremeCardinalities();
		minCardinality = p.getLeft();
		maxCardinality = p.getRight();

		dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		int root = tree.getRoot();

		traceViewColourMap = new TraceViewEventColourMap(tree);

		//source & sink
		info = new ProcessTreeVisualisationInfo();
		LocalDotNode source = new LocalDotNode(dot, info, NodeType.source, "", 0, null);
		LocalDotNode sink = new LocalDotNode(dot, info, NodeType.sink, "", 0, source);
		info.setRoot(source, sink);
		//convert root node
		convertNode(tree, root, source, sink, true);

		//add log-move-arcs to source and sink
		//a parallel root will project its own log moves 
		if (parameters.isShowLogMoves() && !(tree.isConcurrent(root) || tree.isOr(root) || tree.isInterleaved(root))) {
			visualiseLogMove(tree, source, source, root, LogMovePosition.atSource(root), true);
			visualiseLogMove(tree, sink, sink, root, LogMovePosition.atSink(root), false);
		}

		return Triple.of(dot, info, traceViewColourMap);
	}

	private void convertNode(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {
		if (tree.isSequence(node)) {
			convertSequence(tree, node, source, sink, directionForward);
		} else if (tree.isLoop(node)) {
			convertLoop(tree, node, source, sink, directionForward);
		} else if (tree.isInterleaved(node)) {
			convertInterleaved(tree, node, source, sink, directionForward);
		} else if (tree.isConcurrent(node)) {
			convertConcurrent(tree, node, source, sink, directionForward);
		} else if (tree.isOr(node)) {
			convertOr(tree, node, source, sink, directionForward);
		} else if (tree.isXor(node)) {
			convertXor(tree, node, source, sink, directionForward);
		} else if (tree.isActivity(node)) {
			convertActivity(tree, node, source, sink, directionForward);
		} else if (tree.isTau(node)) {
			convertTau(tree, node, source, sink, directionForward);
		} else {
			throw new UnknownTreeNodeException();
		}
	}

	private void convertActivity(IvMEfficientTree tree, int unode, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {
		Triple<String, Long, Long> cardinality = data.getNodeLabel(unode, false);
		LocalDotNode dotNode = convertActivity(tree, unode, cardinality);

		addArc(tree, source, dotNode, unode, directionForward, false);
		addArc(tree, dotNode, sink, unode, directionForward, false);

		//draw model moves
		if (parameters.isShowModelMoves()) {
			Pair<String, Long> modelMoves = data.getModelMoveEdgeLabel(unode);
			if (modelMoves.getB() != 0) {
				addMoveArc(tree, source, sink, unode, EdgeType.modelMove, -1, -1, modelMoves, directionForward);
			}
		}

		//draw log moves
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(tree, dotNode, dotNode, unode, LogMovePosition.onLeaf(unode), directionForward);
		}
	}

	private LocalDotNode convertActivity(IvMEfficientTree tree, int unode, Triple<String, Long, Long> cardinality) {
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
		traceViewColourMap.set(unode, fillColour, fontColour);

		String label = tree.getActivityName(unode);
		if (label.length() == 0) {
			label = " ";
		}
		if (!cardinality.getA().isEmpty()) {
			label += "\n" + cardinality.getA();
		}

		final LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.activity, label, unode, null);
		if (gradientColour == null) {
			dotNode.setOption("fillcolor", ColourMap.toHexString(fillColour));
		} else {
			dotNode.setOption("fillcolor",
					ColourMap.toHexString(fillColour) + ":" + ColourMap.toHexString(gradientColour));
		}
		dotNode.setOption("fontcolor", ColourMap.toHexString(fontColour));

		info.addNode(unode, dotNode, null);
		return dotNode;
	}

	private void convertTau(IvMEfficientTree tree, int unode, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {
		addArc(tree, source, sink, unode, directionForward, false);
	}

	private void convertSequence(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {
		LocalDotNode split;
		LocalDotNode join = source;

		Iterator<Integer> it = tree.getChildren(node).iterator();
		while (it.hasNext()) {
			int child = it.next();

			split = join;
			if (it.hasNext()) {
				join = new LocalDotNode(dot, info, NodeType.xor, "", node, null);
			} else {
				join = sink;
			}

			convertNode(tree, child, split, join, directionForward);

			//draw log-move-arc if necessary
			if (parameters.isShowLogMoves()) {
				visualiseLogMove(tree, split, split, node, LogMovePosition.beforeChild(node, child), directionForward);
			}
		}
	}

	private void convertLoop(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.xor, "", node, null);
		addArc(tree, source, split, node, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.xor, "", node, null);

		int bodyChild = tree.getChild(node, 0);
		convertNode(tree, bodyChild, split, join, directionForward);

		int redoChild = tree.getChild(node, 1);
		convertNode(tree, redoChild, join, split, !directionForward);

		int exitChild = tree.getChild(node, 2);
		convertNode(tree, exitChild, join, sink, directionForward);

		//put log-moves on children
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(tree, split, split, node, LogMovePosition.beforeChild(node, bodyChild), directionForward);
			visualiseLogMove(tree, join, join, node, LogMovePosition.beforeChild(node, redoChild), directionForward);
			/*
			 * In principle, there can be log moves before the exit node.
			 * However, we assume them to be mapped before the redo child, as
			 * that is the same position in the model. It's up to the log move
			 * mapping to assure this.
			 */
			assert (data.getLogMoveEdgeLabel(LogMovePosition.beforeChild(node, exitChild)).getB().size() == 0);

		}
	}

	private void convertConcurrent(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.concurrentSplit, "+", node, null);
		addArc(tree, source, split, node, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.concurrentJoin, "+", node, split);
		addArc(tree, join, sink, node, directionForward, true);

		for (int child : tree.getChildren(node)) {
			convertNode(tree, child, split, join, directionForward);
		}

		//put log-moves, if necessary
		if (parameters.isShowLogMoves()) {
			//on split
			visualiseLogMove(tree, split, split, node, LogMovePosition.atSource(node), directionForward);

			//on join
			visualiseLogMove(tree, join, join, node, LogMovePosition.atSink(node), directionForward);
		}
	}

	private void convertInterleaved(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.interleavedSplit, "\u2194", node, null);
		addArc(tree, source, split, node, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.interleavedJoin, "\u2194", node, split);
		addArc(tree, join, sink, node, directionForward, true);

		for (int child : tree.getChildren(node)) {
			convertNode(tree, child, split, join, directionForward);
		}

		//put log-moves, if necessary
		if (parameters.isShowLogMoves()) {
			//on split
			visualiseLogMove(tree, split, split, node, LogMovePosition.atSource(node), directionForward);

			//on join
			visualiseLogMove(tree, join, join, node, LogMovePosition.atSink(node), directionForward);
		}
	}

	private void convertOr(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.orSplit, "o", node, null);
		addArc(tree, source, split, node, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.orJoin, "o", node, split);
		addArc(tree, join, sink, node, directionForward, true);

		for (int child : tree.getChildren(node)) {
			convertNode(tree, child, split, join, directionForward);
		}

		//put log-moves, if necessary
		if (parameters.isShowLogMoves()) {
			//on split
			visualiseLogMove(tree, split, split, node, LogMovePosition.atSource(node), directionForward);

			//on join
			visualiseLogMove(tree, join, join, node, LogMovePosition.atSink(node), directionForward);
		}
	}

	private void convertXor(IvMEfficientTree tree, int node, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) throws UnknownTreeNodeException {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.xor, "", node, null);
		addArc(tree, source, split, node, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.xor, "", node, split);
		addArc(tree, join, sink, node, directionForward, true);

		for (int child : tree.getChildren(node)) {
			convertNode(tree, child, split, join, directionForward);
		}

		//log-moves
		//are never put on xor
	}

	private LocalDotEdge addArc(IvMEfficientTree tree, final LocalDotNode from, final LocalDotNode to, final int node,
			boolean directionForward, boolean includeModelMoves) throws UnknownTreeNodeException {
		return addModelArc(tree, from, to, node, directionForward, data.getEdgeLabel(node, includeModelMoves));
	}

	private LocalDotEdge addModelArc(IvMEfficientTree tree, final LocalDotNode from, final LocalDotNode to,
			final int unode, final boolean directionForward, final Pair<String, Long> cardinality) {

		final LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(tree, dot, info, from, to, "", unode, EdgeType.model, -1, -1, directionForward);
		} else {
			edge = new LocalDotEdge(tree, dot, info, to, from, "", unode, EdgeType.model, -1, -1, directionForward);
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

	private void visualiseLogMove(IvMEfficientTree tree, LocalDotNode from, LocalDotNode to, int unode,
			LogMovePosition logMovePosition, boolean directionForward) {
		Pair<String, MultiSet<XEventClass>> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
		Pair<String, Long> t = Pair.of(logMoves.getA(), logMoves.getB().size());
		if (logMoves.getB().size() > 0) {
			if (parameters.isRepairLogMoves()) {
				for (XEventClass e : logMoves.getB()) {
					LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.logMoveActivity, e.toString(), unode,
							null);
					addMoveArc(tree, from, dotNode, unode, EdgeType.logMove, logMovePosition.getOn(),
							logMovePosition.getBeforeChild(), t, directionForward);
					addMoveArc(tree, dotNode, to, unode, EdgeType.logMove, logMovePosition.getOn(),
							logMovePosition.getBeforeChild(), t, directionForward);
				}
			} else {
				addMoveArc(tree, from, to, unode, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t, directionForward);
			}
		}
	}

	private LocalDotEdge addMoveArc(IvMEfficientTree tree, LocalDotNode from, LocalDotNode to, int node, EdgeType type,
			int lookupNode1, int lookupNode2, Pair<String, Long> cardinality, boolean directionForward) {

		LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(tree, dot, info, from, to, "", node, type, lookupNode1, lookupNode2,
					directionForward);
		} else {
			edge = new LocalDotEdge(tree, dot, info, to, from, "", node, type, lookupNode1, lookupNode2,
					directionForward);
			edge.setOption("dir", "back");
		}

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

	private double getOccurrenceFactor(long cardinality) {
		return ProcessTreeVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}
}
