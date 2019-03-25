package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.util.HashMap;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;

public class LocalDotEdge extends DotEdge {

	public enum EdgeType {
		model, logMove, modelMove
	};

	public class Appearance {
		public String textFill;
		public String textStroke;
		public String textStrokeWidth;
		public String lineStrokeDashArray;
	}

	private final EdgeType type;
	private final DFMEdgeType dfmType;
	private final int node;
	private final int lookupNode1;
	private final int lookupNode2;
	private final boolean directionForward;
	public final Appearance unselectedAppearance = new Appearance();

	public LocalDotEdge(EfficientTree tree, Dot dot, ProcessTreeVisualisationInfo info, LocalDotNode source,
			LocalDotNode target, String label, int node, EdgeType type, DFMEdgeType dfmType, int lookupNode1, int lookupNode2,
			boolean directionForward) {
		super(source, target, label, new HashMap<String, String>());
		this.node = node;
		this.lookupNode1 = lookupNode1;
		this.lookupNode2 = lookupNode2;
		this.type = type;
		this.dfmType = dfmType;
		this.directionForward = directionForward;

		dot.addEdge(this);
		if (lookupNode1 == -1 && lookupNode2 == -1) {
			info.addEdge(tree, node, -1, this);
		} else {
			info.addEdge(tree, lookupNode1, lookupNode2, this);
		}
	}

	public LocalDotNode getTarget() {
		if (directionForward) {
			return (LocalDotNode) super.getTarget();
		} else {
			return (LocalDotNode) super.getSource();
		}
	}

	public LocalDotNode getSource() {
		if (directionForward) {
			return (LocalDotNode) super.getSource();
		} else {
			return (LocalDotNode) super.getTarget();
		}
	}

	public EdgeType getType() {
		return type;
	}
	
	public DFMEdgeType getDfmType() {
		return dfmType;
	}

	public int getUnode() {
		return node;
	}

	public boolean isDirectionForward() {
		return directionForward;
	}

	public int getLookupNode1() {
		return lookupNode1;
	}

	public int getLookupNode2() {
		return lookupNode2;
	}
}