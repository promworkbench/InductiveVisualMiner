package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import java.util.ArrayList;

import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.EdgeType;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class LocalDotEdge extends DotEdge {

	/**
	 * 
	 */
	private final AlignedLogVisualisation alignedLogVisualisation;
	private final EdgeType type;
	private final UnfoldedNode unode;
	private final UnfoldedNode lookupNode1;
	private final UnfoldedNode lookupNode2;
	private final boolean directionForward;

	//constructor for model edge
	public LocalDotEdge(AlignedLogVisualisation alignedLogVisualisation, LocalDotNode source, LocalDotNode target, String label, String options, UnfoldedNode unode, boolean directionForward) {
		super(source, target, label, options);
		this.alignedLogVisualisation = alignedLogVisualisation;
		this.alignedLogVisualisation.dot.addEdge(this);
		this.unode = unode;
		this.lookupNode1 = null;
		this.lookupNode2 = null;
		this.type = EdgeType.model;
		this.directionForward = directionForward;

		if (!this.alignedLogVisualisation.unfoldedNode2dotEdgesModel.containsKey(unode)) {
			this.alignedLogVisualisation.unfoldedNode2dotEdgesModel.put(unode, new ArrayList<LocalDotEdge>());
		}
		this.alignedLogVisualisation.unfoldedNode2dotEdgesModel.get(unode).add(this);
		this.alignedLogVisualisation.dotEdges.add(this);
	}

	public LocalDotEdge(AlignedLogVisualisation alignedLogVisualisation, LocalDotNode source, LocalDotNode target, String label, String options, UnfoldedNode unode,
			EdgeType type, UnfoldedNode lookupNode1, UnfoldedNode lookupNode2, boolean directionForward) {
		super(source, target, label, options);
		this.alignedLogVisualisation = alignedLogVisualisation;
		this.alignedLogVisualisation.dot.addEdge(this);
		this.unode = unode;
		this.lookupNode1 = lookupNode1;
		this.lookupNode2 = lookupNode2;
		this.type = type;
		this.directionForward = directionForward;

		if (!this.alignedLogVisualisation.unfoldedNode2dotEdgesMove.containsKey(unode)) {
			this.alignedLogVisualisation.unfoldedNode2dotEdgesMove.put(unode, new ArrayList<LocalDotEdge>());
		}
		this.alignedLogVisualisation.unfoldedNode2dotEdgesMove.get(unode).add(this);
		this.alignedLogVisualisation.dotEdges.add(this);
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