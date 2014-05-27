package org.processmining.plugins.inductiveVisualMiner.animation.shortestPath;

import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

public class ShortestPathNodeEnd extends ShortestPathNode {
	public ShortestPathNodeEnd(LocalDotNode dotNode) {
		super(dotNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ShortestPathNodeEnd)) {
			return false;
		}
		return getDotNode().equals(((ShortestPathNode) obj).getDotNode());
	}
}
