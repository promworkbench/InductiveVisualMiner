package org.processmining.plugins.inductiveVisualMiner;

import java.util.List;
import java.util.Set;

import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.LogUtils;
import org.processmining.plugins.inductiveVisualMiner.visualisation.DFMEdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

public class Selection {

	private final TIntSet treeNodesOfSelectedActivities;
	private final Set<LogMovePosition> logMovePositionsOfSelectedLogMoveEdges;
	private final TIntSet treeNodesOfSelectedModelMoveEdges;
	private final TLongSet treeNodesOfSelectedModelEdges;

	public Selection() {
		treeNodesOfSelectedActivities = new TIntHashSet(10, 0.5f, -1);
		logMovePositionsOfSelectedLogMoveEdges = new THashSet<>();
		treeNodesOfSelectedModelMoveEdges = new TIntHashSet(10, 0.5f, -1);
		treeNodesOfSelectedModelEdges = new TLongHashSet(10, 0.5f, -1);
	}

	public Selection(TIntSet selectedActivities, Set<LogMovePosition> selectedLogMoves, TIntSet selectedModelMoves,
			TLongSet selectedTaus) {
		this.treeNodesOfSelectedActivities = new TIntHashSet(selectedActivities);
		this.logMovePositionsOfSelectedLogMoveEdges = new THashSet<>(selectedLogMoves);
		this.treeNodesOfSelectedModelMoveEdges = new TIntHashSet(selectedModelMoves);
		this.treeNodesOfSelectedModelEdges = new TLongHashSet(selectedTaus);
	}

	public boolean isSelected(IvMModel model, List<Move> trace, Move move) {
		if (move.isIgnoredLogMove() || move.isIgnoredModelMove() || !move.isComplete()) {
			return false;
		}
		if (move.isSyncMove() && treeNodesOfSelectedActivities.contains(move.getTreeNode())) {
			return true;
		}
		if (move.isLogMove() && logMovePositionsOfSelectedLogMoveEdges.contains(LogMovePosition.of(move))) {
			return true;
		}
		if (!move.isSyncMove() && move.isModelMove()
				&& treeNodesOfSelectedModelMoveEdges.contains(move.getTreeNode())) {
			return true;
		}

		if (move.isSyncMove() || model.isDfg()) {
			for (TLongIterator it = treeNodesOfSelectedModelEdges.iterator(); it.hasNext();) {
				if (model.isTree()) {
					//tree model
					int selectedEdgeUnode = (int) it.next();

					if (model.isParentOf(selectedEdgeUnode, move.getTreeNode())) {
						return true;
					}
				} else {
					//dfg model
					long selectedEdge = it.next();
					int source = (int) (selectedEdge >> 32);
					int target = (int) selectedEdge;

					if (move.getTreeNode() == source || move.getTreeNode() == target) {
						if (source == -2 && target == -2) {
							//selected empty trace move
							return true;
						}
						Move nextMove = LogUtils.findNextCompleteModelMove(trace, move.getIndexInAlignedTrace());
						if (nextMove == null && target == -1 && move.getTreeNode() == source) {
							//selected end move
							return true;
						}
						if (move.getTreeNode() == source && nextMove != null && nextMove.getTreeNode() == target) {
							//selected edge move
							return true;
						}
						Move previousMove = LogUtils.findPreviousCompleteModelMove(trace,
								move.getIndexInAlignedTrace());
						if (previousMove == null && source == -1 && move.getTreeNode() == target) {
							//selected start move
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean isSelected(LocalDotNode dotNode) {
		return treeNodesOfSelectedActivities.contains(dotNode.getUnode());
	}

	public boolean isSelected(LocalDotEdge dotEdge) {
		switch (dotEdge.getType()) {
			case model :
				return treeNodesOfSelectedModelEdges.contains(dotEdge.getUnode());
			case logMove :
				return logMovePositionsOfSelectedLogMoveEdges.contains(LogMovePosition.of(dotEdge));
			case modelMove :
				return treeNodesOfSelectedModelMoveEdges.contains(dotEdge.getUnode());
		}
		assert (false);
		return false;
	}

	public TIntSet getSelectedActivities() {
		return treeNodesOfSelectedActivities;
	}

	public TLongSet getSelectedTaus() {
		return treeNodesOfSelectedModelEdges;
	}

	public TIntSet getSelectedModelMoves() {
		return treeNodesOfSelectedModelMoveEdges;
	}

	public void select(DotElement dotElement) {
		if (dotElement instanceof LocalDotNode) {
			treeNodesOfSelectedActivities.add(((LocalDotNode) dotElement).getUnode());
			return;
		} else if (dotElement instanceof LocalDotEdge) {
			switch (((LocalDotEdge) dotElement).getType()) {
				case logMove :
					logMovePositionsOfSelectedLogMoveEdges.add(LogMovePosition.of(((LocalDotEdge) dotElement)));
					return;
				case modelMove :
					treeNodesOfSelectedModelMoveEdges.add(((LocalDotEdge) dotElement).getUnode());
					return;
				case model :

					LocalDotEdge edge = (LocalDotEdge) dotElement;
					if (edge.getUnode() != -1) {
						//model is a tree
						treeNodesOfSelectedModelEdges.add(edge.getUnode());
					} else {
						//model is a dfg: put two endpoints of the edge in a single long

						if (edge.getDfmType() == DFMEdgeType.modelIntraActivity) {
							treeNodesOfSelectedActivities.add(((LocalDotEdge) dotElement).getLookupNode1());
						} else {
							int x = edge.getLookupNode1();
							int y = edge.getLookupNode2();
							long l = (((long) x) << 32) | (y & 0xffffffffL);
							treeNodesOfSelectedModelEdges.add(l);
						}
					}

					return;
			}
			throw new RuntimeException("Selection not supported.");
		}
	}

	public boolean isAnActivitySelected() {
		return !treeNodesOfSelectedActivities.isEmpty();
	}

	public boolean isALogMoveSelected() {
		return !logMovePositionsOfSelectedLogMoveEdges.isEmpty();
	}

	public boolean isAModelMoveSelected() {
		return !treeNodesOfSelectedModelMoveEdges.isEmpty();
	}

	public boolean isAModelEdgeSelected() {
		return !treeNodesOfSelectedModelEdges.isEmpty();
	}

	public boolean isSomethingSelected() {
		return isAnActivitySelected() || isALogMoveSelected() || isAModelMoveSelected() || isAModelEdgeSelected();
	}
}
