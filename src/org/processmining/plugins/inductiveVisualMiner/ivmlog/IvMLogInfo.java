package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.alignment.PositionLogMoves;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;

@SuppressWarnings("deprecation")
public class IvMLogInfo {

	private final TIntLongMap modelMoves;
	private final MultiSet<String> unlabeledLogMoves;
	private final TIntLongMap nodeExecutions;

	//for each position in the tree, the xeventclasses that were moved
	//position:
	// (node1, node1) on node1
	// (node1, node2) on node1, before node2 (only in case of node1 sequence or loop)
	// (root, null) at end of trace
	// (null, root) at start of trace
	private final Map<LogMovePosition, MultiSet<XEventClass>> logMoves;
	private final MultiSet<Move> activities;

	public IvMLogInfo() {
		modelMoves = new TIntLongHashMap(10, 0.5f, -1, 0);
		logMoves = new HashMap<LogMovePosition, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		activities = new MultiSet<>();
		nodeExecutions = new TIntLongHashMap(10, 0.5f, -1, 0);
	}

	public IvMLogInfo(IvMLog log, IvMEfficientTree tree) {
		modelMoves = new TIntLongHashMap(10, 0.5f, -1, 0);
		unlabeledLogMoves = new MultiSet<String>();
		activities = new MultiSet<>();
		nodeExecutions = new TIntLongHashMap(10, 0.5f, -1, -1);
		PositionLogMoves positionLogMoves = new PositionLogMoves();
		int lastModelSyncNode;
		for (IvMTrace trace : log) {
			int lastDfgUnode = -1;
			int lastUnode = -1;
			lastModelSyncNode = -1;
			boolean traceContainsLogMove = false;
			for (int i = 0; i < trace.size(); i++) {
				Move move = trace.get(i);
				activities.add(move);
				if (move.getType() == Type.modelMove) {
					//add model move to list of model moves
					modelMoves.adjustOrPutValue(move.getTreeNode(), 1, 1);
				} else if (move.isLogMove()) {
					traceContainsLogMove = true;
					move.setLogMoveParallelBranchMappedTo(lastUnode);
					unlabeledLogMoves.add(move.getActivityEventClass().toString());
				}

				if (move.isModelSync() && !move.isIgnoredModelMove()) {
					lastUnode = move.getTreeNode();
				}

				if (move.getTreeNode() != -1 && tree.isActivity(move.getTreeNode())) {
					lastDfgUnode = move.getTreeNode();
				}

				/*
				 * Keep track of entering and exiting nodes. Notice that in
				 * process trees, it is impossible to enter a node a second time
				 * without seeing some other node in between.
				 */

				if (move.isModelSync()) {
					processEnteringExitingNodes(tree, lastModelSyncNode, move.getTreeNode());
					lastModelSyncNode = move.getTreeNode();
				}
			}

			//position the log moves
			if (traceContainsLogMove) {
				positionLogMoves.position(tree, tree.getRoot(), trace);
			}
		}
		logMoves = positionLogMoves.getLogMoves();
	}

	public TIntLongMap getModelMoves() {
		return modelMoves;
	}

	public Map<LogMovePosition, MultiSet<XEventClass>> getLogMoves() {
		return logMoves;
	}

	public MultiSet<String> getUnlabeledLogMoves() {
		return unlabeledLogMoves;
	}

	public MultiSet<Move> getActivities() {
		return activities;
	}

	public long getNodeExecutions(IvMEfficientTree tree, int node) {
		return nodeExecutions.get(node);
	}

	private void processEnteringExitingNodes(IvMEfficientTree tree, int lastNode, int newNode) {

		int lowestCommonParent = tree.getRoot();
		if (lastNode != -1) {
			lowestCommonParent = tree.getLowestCommonParent(lastNode, newNode);
		} else {
			//the first move always enters the root
			nodeExecutions.adjustOrPutValue(lowestCommonParent, 1, 1);
		}

		//we entered all nodes between the lowestCommonParent (exclusive) and newNode (inclusive)
		int node = lowestCommonParent;
		while (node != newNode) {
			node = tree.getChildWith(node, newNode);
			nodeExecutions.adjustOrPutValue(node, 1, 1);
		}
	}

	private static void debug(Object s) {
		//System.out.println(s);
		//InductiveVisualMinerController.debug(s.toString().replaceAll("\\n", " "));
	}

}
