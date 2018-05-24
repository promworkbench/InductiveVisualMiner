package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.awt.Color;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;

public class Move implements Event {

	public enum Type {
		modelMove, logMove, synchronousMove, ignoredLogMove, ignoredModelMove
	}

	private final Type type;

	/**
	 * A modelNode can denote two things, depending on whether we're in tree or
	 * dfg mode:
	 * 
	 * For trees, modelNode is the node of the tree;
	 * 
	 * For dfgs, modelNode is the target of step in the dfg, and sourceNode is the source;
	 */
	private final int modelNode;
	private final int sourceNode;
	
	private final IvMModel model;
	private final XEventClass activityEventClass;
	private final XEventClass performanceEventClass;
	private final PerformanceTransition lifeCycleTransition;

	private int logMoveNode = -1;
	private int logMoveBeforeChildNode = -1;
	private int logMoveParallelBranchMappedToNode = -1;

	public Move(IvMModel model, Type type, int sourceNode, int node, XEventClass activityEventClass, XEventClass performanceEventClass,
			PerformanceTransition lifeCycle) {
		this.model = model;
		this.type = type;
		this.modelNode = node;
		this.sourceNode = sourceNode;
		this.activityEventClass = activityEventClass;
		this.performanceEventClass = performanceEventClass;
		this.lifeCycleTransition = lifeCycle;
	}

	public String toString() {
		if (isModelSync()) {
			if (!model.isTau(getTreeNode())) {
				return getType() + " " + model.getActivityName(getTreeNode()) + " " + lifeCycleTransition;
			} else {
				return getType() + " tau (" + getTreeNode() + ") " + lifeCycleTransition;
			}
		} else {
			return getType() + " " + getActivityEventClass().toString() + " " + lifeCycleTransition;
		}
	}

	@Override
	public int hashCode() {
		if (getTreeNode() != -1) {
			return getType().hashCode() ^ getTreeNode();
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
		if (((Move) obj).isComplete() != isComplete()) {
			return false;
		}
		if (getTreeNode() != -1) {
			return getTreeNode() == arg.getTreeNode();
		} else {
			return getActivityEventClass().equals(arg.getActivityEventClass());
		}
	}

	public boolean isModelSync() {
		return type == Type.modelMove || type == Type.synchronousMove || type == Type.ignoredModelMove;
	}

	public Type getType() {
		return type;
	}

	public int getTreeNode() {
		return modelNode;
	}

	public XEventClass getActivityEventClass() {
		return activityEventClass;
	}

	public XEventClass getPerformanceEventClass() {
		return performanceEventClass;
	}

	public int getLogMoveBeforeChild() {
		return logMoveBeforeChildNode;
	}

	public void setLogMovePosition(LogMovePosition logMovePosition) {
		this.logMoveNode = logMovePosition.getOn();
		this.logMoveBeforeChildNode = logMovePosition.getBeforeChild();
	}

	public int getLogMoveUnode() {
		return logMoveNode;
	}

	public int getPositionUnode() {
		if (modelNode != -1) {
			return modelNode;
		}
		if (logMoveNode != -1) {
			return logMoveNode;
		}
		return logMoveBeforeChildNode;
	}

	public boolean isLogMove() {
		return type == Type.logMove || type == Type.ignoredLogMove;
	}

	public boolean isModelMove() {
		return type == Type.modelMove || type == Type.ignoredModelMove;
	}

	public boolean isSyncMove() {
		return type == Type.synchronousMove || type == Type.ignoredModelMove;
	}

	public PerformanceTransition getLifeCycleTransition() {
		return lifeCycleTransition;
	}

	public boolean isStart() {
		return lifeCycleTransition == PerformanceTransition.start;
	}

	public boolean isComplete() {
		return lifeCycleTransition == PerformanceTransition.complete;
	}

	/**
	 * 
	 * @return whether this move is a missing start
	 */
	public boolean isTauStart() {
		return type == Type.ignoredModelMove;
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
	public int getLogMoveParallelBranchMappedTo() {
		return logMoveParallelBranchMappedToNode;
	}

	public void setLogMoveParallelBranchMappedTo(int logMoveParallelBranch) {
		this.logMoveParallelBranchMappedToNode = logMoveParallelBranch;
	}

	// ==== methods from the list view widget ====

	public String getLabel() {
		if (isModelMove()) {
			return model.getActivityName(modelNode);
		}

		if (isSyncMove() && model.isTau(modelNode)) {
			//tau
			return "";
		}

		return activityEventClass.toString();
	}

	public String getTopLabel() {
		return "";
	}

	public String getBottomLabel() {
		if (isSyncMove() && model.isTau(modelNode)) {
			return null;
		} else {
			return lifeCycleTransition.toString();
		}
	}

	public String getBottomLabel2() {
		if (isModelMove()) {
			return "only in model";
		} else if (isIgnoredLogMove()) {
			return "only in log; ignored";
		} else if (isLogMove()) {
			return "only in log";
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
		return IvMDecorator.backGroundColour2;
	}

	public Color getLabelColor() {
		return IvMDecorator.textColour;
	}

	public Color getTopLabelColor() {
		return IvMDecorator.textColour;
	}

	public Color getBottomLabelColor() {
		return IvMDecorator.textColour;
	}

	public Color getBottomLabel2Color() {
		if (isLogMove() || isModelMove()) {
			return Color.red;
		} else {
			return new Color(0.5f, 0.5f, 0.5f);
		}
	}

	public boolean isIgnoredModelMove() {
		return type == Type.ignoredModelMove;
	}

	public int getSourceNode() {
		return sourceNode;
	}
}
