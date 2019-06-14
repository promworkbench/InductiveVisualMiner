package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class PositionLogMoves {
	private THashMap<LogMovePosition, MultiSet<XEventClass>> logMoves = new THashMap<>();

	public void position(IvMModel model, List<IvMMove> trace) {
		/**
		 * First, strip the ignored moves from the trace
		 */
		List<IvMMove> newTrace = new ArrayList<>();
		for (IvMMove move : trace) {
			if (!move.isIgnoredLogMove() && !move.isIgnoredModelMove()) {
				newTrace.add(move);
			}
		}

		if (model.isTree()) {
			positionLogMovesRoot(model.getTree(), model.getTree().getRoot(), model.getTree().getRoot(), newTrace, 1);
		} else {
			positionLogMovesDfg(model.getDfg(), newTrace, 1);
		}
	}

	private void positionLogMovesDfg(DirectlyFollowsModel dfg, List<IvMMove> trace, long cardinality) {
		if (trace.isEmpty()) {
			return;
		}

		int previousActivity = -1;
		int nextActivity = findNextActivityAfter(trace, previousActivity);
		boolean previousActivityComplete = false;

		for (int i = 0; i < trace.size(); i++) {
			Move move = trace.get(i);
			if (move.isLogMove()) {

				if (previousActivity < 0 && nextActivity < 0) {
					/*
					 * If the previous and the next activity are non-existent,
					 * then the model must have been empty.
					 */
					addLogMove(move, -1, -1, move.getActivityEventClass(), cardinality);
				} else if (previousActivity == nextActivity && previousActivityComplete) {
					/*
					 * If the previous event is a completion event AND the next
					 * event is of the same activity, then we are in between two
					 * executions of the same activity. That's a special case
					 * for DFGs.
					 */
					addLogMove(move, previousActivity, -2, move.getActivityEventClass(), cardinality);
				} else {
					addLogMove(move, previousActivity, nextActivity, move.getActivityEventClass(), cardinality);
				}
			}

			if ((move.isComplete() || move.isStart()) && move.isModelSync()) {
				previousActivity = nextActivity;
				nextActivity = findNextActivityAfter(trace, i);
				previousActivityComplete = move.isComplete();
			}
		}
	}

	private int findNextActivityAfter(List<IvMMove> trace, int position) {
		for (int i = position + 1; i < trace.size(); i++) {
			Move move = trace.get(i);
			if (move.isModelSync()) {
				return move.getTreeNode();
			}
		}
		return -1;
	}

	/**
	 * Position log moves without assuming there are no leading or trailing log
	 * moves.
	 * 
	 * @param root
	 * @param continueOn
	 * @param trace
	 * @param cardinality
	 */
	private void positionLogMovesRoot(IvMEfficientTree tree, int root, int continueOn, List<IvMMove> trace,
			long cardinality) {
		if (trace.isEmpty()) {
			return;
		}
		//remove the leading and trailing log moves and position them on the root
		int start = 0;
		while (trace.get(start).isLogMove() || trace.get(start).isIgnoredModelMove()) {
			start++;
		}
		int end = trace.size() - 1;
		while (trace.get(end).isLogMove() || trace.get(end).isIgnoredModelMove()) {
			end--;
		}
		List<IvMMove> subtrace = trace.subList(start, end + 1);

		//position the leading log moves
		for (Move logMove : trace.subList(0, start)) {
			if (!logMove.isIgnoredLogMove() && !logMove.isTauStart() && !trace.get(start).isIgnoredModelMove()) {
				addLogMove(logMove, -1, root, logMove.getActivityEventClass(), cardinality);
			}
		}

		//position the trailing log moves
		for (Move logMove : trace.subList(end + 1, trace.size())) {
			if (!logMove.isIgnoredLogMove() && !logMove.isTauStart() && !trace.get(start).isIgnoredModelMove()) {
				addLogMove(logMove, root, -1, logMove.getActivityEventClass(), cardinality);
			}
		}

		//recurse on the subtrace
		positionLogMovesTree(tree, continueOn, subtrace, cardinality);
	}

	/*
	 * Invariant: the first and the last move of the trace are not log moves.
	 */
	private void positionLogMovesTree(IvMEfficientTree tree, int node, List<IvMMove> trace, long cardinality) {
		//debug(" process trace " + trace + " on " + unode);

		assert (trace.get(0).isModelSync() && !trace.get(0).isIgnoredLogMove() && !trace.get(0).isIgnoredModelMove());
		int l = trace.size() - 1;
		assert (trace.get(l).isModelSync() && !trace.get(l).isIgnoredLogMove() && !trace.get(l).isIgnoredModelMove());

		if (tree.isActivity(node)) {
			//unode is an activity
			for (Move move : trace) {
				if (move.isLogMove()) {
					//put this log move on this leaf
					addLogMove(move, node, node, move.getActivityEventClass(), cardinality);
				}
			}
		} else if (tree.isTau(node)) {
			//unode is a tau
			//by the invariant, the trace contains no log moves
		} else if (tree.isXor(node)) {
			//an xor cannot have log moves, just recurse on the child that produced this trace

			//by the invariant, the trace does not start with a log move
			//find the child that this grandchild belongs to
			int child = EfficientTreeUtils.getChildWith(tree, node, trace.get(0).getTreeNode());

			positionLogMovesTree(tree, child, trace, cardinality);

		} else if (tree.isSequence(node)) {
			splitSequence(tree, node, trace, cardinality);
		} else if (tree.isLoop(node)) {
			splitLoop(tree, node, trace, cardinality);
		} else if (tree.isConcurrent(node) || tree.isOr(node) || tree.isInterleaved(node)) {

			//set up subtraces for children
			TIntObjectMap<List<IvMMove>> subTraces = new TIntObjectHashMap<>(10, 0.5F, -1);
			for (int child : tree.getChildren(node)) {
				subTraces.put(child, new ArrayList<IvMMove>());
			}

			//by the invariant, the first move is not a log move
			int lastSeenChild = -1;

			//walk through the trace to split it
			for (IvMMove move : trace) {
				if (move.isLogMove()) {
					//put this log move on the last seen child
					//we cannot know a better place to put it
					subTraces.get(lastSeenChild).add(move);
				} else {
					//put this move in the subtrace of the child it belongs to
					int child = EfficientTreeUtils.getChildWith(tree, node, move.getTreeNode());
					subTraces.get(child).add(move);
					lastSeenChild = child;
				}
			}

			//invariant might be invalid on sub traces; position leading and trailing log moves
			for (int child : tree.getChildren(node)) {
				List<IvMMove> subTrace = subTraces.get(child);
				positionLogMovesRoot(tree, node, child, subTrace, cardinality);
			}
		}
	}

	private void splitSequence(IvMEfficientTree tree, int unode, List<IvMMove> trace, long cardinality) {
		//by the invariant, the first move is not a log move
		int lastSeenChild = EfficientTreeUtils.getChildWith(tree, unode, trace.get(0).getTreeNode());
		List<IvMMove> logMoves = new ArrayList<IvMMove>();
		List<IvMMove> subTrace = new ArrayList<IvMMove>();

		//walk through the trace to split it
		for (IvMMove move : trace) {
			if (move.isIgnoredLogMove() || move.isIgnoredModelMove()) {
				//skip
			} else if (move.isLogMove()) {
				logMoves.add(move);
			} else {
				int child = EfficientTreeUtils.getChildWith(tree, unode, move.getTreeNode());
				if (child == lastSeenChild) {
					//we are not leaving the previous child with this move
					//the log moves we have seen now are internal to the subtrace; add them
					subTrace.addAll(logMoves);
					subTrace.add(move);
					logMoves.clear();
				} else {
					//we are leaving the last child with this move

					//recurse on the subtrace up till now
					positionLogMovesTree(tree, lastSeenChild, subTrace, cardinality);

					//the log moves we have seen now are external to both subtraces; position them on this unode
					for (IvMMove logMove : logMoves) {
						addLogMove(logMove, unode, child, logMove.getActivityEventClass(), cardinality);
					}

					subTrace.clear();
					logMoves.clear();
					subTrace.add(move);
					lastSeenChild = child;
				}
			}
		}

		//recurse on subtrace
		positionLogMovesTree(tree, lastSeenChild, subTrace, cardinality);

		//the log moves we have seen now are external to both subtraces; position them on this unode
		for (Move logMove : logMoves) {
			addLogMove(logMove, unode, -1, logMove.getActivityEventClass(), cardinality);
		}
	}

	private void splitLoop(IvMEfficientTree tree, int unode, List<IvMMove> trace, long cardinality) {
		//by the invariant, the first move is not a log move
		int lastSeenChild = EfficientTreeUtils.getChildWith(tree, unode, trace.get(0).getTreeNode());
		List<IvMMove> logMoves = new ArrayList<IvMMove>();
		List<IvMMove> subTrace = new ArrayList<IvMMove>();

		int redo = tree.getChild(unode, 1);
		int exit = tree.getChild(unode, 2);

		//walk through the trace to split it
		for (IvMMove move : trace) {
			if (move.isIgnoredLogMove() || move.isIgnoredModelMove()) {
				//skip
			} else if (move.isLogMove()) {
				logMoves.add(move);
			} else {
				int child = EfficientTreeUtils.getChildWith(tree, unode, move.getTreeNode());
				if (child == lastSeenChild) {
					//we are not leaving the previous child with this move
					//the log moves we have seen now are internal to the subtrace; add them
					subTrace.addAll(logMoves);
					subTrace.add(move);
					logMoves.clear();
				} else {
					//we are leaving the last child with this move

					//recurse on the subtrace up till now
					positionLogMovesTree(tree, lastSeenChild, subTrace, cardinality);

					//the log moves we have seen now are external to both subtraces; position them on this unode
					for (IvMMove logMove : logMoves) {
						if (child == exit) {
							/*
							 * Exception: before the exit is in our
							 * visualisation the same as before the redo. Design
							 * decision: merge them here.
							 */
							addLogMove(logMove, unode, redo, logMove.getActivityEventClass(), cardinality);
						} else {
							addLogMove(logMove, unode, child, logMove.getActivityEventClass(), cardinality);
						}
					}

					subTrace.clear();
					logMoves.clear();
					subTrace.add(move);
					lastSeenChild = child;
				}
			}
		}

		//recurse on subtrace
		positionLogMovesTree(tree, lastSeenChild, subTrace, cardinality);

		//the log moves we have seen now are external to both subtraces; position them on the end of this unode
		for (Move logMove : logMoves) {
			addLogMove(logMove, unode, -1, logMove.getActivityEventClass(), cardinality);
		}
	}

	private void addLogMove(Move move, int unode, int beforeChild, XEventClass e, long cardinality) {
		LogMovePosition logMovePosition = LogMovePosition.beforeChild(unode, beforeChild);
		move.setLogMovePosition(logMovePosition);
		if (!logMoves.containsKey(logMovePosition)) {
			logMoves.put(logMovePosition, new MultiSet<XEventClass>());
		}
		logMoves.get(logMovePosition).add(e, cardinality);
	}

	public THashMap<LogMovePosition, MultiSet<XEventClass>> getLogMoves() {
		return logMoves;
	}
}
