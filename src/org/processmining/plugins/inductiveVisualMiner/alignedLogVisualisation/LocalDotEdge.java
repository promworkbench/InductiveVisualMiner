package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class LocalDotEdge extends DotEdge {

	public enum EdgeType {
		model, logMove, modelMove
	};
	
	private final EdgeType type;
	private final UnfoldedNode unode;
	private final UnfoldedNode lookupNode1;
	private final UnfoldedNode lookupNode2;
	private final boolean directionForward;

	public LocalDotEdge(Dot dot, AlignedLogVisualisationInfo info, LocalDotNode source, LocalDotNode target,
			String label, String options, UnfoldedNode unode, EdgeType type, UnfoldedNode lookupNode1,
			UnfoldedNode lookupNode2, boolean directionForward) {
		super(source, target, label, options);
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