package org.processmining.plugins.inductiveVisualMiner.animation.shortestPath;

import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;

public class ShortestPathEdge {
	private final LocalDotEdge dotEdge;
	
	public ShortestPathEdge(LocalDotEdge dotEdge) {
		this.dotEdge = dotEdge;
	}

	public LocalDotEdge getDotEdge() {
		return dotEdge;
	}
}
