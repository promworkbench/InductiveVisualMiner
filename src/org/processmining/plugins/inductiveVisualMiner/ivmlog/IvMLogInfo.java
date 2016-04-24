package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

@SuppressWarnings("deprecation")
public class IvMLogInfo {

	private final MultiSet<UnfoldedNode> modelMoves;
	private final MultiSet<String> unlabeledLogMoves;

	//for each position in the tree, the xeventclasses that were moved
	//position:
	// (node1, node1) on node1
	// (node1, node2) on node1, before node2 (only in case of node1 sequence or loop)
	// (root, null) at end of trace
	// (null, root) at start of trace
	private final Map<LogMovePosition, MultiSet<XEventClass>> logMoves;
	private final MultiSet<Pair<UnfoldedNode, UnfoldedNode>> dfg;
	private final MultiSet<Move> activities;

	public IvMLogInfo() {
		modelMoves = new MultiSet<UnfoldedNode>();
		logMoves = new HashMap<LogMovePosition, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		dfg = new MultiSet<Pair<UnfoldedNode, UnfoldedNode>>();
		activities = new MultiSet<>();
	}

	public IvMLogInfo(IvMLog log) {
		modelMoves = new MultiSet<UnfoldedNode>();
		logMoves = new THashMap<LogMovePosition, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		dfg = new MultiSet<Pair<UnfoldedNode, UnfoldedNode>>();
		activities = new MultiSet<>();
		UnfoldedNode root = null;
		for (IvMTrace trace : log) {
			UnfoldedNode lastDfgUnode = null;
			UnfoldedNode lastUnode = null;
			boolean traceContainsLogMove = false;
			for (int i = 0; i < trace.size(); i++) {
				Move move = trace.get(i);
				activities.add(move);
				if (move.getType() == Type.modelMove) {
					//add model move to list of model moves
					modelMoves.add(move.getUnode());
				} else if (move.isLogMove()) {
					traceContainsLogMove = true;
					move.setLogMoveParallelBranchMappedTo(lastUnode);
					unlabeledLogMoves.add(move.getActivityEventClass().toString());
				}

				if (root == null && move.isModelSync()) {
					root = new UnfoldedNode(move.getUnode().getPath().get(0));
				}

				if (move.isModelSync() && !move.isIgnoredModelMove()) {
					lastUnode = move.getUnode();
				}

				if (move.getUnode() != null && move.getUnode().getNode() instanceof Manual) {
					dfg.add(new Pair<UnfoldedNode, UnfoldedNode>(lastDfgUnode, move.getUnode()));
					lastDfgUnode = move.getUnode();
				}
			}
			if (lastDfgUnode != null) {
				dfg.add(new Pair<UnfoldedNode, UnfoldedNode>(lastDfgUnode, null));
			}

			//position the log moves
			if (traceContainsLogMove) {
				positionLogMovesRoot(root, root, trace, 1);
			}
		}
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
	public void positionLogMovesRoot(UnfoldedNode root, UnfoldedNode continueOn, List<IvMMove> trace, long cardinality) {
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
				addLogMove(logMove, null, root, logMove.getActivityEventClass(), cardinality);
			}
		}

		//position the trailing log moves
		for (Move logMove : trace.subList(end + 1, trace.size())) {
			if (!logMove.isIgnoredLogMove() && !logMove.isTauStart() && !trace.get(start).isIgnoredModelMove()) {
				addLogMove(logMove, root, null, logMove.getActivityEventClass(), cardinality);
			}
		}

