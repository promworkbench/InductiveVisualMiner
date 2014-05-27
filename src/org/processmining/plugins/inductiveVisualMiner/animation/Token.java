package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

public class Token {
	private final LocalDotNode startPosition;
	private final double startTime;
	private final boolean fade;
	private List<Pair<LocalDotEdge, Double>> points;

	public Token(LocalDotNode startPosition, double startTime, boolean fade) {
		//		System.out.println("create new token @" + startTime);
		this.startPosition = startPosition;
		this.startTime = startTime;
		this.fade = fade;
		points = new ArrayList<Pair<LocalDotEdge, Double>>();
	}

	public double getLastTime() {
		if (points.isEmpty()) {
			return startTime;
		}
		return points.get(points.size() - 1).getRight();
	}

	public LocalDotNode getLastPosition() {
		if (points.isEmpty()) {
			return startPosition;
		}
		return points.get(points.size() - 1).getLeft().getTarget();
	}

	public void addPoint(LocalDotEdge edge, double time) {
		//perform sanity check
		if (getLastTime() > time) {
			throw new RuntimeException("token cannot move back in time");
		}

		points.add(Pair.of(edge, time));
		//		System.out.println("  add point to token " + points.get(points.size()-1));
	}

	public List<Pair<LocalDotEdge, Double>> getPoints() {
		return points;
	}

	public LocalDotNode getStartPosition() {
		return startPosition;
	}

	public double getStartTime() {
		return startTime;
	}

	public boolean isFade() {
		return fade;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Token starts at " + startPosition.getId() + " @" + startTime);
		result.append("\n");
		for (Pair<LocalDotEdge, Double> p : points) {
			result.append(p);
			result.append("\n");
		}
		result.append("\n");
		return result.toString();
	}
}
