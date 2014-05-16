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
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.NodeType;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
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

import com.kitfox.svg.Path;

public class Animation {
	public static String getPath(DotPanel panel, Path path) {
		return panel.getAttributeOf(path, "d");
	}

	public static void positionTrace(IMTraceG<Move> trace, UnfoldedNode unode, InductiveVisualMinerPanel panel) {
		//make dummy timestamps
		TimedTrace timedTrace = new TimedTrace();
		double t = 1;
		for (Move move : trace) {
			timedTrace.add(new TimedMove(move, t++));
		}

		System.out.println(" ");

		//create new token and put it on source/root
		Tokens tokens = new Tokens();
		Token token = new Token(panel.getRootSource(), 0.0);
		tokens.add(token);

		positionTimedTrace(timedTrace, unode, null, null, token, tokens, panel.getRootSink(), t, panel);
		
		System.out.println();
		System.out.println(tokens);
	}

	public static void positionTimedTrace(TimedTrace trace, UnfoldedNode unode, TimedMove lastMoveBeforeThisTrace,
			TimedMove nextMoveAfterThisTrace, Token token, Tokens tokens, LocalDotNode destination, double destinationTime,
			InductiveVisualMinerPanel panel) {

		System.out.println(" enter " + unode + " @" + token.getLastTime() + ": " + lastMoveBeforeThisTrace + " -> "
				+ trace + " -> " + nextMoveAfterThisTrace);

		//find the length of the path that this token is currently traveling
		//i.e., from the last known position to the first move
		LocalDotEdge enterEdge;
		double enterTime = -1;
		{
			Pair<LocalDotNode, Double> firstMove = getNextDestination(trace, 0, destination, destinationTime, panel);
			List<LocalDotEdge> edgesTillDestination = ShortestPath.shortestPath(token.getLastPosition(),
					firstMove.getLeft(), panel);
			System.out.println("  " + edgesTillDestination.size() + " edges to go, to arrive @" + firstMove.getRight());

			//see if the first edge belongs to this node
			enterEdge = edgesTillDestination.get(0);
			if (panel.getUnfoldedNode2dotEdgesModel().get(unode) != null
					&& panel.getUnfoldedNode2dotEdgesModel().get(unode).contains(enterEdge)) {
				//compute end time of this node
				enterTime = token.getLastTime() + (firstMove.getRight() - token.getLastTime())
						/ (edgesTillDestination.size() * 1.0);
				System.out.println("  end time of enter edge @" + enterTime);
				token.addPoint(enterEdge, enterTime);
			} else {
				System.out.println("  enter edge does not belong to this node");
			}
		}

		if (unode.getNode() instanceof Manual) {
			System.out.println("  execute " + unode + " " + getDotNodeFromActivity(trace.get(0), panel).getId() + " @"
					+ trace.get(0).timestamp);
		} else if (unode.getNode() instanceof Automatic) {
			System.out.println("  execute tau ");
		} else if (unode.getBlock() instanceof DefLoop || unode.getBlock() instanceof XorLoop) {
			positionLoop(trace, unode, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, token, tokens, destination,
					destinationTime, panel);
		} else if (unode.getBlock() instanceof Seq) {
			positionSequence(trace, unode, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, token, tokens, destination,
					destinationTime, panel);
		} else if (unode.getBlock() instanceof Xor || unode.getBlock() instanceof Def) {
			positionXor(trace, unode, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, token, tokens, destination,
					destinationTime, panel);
		} else if (unode.getBlock() instanceof And) {
			positionParallel(trace, unode, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, token, tokens, destination,
					destinationTime, enterEdge.getTarget(), enterTime, panel);
		}

		//node exit
		{
			System.out.println(" exit " + unode);
			List<LocalDotEdge> edgesTillDestination = ShortestPath.shortestPath(token.getLastPosition(), destination,
					panel);
			System.out.println("  " + edgesTillDestination.size() + " edges to go, to arrive @" + destinationTime);
			//see if we have not reached the destination yet, and whether the first edge belongs to this node
			if (!edgesTillDestination.isEmpty()) {
				LocalDotEdge exitEdge = edgesTillDestination.get(0);
				if (panel.getUnfoldedNode2dotEdgesModel().get(unode) != null
						&& panel.getUnfoldedNode2dotEdgesModel().get(unode).contains(exitEdge)) {
					//compute end time of this node
					double exitEndTime = token.getLastTime() + (destinationTime - token.getLastTime())
							/ (edgesTillDestination.size() * 1.0);
					System.out.println("  end time of exit edge @" + exitEndTime);
					token.addPoint(exitEdge, exitEndTime);
				} else {
					System.out.println("  enter edge does not belong to this node");
				}
			}
		}
	}

