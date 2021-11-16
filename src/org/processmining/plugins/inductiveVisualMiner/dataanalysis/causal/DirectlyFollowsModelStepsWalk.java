package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.DirectlyFollowsModelWalk;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public abstract class DirectlyFollowsModelStepsWalk {

	private TIntSet currentSteps;
	private final TIntObjectMap<TIntSet> node2steps;
	private final DirectlyFollowsModelWalk walk;
	private final DirectlyFollowsModel dfm;

	public DirectlyFollowsModelStepsWalk(final DirectlyFollowsModel dfm, final TIntObjectMap<TIntSet> node2steps) {
		this.dfm = dfm;
		this.node2steps = node2steps;

		walk = new DirectlyFollowsModelWalk() {
			public void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex) {
				if (currentSteps != null && currentSteps.contains(node)) {
					//this event is in the current choice, so it's a new choice
					TIntSet newSteps = node2steps.get(node);

					stepsEncountered(currentSteps, node, newSteps);

					currentSteps = newSteps;
				}
			}

			public void nodeEntered(IvMTrace trace, int node, int eventIndex) {

			}

			public void emptyTraceExecuted(IvMTrace trace) {
				stepsEncountered(currentSteps, DirectlyFollowsModel2CausalGraph.END_NODE,
						new TIntHashSet(0, 0.5f, DirectlyFollowsModel2CausalGraph.NO_NODE));
			}
		};
	}

	public void walk(IvMTrace trace) {
		//trace starts with initial choice
		currentSteps = node2steps.get(DirectlyFollowsModel2CausalGraph.START_NODE);

		walk.walk(dfm, trace);
	}

	/**
	 * We have encountered a node execution that:
	 * 
	 * @param currentSteps
	 *            resulted from this choice
	 * @param chosenNode
	 *            was this node
	 * @param newSteps
	 *            enabled this choice next
	 */
	public abstract void stepsEncountered(TIntSet currentSteps, int chosenNode, TIntSet newSteps);
}