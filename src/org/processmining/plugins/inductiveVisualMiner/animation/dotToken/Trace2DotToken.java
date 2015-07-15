package org.processmining.plugins.inductiveVisualMiner.animation.dotToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.LogSplitter;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class Trace2DotToken {

	public static DotToken trace2token(IvMTrace trace, boolean showDeviations, ShortestPathGraph shortestPath,
			AlignedLogVisualisationInfo info, Scaler scaler) {

		debug("", 0);

		//copy the trace; remove ignored log moves and everything not start or complete
		List<IvMMove> copyTrace = new ArrayList<IvMMove>();
		for (IvMMove t : trace) {
			if (!t.isIgnoredLogMove()
					&& (t.getLifeCycleTransition() == PerformanceTransition.start || t.getLifeCycleTransition() == PerformanceTransition.complete)) {
				copyTrace.add(t);
			}
		}

		DotToken token = trace2dotToken(copyTrace, Pair.of(info.getSource(), trace.getStartTime()),
				Pair.of(info.getSink(), trace.getEndTime()), new ArrayList<UnfoldedNode>(), showDeviations,
				shortestPath, info, 0, scaler);

		//interpolate the missing timestamps from the token
		try {
			InterpolateDotToken.interpolateToken(token);
		} catch (Exception e) {
			throw e;
		}

		debug(token, 0);

		return token;
	}

	public static DotToken trace2dotToken(List<IvMMove> trace, Pair<LocalDotNode, Double> startPosition,
			Pair<LocalDotNode, Double> endPosition, List<UnfoldedNode> inParallelUnodes, boolean showDeviations,
			ShortestPathGraph shortestPath, AlignedLogVisualisationInfo info, int indent, Scaler scaler) {

		debug("translate trace " + trace, indent);

		DotToken dotToken = new DotToken(startPosition.getLeft(), startPosition.getRight(), inParallelUnodes.isEmpty());
		List<UnfoldedNode> localInParallelUnodes = new ArrayList<>(inParallelUnodes);

		//walk through the trace
		for (int i = 0; i < trace.size(); i++) {
			IvMMove move = trace.get(i);

			if (move.isLogMove()) {
				debug(" consider move " + move + " (" + move.getLogMoveUnode() + " before "
						+ move.getLogMoveBeforeChild() + ")", indent);
			} else {
				debug(" consider move " + move, indent);
			}

			//see if we are leaving a parallel subtrace
			leaveParallelSubtrace(shortestPath, info, indent, dotToken, localInParallelUnodes, move);

			UnfoldedNode enteringParallel = entersParallel(move, localInParallelUnodes);
			if (enteringParallel != null) {
				//we are entering a parallel subtree

				enterParallelSubTrace(trace, showDeviations, shortestPath, info, indent, scaler, dotToken,
						localInParallelUnodes, i, move, enteringParallel);

				//sanity check
				if (dotToken.getLastSubTokens().size() == 0) {
					debug(dotToken, 0);
					debug("no subtokens", 0);
					throw new RuntimeException("no subtokens created");
				}

				//continue, and in the next iteration re-process the same move
				i--;
				continue;
			}

			/*
			 * case: this is a start move according to the model. At this point,
			 * we don't know whether this will be a synchronous move or a model.
			 * Therefore, look ahead in the trace to find out.
			 */
			if (move.isStart() && move.isModelSync()) {
				//search for the corresponding complete
				int complete = findCompleteIndex(i, trace);

				if (trace.get(complete).isSyncMove()) {
					//the corresponding complete is a synchronous move; treat this start as a synchronous move

					//move to the activity; arrive at the given timestamp
					LocalDotNode nextDestination = Animation.getDotNodeFromActivity(move, info);
					List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), nextDestination);
					if (!path.isEmpty()) {
						for (int j = 0; j < path.size() - 1; j++) {
							dotToken.addStepOverEdge(path.get(j), null);
						}
						dotToken.addStepOverEdge(path.get(path.size() - 1), move.getUserTimestamp(scaler));
					}

					continue;
				} else {
					//the corresponding complete is a model move; treat this start as the beginning of a model move
					//hence, do nothing
					continue;
				}
			}

			/*
			 * Case: tau. By definition synchronous.
			 */
			if (move.getUnode() != null && move.getUnode().getNode() instanceof Automatic) {
				//tau, by definition synchronous

				//move from the last known position to the start of the tau edge,
				//then take the move tau itself
				LocalDotEdge tauEdge = Animation.getTauEdge(move, info);

				List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), tauEdge.getSource());
				for (LocalDotEdge edge : path) {
					dotToken.addStepOverEdge(edge, null);
				}

				dotToken.addStepOverEdge(tauEdge, null);
				continue;
			}

			if (move.isSyncMove() || (move.isModelMove() && !showDeviations)) {
				//synchronous move or model move without deviations showing
				LocalDotNode nextDestination = Animation.getDotNodeFromActivity(move, info);

				//first: walk to the node

				//move from the last known position to the new position
				//the last edge gets a timestamp
				List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), nextDestination);
				if (!path.isEmpty()) {
					for (int j = 0; j < path.size() - 1; j++) {
						dotToken.addStepOverEdge(path.get(j), null);
					}
					dotToken.addStepOverEdge(path.get(path.size() - 1), move.getUserTimestamp(scaler));
				}

				//second: walk over the node
				dotToken.addStepInNode(nextDestination, move.getUserTimestamp(scaler));

			} else if (move.isModelMove() && showDeviations) {
				//model move, showing deviations

				//move from the last known position to the start of the move edge,
				//then take the move edge itself
				LocalDotEdge moveEdge = Animation.getModelMoveEdge(move, info);

				List<LocalDotEdge> path = shortestPath
						.getShortestPath(dotToken.getLastPosition(), moveEdge.getSource());
				for (LocalDotEdge edge : path) {
					dotToken.addStepOverEdge(edge, null);
				}

				dotToken.addStepOverEdge(moveEdge, null);
			} else if (move.isLogMove() && showDeviations) {

				//log move (should be filtered out if not showing deviations)
				LocalDotEdge moveEdge = Animation.getLogMoveEdge(move.getLogMoveUnode(), move.getLogMoveBeforeChild(),
						info);

				//move from the last known position to the start of the move edge,
				//then take the move edge itself
				List<LocalDotEdge> path = shortestPath
						.getShortestPath(dotToken.getLastPosition(), moveEdge.getSource());
				for (LocalDotEdge edge : path) {
					dotToken.addStepOverEdge(edge, null);
				}

				//add the move edge
				dotToken.addStepOverEdge(moveEdge, move.getUserTimestamp(scaler));
			}
		}

		//exit remaining parallel unodes
		for (UnfoldedNode parallel : localInParallelUnodes) {
			if (!inParallelUnodes.contains(parallel)) {
				exitParallel(parallel, dotToken, shortestPath, info, indent);
			}
		}

		//add path to final destination
		List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), endPosition.getLeft());
		for (int j = 0; j < path.size() - 1; j++) {
			dotToken.addStepOverEdge(path.get(j), null);
		}
		if (path.size() != 0) {
			dotToken.addStepOverEdge(path.get(path.size() - 1), endPosition.getRight());
		} else if (endPosition.getRight() != null) {
			//the trace has already ended, fill in the end time
			dotToken.setTimestampOfPoint(dotToken.size() - 1, endPosition.getRight());
		}

		return dotToken;
	}

	private static void enterParallelSubTrace(List<IvMMove> trace, boolean showDeviations,
			ShortestPathGraph shortestPath, AlignedLogVisualisationInfo info, int indent, Scaler scaler,
			DotToken token, List<UnfoldedNode> localInParallelUnodes, int i, IvMMove move, UnfoldedNode enteringParallel) {
		//find out where we are exiting it again
		int exitsAt = findParallelExit(trace, enteringParallel, i);

		debug("  entering parallel " + enteringParallel + " with move " + move, indent);
		debug("  will exit at " + exitsAt, indent);

		//extract parallel subtrace we're entering
		List<IvMMove> parallelSubtrace = trace.subList(i, exitsAt + 1);
		debug("  parallel subtrace " + parallelSubtrace, indent);

		//spit the subtrace into sublogs
		List<List<IvMMove>> subsubTraces = splitTrace(enteringParallel, parallelSubtrace);
		debug("  subsub traces " + subsubTraces, indent);

		//move the token up to the parallel split
		LocalDotNode parallelSplit = Animation.getParallelSplit(enteringParallel, info);
		{
			List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), parallelSplit);
			for (LocalDotEdge edge : path) {
				token.addStepOverEdge(edge, null);
			}
		}

		//alter the trace:
		//remove the parallel subtrace, and replace with an arbitrary subsubtrace
		{
			for (int j = i; j < exitsAt + 1; j++) {
				trace.remove(i);
			}
			List<IvMMove> subsubTrace = subsubTraces.get(0);
			for (int j = 0; j < subsubTrace.size(); j++) {
				trace.add(i + j, subsubTrace.get(j));
			}
		}

		debug(" trace after parallel reduction " + trace, indent);

		//denote that we have entered this parallel unode (prevents infinite loops)
		localInParallelUnodes.add(enteringParallel);

		//recurse on other subsubTraces
		LocalDotNode parallelJoin = Animation.getParallelJoin(enteringParallel, info);
		for (int j = 1; j < subsubTraces.size(); j++) {
			List<IvMMove> subsubTrace = subsubTraces.get(j);
			List<UnfoldedNode> childInParallelUnodes = new ArrayList<>(localInParallelUnodes);
			DotToken childToken = trace2dotToken(subsubTrace, Pair.of(parallelSplit, (Double) null),
					Pair.of(parallelJoin, (Double) null), childInParallelUnodes, showDeviations, shortestPath, info,
					indent + 1, scaler);

			token.addSubToken(childToken);
		}
	}

	private static void leaveParallelSubtrace(ShortestPathGraph shortestPath, AlignedLogVisualisationInfo info,
			int indent, DotToken token, List<UnfoldedNode> localInParallelUnodes, IvMMove move) {
		for (int j = localInParallelUnodes.size() - 1; j >= 0; j--) {
			UnfoldedNode parallel = localInParallelUnodes.get(j);
			if (!isInNode(move, parallel)) {
				//we are not in this parallel unode anymore, remove from the list

				localInParallelUnodes.remove(j);

				exitParallel(parallel, token, shortestPath, info, indent);
			} else {
				break;
			}
		}
	}

	/*
	 * leave a parallel unode
	 */
	private static void exitParallel(UnfoldedNode parallel, DotToken token, ShortestPathGraph shortestPath,
			AlignedLogVisualisationInfo info, int indent) {
		//move the token to the parallel join
		LocalDotNode parallelJoin = Animation.getParallelJoin(parallel, info);
		List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), parallelJoin);
		for (LocalDotEdge edge : path) {
			token.addStepOverEdge(edge, null);
		}

		debug("  leaving parallel " + parallel, indent);
	}

	/*
	 * returns the parallel unode that is being entered to move, if any
	 * inParallelUnodes parallel nodes are not reported
	 */
	private static UnfoldedNode entersParallel(IvMMove move, List<UnfoldedNode> inParallelUnodes) {

		if (move == null) {
			//there's nothing being entered here
			return null;
		}

		//get the unode
		UnfoldedNode unode = move.getPositionUnode();

		//get the root of the tree
		UnfoldedNode root = new UnfoldedNode(unode.getPath().get(0));

		if (root.getNode() instanceof And && !inParallelUnodes.contains(root)) {
			return root;
		}

		for (int i = 1; i < unode.getPath().size(); i++) {
			root = root.unfoldChild(unode.getPath().get(i));
			if (root.getNode() instanceof And && !inParallelUnodes.contains(root)) {
				return root;
			}
		}
		return null;
	}

	/*
	 * finds the position of the last move in trace (from offset) that is still
	 * in unode
	 */
	private static int findParallelExit(List<IvMMove> trace, UnfoldedNode unode, int offset) {
		for (int i = offset + 1; i < trace.size(); i++) {
			Move move = trace.get(i);
			if (!isInNode(move, unode)) {
				return i - 1;
			}
		}
		return trace.size() - 1;
	}

	/*
	 * return whether the move happened in unode
	 */
	private static boolean isInNode(Move move, UnfoldedNode unode) {
		List<Node> path1 = new ArrayList<>(move.getPositionUnode().getPath());
		List<Node> path2 = unode.getPath();

		Iterator<Node> it1 = path1.iterator();

		//the path of 2 must be in 1
		for (Node node : path2) {
			if (!it1.hasNext()) {
				return false;
			}

			if (!node.equals(it1.next())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * split a trace according to a node
	 */
	public static List<List<IvMMove>> splitTrace(UnfoldedNode unode, List<IvMMove> trace) {

		LogSplitter.SigmaMaps<IvMMove> maps = LogSplitter.makeSigmaMaps(unode);

		//split the trace
		for (IvMMove move : trace) {
			Set<UnfoldedNode> sigma = maps.mapUnode2sigma.get(move.getPositionUnode());
			if (sigma != null) {
				maps.mapSigma2subtrace.get(sigma).add(move);
			} else {
				//this is a log move that was mapped on unode itself and not on one of its children
				//put it in the mapped sigma
				sigma = maps.mapUnode2sigma.get(move.getLogMoveParallelBranchMappedTo());
				if (sigma != null) {
					maps.mapSigma2subtrace.get(sigma).add(move);
				} else {
					//put it on the first branch
					maps.sublogs.get(0).add(move);
				}
			}
		}

		return maps.sublogs;
	}

	/**
	 * 
	 * @param i
	 * @param trace
	 * @return the index of the first complete after @i in @trace.
	 */
	public static int findCompleteIndex(int i, List<IvMMove> trace) {
		//walk over log moves until the complete is encountered
		//by construction, only log moves will occur until the complete that belongs to this start
		int j = i + 1;
		while (!trace.get(j).isComplete() || trace.get(j).isLogMove()) {
			j++;
		}
		return j;
	}

	private static void debug(Object s, int indent) {
		//		String sIndent = new String(new char[indent]).replace("\0", "   ");
		//		System.out.print(sIndent);
		//		System.out.println(s);
	}
}
