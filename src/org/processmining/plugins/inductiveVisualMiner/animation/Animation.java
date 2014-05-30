package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class Animation {

	public static List<LocalDotEdge> getEdgesOnMovePath(List<TimedMove> movePath, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel, boolean addSource, boolean addSink) {

		//make a node-path
		List<LocalDotNode> nodePath = new ArrayList<>();
		if (addSource) {
			nodePath.add(panel.getRootSource());
		}
		for (TimedMove move : movePath) {
			LocalDotNode node = getDotNodeFromActivity(move, panel);
			if (node != null) {
				nodePath.add(node);
			} else if (move.isModelSync() && move.getUnode().getNode() instanceof Automatic) {
				nodePath.add(panel.getUnfoldedNode2dotEdgesModel().get(move.getUnode()).get(0).getSource());
			}
		}
		if (addSink) {
			nodePath.add(panel.getRootSink());
		}

		//construct edge-path
		List<LocalDotEdge> result = new ArrayList<>();
		Iterator<LocalDotNode> it = nodePath.iterator();

		if (nodePath.size() < 2) {
			return result;
		}

		LocalDotNode from;
		LocalDotNode to = it.next();

		while (it.hasNext()) {
			from = to;
			to = it.next();

			result.addAll(shortestPath.getShortestPath(from, to));
		}

		return result;
	}

	public static void positionTrace(TimedTrace timedTrace, UnfoldedNode unode, Tokens tokens, boolean showDeviations,
			ShortestPathGraph shortestPath, InductiveVisualMinerPanel panel) {

		debug("");

		//create new token and put it on source/root
		Token token = new Token(panel.getRootSource(), timedTrace.getStartTime(), true);
		tokens.add(token);

		//position the trace
		positionTimedTrace(timedTrace, unode, token, tokens, panel.getRootSink(), timedTrace.getEndTime(),
				showDeviations, shortestPath, panel);
	}

	public static void positionTimedTrace(TimedTrace trace, UnfoldedNode unode, Token token, Tokens tokens,
			LocalDotNode destination, double destinationTime, boolean showDeviations, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel) {

		debug(" enter " + unode + " @" + token.getLastTime());
		debug("  trace " + trace);
		debug("  node-destination @" + destinationTime + ", " + destination);

		if (unode.getNode() instanceof Manual) {
			TokenEnter(trace, unode, token, destination, destinationTime, shortestPath, panel);
			debug("  execute " + unode + " " + getDotNodeFromActivity(trace.get(0), panel).getId() + " @"
					+ trace.get(0).getTimestamp());
		} else if (unode.getNode() instanceof Automatic) {
			TokenEnter(trace, unode, token, destination, destinationTime, shortestPath, panel);
			debug("  execute tau ");
		} else if (unode.getBlock() instanceof Xor || unode.getBlock() instanceof Def) {
			TokenEnter(trace, unode, token, destination, destinationTime, shortestPath, panel);
			positionXor(trace, unode, token, tokens, destination, destinationTime, showDeviations, shortestPath, panel);
		} else if (unode.getBlock() instanceof Seq) {
			//a sequence has no entry edge
			positionSequence(trace, unode, token, tokens, destination, destinationTime, showDeviations, shortestPath,
					panel);
		} else if (unode.getBlock() instanceof And) {
			Pair<LocalDotEdge, Double> enter = TokenEnter(trace, unode, token, destination, destinationTime,
					shortestPath, panel);

			positionParallel(trace, unode, token, tokens, destination, destinationTime, enter.getLeft().getTarget(),
					enter.getRight(), showDeviations, shortestPath, panel);
		} else if (unode.getBlock() instanceof DefLoop || unode.getBlock() instanceof XorLoop) {
			TokenEnter(trace, unode, token, destination, destinationTime, shortestPath, panel);
			positionLoop(trace, unode, token, tokens, destination, destinationTime, showDeviations, shortestPath, panel);
		}

		//node exit
		{
			debug(" exit " + unode);
			List<LocalDotEdge> edgesTillDestination = shortestPath
					.getShortestPath(token.getLastPosition(), destination);
			debug("  " + edgesTillDestination.size() + " edges to go, to arrive " + destination + " @"
					+ destinationTime);
			//see if we have not reached the destination yet, and whether the first edge belongs to this node
			if (!edgesTillDestination.isEmpty()) {
				LocalDotEdge exitEdge = edgesTillDestination.get(0);
				if (panel.getUnfoldedNode2dotEdgesModel().get(unode) != null
						&& panel.getUnfoldedNode2dotEdgesModel().get(unode).contains(exitEdge)) {
					//compute end time of this node
					double exitEndTime = token.getLastTime() + (destinationTime - token.getLastTime())
							/ (edgesTillDestination.size() * 1.0);
					debug("  end time of exit edge @" + exitEndTime);
					token.addPoint(exitEdge, exitEndTime);
				} else {
					debug("  exit edge does not belong to this node");
				}
			}
		}
	}

	/*
	 * Let a token enter a node Returns the enter edge and the time its
	 * traversal ends
	 */
	private static Pair<LocalDotEdge, Double> TokenEnter(TimedTrace trace, UnfoldedNode unode, Token token,
			LocalDotNode destination, double destinationTime, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel) {

		Pair<LocalDotNode, Double> firstTimedMove = getNextDestination(trace, 0, destination, destinationTime, panel);

		//perform sanity checks
		if (firstTimedMove.getRight() > destinationTime) {
			throw new RuntimeException("trace cannot finish on time");
		}
		if (trace.get(trace.size() - 1).getTimestamp() != null
				&& trace.get(trace.size() - 1).getTimestamp() > destinationTime) {
			throw new RuntimeException("trace cannot finish on time");
		}

		TimedMove firstMove = trace.get(0);
		List<LocalDotEdge> edgesTillDestination;
		if (firstMove.getType() == Type.log) {
			//log move
			LocalDotEdge logMoveEdge = null;
			edgesTillDestination = shortestPath.getShortestPath(token.getLastPosition(), logMoveEdge.getSource());
			edgesTillDestination.add(logMoveEdge);
		} else if (getDotNodeFromActivity(firstMove, panel) == null) {
			//if the first node has no corresponding dot node, we might need to make a detour to reach the requested destination
			LocalDotNode detour = panel.getUnfoldedNode2dotEdgesModel().get(firstMove.getUnode()).get(0).getTarget();
			edgesTillDestination = shortestPath.getShortestPath(token.getLastPosition(), detour);
			edgesTillDestination.addAll(shortestPath.getShortestPath(detour, firstTimedMove.getLeft()));
		} else {
			//otherwise, we can just take the shortest path
			edgesTillDestination = shortestPath.getShortestPath(token.getLastPosition(), firstTimedMove.getLeft());
		}
		debug("  " + edgesTillDestination.size() + " edges to go to arrive @" + firstTimedMove.getRight() + ", "
				+ firstTimedMove.getLeft());

		//see if the first edge belongs to this node
		if (edgesTillDestination.size() == 0) {
			throw new RuntimeException("there are no edges left to traverse");
		}
		//compute end time of this node
		LocalDotEdge enterEdge = edgesTillDestination.get(0);
		double enterTime = token.getLastTime() + (firstTimedMove.getRight() - token.getLastTime())
				/ (edgesTillDestination.size() * 1.0);
		if (panel.getUnfoldedNode2dotEdgesModel().get(unode) != null
				&& panel.getUnfoldedNode2dotEdgesModel().get(unode).contains(enterEdge)) {
			debug("  end time of enter edge @" + enterTime);
			token.addPoint(enterEdge, enterTime);
			return Pair.of(enterEdge, enterTime);
		} else if (panel.getUnfoldedNode2dotEdgesMove().get(unode) != null
				&& panel.getUnfoldedNode2dotEdgesMove().get(unode).contains(enterEdge)) {
			debug("  end time of enter edge @" + enterTime + " with model-move-edge");
			token.addPoint(enterEdge, enterTime);
			return Pair.of(enterEdge, enterTime);
		} else {
			debug("  enter edge does not belong to this node");
		}

		return null;
	}

	public static LocalDotEdge getModelMoveEdge(TimedMove move, InductiveVisualMinerPanel panel) {
		return panel.getUnfoldedNode2dotEdgesMove().get(move.getUnode()).get(0);
	}
	
	public static LocalDotEdge getTauEdge(TimedMove move, InductiveVisualMinerPanel panel) {
		return panel.getUnfoldedNode2dotEdgesModel().get(move.getUnode()).get(0);
	}
	
	public static LocalDotNode getParallelSplit(UnfoldedNode unode, InductiveVisualMinerPanel panel) {
		for (LocalDotNode node: panel.getUnfoldedNode2dotNodes().get(unode)) {
			if (node.type == NodeType.parallel) {
				return node;
			}
		}
		return null;
	}
	
	public static LocalDotNode getParallelJoin(UnfoldedNode unode, InductiveVisualMinerPanel panel) {
		boolean first = false;
		for (LocalDotNode node: panel.getUnfoldedNode2dotNodes().get(unode)) {
			if (node.type == NodeType.parallel) {
				if (!first) {
					first = true;
				} else {
					return node;
				}
			}
		}
		return null;
	}

	private static void positionXor(TimedTrace trace, UnfoldedNode unode, Token token, Tokens tokens,
			LocalDotNode destination, double destinationTime, boolean showDeviations, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel) {
		//only execute the child that produced the trace
		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);
		Iterator<List<TimedTrace>> itSublog = sublogs.iterator();
		Iterator<Node> itChild = unode.getBlock().getChildren().iterator();
		while (itSublog.hasNext() && itChild.hasNext()) {
			TimedTrace childSublog = itSublog.next().get(0);
			UnfoldedNode child = unode.unfoldChild(itChild.next());
			if (childSublog.size() > 0) {
				positionTimedTrace(childSublog, child, token, tokens, destination, destinationTime, showDeviations,
						shortestPath, panel);
			}
		}
	}

	private static void positionSequence(TimedTrace trace, UnfoldedNode unode, Token token, Tokens tokens,
			LocalDotNode destination, double destinationTime, boolean showDeviations, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel) {

		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);

		for (int i = 0; i < sublogs.size(); i++) {

			UnfoldedNode child = unode.unfoldChild(unode.getBlock().getChildren().get(i));
			TimedTrace childTrace = sublogs.get(i).get(0);

			//compute the destination of this child
			Pair<LocalDotNode, Double> childDestination;
			if (i == sublogs.size() - 1) {
				//the destination of this child is the destination of the sequence
				childDestination = Pair.of(destination, destinationTime);
			} else {
				//the destination of this child is the first move of the next trace
				TimedMove nextMove = sublogs.get(i + 1).get(0).get(0);
				childDestination = getNextDestination(trace, trace.indexOf(nextMove), destination, destinationTime,
						panel);
			}

			positionTimedTrace(childTrace, child, token, tokens, childDestination.getLeft(),
					childDestination.getRight(), showDeviations, shortestPath, panel);
		}
	}

	private static void positionParallel(TimedTrace trace, UnfoldedNode unode, Token token, Tokens tokens,
			LocalDotNode destination, double destinationTime, LocalDotNode split, double splitTime,
			boolean showDeviations, ShortestPathGraph shortestPath, InductiveVisualMinerPanel panel) {
		//split the trace
		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);

		//find the last-ending non-tau subtrace
		//if there is none (all subtraces are taus), pick the first one
		int lastEndingSubtrace = 0;
		{
			TimedMove lastMove = getLastTimedMove(trace);
			debug("   last timed move " + lastMove);
			if (lastMove != null) {
				//find the child that this last move belongs to
				for (int i = 0; i < sublogs.size(); i++) {
					TimedTrace childTrace = sublogs.get(i).get(0);
					if (childTrace.contains(lastMove)) {
						lastEndingSubtrace = i;
						break;
					}
				}
			}
		}

		//first, process the trace that takes longest
		{
			TimedTrace childTrace = sublogs.get(lastEndingSubtrace).get(0);
			UnfoldedNode child = unode.unfoldChild(unode.getBlock().getChildren().get(lastEndingSubtrace));
			positionTimedTrace(childTrace, child, token, tokens, destination, destinationTime, showDeviations,
					shortestPath, panel);
		}

		//record where the token is at the end of the trace that takes longest
		LocalDotNode join = token.getLastPosition();
		double joinTime = token.getLastTime();

		for (int i = 0; i < sublogs.size(); i++) {
			TimedTrace childTrace = sublogs.get(i).get(0);
			UnfoldedNode child = unode.unfoldChild(unode.getBlock().getChildren().get(i));
			if (i != lastEndingSubtrace) {
				//this child does not end last

				//create a new token
				Token childToken = new Token(split, splitTime, false);
				tokens.add(childToken);

				//animate this child on the new token
				positionTimedTrace(childTrace, child, childToken, tokens, join, joinTime, showDeviations, shortestPath,
						panel);

				if (childToken.getPoints().size() == 0) {
					throw new RuntimeException("empty token created");
				}
			}
		}
	}

	private static void positionLoop(TimedTrace trace, UnfoldedNode unode, Token token, Tokens tokens,
			LocalDotNode destination, double destinationTime, boolean showDeviations, ShortestPathGraph shortestPath,
			InductiveVisualMinerPanel panel) {
		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);

		List<TimedTrace> sublogBody = sublogs.get(0);
		List<TimedTrace> sublogRedo = sublogs.get(1);
		List<TimedTrace> sublogExit = sublogs.get(2);
		UnfoldedNode childBody = unode.unfoldChild(unode.getBlock().getChildren().get(0));
		UnfoldedNode childRedo = unode.unfoldChild(unode.getBlock().getChildren().get(1));
		UnfoldedNode childExit = unode.unfoldChild(unode.getBlock().getChildren().get(2));

		//keep track of where in the trace we are
		//that is, what part of the trace has been animated
		int traceIndex = 0;

		//start with body
		{
			TimedTrace bodyTrace = sublogBody.get(0);
			traceIndex += bodyTrace.size();

			Pair<LocalDotNode, Double> bodyDestination = getNextDestination(trace, traceIndex, destination,
					destinationTime, panel);
			positionTimedTrace(bodyTrace, childBody, token, tokens, bodyDestination.getLeft(),
					bodyDestination.getRight(), showDeviations, shortestPath, panel);
		}

		//then redo + body
		for (int i = 0; i < sublogRedo.size(); i++) {
			TimedTrace traceRedo = sublogRedo.get(i);
			TimedTrace traceBody = sublogBody.get(i + 1);

			debug("  cross loop body -> redo");

			//redo
			{
				traceIndex += traceRedo.size();
				Pair<LocalDotNode, Double> redoDestination = getNextDestination(trace, traceIndex, destination,
						destinationTime, panel);
				positionTimedTrace(traceRedo, childRedo, token, tokens, redoDestination.getLeft(),
						redoDestination.getRight(), showDeviations, shortestPath, panel);
			}

			debug("  cross loop redo -> body");

			//body
			{
				traceIndex += traceBody.size();
				Pair<LocalDotNode, Double> bodyDestination = getNextDestination(trace, traceIndex, destination,
						destinationTime, panel);
				positionTimedTrace(traceBody, childBody, token, tokens, bodyDestination.getLeft(),
						bodyDestination.getRight(), showDeviations, shortestPath, panel);
			}
		}

		debug("  cross loop body -> redo");

		//exit with exit
		positionTimedTrace(sublogExit.get(0), childExit, token, tokens, destination, destinationTime, showDeviations,
				shortestPath, panel);
	}

	public static List<List<TimedTrace>> getSublogs(UnfoldedNode unode, TimedTrace trace) {

		//make a partition
		List<Set<UnfoldedNode>> partition = new LinkedList<Set<UnfoldedNode>>();
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uchild = unode.unfoldChild(child);
			partition.add(new HashSet<UnfoldedNode>(AlignedLogMetrics.unfoldAllNodes(uchild)));
		}

		//split the log
		return split(trace, partition, unode.getBlock() instanceof XorLoop || unode.getBlock() instanceof DefLoop);
	}

	public static List<List<TimedTrace>> split(TimedTrace trace, List<Set<UnfoldedNode>> partition, boolean loop) {
		List<List<TimedTrace>> result = new ArrayList<List<TimedTrace>>();

		//map activities to sigmas
		HashMap<Set<UnfoldedNode>, List<TimedTrace>> mapSigma2sublog = new HashMap<Set<UnfoldedNode>, List<TimedTrace>>();
		HashMap<UnfoldedNode, Set<UnfoldedNode>> mapUnode2sigma = new HashMap<UnfoldedNode, Set<UnfoldedNode>>();
		for (Set<UnfoldedNode> sigma : partition) {
			List<TimedTrace> sublog = new ArrayList<TimedTrace>();
			result.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (UnfoldedNode unode : sigma) {
				mapUnode2sigma.put(unode, sigma);
			}
		}

		if (!loop) {
			splitParallel(result, trace, partition, mapSigma2sublog, mapUnode2sigma);
		} else {
			splitLoop(result, trace, partition, mapSigma2sublog, mapUnode2sigma);
		}

		return result;
	}

	public static void splitParallel(List<List<TimedTrace>> result, TimedTrace trace,
			List<Set<UnfoldedNode>> partition, HashMap<Set<UnfoldedNode>, List<TimedTrace>> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapUnode2sigma) {

		//add a new trace to every sublog
		HashMap<Set<UnfoldedNode>, TimedTrace> mapSigma2subtrace = new HashMap<Set<UnfoldedNode>, TimedTrace>();
		for (Set<UnfoldedNode> sigma : partition) {
			TimedTrace subtrace = new TimedTrace();
			mapSigma2subtrace.put(sigma, subtrace);
		}

		for (TimedMove move : trace) {
			if (move.isModelSync()) {
				//model or synchronous move
				Set<UnfoldedNode> sigma = mapUnode2sigma.get(move.getUnode());
				mapSigma2subtrace.get(sigma).add(move);
			} else {
				//log move
				Set<UnfoldedNode> sigma = mapUnode2sigma.get(move.getLogMoveUnode());
				if (sigma != null) {
					mapSigma2subtrace.get(sigma).add(move);
				}
			}
		}

		for (Set<UnfoldedNode> sigma : partition) {
			mapSigma2sublog.get(sigma).add(mapSigma2subtrace.get(sigma));
		}
	}

	public static void splitLoop(List<List<TimedTrace>> result, TimedTrace trace,
			Collection<Set<UnfoldedNode>> partition, HashMap<Set<UnfoldedNode>, List<TimedTrace>> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma) {
		TimedTrace partialTrace = new TimedTrace();

		Set<UnfoldedNode> lastSigma = partition.iterator().next();
		for (TimedMove move : trace) {
			if (move.isModelSync()) {
				if (!lastSigma.contains(move.getUnode())) {
					mapSigma2sublog.get(lastSigma).add(partialTrace);
					partialTrace = new TimedTrace();
					lastSigma = mapActivity2sigma.get(move.getUnode());
				}
				partialTrace.add(move);
			}
		}
		mapSigma2sublog.get(lastSigma).add(partialTrace);
	}

	private static Pair<LocalDotNode, Double> getNextDestination(TimedTrace trace, int offset,
			LocalDotNode destination, double destinationTime, InductiveVisualMinerPanel panel) {
		for (int i = offset; i < trace.size(); i++) {
			TimedMove move = trace.get(i);
			LocalDotNode node = getDotNodeFromActivity(move, panel);
			if (node != null && move.getTimestamp() != null) {
				return Pair.of(node, move.getTimestamp());
			}
		}
		return Pair.of(destination, destinationTime);
	}

	private static TimedMove getLastTimedMove(TimedTrace trace) {
		for (int i = trace.size() - 1; i >= 0; i--) {
			TimedMove move = trace.get(i);
			if (move.getTimestamp() != null) {
				return move;
			}
		}
		return null;
	}

	public static LocalDotNode getDotNodeFromActivity(TimedMove move, InductiveVisualMinerPanel panel) {
		if (!panel.getUnfoldedNode2dotNodes().containsKey(move.getUnode())) {
			return null;
		}
		for (LocalDotNode node : panel.getUnfoldedNode2dotNodes().get(move.getUnode())) {
			if (node.type == NodeType.activity) {
				return node;
			}
		}
		return null;
	}

	private static void debug(Object s) {
//		System.out.println(s.toString().replaceAll("\\n", " "));
	}
}
