package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Arrays;
import java.util.List;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;

public class DirectlyFollowsModel2CausalDataTable {

	//intermediate state variables
	private static class State {
		DirectlyFollowsModelStepsWalk walker;
		int[] currentRow;
		int unfolding;
		TObjectIntMap<Choice> choice2column;
		TObjectIntMap<TIntSet> steps2rank;
	}

	public static CausalDataTable create(DirectlyFollowsModel dfm, IvMLogFiltered log, List<Choice> choices,
			final TIntObjectMap<TIntSet> node2steps, TObjectIntMap<TIntSet> steps2rank, final int maxUnfolding,
			IvMCanceller canceller) {
		CausalDataTable result = new CausalDataTable(choices);

		//initialise intermediate state variables
		final State state = new State();
		{
			state.steps2rank = steps2rank;

			//create map to find columns by choices
			state.choice2column = new TObjectIntHashMap<>(10, 0.5f, DirectlyFollowsModel2UpperBoundCausalGraph.NO_NODE);
			int i = 0;
			for (Choice choice : choices) {
				state.choice2column.put(choice, i);
				i++;
			}
		}

		state.walker = new DirectlyFollowsModelStepsWalk(dfm, node2steps, canceller) {
			public void stepsEncountered(TIntSet currentSteps, int chosenNode, TIntSet newSteps) {
				Choice choice = DirectlyFollowsModel2UpperBoundCausalGraph.getChoice(currentSteps, state.unfolding);
				if (state.unfolding < maxUnfolding) {
					reportChoice(state, choice, chosenNode);
				}

				//update the unfolding if necessary
				if (!newSteps.isEmpty() && state.steps2rank.get(currentSteps) >= state.steps2rank.get(newSteps)) {
					/**
					 * This moves back in the ranking of the steps. Move to the
					 * next unfolding.
					 */
					state.unfolding++;
				}
			}
		};

		//walk through traces
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			state.currentRow = new int[choices.size()];
			Arrays.fill(state.currentRow, CausalDataTable.NO_VALUE);
			state.unfolding = 0;

			IvMTrace trace = it.next();

			if (trace.isEmpty()) {
				Choice choice = DirectlyFollowsModel2UpperBoundCausalGraph
						.getChoice(node2steps.get(DirectlyFollowsModel2UpperBoundCausalGraph.START_NODE), 0);
				reportChoice(state, choice, DirectlyFollowsModel2UpperBoundCausalGraph.END_NODE);
			} else {
				state.walker.walk(trace);
			}

			result.addRow(state.currentRow);

			if (canceller.isCancelled()) {
				return null;
			}
		}
		return result;
	}

	private static void reportChoice(State state, Choice choice, int chosenNode) {

		/**
		 * Ensure that the node we chose can actually be chosen.
		 */
		assert choice.nodes.contains(chosenNode);

		int columnNumber = state.choice2column.get(choice);
		if (columnNumber >= 0) {
			/**
			 * Ensure each choice is made only once (should be guaranteed by
			 * unfolding).
			 */
			assert state.currentRow[columnNumber] < 0;

			state.currentRow[columnNumber] = chosenNode;
		} else {
			/**
			 * The choice was not a column, which means that a loop was not
			 * unfolded far enough; do nothing.
			 */
		}
	}
}