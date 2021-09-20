package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.LogSplitter;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.processtree.Node;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;

public class IvMTrace2DotToken {

	public static DotToken trace2token(IvMEfficientTree tree, IvMTrace trace, boolean showDeviations,
			ShortestPathGraph shortestPath, ProcessTreeVisualisationInfo info, Scaler scaler) {

		//		debug("", 0);

		//copy the trace; remove ignored log moves and everything not start or complete
		List<IvMMove> copyTrace = new ArrayList<IvMMove>();
		for (IvMMove t : trace) {
			if (!t.isIgnoredLogMove() && !t.isIgnoredModelMove()
					&& (t.getLifeCycleTransition() == PerformanceTransition.start
							|| t.getLifeCycleTransition() == PerformanceTransition.complete)) {
				copyTrace.add(t);
			}
		}

		DotToken token = trace2dotToken(tree, copyTrace, Pair.of(info.getSource(), trace.getStartTime()),
				Pair.of(info.getSink(), trace.getEndTime()), new TIntArrayList(), showDeviations, shortestPath, info, 0,
				scaler);

		//interpolate the missing timestamps from the token
		DotTokenInterpolate.interpolateToken(info, token);

		//		debug(token, 0);

		return token;
	}

	public static DotToken trace2dotToken(IvMEfficientTree tree, List<IvMMove> trace,
			Pair<LocalDotNode, Double> startPosition, Pair<LocalDotNode, Double> endPosition,
			TIntArrayList inParallelUnodes, boolean showDeviations, ShortestPathGraph shortestPath,
			ProcessTreeVisualisationInfo info, int indent, Scaler scaler) {

		//		debug("translate trace " + trace, indent);

		DotToken dotToken = new DotToken(startPosition.getLeft(), startPosition.getRight(), inParallelUnodes.isEmpty());
		TIntArrayList localInParallelUnodes = new TIntArrayList(inParallelUnodes);

		//walk through the trace
		for (int i = 0; i < trace.size(); i++) {
			IvMMove move = trace.get(i);

			if (move.isLogMove()) {
				//				debug(" consider move " + move + " (" + move.getLogMoveUnode() + " before "
				//						+ move.getLogMoveBeforeChild() + ")", indent);
			} else {
				//				debug(" consider move " + move, indent);
			}

			//see if we are leaving a parallel subtrace
			leaveParallelSubtrace(tree, shortestPath, info, indent, dotToken, localInParallelUnodes, move);

			int enteringParallel = entersParallel(tree, move, localInParallelUnodes);
			if (enteringParallel != -1) {
				//we are entering a parallel subtree

				enterParallelSubTrace(tree, trace, showDeviations, shortestPath, info, indent, scaler, dotToken,
						localInParallelUnodes, i, move, enteringParallel);

				//sanity check
				if ((tree.isConcurrent(enteringParallel) || tree.isInterleaved(enteringParallel))
						&& dotToken.getLastSubTokens().size() == 0) {
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
					List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), nextDestination,
							true);
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
			if (move.getTreeNode() != -1 && tree.isTau(move.getTreeNode())) {
				//tau, by definition synchronous

				//move from the last known position to the start of the tau edge,
				//then take the move tau itself
				LocalDotEdge tauEdge = Animation.getTauEdge(move, info);

				List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), tauEdge.getSource(),
						false);
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
				List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), nextDestination,
						true);
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

				List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), moveEdge.getSource(),
						false);
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
				List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), moveEdge.getSource(),
						false);
				for (LocalDotEdge edge : path) {
					dotToken.addStepOverEdge(edge, null);
				}

				//add the move edge
				dotToken.addStepOverEdge(moveEdge, move.getUserTimestamp(scaler));
			}
		}

		//exit remaining parallel unodes
		for (int parallel : localInParallelUnodes.toArray()) {
			if (!inParallelUnodes.contains(parallel)) {
				exitParallel(parallel, dotToken, shortestPath, info, indent);
			}
		}

		//add path to final destination
		List<LocalDotEdge> path = shortestPath.getShortestPath(dotToken.getLastPosition(), endPosition.getLeft(),
				false);
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

	private static void enterParallelSubTrace(IvMEfficientTree tree, List<IvMMove> trace, boolean showDeviations,
			ShortestPathGraph shortestPath, ProcessTreeVisualisationInfo info, int indent, Scaler scaler,
			DotToken token, TIntArrayList localInParallelUnodes, int offset, IvMMove move, int enteringParallel) {
		//find out where we are exiting it again
		int exitsAt = findParallelExit(tree, trace, enteringParallel, offset);

		//		debug("  entering parallel " + enteringParallel + " with move " + move, indent);
		//		debug("  will exit at " + exitsAt, indent);

		//extract parallel subtrace we're entering
		List<IvMMove> parallelSubtrace = trace.subList(offset, exitsAt + 1);
		//		debug("  parallel subtrace " + parallelSubtrace, indent);

		//spit the subtrace into sublogs
		List<List<IvMMove>> subsubTraces = splitTrace(tree, enteringParallel, parallelSubtrace);
		//		debug("  subsub traces " + subsubTraces, indent);

		//move the token up to the parallel split
		LocalDotNode parallelSplit = Animation.getParallelSplit(enteringParallel, info);
		{
			List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), parallelSplit, false);
			for (LocalDotEdge edge : path) {
				token.addStepOverEdge(edge, null);
			}
		}

		//		debug(" trace after parallel reduction " + trace, indent);

		//denote that we have entered this parallel unode (prevents infinite loops)
		localInParallelUnodes.add(enteringParallel);

		//recurse on other subsubTraces
		LocalDotNode parallelJoin = Animation.getParallelJoin(enteringParallel, info);
		for (int j = 1; j < subsubTraces.size(); j++) {
			List<IvMMove> subsubTrace = subsubTraces.get(j);
			assert (!subsubTrace.isEmpty());
			TIntArrayList childInParallelUnodes = new TIntArrayList(localInParallelUnodes);
			DotToken childToken = trace2dotToken(tree, subsubTrace, Pair.of(parallelSplit, (Double) null),
					Pair.of(parallelJoin, (Double) null), childInParallelUnodes, showDeviations, shortestPath, info,
					indent + 1, scaler);

			token.addSubToken(childToken);
		}

		//alter the trace:
		//remove the parallel subtrace, and replace with an arbitrary subsubtrace
		{
			for (int j = offset; j < exitsAt + 1; j++) {
				trace.remove(offset);
			}
			List<IvMMove> subsubTrace = subsubTraces.get(0);
			for (int j = 0; j < subsubTrace.size(); j++) {
				trace.add(offset + j, subsubTrace.get(j));
			}
		}
	}

	private static void leaveParallelSubtrace(IvMEfficientTree tree, ShortestPathGraph shortestPath,
			ProcessTreeVisualisationInfo info, int indent, DotToken token, TIntArrayList localInParallelUnodes,
			IvMMove move) {
		for (int j = localInParallelUnodes.size() - 1; j >= 0; j--) {
			int parallel = localInParallelUnodes.get(j);
			if (!isInNode(tree, move, parallel)) {
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
	private static void exitParallel(int parallel, DotToken token, ShortestPathGraph shortestPath,
			ProcessTreeVisualisationInfo info, int indent) {
		//move the token to the parallel join
		LocalDotNode parallelJoin = Animation.getParallelJoin(parallel, info);
		List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), parallelJoin, false);
		for (LocalDotEdge edge : path) {
			token.addStepOverEdge(edge, null);
		}

		//		debug("  leaving parallel " + parallel, indent);
	}

	/**
	 * returns the parallel unode that is being entered to move, if any
	 * inParallelUnodes parallel nodes are not reported
	 */
	private static int entersParallel(IvMEfficientTree tree, IvMMove move, TIntArrayList inParallelUnodes) {

		if (move == null) {
			//there's nothing being entered here
			return -1;
		}

		//get the root of the tree
		int now = tree.getRoot();

		//get the unode
		int unode = move.getPositionUnode();

		while (unode != now) {
			if ((tree.isConcurrent(now) || tree.isOr(now) || tree.isInterleaved(now))
					&& !inParallelUnodes.contains(now)) {
				return now;
			}
			now = EfficientTreeUtils.getChildWith(tree, now, unode);
		}
		return -1;
	}

	/**
	 * finds the position of the last move in trace (from offset) that is still
	 * in unode
	 */
	private static int findParallelExit(IvMEfficientTree tree, List<IvMMove> trace, int node, int offset) {
		for (int i = offset + 1; i < trace.size(); i++) {
			Move move = trace.get(i);
			if (!isInNode(tree, move, node)) {
				return i - 1;
			}
		}
		return trace.size() - 1;
	}

	/*
	 * return whether the move happened in unode
	 */
	private static boolean isInNode(IvMEfficientTree tree, Move move, int unode) {
		List<Node> path1 = new ArrayList<>(tree.getUnfoldedNode(move.getPositionUnode()).getPath());
		List<Node> path2 = tree.getUnfoldedNode(unode).getPath();

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
	public static List<List<IvMMove>> splitTrace(IvMEfficientTree tree, int unode, List<IvMMove> trace) {

		LogSplitter.SigmaMaps<IvMMove> maps = LogSplitter.makeSigmaMaps(tree, unode);

		//split the trace
		for (IvMMove move : trace) {
			TIntSet sigma = maps.mapUnode2sigma.get(move.getPositionUnode());
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

		//filter empty subtraces (for Or)
		if (tree.isOr(unode)) {
			for (Iterator<List<IvMMove>> it = maps.sublogs.iterator(); it.hasNext();) {
				if (it.next().isEmpty()) {
					it.remove();
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
