package org.processmining.plugins.inductiveVisualMiner.animation;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation.Input;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation.Position;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.processtree.Node;

/**
 * Rewritten animation class
 * 
 * @author sleemans
 *
 */

public class IvMTrace2dotToken2 {

	/**
	 * 
	 * @param tree
	 * @param trace
	 * @param showDeviations
	 * @param shortestPath
	 * @param info
	 * @param scaler
	 * @return The dotToken of this trace, starting at the source and ending at
	 *         sink. All time stamps are represented in the dotToken.
	 */
	public static DotToken trace2token(IvMEfficientTree tree, IvMTrace trace, boolean showDeviations,
			ShortestPathGraph shortestPath, ProcessTreeVisualisationInfo info, Scaler scaler) {
		Animation.Input input = new Animation.Input(tree, showDeviations, shortestPath, info, scaler);
		DotToken dotToken = new DotToken(info.getSource(), trace.getStartTime(), true);
		Animation.Position endPosition = new Animation.Position(info.getSink(), trace.getEndTime());

		trace2token(input, trace, new TIntArrayList(), endPosition, dotToken);

		//interpolate the missing timestamps from the token
		DotTokenInterpolate.interpolateToken(info, dotToken);

		return dotToken;
	}

	/**
	 * 
	 * @param in
	 * @param moveIndex
	 *            The first move that this dotToken should traverse.
	 * @param inParallelNodes
	 *            The nodes that this dotToken should limit itself to.
	 * @param endPosition
	 * @return A new DotToken that starts in startPosition and ends in
	 *         endPosition.
	 */
	private static void trace2token(Animation.Input in, List<IvMMove> trace, TIntArrayList inParallelNodes,
			Animation.Position endPosition, DotToken dotToken) {

		if (trace.isEmpty()) {
			//add path to final destination
			Animation.moveDotTokenToFinalPosition(in, dotToken, endPosition);
			return;
		}

		//consider the first move
		IvMMove move = trace.get(0);

		//ignored log and model moves are not relevant for the animation
		if (move.isIgnoredLogMove() || move.isIgnoredModelMove()) {
			trace2token(in, removeFirst(trace), inParallelNodes, endPosition, dotToken);
			return;
		}

		//enqueue events are not relevant for the animation
		if (move.getLifeCycleTransition() == PerformanceTransition.enqueue) {
			trace2token(in, removeFirst(trace), inParallelNodes, endPosition, dotToken);
			return;
		}

		//if we're not showing deviations, log moves are not relevant for the animation.
		if (move.isLogMove() && !in.showDeviations) {
			trace2token(in, removeFirst(trace), inParallelNodes, endPosition, dotToken);
			return;
		}

		//see if we are entering a parallel node
		int enteringParallel = entersParallel(in, move, inParallelNodes);
		if (enteringParallel != -1) {

			//find the parallel split and join
			LocalDotNode parallelSplit = Animation.getParallelSplit(enteringParallel, in.info);
			LocalDotNode parallelJoin = Animation.getParallelJoin(enteringParallel, in.info);

			//walk to the parallel split
			Animation.moveDotTokenTo(in, dotToken, parallelSplit);

			//split the trace: for each parallel one a new trace and sub-token
			List<List<IvMMove>> subTraces = new ArrayList<>();
			{
				for (int i = 0; i < in.tree.getNumberOfChildren(enteringParallel); i++) {
					subTraces.add(new ArrayList<IvMMove>());
				}

				//walk over the trace to split it
				Iterator<IvMMove> it = trace.iterator();
				boolean left = false; //denotes whether we already left the parallel part of this node
				while (it.hasNext()) {
					IvMMove childMove = it.next();
					if (left) {
						subTraces.get(0).add(childMove);
					} else {
						if (childMove.isIgnoredLogMove() || childMove.isIgnoredModelMove()) {
							continue;
						}
						
						int child = getChildNumberWith(in, enteringParallel, childMove);
						if (child == -1) {
							//This did not happen on a child of enteringParallel, thus we're leaving it.
							left = true;
							subTraces.get(0).add(childMove);
						} else {
							subTraces.get(child).add(childMove);
						}
					}
				}

				//in case of or, empty subtraces may appear and need to be removed
				if (in.tree.isOr(enteringParallel)) {
					for (Iterator<List<IvMMove>> it1 = subTraces.iterator(); it1.hasNext();) {
						if (it1.next().isEmpty()) {
							it1.remove();
						}
					}
				}
			}

			//recurse on children
			TIntArrayList recursiveInParallelNodes = new TIntArrayList(inParallelNodes);
			recursiveInParallelNodes.add(enteringParallel);
			Position recursiveEndPosition = new Position(parallelJoin, null);

			//recurse on the non-first children
			for (int childNumber = 1; childNumber < subTraces.size(); childNumber++) {
				DotToken childToken = new DotToken(parallelSplit, null, false);
				dotToken.addSubToken(childToken);
				trace2token(in, subTraces.get(childNumber), recursiveInParallelNodes, recursiveEndPosition, childToken);
			}

			//recurse on child 0
			trace2token(in, subTraces.get(0), recursiveInParallelNodes, endPosition, dotToken);
			return;
		}

		if (in.showDeviations && (move.isModelMove())) {
			//this is a model move and we need to visualise it
			LocalDotEdge moveEdge = Animation.getModelMoveEdge(move, in.info);
			//walk to the edge
			Animation.moveDotTokenTo(in, dotToken, moveEdge.getSource());
			//take the edge
			dotToken.addStepOverEdge(moveEdge, null);
		} else if (in.showDeviations && (move.isLogMove())) {
			//this is a log move and we need to visualise it
			LocalDotEdge moveEdge = Animation.getLogMoveEdge(move.getLogMoveUnode(), move.getLogMoveBeforeChild(),
					in.info);
			//walk to the edge
			Animation.moveDotTokenTo(in, dotToken, moveEdge.getSource());
			//take the edge
			dotToken.addStepOverEdge(moveEdge, null);
		} else if (move.isModelSync()) {
			//this is an activity or a tau
			if (in.tree.isTau(move.getTreeNode())) {
				//tau
				LocalDotEdge tauEdge = Animation.getTauEdge(move, in.info);
				Animation.moveDotTokenTo(in, dotToken, tauEdge.getSource());
				dotToken.addStepOverEdge(tauEdge, null);
			} else {
				//activity
				LocalDotNode destination = Animation.getDotNodeFromActivity(move, in.info);

				if (!move.isStart()) {
					Animation.moveDotTokenTo(in, dotToken, destination);
					dotToken.addStepInNode(destination, move.getUserTimestamp(in.scaler));
				}
			}
		}

		trace2token(in, removeFirst(trace), inParallelNodes, endPosition, dotToken);
	}

