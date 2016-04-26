package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.alignment.PositionLogMoves;
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
		unlabeledLogMoves = new MultiSet<String>();
		dfg = new MultiSet<Pair<UnfoldedNode, UnfoldedNode>>();
		activities = new MultiSet<>();
		PositionLogMoves positionLogMoves = new PositionLogMoves();
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
				positionLogMoves.position(root, trace);
			}
		}
		logMoves = positionLogMoves.getLogMoves();
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
