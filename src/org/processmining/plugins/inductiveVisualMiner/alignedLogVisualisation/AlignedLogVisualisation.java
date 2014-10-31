package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.etm.termination.ProMCancelTerminationCondition;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentETM;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class AlignedLogVisualisation {

	public Dot fancy(PluginContext context, ProcessTree tree, XLog xLog) {
		AlignmentResult result = AlignmentETM.alignTree(tree, MiningParameters.getDefaultClassifier(), xLog,
				new HashSet<XEventClass>(), ProMCancelTerminationCondition.buildDummyCanceller());
		return fancy(tree, result.logInfo, new AlignedLogVisualisationParameters()).getLeft();
	}

	Dot dot;
	private AlignedLogInfo logInfo;
	private long maxCardinality;
	private long minCardinality;
	AlignedLogVisualisationParameters parameters;

	private AlignedLogVisualisationInfo info;

	public Pair<Dot, AlignedLogVisualisationInfo> fancy(ProcessTree tree, AlignedLogInfo logInfo,
			AlignedLogVisualisationParameters parameters) {
		this.parameters = parameters;
		if (logInfo == null) {
			//use empty logInfo
			logInfo = new AlignedLogInfo();
			//set parameters to not show frequencies
			parameters.setShowFrequenciesOnModelEdges(false);
			parameters.setShowFrequenciesOnMoveEdges(false);
			parameters.setShowFrequenciesOnNodes(false);
		}
		this.logInfo = logInfo;

		//find maximum and mimimum occurrences
		Pair<Long, Long> p = AlignedLogMetrics.getExtremes(new UnfoldedNode(tree.getRoot()), logInfo);
		minCardinality = p.getLeft();
		maxCardinality = p.getRight();

		dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		UnfoldedNode root = new UnfoldedNode(tree.getRoot());

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

		return Pair.of(dot, info);
	}

	private void convertNode(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		if (unode.getNode() instanceof Seq) {
			convertSequence(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof XorLoop) {
			convertLoop(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof And) {
			convertParallel(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Xor) {
			convertXor(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Manual) {
			convertActivity(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Automatic) {
			convertTau(unode, source, sink, directionForward);
		}
	}

	private void convertActivity(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, false, logInfo);
		LocalDotNode dotNode = convertActivity(unode, cardinality);

		addArc(source, dotNode, unode, directionForward, false);
		addArc(dotNode, sink, unode, directionForward, false);

		//draw model moves
		if (parameters.isShowModelMoves()) {
			long modelMoves = AlignedLogMetrics.getModelMovesLocal(unode, logInfo);
			if (modelMoves != 0) {
				addMoveArc(source, sink, unode, EdgeType.modelMove, null, null, modelMoves, directionForward);
			}
		}
	}

	private LocalDotNode convertActivity(UnfoldedNode unode, long cardinality) {
		//style the activity by the occurrences of it
		Color fillColour = Color.white;
		if (cardinality != 0 && parameters.getColourNodes() != null) {
			fillColour = parameters.getColourNodes().colour((long) (getOccurrenceFactor(cardinality) * 100), 0, 100);
		}

		//determine label colour
		Color fontColour = Color.black;
		if (ColourMaps.getLuma(fillColour) < 128) {
			fontColour = Color.white;
		}

		String label = unode.getNode().getName();
		if (label.length() == 0) {
			label = " ";
		}
		if (cardinality != -1 && parameters.isShowFrequenciesOnNodes()) {
			label += "\n" + cardinality;
		}

		final LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.activity, label, unode);
		dotNode.setOptions(dotNode.getOptions() + ", fillcolor=\"" + ColourMap.toHexString(fillColour)
				+ "\", fontcolor=\"" + ColourMap.toHexString(fontColour) + "\"");

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
			visualiseLogMove(join, join, unode, LogMovePosition.beforeChild(unode, unode.unfoldChild(bodyChild)),
					directionForward);
			visualiseLogMove(split, split, unode, LogMovePosition.beforeChild(unode, unode.unfoldChild(redoChild)),
					directionForward);

			//log moves can be projected before the exit-tau
			//(assume it's tau)
			info.registerExtraEdge(unode, unode.unfoldChild(exitChild),
					info.getLogMoveEdge(unode, unode.unfoldChild(redoChild)));
		}
	}

	private void convertParallel(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(dot, info, NodeType.parallelSplit, "+", unode);
		addArc(source, split, unode, directionForward, true);

		//operator join
		LocalDotNode join = new LocalDotNode(dot, info, NodeType.parallelJoin, "+", unode);
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

	private LocalDotEdge addArc(LocalDotNode from, LocalDotNode to, final UnfoldedNode unode, boolean directionForward,
			boolean includeModelMoves) {
		return addModelArc(from, to, unode, directionForward,
				AlignedLogMetrics.getNumberOfTracesRepresented(unode, includeModelMoves, logInfo));
	}

	private LocalDotEdge addModelArc(LocalDotNode from, LocalDotNode to, final UnfoldedNode unode,
			boolean directionForward, long cardinality) {

		String options = "";

		if (parameters.getColourModelEdges() != null) {
			String lineColour = parameters.getColourModelEdges().colourString(cardinality, minCardinality,
					maxCardinality);
			options += "color=\"" + lineColour + "\", ";
		}

		options += "penwidth=" + parameters.getModelEdgesWidth().size(cardinality, minCardinality, maxCardinality);

		final LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(dot, info, from, to, "", options, unode, EdgeType.model, null, null,
					directionForward);
		} else {
			edge = new LocalDotEdge(dot, info, to, from, "", options + ", dir=\"back\"", unode, EdgeType.model, null,
					null, directionForward);
		}

		if (parameters.isShowFrequenciesOnModelEdges()) {
			edge.setLabel(cardinality + "");
		}

		return edge;
	}

	private void visualiseLogMove(LocalDotNode from, LocalDotNode to, UnfoldedNode unode,
			LogMovePosition logMovePosition, boolean directionForward) {
		MultiSet<XEventClass> logMoves = AlignedLogMetrics.getLogMoves(logMovePosition, logInfo);
		if (logMoves.size() > 0) {
			if (parameters.isRepairLogMoves()) {
				for (XEventClass e : logMoves) {
					long cardinality = logMoves.getCardinalityOf(e);
					LocalDotNode dotNode = new LocalDotNode(dot, info, NodeType.logMoveActivity, e.toString(), unode);
					addMoveArc(from, dotNode, unode, EdgeType.logMove, logMovePosition.getOn(),
							logMovePosition.getBeforeChild(), cardinality, directionForward);
					addMoveArc(dotNode, to, unode, EdgeType.logMove, logMovePosition.getOn(),
							logMovePosition.getBeforeChild(), cardinality, directionForward);
				}
			} else {
				addMoveArc(from, to, unode, EdgeType.logMove, logMovePosition.getOn(),
						logMovePosition.getBeforeChild(), logMoves.size(), directionForward);
			}
		}
	}

	private LocalDotEdge addMoveArc(LocalDotNode from, LocalDotNode to, UnfoldedNode unode, EdgeType type,
			UnfoldedNode lookupNode1, UnfoldedNode lookupNode2, long cardinality, boolean directionForward) {

		String options = "style=\"dashed\", arrowsize=.5";

		if (parameters.getColourMoves() != null) {
			String lineColour = parameters.getColourMoves().colourString(cardinality, minCardinality, maxCardinality);
			options += ", color=\"" + lineColour + "\", fontcolor=\"" + lineColour + "\"";
		}

		options += ", penwidth=" + parameters.getMoveEdgesWidth().size(cardinality, minCardinality, maxCardinality);

		LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(dot, info, from, to, "", options, unode, type, lookupNode1, lookupNode2,
					directionForward);
		} else {
			edge = new LocalDotEdge(dot, info, to, from, "", options + ", dir=\"back\", " + options, unode, type,
					lookupNode1, lookupNode2, directionForward);
		}

		if (parameters.isShowFrequenciesOnMoveEdges()) {
			edge.setLabel(cardinality + "");
		}

		return edge;
	}

	private double getOccurrenceFactor(long cardinality) {
		return AlignedLogVisualisationHelper.getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}
}