	private static void positionXor(TimedTrace trace, UnfoldedNode unode, TimedMove lastMoveBeforeThisTrace,
			TimedMove nextMoveAfterThisTrace, Token token, Tokens tokens, LocalDotNode destination, double destinationTime,
			InductiveVisualMinerPanel panel) {
		//only execute the child that produced the trace
		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);
		Iterator<List<TimedTrace>> itSublog = sublogs.iterator();
		Iterator<Node> itChild = unode.getBlock().getChildren().iterator();
		while (itSublog.hasNext() && itChild.hasNext()) {
			TimedTrace childSublog = itSublog.next().get(0);
			UnfoldedNode child = unode.unfoldChild(itChild.next());
			if (childSublog.size() > 0) {
				positionTimedTrace(childSublog, child, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, token,
						tokens, destination, destinationTime, panel);
			}
		}
	}

	private static void positionParallel(TimedTrace trace, UnfoldedNode unode, TimedMove lastMoveBeforeThisTrace,
			TimedMove nextMoveAfterThisTrace, Token token, Tokens tokens, LocalDotNode destination, double destinationTime,
			LocalDotNode split, double splitTime, InductiveVisualMinerPanel panel) {
		//split the trace
		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);

		//find the last-ending subtrace
		int lastEndingSubtrace = -1;
		{
			TimedMove lastMove = trace.get(trace.size() - 1);
			for (int i = 0; i < sublogs.size(); i++) {
				TimedTrace childTrace = sublogs.get(i).get(0);
				if (childTrace.get(childTrace.size() - 1) == lastMove) {
					lastEndingSubtrace = i;
					break;
				}
			}
		}
		
		//first, process the trace that takes longest
		{
			TimedTrace childTrace = sublogs.get(lastEndingSubtrace).get(0);
			UnfoldedNode child = unode.unfoldChild(unode.getBlock().getChildren().get(lastEndingSubtrace));
			positionTimedTrace(childTrace, child, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, token,
					tokens, destination, destinationTime, panel);
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
				Token childToken = new Token(split, splitTime);
				tokens.add(childToken);
				
				//animate this child on the new token
				positionTimedTrace(childTrace, child, lastMoveBeforeThisTrace, nextMoveAfterThisTrace, childToken,
						tokens, join, joinTime, panel);
			}
		}
	}

	private static void positionLoop(TimedTrace trace, UnfoldedNode unode, TimedMove lastMoveBeforeThisTrace,
			TimedMove nextMoveAfterThisTrace, Token token, Tokens tokens, LocalDotNode destination, double destinationTime,
			InductiveVisualMinerPanel panel) {
		List<List<TimedTrace>> sublogs = getSublogs(unode, trace);

		List<TimedTrace> sublogBody = sublogs.get(0);
		List<TimedTrace> sublogRedo = sublogs.get(1);
		List<TimedTrace> sublogExit = sublogs.get(2);
		UnfoldedNode childBody = unode.unfoldChild(unode.getBlock().getChildren().get(0));
		UnfoldedNode childRedo = unode.unfoldChild(unode.getBlock().getChildren().get(1));
		UnfoldedNode childExit = unode.unfoldChild(unode.getBlock().getChildren().get(2));

		//start with body
		{
			TimedMove moveAfterBody;
			if (sublogRedo.size() > 0) {
				moveAfterBody = sublogRedo.get(0).get(0);
			} else {
				moveAfterBody = sublogExit.get(0).get(0);
			}
			Pair<LocalDotNode, Double> bodyDestination = getNextDestination(trace, trace.indexOf(moveAfterBody),
					destination, destinationTime, panel);
			positionTimedTrace(sublogBody.get(0), childBody, lastMoveBeforeThisTrace, moveAfterBody, token,
					tokens, bodyDestination.getLeft(), bodyDestination.getRight(), panel);
		}
		TimedMove lastMoveOfBody = sublogBody.get(0).get(sublogBody.get(0).size() - 1);

		//then redo + body
		for (int i = 0; i < sublogRedo.size(); i++) {
			TimedTrace traceRedo = sublogRedo.get(i);
			TimedTrace traceBody = sublogBody.get(i + 1);

			//redo
			{
				Pair<LocalDotNode, Double> redoDestination =getNextDestination(trace, trace.indexOf(traceBody.get(0)), destination, destinationTime, panel);
				positionTimedTrace(traceRedo, childRedo, lastMoveOfBody, traceBody.get(traceBody.size() - 1), token,
						tokens, redoDestination.getLeft(), redoDestination.getRight(), panel);
			}

			//body
			{
				TimedMove moveAfterBody;
				if (i == sublogRedo.size() - 1) {
					moveAfterBody = sublogExit.get(0).get(0);
				} else {
					moveAfterBody = sublogRedo.get(i + 1).get(0);
				}
				Pair<LocalDotNode, Double> bodyDestination = getNextDestination(trace, trace.indexOf(moveAfterBody),
						destination, destinationTime, panel);
				positionTimedTrace(traceBody, childBody, traceRedo.get(traceRedo.size() - 1), moveAfterBody, token,
						tokens, bodyDestination.getLeft(), bodyDestination.getRight(), panel);
			}

			lastMoveOfBody = traceBody.get(traceBody.size() - 1);
		}

		//exit with exit
		positionTimedTrace(sublogExit.get(0), childExit, lastMoveOfBody, nextMoveAfterThisTrace, token, tokens, destination,
				destinationTime, panel);
	}

	private static void positionSequence(TimedTrace trace, UnfoldedNode unode, TimedMove lastMoveBeforeThisTrace,
			TimedMove nextMoveAfterThisTrace, Token token, Tokens tokens, LocalDotNode destination, double destinationTime,
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

			TimedMove before;
			if (i == 0) {
				before = lastMoveBeforeThisTrace;
			} else {
				IMTraceG<TimedMove> lastTrace = sublogs.get(i - 1).get(0);
				before = lastTrace.get(lastTrace.size() - 1);
			}

			TimedMove after;
			if (i < sublogs.size() - 1) {
				IMTraceG<TimedMove> nextTrace = sublogs.get(i + 1).get(0);
				after = nextTrace.get(0);
			} else {
				after = nextMoveAfterThisTrace;
			}

			positionTimedTrace(childTrace, child, before, after, token, tokens, childDestination.getLeft(),
					childDestination.getRight(), panel);
		}
	}

	private static List<List<TimedTrace>> getSublogs(UnfoldedNode unode, TimedTrace trace) {

		//make a partition
		List<Set<UnfoldedNode>> partition = new LinkedList<Set<UnfoldedNode>>();
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uchild = unode.unfoldChild(child);
			partition.add(new HashSet<UnfoldedNode>(AlignedLogMetrics.unfoldAllNodes(uchild)));
		}

		//split the log
		return split(trace, partition, unode.getBlock() instanceof XorLoop);
	}

	public static List<List<TimedTrace>> split(TimedTrace trace, List<Set<UnfoldedNode>> partition, boolean loop) {
		List<List<TimedTrace>> result = new ArrayList<List<TimedTrace>>();

		//map activities to sigmas
		HashMap<Set<UnfoldedNode>, List<TimedTrace>> mapSigma2sublog = new HashMap<Set<UnfoldedNode>, List<TimedTrace>>();
		HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma = new HashMap<UnfoldedNode, Set<UnfoldedNode>>();
		for (Set<UnfoldedNode> sigma : partition) {
			List<TimedTrace> sublog = new ArrayList<TimedTrace>();
			result.add(sublog);
			mapSigma2sublog.put(sigma, sublog);
			for (UnfoldedNode activity : sigma) {
				mapActivity2sigma.put(activity, sigma);
			}
		}

		if (!loop) {
			splitParallel(result, trace, partition, mapSigma2sublog, mapActivity2sigma);
		} else {
			splitLoop(result, trace, partition, mapSigma2sublog, mapActivity2sigma);
		}

		return result;
	}

	public static void splitParallel(List<List<TimedTrace>> result, TimedTrace trace,
			List<Set<UnfoldedNode>> partition, HashMap<Set<UnfoldedNode>, List<TimedTrace>> mapSigma2sublog,
			HashMap<UnfoldedNode, Set<UnfoldedNode>> mapActivity2sigma) {

		//add a new trace to every sublog
		HashMap<Set<UnfoldedNode>, TimedTrace> mapSigma2subtrace = new HashMap<Set<UnfoldedNode>, TimedTrace>();
		for (Set<UnfoldedNode> sigma : partition) {
			TimedTrace subtrace = new TimedTrace();
			mapSigma2subtrace.put(sigma, subtrace);
		}

		for (TimedMove move : trace) {
			if (move.unode != null) {
				Set<UnfoldedNode> sigma = mapActivity2sigma.get(move.unode);
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
			if (move.unode != null) {
				if (!lastSigma.contains(move.unode)) {
					mapSigma2sublog.get(lastSigma).add(partialTrace);
					partialTrace = new TimedTrace();
					lastSigma = mapActivity2sigma.get(move.unode);
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
			if (node != null) {
				return Pair.of(node, move.timestamp);
			}
		}
		return Pair.of(destination, destinationTime);
	}

	private static LocalDotNode getDotNodeFromActivity(TimedMove move, InductiveVisualMinerPanel panel) {
		if (!panel.getUnfoldedNode2dotNodes().containsKey(move.unode)) {
			return null;
		}
		for (LocalDotNode node : panel.getUnfoldedNode2dotNodes().get(move.unode)) {
			if (node.type == NodeType.activity) {
				return node;
			}
		}
		return null;
	}
}
