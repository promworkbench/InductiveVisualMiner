package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class LogMovePosition {
	private final UnfoldedNode on;
	private final UnfoldedNode beforeChild;

	/**
	 * Create a log move position. (null, unode) = at source; (unode, null) = at
	 * sink; (unode1, unode2) = at unode1, before child unode2; (unode, unode) = on leaf unode.
	 * 
	 * @param on
	 * @param beforeChild
	 */
	private LogMovePosition(UnfoldedNode on, UnfoldedNode beforeChild) {
		this.on = on;
		this.beforeChild = beforeChild;
	}

	/**
	 * Returns a log move position in which the log move happens just before the
	 * given child of unode.
	 * 
	 * @param unode
	 * @param child
	 * @return
	 */
	public static LogMovePosition beforeChild(UnfoldedNode unode, UnfoldedNode child) {
		return new LogMovePosition(unode, child);
	}
	
	public static LogMovePosition onLeaf(UnfoldedNode unode) {
		return new LogMovePosition(unode, unode);
	}

	/**
	 * Creates a log move position in which the log move happens before unode.
	 * 
	 * @param unode
	 * @return
	 */
	public static LogMovePosition atSource(UnfoldedNode unode) {
		return new LogMovePosition(null, unode);
	}

	/**
	 * Creates a log move position in which the log move happens after unode.
	 * 
	 * @param unode
	 * @return
	 */
	public static LogMovePosition atSink(UnfoldedNode unode) {
		return new LogMovePosition(unode, null);
	}

	/**
	 * Retrieves the log move position of a LocalDotEdge that belongs to a log
	 * move.
	 * 
	 * @param edge
	 * @return
	 */
	public static LogMovePosition of(LocalDotEdge edge) {
		return new LogMovePosition(edge.getLookupNode1(), edge.getLookupNode2());
	}

	public static LogMovePosition of(Move move) {
		if (!move.isLogMove()) {
			return null;
		}
		return new LogMovePosition(move.getLogMoveUnode(), move.getLogMoveBeforeChild());
	}

	public UnfoldedNode getOn() {
		return on;
	}

	public UnfoldedNode getBeforeChild() {
		return beforeChild;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((on == null) ? 0 : on.hashCode());
		result = prime * result + ((beforeChild == null) ? 0 : beforeChild.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogMovePosition other = (LogMovePosition) obj;
		if (on == null) {
			if (other.on != null)
				return false;
		} else if (!on.equals(other.on))
			return false;
		if (beforeChild == null) {
			if (other.beforeChild != null)
				return false;
		} else if (!beforeChild.equals(other.beforeChild))
			return false;
		return true;
	}

}
