package org.processmining.plugins.inductiveVisualMiner;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Selection {

	private final Set<UnfoldedNode> selectedActivities;
	private final Set<LogMovePosition> selectedLogMoves;
	private final Set<UnfoldedNode> selectedModelMoves;

	public Selection() {
		selectedActivities = new THashSet<>();
		selectedLogMoves = new THashSet<>();
		selectedModelMoves = new THashSet<>();
	}

	public Selection(Set<UnfoldedNode> selectedActivities, Set<LogMovePosition> selectedLogMoves,
			Set<UnfoldedNode> selectedModelMoves) {
		this.selectedActivities = selectedActivities;
		this.selectedLogMoves = selectedLogMoves;
		this.selectedModelMoves = selectedModelMoves;
	}

	public boolean isSelected(Move move) {
		if (move.isIgnoredLogMove() || move.isIgnoredModelMove()) {
			return false;
		}
		if (move.isSyncMove() && selectedActivities.contains(move.getUnode())) {
			return true;
		}
		if (move.isLogMove() && selectedLogMoves.contains(LogMovePosition.of(move))) {
			return true;
		}
		return !move.isSyncMove() && move.isModelMove() && selectedModelMoves.contains(move.getUnode());
	}

	public boolean isSelected(LocalDotNode dotNode) {
		return selectedActivities.contains(dotNode.getUnode());
	}

	@SuppressWarnings("incomplete-switch")
	public boolean isSelected(LocalDotEdge dotEdge) {
		switch (dotEdge.getType()) {
			case logMove :
				return selectedLogMoves.contains(LogMovePosition.of(dotEdge));
			case modelMove :
				return selectedModelMoves.contains(dotEdge.getUnode());
		}
		throw new RuntimeException("Only model-move and log-move edges can be clicked on.");
	}

	public Set<UnfoldedNode> getSelectedActivities() {
		return Collections.unmodifiableSet(selectedActivities);
	}
	
	public Set<UnfoldedNode> getSelectedModelMoves() {
		return Collections.unmodifiableSet(selectedModelMoves);
	}

	@SuppressWarnings("incomplete-switch")
	public void select(DotElement dotElement) {
		if (dotElement instanceof LocalDotNode) {
			selectedActivities.add(((LocalDotNode) dotElement).getUnode());
			return;
		} else if (dotElement instanceof LocalDotEdge) {
			switch (((LocalDotEdge) dotElement).getType()) {
				case logMove :
					selectedLogMoves.add(LogMovePosition.of(((LocalDotEdge) dotElement)));
					return;
				case modelMove :
					selectedModelMoves.add(((LocalDotEdge) dotElement).getUnode());
					return;
			}
			throw new RuntimeException("Only ");
		}
	}

	public boolean isAnActivitySelected() {
		return !selectedActivities.isEmpty();
	}

	public boolean isALogMoveSelected() {
		return !selectedLogMoves.isEmpty();
	}

	public boolean isAModelMoveSelected() {
		return !selectedModelMoves.isEmpty();
	}

	public boolean isSomethingSelected() {
		return isAnActivitySelected() || isALogMoveSelected() || isAModelMoveSelected();
	}
}
