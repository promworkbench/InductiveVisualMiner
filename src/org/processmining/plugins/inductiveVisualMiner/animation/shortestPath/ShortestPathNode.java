package org.processmining.plugins.inductiveVisualMiner.animation.shortestPath;

import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;



public abstract class ShortestPathNode {
	private final LocalDotNode dotNode;
	
	public ShortestPathNode(LocalDotNode dotNode) {
		this.dotNode = dotNode;
	}
	
	public LocalDotNode getDotNode() {
		return dotNode;
	}
}
