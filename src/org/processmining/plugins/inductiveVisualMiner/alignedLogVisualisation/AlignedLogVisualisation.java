package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import java.awt.Color;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.etm.termination.ProMCancelTerminationCondition;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.inductiveVisualMiner.TraceView.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentETM;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Or;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

// import org.processmining.plugins.InductiveMiner.mining.operators.Interleaved;

public class AlignedLogVisualisation {

	public Dot fancy(PluginContext context, ProcessTree tree, XLog xLog, XLogInfo xLogInfo, XLogInfo XLogInfoPerformance) {
		AlignmentResult result = AlignmentETM.alignTree(tree,
				new XEventPerformanceClassifier(MiningParametersIM.getDefaultClassifier()), xLog,
				xLogInfo.getEventClasses(), XLogInfoPerformance.getEventClasses(),
				ProMCancelTerminationCondition.buildDummyCanceller());
		return fancy(tree, new AlignedLogVisualisationDataImplFrequencies(tree, result.logInfo),
				new AlignedLogVisualisationParameters()).getA();
	}

	private long maxCardinality;
	private long minCardinality;
	AlignedLogVisualisationParameters parameters;

	private AlignedLogVisualisationData data;
	
	private Dot dot;
	private AlignedLogVisualisationInfo info;
	private TraceViewColourMap traceViewColourMap;

	public Triple<Dot, AlignedLogVisualisationInfo, TraceViewColourMap> fancy(ProcessTree tree, AlignedLogVisualisationData data,
			AlignedLogVisualisationParameters parameters) {
		this.parameters = parameters;
		this.data = data;

		//find maximum and mimimum occurrences
		Pair<Long, Long> p = data.getExtremeCardinalities();
		minCardinality = p.getLeft();
		maxCardinality = p.getRight();

		dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		UnfoldedNode root = new UnfoldedNode(tree.getRoot());
		
		traceViewColourMap = new TraceViewColourMap();

		//source & sink
		info = new AlignedLogVisualisationInfo();
		LocalDotNode source = new LocalDotNode(dot, info, NodeType.source, "", root);
		LocalDotNode sink = new LocalDotNode(dot, info, NodeType.sink, "", root);
		info.setRoot(source, sink);
		//convert root node
		convertNode(root, source, sink, true);

		//add log-move-arcs to source and sink
		//a parallel root will project its own log moves 
		if (parameters.isShowLogMoves() && !(root.getBlock() instanceof And)) {
			visualiseLogMove(source, source, root, LogMovePosition.atSource(root), true);
			visualiseLogMove(sink, sink, root, LogMovePosition.atSink(root), false);
		}

		return Triple.of(dot, info, traceViewColourMap);
	}

