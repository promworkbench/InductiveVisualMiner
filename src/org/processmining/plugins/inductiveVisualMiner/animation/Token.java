package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

public class Token {
	private final LocalDotNode startPosition;
	private Double startTime;
	private final boolean fade;
	private List<Pair<LocalDotEdge, Double>> points;
	private List<Pair<Integer, Token>> subTokens;

	public Token(LocalDotNode startPosition, Double startTime, boolean fade) {
		//		System.out.println("create new token @" + startTime);
		this.startPosition = startPosition;
		this.startTime = startTime;
		this.fade = fade;
		points = new ArrayList<Pair<LocalDotEdge, Double>>();
		subTokens = new ArrayList<>();
	}

	public Double getLastTime() {
		for (int i = points.size() - 1; i >= 0; i--) {
			if (points.get(i).getRight() != null) {
				return points.get(i).getRight();
			}
		}
		return startTime;
	}

	public LocalDotNode getLastPosition() {
		if (points.isEmpty()) {
			return startPosition;
		}
		return points.get(points.size() - 1).getLeft().getTarget();
	}

	public void addPoint(LocalDotEdge edge, Double time) {
		//perform sanity check
		if (time != null && getLastTime() > time) {
			throw new RuntimeException("token cannot move back in time");
		}

		points.add(Pair.of(edge, time));
		System.out.println("  add point to token " + points.get(points.size() - 1) + fade);
		
		System.out.println("   to " + edge.getTarget().toString().replaceAll("\\n", " "));
	}

	public void addSubToken(Token token) {
		subTokens.add(Pair.of(points.size() - 1, token));
		System.out.println("  add subtoken");
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
