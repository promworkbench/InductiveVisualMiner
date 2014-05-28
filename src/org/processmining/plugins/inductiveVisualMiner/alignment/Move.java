package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class Move {

	public enum Type {
		model, log, synchronous
	}

	private final Type type;
	private final UnfoldedNode unode;
	private final XEventClass eventClass;

	public Move(Type type, UnfoldedNode unode) {
		this.type = type;
		this.unode = unode;
		this.eventClass = null;
	}

	public Move(Type type, UnfoldedNode unode, XEventClass eventClass) {
		this.type = type;
		this.unode = unode;
		this.eventClass = eventClass;
	}

	public Move(XEventClass eventClass, UnfoldedNode unode) {
		this.unode = unode;
		this.eventClass = eventClass;
		if ((unode != null && eventClass != null) || (unode != null && unode.getNode() instanceof Automatic)) {
			this.type = Type.synchronous;
		} else if (unode != null) {
			this.type = Type.model;
		} else {
			this.type = Type.log;
		}
	}

	public String toString() {
		if (getUnode() != null) {
			return getType() + " " + getUnode().toString();
		} else {
			return getType() + " " + getEventClass().toString();
		}
	}

	@Override
	public int hashCode() {
		if (getUnode() != null) {
			return getType().hashCode() ^ getUnode().hashCode();
		} else {
			return getType().hashCode() ^ getEventClass().hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Move)) {
			return false;
		}
		Move arg = (Move) obj;
		if (!getType().equals(arg.getType())) {
			return false;
		}
		if (getUnode() != null) {
			return getUnode().equals(arg.getUnode());
		} else {
			return getEventClass().equals(arg.getEventClass());
		}
	}

	public boolean isMoveSync() {
		return type == Type.model || type == Type.synchronous;
	}

	public Type getType() {
		return type;
	}

	public UnfoldedNode getUnode() {
		return unode;
	}

	public XEventClass getEventClass() {
		return eventClass;
	}
}