	private void convertNode(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		if (unode.getNode() instanceof Seq) {
			convertSequence(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof XorLoop) {
			convertLoop(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Interleaved) {
			//			convertParallel(unode, source, sink, directionForward, "\u2194");
			convertParallel(unode, source, sink, directionForward, "-");
		} else if (unode.getNode() instanceof And) {
			convertParallel(unode, source, sink, directionForward, "+");
		} else if (unode.getNode() instanceof Or) {
			convertOr(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Xor) {
			convertXor(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Manual) {
			convertActivity(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Automatic) {
			convertTau(unode, source, sink, directionForward);
		}
	}

	private void convertActivity(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		Triple<String, Long, String> cardinality = data.getNodeLabel(unode, false);
		LocalDotNode dotNode = convertActivity(unode, cardinality);

		addArc(source, dotNode, unode, directionForward, false);
		addArc(dotNode, sink, unode, directionForward, false);

		//draw model moves
		if (parameters.isShowModelMoves()) {
			Triple<String, Long, String> modelMoves = data.getModelMoveEdgeLabel(unode);
			if (modelMoves.getB() != 0) {
				addMoveArc(source, sink, unode, EdgeType.modelMove, null, null, modelMoves, directionForward);
			}
		}

		//draw log moves
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(dotNode, dotNode, unode, LogMovePosition.onLeaf(unode), directionForward);
		}
	}

	private LocalDotNode convertActivity(UnfoldedNode unode, Triple<String, Long, String> cardinality) {
		//style the activity by the occurrences of it
		Color fillColour = Color.white;
		if (cardinality.getB() != 0 && parameters.getColourNodes() != null) {
			fillColour = parameters.getColourNodes().colour((long) (getOccurrenceFactor(cardinality.getB()) * 100), 0, 100);
		}

		//determine label colour
		Color fontColour = Color.black;
		if (ColourMaps.getLuma(fillColour) < 128) {
			fontColour = Color.white;
		}
		traceViewColourMap.set(unode, fillColour, fontColour);

		String label = unode.getNode().getName();
		if (label.length() == 0) {
			label = " ";
		}
		label += "\n" + cardinality.getA();
		if (cardinality.getB() != -1 && parameters.isShowFrequenciesOnNodes()) {
			label += cardinality.getB();
		}
		label += cardinality.getC();

		final LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.activity, label, unode);
		dotNode.setOption("fillcolor", ColourMap.toHexString(fillColour));
		dotNode.setOption("fontcolor", ColourMap.toHexString(fontColour));

		info.addNode(unode, dotNode);
		return dotNode;
	}

	private void convertTau(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		addArc(source, sink, unode, directionForward, false);
	}

	private void convertSequence(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		LocalDotNode split;
		LocalDotNode join = source;

		Iterator<Node> it = unode.getBlock().getChildren().iterator();
		while (it.hasNext()) {
			Node child = it.next();

			split = join;
			if (it.hasNext()) {
				join = new LocalDotNode(dot, info, NodeType.xor, "", unode);
			} else {
				join = sink;
			}

			convertNode(unode.unfoldChild(child), split, join, directionForward);

			//draw log-move-arc if necessary
			if (parameters.isShowLogMoves()) {
				visualiseLogMove(split, split, unode, LogMovePosition.beforeChild(unode, unode.unfoldChild(child)),
						directionForward);
			}
		}
	}

	private void convertLoop(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.xor, "", unode);
		addArc(source, split, unode, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.xor, "", unode);

		Node bodyChild = unode.getBlock().getChildren().get(0);
		convertNode(unode.unfoldChild(bodyChild), split, join, directionForward);

		Node redoChild = unode.getBlock().getChildren().get(1);
		convertNode(unode.unfoldChild(redoChild), join, split, !directionForward);

		Node exitChild = unode.getBlock().getChildren().get(2);
		convertNode(unode.unfoldChild(exitChild), join, sink, directionForward);

		//put log-moves on children
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(split, split, unode, LogMovePosition.beforeChild(unode, unode.unfoldChild(bodyChild)),
					directionForward);
			visualiseLogMove(join, join, unode, LogMovePosition.beforeChild(unode, unode.unfoldChild(redoChild)),
					directionForward);

			//log moves can be projected before the exit-tau
			//(assume it's tau)
			info.registerExtraEdge(unode, unode.unfoldChild(exitChild),
					info.getLogMoveEdge(unode, unode.unfoldChild(redoChild)));
		}
	}

	private void convertParallel(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward,
			String sign) {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.parallelSplit, sign, unode);
		addArc(source, split, unode, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.parallelJoin, sign, unode);
		addArc(join, sink, unode, directionForward, true);

		for (Node child : unode.getBlock().getChildren()) {
			convertNode(unode.unfoldChild(child), split, join, directionForward);
		}

		//put log-moves, if necessary
		if (parameters.isShowLogMoves()) {
			//on split
			visualiseLogMove(split, split, unode, LogMovePosition.atSource(unode), directionForward);

			//on join
			visualiseLogMove(join, join, unode, LogMovePosition.atSink(unode), directionForward);
		}
	}

	private void convertOr(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.parallelSplit, "o", unode);
		addArc(source, split, unode, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.parallelJoin, "o", unode);
		addArc(join, sink, unode, directionForward, true);

		for (Node child : unode.getBlock().getChildren()) {
			convertNode(unode.unfoldChild(child), split, join, directionForward);
		}

