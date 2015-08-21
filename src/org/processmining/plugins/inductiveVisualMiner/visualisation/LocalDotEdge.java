package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.util.HashMap;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

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
	private final UnfoldedNode unode;
	private final UnfoldedNode lookupNode1;
	private final UnfoldedNode lookupNode2;
	private final boolean directionForward;
	public final Appearance unselectedAppearance = new Appearance();

	public LocalDotEdge(Dot dot, ProcessTreeVisualisationInfo info, LocalDotNode source, LocalDotNode target,
			String label, UnfoldedNode unode, EdgeType type, UnfoldedNode lookupNode1,
			UnfoldedNode lookupNode2, boolean directionForward) {
		super(source, target, label, new HashMap<String, String>());
		this.unode = unode;
		this.lookupNode1 = lookupNode1;
		this.lookupNode2 = lookupNode2;
		this.type = type;
		this.directionForward = directionForward;
		
		dot.addEdge(this);
		if (lookupNode1 == null && lookupNode2 == null) {
			info.addEdge(unode, null, this);
		} else {
			info.addEdge(lookupNode1, lookupNode2, this);
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

	public UnfoldedNode getUnode() {
		return unode;
	}

	public boolean isDirectionForward() {
		return directionForward;
	}

	public UnfoldedNode getLookupNode1() {
		return lookupNode1;
	}

	public UnfoldedNode getLookupNode2() {
		return lookupNode2;
	}
}