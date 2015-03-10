package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

public class TokenDestination {
	private final Double arrivalTime;
	
	private final LocalDotEdge edge;
	private final LocalDotNode node;
	
	private TokenDestination(LocalDotEdge edge, LocalDotNode node, Double arrivalTime) {
		this.edge = edge;
		this.node = node;
		this.arrivalTime = arrivalTime;
	}
	
	public static TokenDestination edge(LocalDotEdge edge, Double arrivalTime) {
		return new TokenDestination(edge, null, arrivalTime);
	}
	
	public static TokenDestination node(LocalDotNode node, Double arrivalTime) {
		return new TokenDestination(null, node, arrivalTime);
	}
	
	public LocalDotNode getDestinationNode() {
		if (node != null) {
			return node;
		}
		return edge.getTarget();
	}
	
	public boolean hasArrivalTime() {
		return arrivalTime != null;
	}
	
	public double getArrivalTime() {
		return arrivalTime;
	}
	
}
