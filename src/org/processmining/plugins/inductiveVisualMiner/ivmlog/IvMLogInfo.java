package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.alignment.PositionLogMoves;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

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

	private final TObjectIntMap<Pair<Integer, Integer>> modelEdgeExecutions;
	private final TObjectIntMap<Pair<Integer, Integer>> modelMoveEdgeExecutions;

	public IvMLogInfo() {
		modelMoves = new TIntLongHashMap(10, 0.5f, -1, 0);
		logMoves = new HashMap<LogMovePosition, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		activities = new MultiSet<>();
		nodeExecutions = new TIntLongHashMap(10, 0.5f, -1, 0);
		modelEdgeExecutions = new TObjectIntHashMap<>(10, 0.5f, 0);
		modelMoveEdgeExecutions = new TObjectIntHashMap<>(10, 0.5f, 0);
	}

	public IvMLogInfo(IvMLog log, IvMModel model) {
		modelMoves = new TIntLongHashMap(10, 0.5f, -1, 0);
		unlabeledLogMoves = new MultiSet<String>();
		activities = new MultiSet<>();
		nodeExecutions = new TIntLongHashMap(10, 0.5f, -1, -1);
		modelEdgeExecutions = new TObjectIntHashMap<>(10, 0.5f, 0);
		modelMoveEdgeExecutions = new TObjectIntHashMap<>(10, 0.5f, 0);
		TIntSet inParallelNodes = new TIntHashSet(10, 0.5f, -1);
		PositionLogMoves positionLogMoves = new PositionLogMoves();
		int lastModelSyncNode;
		for (IvMTrace trace : log) {
			int lastUnode = -1;
			lastModelSyncNode = -1;
			inParallelNodes.clear();
			boolean traceContainsLogMove = false;
			for (int i = 0; i < trace.size(); i++) {
				Move move = trace.get(i);
				activities.add(move);
				if (move.getType() == Type.modelMove) {
					//add model move to list of model moves
					modelMoves.adjustOrPutValue(move.getTreeNode(), 1, 1);
					modelMoveEdgeExecutions.adjustOrPutValue(Pair.of(move.getSourceNode(), move.getTreeNode()), 1, 1);
				} else if (move.isLogMove()) {
					traceContainsLogMove = true;
					move.setLogMoveParallelBranchMappedTo(lastUnode);
					unlabeledLogMoves.add(move.getActivityEventClass().toString());
				} else if (move.isComplete()) {
					modelEdgeExecutions.adjustOrPutValue(Pair.of(move.getSourceNode(), move.getTreeNode()), 1, 1);
				}

				if (move.isModelSync() && !move.isIgnoredModelMove()) {
					lastUnode = move.getTreeNode();
				}

				/*
				 * Keep track of entering and exiting nodes. Notice that in
				 * process trees, it is impossible to enter a node a second time
				 * without seeing some other node in between.
				 */

				if (move.isModelSync()) {
					if (model.isTree()) {
						processEnteringExitingNodes(model.getTree(), lastModelSyncNode, move.getTreeNode(),
								inParallelNodes);
					}
					lastModelSyncNode = move.getTreeNode();
				}
			}
			
			if (lastUnode != -1) {
				modelEdgeExecutions.adjustOrPutValue(Pair.of(lastUnode, -1), 1, 1);
			}

			//position the log moves
			if (traceContainsLogMove) {
				positionLogMoves.position(model, trace);
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

	private void processEnteringExitingNodes(IvMEfficientTree tree, int lastNode, int newNode,
			TIntSet inParallelNodes) {

		int lowestCommonParent = tree.getRoot();
		if (lastNode != -1) {
			lowestCommonParent = EfficientTreeUtils.getLowestCommonParent(tree, lastNode, newNode);
		} else {
			//the first move always enters the root
			nodeExecutions.adjustOrPutValue(lowestCommonParent, 1, 1);
		}

		//process the leaving of nodes
		for (TIntIterator it = inParallelNodes.iterator(); it.hasNext();) {
			int parallelNode = it.next();

			if (nodeLeavesParallel(tree, parallelNode, newNode)) {
				it.remove();
			}
		}

		//we entered all nodes between the lowestCommonParent (exclusive) and newNode (inclusive)
		int node = lowestCommonParent;
		while (node != newNode) {
			node = EfficientTreeUtils.getChildWith(tree, node, newNode);
			if (!inParallelNodes.contains(node)) {
				nodeExecutions.adjustOrPutValue(node, 1, 1);

				inParallelNodes.add(node);
			}
		}
	}

	/**
	 * 
	 * @param parallelNode
	 * @param newNode
	 * @return Whether execution of newNode is proof that parallelNode is not
	 *         executing anymore.
	 */
	public static boolean nodeLeavesParallel(IvMEfficientTree tree, int parallelNode, int newNode) {
		int lowestCommonParent = EfficientTreeUtils.getLowestCommonParent(tree, parallelNode, newNode);
		return !tree.isConcurrent(lowestCommonParent) && !tree.isOr(lowestCommonParent);
	}

	private static void debug(Object s) {
		//System.out.println(s);
		//InductiveVisualMinerController.debug(s.toString().replaceAll("\\n", " "));
	}

	public long getModelEdgeExecutions(int from, int to) {
		return modelEdgeExecutions.get(Pair.of(from, to));
	}

	public long getModelMoveEdgeExecutions(int from, int to) {
		return modelMoveEdgeExecutions.get(Pair.of(from, to));
	}

}