		//recurse on the subtrace
		positionLogMoves(continueOn, subtrace, cardinality);
	}

	/*
	 * Invariant: the first and the last move of the trace are not log moves.
	 */
	private void positionLogMoves(UnfoldedNode unode, List<IvMMove> trace, long cardinality) {
		debug(" process trace " + trace + " on " + unode);
		
		assert (trace.get(0).isModelSync() && !trace.get(0).isIgnoredLogMove() && !trace.get(0).isIgnoredModelMove());
		int l = trace.size() - 1;
		assert (trace.get(l).isModelSync() && !trace.get(l).isIgnoredLogMove() && !trace.get(l).isIgnoredModelMove());

		if (unode.getNode() instanceof Manual) {
			//unode is an activity
			for (Move move : trace) {
				if (move.isLogMove()) {
					//put this log move on this leaf
					addLogMove(move, unode, unode, move.getActivityEventClass(), cardinality);
				}
			}
		} else if (unode.getNode() instanceof Automatic) {
			//unode is a tau
			//by the invariant, the trace contains no log moves
		} else if (unode.getBlock() instanceof Xor || unode.getBlock() instanceof Def) {
			//an xor cannot have log moves, just recurse on the child that produced this trace

			//by the invariant, the trace does not start with a log move
			//find the child that this grandchild belongs to
			UnfoldedNode child = findChildWith(unode, trace.get(0).getUnode());

			positionLogMoves(child, trace, cardinality);

		} else if (unode.getBlock() instanceof Seq) {
			splitSequence(unode, trace, cardinality);
		} else if (unode.getBlock() instanceof XorLoop || unode.getBlock() instanceof DefLoop) {
			splitLoop(unode, trace, cardinality);
		} else if (unode.getBlock() instanceof And) {

			//set up subtraces for children
			Map<UnfoldedNode, List<IvMMove>> subTraces = new THashMap<>();
			for (Node child : unode.getBlock().getChildren()) {
				subTraces.put(unode.unfoldChild(child), new ArrayList<IvMMove>());
			}

			//by the invariant, the first move is not a log move
			UnfoldedNode lastSeenChild = null;

			//walk through the trace to split it
			for (IvMMove move : trace) {
				if (move.isLogMove()) {
					//put this log move on the last seen child
					//we cannot know a better place to put it
					subTraces.get(lastSeenChild).add(move);
				} else {
					//put this move in the subtrace of the child it belongs to
					UnfoldedNode child = findChildWith(unode, move.getUnode());
					subTraces.get(child).add(move);
					lastSeenChild = child;
				}
			}

			//invariant might be invalid on sub traces; position leading and trailing log moves
			for (Node child : unode.getBlock().getChildren()) {
				UnfoldedNode uChild = unode.unfoldChild(child);
				List<IvMMove> subTrace = subTraces.get(uChild);
				positionLogMovesRoot(unode, uChild, subTrace, cardinality);
			}
		}
	}

	private void splitSequence(UnfoldedNode unode, List<IvMMove> trace, long cardinality) {
		//by the invariant, the first move is not a log move
		UnfoldedNode lastSeenChild = findChildWith(unode, trace.get(0).getUnode());
		List<IvMMove> logMoves = new ArrayList<IvMMove>();
		List<IvMMove> subTrace = new ArrayList<IvMMove>();

		//walk through the trace to split it
		for (IvMMove move : trace) {
			if (move.isIgnoredLogMove() || move.isIgnoredModelMove()) {
				//skip
			} else if (move.isLogMove()) {
				logMoves.add(move);
			} else {
				UnfoldedNode child = findChildWith(unode, move.getUnode());
				if (child.equals(lastSeenChild)) {
					//we are not leaving the previous child with this move
					//the log moves we have seen now are internal to the subtrace; add them
					subTrace.addAll(logMoves);
					subTrace.add(move);
					logMoves.clear();
				} else {
					//we are leaving the last child with this move

					//recurse on the subtrace up till now
					positionLogMoves(lastSeenChild, subTrace, cardinality);

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
		positionLogMoves(lastSeenChild, subTrace, cardinality);

		//the log moves we have seen now are external to both subtraces; position them on this unode
		for (Move logMove : logMoves) {
			addLogMove(logMove, unode, null, logMove.getActivityEventClass(), cardinality);
		}
	}

	private void splitLoop(UnfoldedNode unode, List<IvMMove> trace, long cardinality) {
		//by the invariant, the first move is not a log move
		UnfoldedNode lastSeenChild = findChildWith(unode, trace.get(0).getUnode());
		List<IvMMove> logMoves = new ArrayList<IvMMove>();
		List<IvMMove> subTrace = new ArrayList<IvMMove>();

		UnfoldedNode redo = unode.unfoldChild(unode.getBlock().getChildren().get(1));
		UnfoldedNode exit = unode.unfoldChild(unode.getBlock().getChildren().get(2));

		//walk through the trace to split it
		for (IvMMove move : trace) {
			if (move.isIgnoredLogMove() || move.isIgnoredModelMove()) {
				//skip
			} else if (move.isLogMove()) {
				logMoves.add(move);
			} else {
				UnfoldedNode child = findChildWith(unode, move.getUnode());
				if (child.equals(lastSeenChild)) {
					//we are not leaving the previous child with this move
					//the log moves we have seen now are internal to the subtrace; add them
					subTrace.addAll(logMoves);
					subTrace.add(move);
					logMoves.clear();
				} else {
					//we are leaving the last child with this move

					//recurse on the subtrace up till now
					positionLogMoves(lastSeenChild, subTrace, cardinality);

					//the log moves we have seen now are external to both subtraces; position them on this unode
					for (IvMMove logMove : logMoves) {
						if (child.equals(exit)) {
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
		positionLogMoves(lastSeenChild, subTrace, cardinality);

		//the log moves we have seen now are external to both subtraces; position them on the end of this unode
		for (Move logMove : logMoves) {
			addLogMove(logMove, unode, null, logMove.getActivityEventClass(), cardinality);
		}
	}

	/*
	 * find the child that contains a particular grandchild
	 */
	public static UnfoldedNode findChildWith(UnfoldedNode parent, UnfoldedNode grandChild) {
		//first phase: synchronous moves trough the paths
		Iterator<Node> itParent = parent.getPath().iterator();
		Iterator<Node> itGrandChild = grandChild.getPath().iterator();
		while (itParent.hasNext() && itGrandChild.hasNext() && itParent.next().equals(itGrandChild.next())) {

		}
		return parent.unfoldChild(itGrandChild.next());
	}

	public static UnfoldedNode getLowestCommonParent(UnfoldedNode unode1, UnfoldedNode unode2) {
		UnfoldedNode result = new UnfoldedNode(unode1.getPath().get(0));
		int i = 1;
		while (i < unode1.getPath().size() && i < unode2.getPath().size()
				&& unode1.getPath().get(i).equals(unode2.getPath().get(i))) {
			result = result.unfoldChild(unode1.getPath().get(i));
			i++;
		}
		return result;
	}

	private void addLogMove(Move move, UnfoldedNode unode, UnfoldedNode beforeChild, XEventClass e, long cardinality) {
		LogMovePosition logMovePosition = LogMovePosition.beforeChild(unode, beforeChild);
		move.setLogMovePosition(logMovePosition);
		if (!logMoves.containsKey(logMovePosition)) {
			logMoves.put(logMovePosition, new MultiSet<XEventClass>());
		}
		logMoves.get(logMovePosition).add(e, cardinality);
	}

	public MultiSet<UnfoldedNode> getModelMoves() {
		return modelMoves;
	}

	public Map<LogMovePosition, MultiSet<XEventClass>> getLogMoves() {
		return logMoves;
	}

	public MultiSet<String> getUnlabeledLogMoves() {
		return unlabeledLogMoves;
	}

	public long getDfg(UnfoldedNode unode1, UnfoldedNode unode2) {
		return dfg.getCardinalityOf(new Pair<UnfoldedNode, UnfoldedNode>(unode1, unode2));
	}

	public MultiSet<Move> getActivities() {
		return activities;
	}

	private static void debug(Object s) {
		//System.out.println(s);
		//InductiveVisualMinerController.debug(s.toString().replaceAll("\\n", " "));
	}

}
