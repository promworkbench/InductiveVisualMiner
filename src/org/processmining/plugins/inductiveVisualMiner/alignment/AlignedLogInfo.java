package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfoG;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogInfo extends IMLogInfoG<Move> {

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

	public AlignedLogInfo() {
		super(Move.class, new AlignedLog());
		modelMoves = new MultiSet<UnfoldedNode>();
		logMoves = new HashMap<LogMovePosition, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		dfg = new MultiSet<Pair<UnfoldedNode, UnfoldedNode>>();
	}

	public AlignedLogInfo(MultiSet<AlignedTrace> log) {
		super(Move.class, log);
		modelMoves = new MultiSet<UnfoldedNode>();
		logMoves = new HashMap<LogMovePosition, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		dfg = new MultiSet<Pair<UnfoldedNode, UnfoldedNode>>();
		UnfoldedNode root = null;
		for (IMTraceG<Move> trace : log) {
			UnfoldedNode lastDfgUnode = null;
			UnfoldedNode lastUnode = null;
			long cardinality = log.getCardinalityOf(trace);
			boolean traceContainsLogMove = false;
			for (int i = 0; i < trace.size(); i++) {
				Move move = trace.get(i);
				if (move.getType() == Type.model) {
					//add model move to list of model moves
					modelMoves.add(move.getUnode(), cardinality);
				} else if (move.isLogMove()) {
					traceContainsLogMove = true;
					move.setLogMoveParallelBranchMappedTo(lastUnode);
					unlabeledLogMoves.add(move.getEventClass().toString(), cardinality);
				}

				if (root == null && move.isModelSync()) {
					root = new UnfoldedNode(move.getUnode().getPath().get(0));
				}
				
				if (move.isModelSync()) {
					lastUnode = move.getUnode();
				}

				if (move.getUnode() != null && move.getUnode().getNode() instanceof Manual) {
					dfg.add(new Pair<UnfoldedNode, UnfoldedNode>(lastDfgUnode, move.getUnode()), cardinality);
					lastDfgUnode = move.getUnode();
				}
			}
			if (lastDfgUnode != null) {
				dfg.add(new Pair<UnfoldedNode, UnfoldedNode>(lastDfgUnode, null), cardinality);
			}

			//position the log moves
			if (traceContainsLogMove) {
				positionLogMovesRoot(root, root, trace, cardinality);
			}
		}

		debug(logMoves);
	}

	public void positionLogMovesRoot(UnfoldedNode root, UnfoldedNode continueOn, IMTraceG<Move> trace, long cardinality) {
		debug("");

		//remove the leading and trailing log moves and position them on the root
		int start = 0;
		while (!trace.get(start).isModelSync()) {
			start++;
		}
		int end = trace.size() - 1;
		while (!trace.get(end).isModelSync()) {
			end--;
		}
		List<Move> subtrace = trace.subList(start, end + 1);

		//position the leading log moves
		for (Move logMove : trace.subList(0, start)) {
			addLogMove(logMove, null, root, logMove.getEventClass(), cardinality);
		}

		//position the trailing log moves
		for (Move logMove : trace.subList(end + 1, trace.size())) {
			addLogMove(logMove, root, null, logMove.getEventClass(), cardinality);
		}

		//recurse on the subtrace
		positionLogMoves(continueOn, subtrace, cardinality);
	}

	/*
	 * Invariant: the first and the last move of the trace are not log moves.
	 */
	private void positionLogMoves(UnfoldedNode unode, List<Move> trace, long cardinality) {
		debug(" position " + trace + " on " + unode);
		if (unode.getBlock() == null) {
			//unode is an activity or tau
			//by the invariant, the trace contains no log moves
		} else if (unode.getBlock() instanceof Xor || unode.getBlock() instanceof Def) {
			//an xor cannot have log moves, just recurse on the child that produced this trace

			//by the invariant, the trace does not start with a log move
			//find the child that this grandchild belongs to
			UnfoldedNode child = findChildWith(unode, trace.get(0).getUnode());

			positionLogMoves(child, trace, cardinality);

		} else if (unode.getBlock() instanceof Seq || unode.getBlock() instanceof XorLoop) {
			splitSequenceLoop(unode, trace, cardinality);
		} else if (unode.getBlock() instanceof And) {

			//set up subtraces for children
			Map<UnfoldedNode, IMTraceG<Move>> subTraces = new HashMap<>();
			for (Node child : unode.getBlock().getChildren()) {
				subTraces.put(unode.unfoldChild(child), new IMTraceG<Move>());
			}

			//by the invariant, the first move is not a log move
			UnfoldedNode lastSeenChild = null;

			//walk through the trace to split it
			for (Move move : trace) {
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
				IMTraceG<Move> subTrace = subTraces.get(uChild);
				positionLogMovesRoot(unode, uChild, subTrace, cardinality);
			}
		}
	}

	private void splitSequenceLoop(UnfoldedNode unode, List<Move> trace, long cardinality) {
		//by the invariant, the first move is not a log move
		UnfoldedNode lastSeenChild = findChildWith(unode, trace.get(0).getUnode());
		List<Move> logMoves = new ArrayList<Move>();
		List<Move> subTrace = new ArrayList<Move>();

		//walk through the trace to split it
		for (Move move : trace) {
			if (move.isLogMove()) {
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

					//recurse on subtrace
					positionLogMoves(lastSeenChild, subTrace, cardinality);

					//the log moves we have seen now are external to both subtraces; position them on this unode
					for (Move logMove : logMoves) {
						addLogMove(logMove, unode, child, logMove.getEventClass(), cardinality);
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
			addLogMove(logMove, unode, null, logMove.getEventClass(), cardinality);
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
		move.setLogMove(logMovePosition);
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

	private static void debug(Object s) {
		//		System.out.println(s);
		//				debug(s.toString().replaceAll("\\n", " "));
	}

}
