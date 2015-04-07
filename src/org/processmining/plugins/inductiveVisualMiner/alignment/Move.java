package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.awt.Color;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Move implements Event {

	public enum Type {
		model, log, synchronous, ignoredLogMove, tauStart
	}

	private final Type type;
	private final UnfoldedNode unode;
	private final XEventClass activityEventClass;
	private final XEventClass performanceEventClass;
	private final boolean start;

	private UnfoldedNode logMoveUnode;
	private UnfoldedNode logMoveBeforeChild;
	private UnfoldedNode logMoveParallelBranchMappedTo;

	public Move(Type type, UnfoldedNode unode, XEventClass activityEventClass, XEventClass performanceEventClass,
			boolean start) {
		this.type = type;
		this.unode = unode;
		this.activityEventClass = activityEventClass;
		this.performanceEventClass = performanceEventClass;
		this.start = start;
	}

	public String toString() {
		if (isModelSync()) {
			return getType() + " " + getUnode().toString() + " " + (start ? "start" : "complete");
		} else {
			//			return getType() + " " + getEventClass().toString() + " " + getLogMoveUnode() + " "
			//					+ getLogMoveBeforeChild();
			return getType() + " " + getActivityEventClass().toString() + " " + (start ? "start" : "complete");
		}
	}

	@Override
	public int hashCode() {
		if (getUnode() != null) {
			return getType().hashCode() ^ getUnode().hashCode();
		} else {
			return getType().hashCode() ^ getActivityEventClass().hashCode();
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
		if (((Move) obj).isStart() != isStart()) {
			return false;
		}
		if (getUnode() != null) {
			return getUnode().equals(arg.getUnode());
		} else {
			return getActivityEventClass().equals(arg.getActivityEventClass());
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

	public XEventClass getActivityEventClass() {
		return activityEventClass;
	}

	public XEventClass getPerformanceEventClass() {
		return performanceEventClass;
	}

	public UnfoldedNode getLogMoveBeforeChild() {
		return logMoveBeforeChild;
	}

	public void setLogMove(LogMovePosition logMovePosition) {
		this.logMoveUnode = logMovePosition.getOn();
		this.logMoveBeforeChild = logMovePosition.getBeforeChild();
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

	public boolean isModelMove() {
		return type == Type.model;
	}

	public boolean isSyncMove() {
		return type == Type.synchronous;
	}

	public boolean isStart() {
		return start;
	}

	public boolean isComplete() {
		return !isStart();
	}
	
	/**
	 * 
	 * @return whether this move is a missing start
	 */
	public boolean isTauStart() {
		return type == Type.tauStart;
	}
	
	public boolean isIgnoredLogMove() {
		return type == Type.ignoredLogMove;
	}

	/**
	 * Returns the last known unode in the trace before this log move. This is
	 * used in log splitting, to make sure that the log move ends up in the
	 * correct sub trace.
	 * 
	 * @return
	 */
	public UnfoldedNode getLogMoveParallelBranchMappedTo() {
		return logMoveParallelBranchMappedTo;
	}

	public void setLogMoveParallelBranchMappedTo(UnfoldedNode logMoveParallelBranch) {
		this.logMoveParallelBranchMappedTo = logMoveParallelBranch;
	}

	//methods from the list view widget
	public String getLabel() {
		if (isModelMove()) {
			return unode.toString();
		}

		if (isSyncMove() && unode.getNode() instanceof Automatic) {
			//tau
			return null;
		}

		return activityEventClass.toString();
	}

	public String getTopLabel() {
		return "";
	}

	public String getBottomLabel() {
		return isStart() ? "start" : "complete";
	}

	public String getBottomLabel2() {
		if (isModelMove()) {
			return "only in model";
		} else if (isLogMove()) {
			return "only in log";
		} else if (isIgnoredLogMove()) {
			return "only in log; ignored";
		}
		return null;
	}

	public Color getWedgeColor() {
		if (isSyncMove() || isIgnoredLogMove()) {
			return new Color(0.5f, 0.5f, 0.5f);
		} else {
			return new Color(255, 0, 0);
		}
	}

	public Color getBorderColor() {
		return null;
	}

	public Color getLabelColor() {
		return null;
	}

	public Color getTopLabelColor() {
		return Color.white;
	}

	public Color getBottomLabelColor() {
		return null;
	}

	public Color getBottomLabel2Color() {
		if (isLogMove() || isModelMove()) {
			return Color.red;
		} else {
			return new Color(0.5f, 0.5f, 0.5f);
		}
	}
}
