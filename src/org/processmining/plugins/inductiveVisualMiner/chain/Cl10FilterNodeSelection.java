package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Cl10FilterNodeSelection
		extends
		ChainLink<Quintuple<IvMLogNotFiltered, Selection, List<IvMFilter>, IvMLogInfo, IvMEfficientTree>, Pair<IvMLogFilteredImpl, IvMLogInfo>> {

	protected Quintuple<IvMLogNotFiltered, Selection, List<IvMFilter>, IvMLogInfo, IvMEfficientTree> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getIvMLog(), state.getSelection(), state.getColouringFilters(),
				state.getIvMLogInfo(), state.getTree());
	}

	protected Pair<IvMLogFilteredImpl, IvMLogInfo> executeLink(
			Quintuple<IvMLogNotFiltered, Selection, List<IvMFilter>, IvMLogInfo, IvMEfficientTree> input,
			IvMCanceller canceller) {

		IvMLogNotFiltered logBase = input.getA();
		Selection selection = input.getB();
		List<IvMFilter> colouringFilters = input.getC();
		IvMLogInfo oldLogInfo = input.getD();
		IvMEfficientTree tree = input.getE();

		IvMLogFilteredImpl logFiltered = new IvMLogFilteredImpl(logBase);

		//apply the colouring filters
		applyColouringFilter(logFiltered, colouringFilters, canceller);

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

	public static void applyColouringFilter(IvMLogFilteredImpl log, List<IvMFilter> filters,
			final IvMCanceller canceller) {
		//first, walk through the filters to see there is actually one enabled
		List<IvMFilter> enabledColouringFilters = new ArrayList<>();
		for (IvMFilter filter : filters) {
			if (filter.isEnabledFilter()) {
				enabledColouringFilters.add(filter);
			}
		}
		if (enabledColouringFilters.isEmpty()) {
			//no filter is enabled, just return
			return;
		}

		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			//feed this trace to each enabled filter
			for (IvMFilter filter : enabledColouringFilters) {
				if (filter instanceof HighlightingFilter) {
					if (!((HighlightingFilter) filter).countInColouring(trace)) {
						it.remove();
						break;
					}
				}
			}

			if (canceller.isCancelled()) {
				return;
			}
		}

		return;
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