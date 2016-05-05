package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;

public class LogMovePosition {
	private final int on;
	private final int beforeChild;

	/**
	 * Create a log move position. (null, unode) = at source; (unode, null) = at
	 * sink; (unode1, unode2) = at unode1, before child unode2; (unode, unode) =
	 * on leaf unode.
	 * 
	 * @param on
	 * @param beforeChild
	 */
	private LogMovePosition(int on, int beforeChild) {
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
	public static LogMovePosition beforeChild(int unode, int child) {
		return new LogMovePosition(unode, child);
	}

	public static LogMovePosition onLeaf(int unode) {
		return new LogMovePosition(unode, unode);
	}

	/**
	 * Creates a log move position in which the log move happens before unode.
	 * 
	 * @param unode
	 * @return
	 */
	public static LogMovePosition atSource(int unode) {
		return new LogMovePosition(-1, unode);
	}

	/**
	 * Creates a log move position in which the log move happens after unode.
	 * 
	 * @param unode
	 * @return
	 */
	public static LogMovePosition atSink(int unode) {
		return new LogMovePosition(unode, -1);
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

	public int getOn() {
		return on;
	}

	public int getBeforeChild() {
		return beforeChild;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + on;
		result = prime * result + beforeChild;
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
		if (on == -1) {
			if (other.on != -1)
				return false;
		} else if (on != other.on)
			return false;
		if (beforeChild == -1) {
			if (other.beforeChild != -1)
				return false;
		} else if (beforeChild != other.beforeChild)
			return false;
		return true;
	}

}
