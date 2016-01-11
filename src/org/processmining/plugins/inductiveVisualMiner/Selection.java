package org.processmining.plugins.inductiveVisualMiner;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Selection {

	private final Set<UnfoldedNode> selectedActivities;
	private final Set<LogMovePosition> selectedLogMoveEdges;
	private final Set<UnfoldedNode> selectedModelMoveEdges;
	private final Set<UnfoldedNode> selectedModelEdges;

	public Selection() {
		selectedActivities = new THashSet<>();
		selectedLogMoveEdges = new THashSet<>();
		selectedModelMoveEdges = new THashSet<>();
		selectedModelEdges = new THashSet<>();
	}

	public Selection(Set<UnfoldedNode> selectedActivities, Set<LogMovePosition> selectedLogMoves,
			Set<UnfoldedNode> selectedModelMoves, Set<UnfoldedNode> selectedTaus) {
		this.selectedActivities = new THashSet<>(selectedActivities);
		this.selectedLogMoveEdges = new THashSet<>(selectedLogMoves);
		this.selectedModelMoveEdges = new THashSet<>(selectedModelMoves);
		this.selectedModelEdges = new THashSet<>(selectedTaus);
	}

	public boolean isSelected(Move move) {
		if (move.isIgnoredLogMove() || move.isIgnoredModelMove() || !move.isComplete()) {
			return false;
		}
		if (move.isSyncMove() && selectedActivities.contains(move.getUnode())) {
			return true;
		}
		if (move.isLogMove() && selectedLogMoveEdges.contains(LogMovePosition.of(move))) {
			return true;
		}
		if (!move.isSyncMove() && move.isModelMove() && selectedModelMoveEdges.contains(move.getUnode())) {
			return true;
		}
		
		if (move.isSyncMove()) {
			for (UnfoldedNode selectedEdgeUnode : selectedModelEdges) {
				if (TreeUtils.isParent(selectedEdgeUnode, move.getUnode())) {
					return true;
				}
			}
		}
		
		return false;
	}

	public boolean isSelected(LocalDotNode dotNode) {
		return selectedActivities.contains(dotNode.getUnode());
	}

	public boolean isSelected(LocalDotEdge dotEdge) {
		switch (dotEdge.getType()) {
			case model :
				return selectedModelEdges.contains(dotEdge.getUnode());
			case logMove :
				return selectedLogMoveEdges.contains(LogMovePosition.of(dotEdge));
			case modelMove :
				return selectedModelMoveEdges.contains(dotEdge.getUnode());
		}
		throw new RuntimeException("Only model-move, log-move and tau edges can be clicked on.");
	}

	public Set<UnfoldedNode> getSelectedActivities() {
		return Collections.unmodifiableSet(selectedActivities);
	}
	
	public Set<UnfoldedNode> getSelectedTaus() {
		return Collections.unmodifiableSet(selectedModelEdges);
	}

	public Set<UnfoldedNode> getSelectedModelMoves() {
		return Collections.unmodifiableSet(selectedModelMoveEdges);
	}

	public void select(DotElement dotElement) {
		if (dotElement instanceof LocalDotNode) {
			selectedActivities.add(((LocalDotNode) dotElement).getUnode());
			return;
		} else if (dotElement instanceof LocalDotEdge) {
			switch (((LocalDotEdge) dotElement).getType()) {
				case logMove :
					selectedLogMoveEdges.add(LogMovePosition.of(((LocalDotEdge) dotElement)));
					return;
				case modelMove :
					selectedModelMoveEdges.add(((LocalDotEdge) dotElement).getUnode());
					return;
				case model :
					selectedModelEdges.add(((LocalDotEdge) dotElement).getUnode());
					return;
			}
			throw new RuntimeException("Selection not supported.");
		}
	}

	public boolean isAnActivitySelected() {
		return !selectedActivities.isEmpty();
	}

	public boolean isALogMoveSelected() {
		return !selectedLogMoveEdges.isEmpty();
	}

	public boolean isAModelMoveSelected() {
		return !selectedModelMoveEdges.isEmpty();
	}

	public boolean isAModelEdgeSelected() {
		return !selectedModelEdges.isEmpty();
	}

	public boolean isSomethingSelected() {
		return isAnActivitySelected() || isALogMoveSelected() || isAModelMoveSelected() || isAModelEdgeSelected();
	}
}
