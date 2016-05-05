package org.processmining.plugins.inductiveVisualMiner;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Set;

import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;

public class Selection {

	private final TIntSet treeNodesOfSelectedActivities;
	private final Set<LogMovePosition> logMovePositionsOfSelectedLogMoveEdges;
	private final TIntSet treeNodesOfSelectedModelMoveEdges;
	private final TIntSet treeNodesOfSelectedModelEdges;

	public Selection() {
		treeNodesOfSelectedActivities = new TIntHashSet(10, 0.5f, -1);
		logMovePositionsOfSelectedLogMoveEdges = new THashSet<>();
		treeNodesOfSelectedModelMoveEdges = new TIntHashSet(10, 0.5f, -1);
		treeNodesOfSelectedModelEdges = new TIntHashSet(10, 0.5f, -1);
	}

	public Selection(TIntSet selectedActivities, Set<LogMovePosition> selectedLogMoves, TIntSet selectedModelMoves,
			TIntSet selectedTaus) {
		this.treeNodesOfSelectedActivities = new TIntHashSet(selectedActivities);
		this.logMovePositionsOfSelectedLogMoveEdges = new THashSet<>(selectedLogMoves);
		this.treeNodesOfSelectedModelMoveEdges = new TIntHashSet(selectedModelMoves);
		this.treeNodesOfSelectedModelEdges = new TIntHashSet(selectedTaus);
	}

	public boolean isSelected(IvMEfficientTree tree, Move move) {
		if (move.isIgnoredLogMove() || move.isIgnoredModelMove() || !move.isComplete()) {
			return false;
		}
		if (move.isSyncMove() && treeNodesOfSelectedActivities.contains(move.getTreeNode())) {
			return true;
		}
		if (move.isLogMove() && logMovePositionsOfSelectedLogMoveEdges.contains(LogMovePosition.of(move))) {
			return true;
		}
		if (!move.isSyncMove() && move.isModelMove() && treeNodesOfSelectedModelMoveEdges.contains(move.getTreeNode())) {
			return true;
		}

		if (move.isSyncMove()) {
			for (TIntIterator it = treeNodesOfSelectedModelEdges.iterator(); it.hasNext();) {
				int selectedEdgeUnode = it.next();
				if (tree.isParentOf(selectedEdgeUnode, move.getTreeNode())) {
					return true;
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

	public TIntSet getSelectedTaus() {
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
					treeNodesOfSelectedModelEdges.add(((LocalDotEdge) dotElement).getUnode());
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
