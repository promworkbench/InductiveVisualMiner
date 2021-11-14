package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.DirectlyFollowsModelWalk;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;

public abstract class DirectlyFollowsModelStepsWalk {

	private TIntList currentSteps;
	private final TIntObjectMap<TIntList> node2steps;
	private final DirectlyFollowsModelWalk walk;
	private final DirectlyFollowsModel dfm;

	public DirectlyFollowsModelStepsWalk(final DirectlyFollowsModel dfm, final TIntObjectMap<TIntList> node2steps) {
		this.dfm = dfm;
		this.node2steps = node2steps;

		walk = new DirectlyFollowsModelWalk() {
			public void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex) {
				if (currentSteps.contains(node)) {
					//this event is in the current choice, so it's a new choice
					TIntList newSteps = node2steps.get(node);

					stepsEncountered(currentSteps, node, newSteps);

					currentSteps = newSteps;
				}
			}

			public void nodeEntered(IvMTrace trace, int node, int eventIndex) {

			}

			public void emptyTraceExecuted(IvMTrace trace) {

			}
		};
	}

	public void walk(IvMTrace trace) {
		//trace starts with initial choice
		currentSteps = node2steps.get(-1);

		walk.walk(dfm, trace);
	}

	/**
	 * We have encountered a node execution that:
	 * 
	 * @param steps
	 *            resulted from this choice
	 * @param chosenNode
	 *            was this node
	 * @param nextSteps
	 *            enabled this choice next
	 */
	public abstract void stepsEncountered(TIntList steps, int chosenNode, TIntList nextSteps);
}