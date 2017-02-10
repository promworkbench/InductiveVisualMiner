package org.processmining.plugins.inductiveVisualMiner.chain2;

import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Cl12FilterNodeSelection extends
		ChainLink2<Quintuple<IvMLogNotFiltered, Selection, IvMFiltersController, IvMLogInfo, IvMEfficientTree>, Pair<IvMLogFilteredImpl, IvMLogInfo>> {

	protected Quintuple<IvMLogNotFiltered, Selection, IvMFiltersController, IvMLogInfo, IvMEfficientTree> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getIvMLog(), state.getSelection(), state.getFiltersController(),
				state.getIvMLogInfo(), state.getTree());
	}

	protected Pair<IvMLogFilteredImpl, IvMLogInfo> executeLink(
			Quintuple<IvMLogNotFiltered, Selection, IvMFiltersController, IvMLogInfo, IvMEfficientTree> input,
			IvMCanceller canceller) {

		IvMLogNotFiltered logBase = input.getA();
		Selection selection = input.getB();
		IvMFiltersController highLightingFilters = input.getC();
		IvMLogInfo oldLogInfo = input.getD();
		IvMEfficientTree tree = input.getE();

		IvMLogFilteredImpl logFiltered = new IvMLogFilteredImpl(logBase);

		//apply the colouring filters
		highLightingFilters.applyHighlightingFilters(logFiltered, canceller);

		//apply node/edge selection filters
		if (selection.isSomethingSelected()) {
			filterOnSelection(tree, logFiltered, selection);
		}

		//create a log info
		IvMLogInfo resultLogInfo = oldLogInfo;
		if (logFiltered.isSomethingFiltered()) {
			resultLogInfo = new IvMLogInfo(logFiltered, tree);
		}
		return Pair.of(logFiltered, resultLogInfo);
	}

	protected void processResult(Pair<IvMLogFilteredImpl, IvMLogInfo> result, InductiveVisualMinerState state) {
		state.setIvMLogFiltered(result.getA(), result.getB());
		state.setVisualisationData(state.getMode().getVisualisationData(state));
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		if (state.getIvMLog() != null) {
			state.setIvMLogFiltered(new IvMLogFilteredImpl(state.getIvMLog()), state.getIvMLogInfo());
		} else {
			state.setIvMLogFiltered(null, null);
		}
		state.setVisualisationData(null);
	}

	/**
	 * Filter the log: keep all traces that have a selected node/move.
	 * 
	 * @param log
	 * @param selectedNodes
	 * @param selectedLogMoves
	 */
	private static void filterOnSelection(IvMEfficientTree tree, IvMLogFilteredImpl log, Selection selection) {

		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			boolean keepTrace = false;
			for (IvMMove move : trace) {
				if (selection.isSelected(tree, move)) {
					keepTrace = true;
					break;
				}
			}

			if (!keepTrace) {
				it.remove();
			}

		}
	}
}