		//put log-moves, if necessary
		if (parameters.isShowLogMoves()) {
			//on split
			visualiseLogMove(split, split, unode, LogMovePosition.atSource(unode), directionForward);

			//on join
			visualiseLogMove(join, join, unode, LogMovePosition.atSink(unode), directionForward);
		}
	}

	private void convertXor(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.xor, "", unode);
		addArc(source, split, unode, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.xor, "", unode);
		addArc(join, sink, unode, directionForward, true);

		for (Node child : unode.getBlock().getChildren()) {
			convertNode(unode.unfoldChild(child), split, join, directionForward);
		}

		//log-moves
		//are never put on xor
	}

	private LocalDotEdge addArc(final LocalDotNode from, final LocalDotNode to, final UnfoldedNode unode,
			boolean directionForward, boolean includeModelMoves) {
		return addModelArc(from, to, unode, directionForward, data.getEdgeLabel(unode, includeModelMoves));
	}

	private LocalDotEdge addModelArc(final LocalDotNode from, final LocalDotNode to, final UnfoldedNode unode,
			final boolean directionForward, final Triple<String, Long, String> cardinality) {

		final LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(dot, info, from, to, "", unode, EdgeType.model, null, null, directionForward);
		} else {
			edge = new LocalDotEdge(dot, info, to, from, "", unode, EdgeType.model, null, null, directionForward);
			edge.setOption("dir", "back");
		}

		if (parameters.getColourModelEdges() != null) {
			String lineColour = parameters.getColourModelEdges().colourString(cardinality.getB(), minCardinality,
					maxCardinality);
			edge.setOption("color", lineColour);
		}

		edge.setOption("penwidth",
				"" + parameters.getModelEdgesWidth().size(cardinality.getB(), minCardinality, maxCardinality));

		if (parameters.isShowFrequenciesOnModelEdges()) {
			edge.setLabel(cardinality.getA() + cardinality.getB() + cardinality.getC());
		}

		return edge;
	}

	private void visualiseLogMove(LocalDotNode from, LocalDotNode to, UnfoldedNode unode,
			LogMovePosition logMovePosition, boolean directionForward) {
		Triple<String, MultiSet<XEventClass>, String> logMoves = data.getLogMoveEdgeLabel(logMovePosition);
		Triple<String, Long, String> t = Triple.of(logMoves.getA(), logMoves.getB().size(), logMoves.getC());
		if (logMoves.getB().size() > 0) {
			if (parameters.isRepairLogMoves()) {
				for (XEventClass e : logMoves.getB()) {
					long cardinality = logMoves.getB().getCardinalityOf(e);
					LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.logMoveActivity, e.toString(), unode);
					addMoveArc(from, dotNode, unode, EdgeType.logMove, logMovePosition.getOn(),
							logMovePosition.getBeforeChild(), t, directionForward);
					addMoveArc(dotNode, to, unode, EdgeType.logMove, logMovePosition.getOn(),
							logMovePosition.getBeforeChild(), t, directionForward);
				}
			} else {
				addMoveArc(from, to, unode, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), t, directionForward);
			}
		}
	}

	private LocalDotEdge addMoveArc(LocalDotNode from, LocalDotNode to, UnfoldedNode unode, EdgeType type,
			UnfoldedNode lookupNode1, UnfoldedNode lookupNode2, Triple<String, Long, String> cardinality, boolean directionForward) {

		LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(dot, info, from, to, "", unode, type, lookupNode1, lookupNode2, directionForward);
		} else {
			edge = new LocalDotEdge(dot, info, to, from, "", unode, type, lookupNode1, lookupNode2, directionForward);
			edge.setOption("dir", "back");
		}

		edge.setOption("style", "dashed");
		edge.setOption("arrowsize", ".5");

		if (parameters.getColourMoves() != null) {
			String lineColour = parameters.getColourMoves().colourString(cardinality.getB(), minCardinality, maxCardinality);
			edge.setOption("color", lineColour);
			edge.setOption("fontcolor", lineColour);
		}

		edge.setOption("penwidth", ""
				+ parameters.getMoveEdgesWidth().size(cardinality.getB(), minCardinality, maxCardinality));

		if (parameters.isShowFrequenciesOnMoveEdges()) {
			edge.setLabel(cardinality.getA() + cardinality.getB() + cardinality.getC());
		}

		return edge;
	}

	private double getOccurrenceFactor(long cardinality) {
		return AlignedLogVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}
}