	/**
	 * @param trace
	 * @return A list with the first element removed. Lists are not decoupled.
	 */
	private static List<IvMMove> removeFirst(List<IvMMove> trace) {
		return trace.subList(1, trace.size());
	}

	/**
	 * Find the child that produced the given move. Assumption: parent is a
	 * parallel node.
	 * 
	 * @param in
	 * @param move
	 * @return
	 */
	private static int getChildNumberWith(Input in, int parent, Move move) {
		if (move.isLogMove()) {
			int node = move.getLogMoveUnode();
			if (parent > node) {
				return -1;
			}
			if (node >= 0 && (in.tree.isConcurrent(node) || in.tree.isInterleaved(node) || in.tree.isOr(node))) {
				return in.tree.getChildNumberWith(parent, move.getLogMoveParallelBranchMappedTo());
			}
		}
		return in.tree.getChildNumberWith(parent, move.getPositionUnode());
	}

	/**
	 * Returns the highest parallel node that is being entered to move, if any
	 * inParallelUnodes parallel nodes are not reported
	 */
	private static int entersParallel(Animation.Input in, IvMMove move, TIntArrayList inParallelNodes) {
		//get the unode
		int node = move.getPositionUnode();

		//walk from the root to the node, and report the first parallel node that is not in inParallelNodes.
		int now = in.tree.getRoot();
		while (node != now) {
			if ((in.tree.isConcurrent(now) || in.tree.isOr(now) || in.tree.isInterleaved(now))
					&& !inParallelNodes.contains(now)) {
				return now;
			}
			now = in.tree.getChildWith(now, node);
		}
		return -1;
	}

	/**
	 * return whether the move happened in unode
	 */
	private static boolean isInNode(Animation.Input in, Move move, int node) {
		List<Node> path1 = new ArrayList<>(in.tree.getUnfoldedNode(move.getPositionUnode()).getPath());
		List<Node> path2 = in.tree.getUnfoldedNode(node).getPath();

		Iterator<Node> it1 = path1.iterator();

		//the path of 2 must be in 1
		for (Node node2 : path2) {
			if (!it1.hasNext()) {
				return false;
			}

			if (!node2.equals(it1.next())) {
				return false;
			}
		}
		return true;
	}
}
