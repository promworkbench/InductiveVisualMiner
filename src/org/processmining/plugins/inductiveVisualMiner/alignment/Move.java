package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Move {

	public enum Type {
		model, log, synchronous
	}

	private final Type type;
	private final UnfoldedNode unode;
	private final XEventClass eventClass;

	private UnfoldedNode logMoveUnode;
	private UnfoldedNode logMoveBeforeChild;
	private UnfoldedNode logMoveParallelBranchMappedTo;

	public Move(Type type, UnfoldedNode unode, XEventClass eventClass) {
		this.type = type;
		this.unode = unode;
		this.eventClass = eventClass;
	}

	public String toString() {
		if (isModelSync()) {
			return getType() + " " + getUnode().toString();
		} else {
//			return getType() + " " + getEventClass().toString() + " " + getLogMoveUnode() + " "
//					+ getLogMoveBeforeChild();
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

	public boolean isModelSync() {
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

	public UnfoldedNode getLogMoveBeforeChild() {
		return logMoveBeforeChild;
	}

	public void setLogMove(UnfoldedNode logMoveUnode, UnfoldedNode logMoveBeforeChild) {
		this.logMoveUnode = logMoveUnode;
		this.logMoveBeforeChild = logMoveBeforeChild;
	}

	public UnfoldedNode getLogMoveUnode() {
		return logMoveUnode;
	}

	public UnfoldedNode getPositionUnode() {
		if (unode != null) {
			return unode;
		}
		if (logMoveUnode != null) {
			return logMoveUnode;
		}
		return logMoveBeforeChild;
	}

	public boolean isLogMove() {
		return type == Type.log;
	}

	/**
	 * Returns the last known unode in the trace before this log move.
	 * This is used in log splitting, to make sure that the log move ends up in the correct sub trace. 
	 * @return
	 */
	public UnfoldedNode getLogMoveParallelBranchMappedTo() {
		return logMoveParallelBranchMappedTo;
	}

	public void setLogMoveParallelBranchMappedTo(UnfoldedNode logMoveParallelBranch) {
		this.logMoveParallelBranchMappedTo = logMoveParallelBranch;
	}
}
