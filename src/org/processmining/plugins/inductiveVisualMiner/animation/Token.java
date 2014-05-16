package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotNode;


public class Token {
	private final LocalDotNode startPosition;
	private final double startTime;
	private List<Pair<LocalDotEdge, Double>> points;
	
	public Token(LocalDotNode startPosition, double startTime) {
		System.out.println("create new token @" + startTime);
		this.startPosition = startPosition;
		this.startTime = startTime;
		points = new ArrayList<Pair<LocalDotEdge, Double>>();
	}
	
	public double getLastTime() {
		if (points.isEmpty()) {
			return startTime;
		}
		return points.get(points.size()-1).getRight();
	}
	
	public LocalDotNode getLastPosition() {
		if (points.isEmpty()) {
			return startPosition;
		}
		return points.get(points.size()-1).getLeft().getTarget();
	}
	
	public void addPoint(LocalDotEdge edge, double time) {
		//perform sanity check
		if (getLastTime() > time) {
			throw new RuntimeException("token cannot move back in time");
		}
		
		points.add(Pair.of(edge, time));
		System.out.println("  add point to token " + points.get(points.size()-1));
	}
}
