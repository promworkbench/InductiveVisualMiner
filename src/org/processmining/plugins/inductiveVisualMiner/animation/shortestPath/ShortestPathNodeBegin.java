package org.processmining.plugins.inductiveVisualMiner.animation.shortestPath;

import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;


public class ShortestPathNodeBegin extends ShortestPathNode {

	public ShortestPathNodeBegin(LocalDotNode dotNode) {
		super(dotNode);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ShortestPathNodeBegin)) {
			return false;
		}
		return getDotNode().equals(((ShortestPathNode) obj).getDotNode());
	}
}
