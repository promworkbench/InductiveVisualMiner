package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Cl12FilterNodeSelection extends
		ChainLink<Quintuple<IvMLogNotFiltered, Selection, IvMFiltersController, IvMLogInfo, IvMModel>, Pair<IvMLogFilteredImpl, IvMLogInfo>> {

	protected Quintuple<IvMLogNotFiltered, Selection, IvMFiltersController, IvMLogInfo, IvMModel> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getIvMLog(), state.getSelection(), state.getFiltersController(),
				state.getIvMLogInfo(), state.getModel());
	}

	protected Pair<IvMLogFilteredImpl, IvMLogInfo> executeLink(
			Quintuple<IvMLogNotFiltered, Selection, IvMFiltersController, IvMLogInfo, IvMModel> input,
			IvMCanceller canceller) {

		IvMLogNotFiltered logBase = input.getA();
		Selection selection = input.getB();
		IvMFiltersController highLightingFilters = input.getC();
		IvMLogInfo oldLogInfo = input.getD();
		IvMModel model = input.getE();

		IvMLogFilteredImpl logFiltered = new IvMLogFilteredImpl(logBase);

		//apply the colouring filters
		highLightingFilters.applyHighlightingFilters(logFiltered, canceller);

		//apply node/edge selection filters
		if (selection.isSomethingSelected()) {
			filterOnSelection(model, logFiltered, selection);
		}

		//create a log info
		IvMLogInfo resultLogInfo = oldLogInfo;
		if (logFiltered.isSomethingFiltered()) {
			resultLogInfo = new IvMLogInfo(logFiltered, model);
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
	@SuppressWarnings("unchecked")
	private static void filterOnSelection(IvMModel tree, IvMLogFilteredImpl log, Selection selection) {

		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			boolean keepTrace = false;
			for (IvMMove move : trace) {
				List<? extends Move> trace2 = trace;
				if (selection.isSelected(tree, (List<Move>) trace2, move)) {
					keepTrace = true;
					selection.isSelected(tree, (List<Move>) trace2, move);
					break;
				}
			}

			if (!keepTrace) {
				it.remove();
			}

		}
	}

	public String getName() {
		return "highlight selection";
	}
}
