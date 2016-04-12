package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Cl09FilterNodeSelection
		extends
		ChainLink<Quadruple<IvMLogNotFiltered, Selection, List<HighlightingFilter>, IvMLogInfo>, Pair<IvMLogFilteredImpl, IvMLogInfo>> {

	protected Quadruple<IvMLogNotFiltered, Selection, List<HighlightingFilter>, IvMLogInfo> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple
				.of(state.getIvMLog(), state.getSelection(), state.getColouringFilters(), state.getIvMLogInfo());
	}

	protected Pair<IvMLogFilteredImpl, IvMLogInfo> executeLink(
			Quadruple<IvMLogNotFiltered, Selection, List<HighlightingFilter>, IvMLogInfo> input,
			ChainLinkCanceller canceller) {

		IvMLogNotFiltered logBase = input.getA();
		Selection selection = input.getB();
		List<HighlightingFilter> colouringFilters = input.getC();
		IvMLogInfo oldLogInfo = input.getD();

		IvMLogFilteredImpl logFiltered = new IvMLogFilteredImpl(logBase);

		//apply the colouring filters
		applyColouringFilter(logFiltered, colouringFilters, canceller);

		//apply node/edge selection filters
		if (selection.isSomethingSelected()) {
			filterOnSelection(logFiltered, selection);
		}

		//create a log info
		IvMLogInfo resultLogInfo = oldLogInfo;
		if (logFiltered.isSomethingFiltered()) {
			resultLogInfo = new IvMLogInfo(logFiltered);
		}
		return Pair.of(logFiltered, resultLogInfo);
	}

	protected void processResult(Pair<IvMLogFilteredImpl, IvMLogInfo> result, InductiveVisualMinerState state) {
		state.setIvMLogFiltered(result.getA(), result.getB());
		state.setVisualisationData(state.getMode().getVisualisationData(state));
	}

	public static void applyColouringFilter(IvMLogFilteredImpl log, List<HighlightingFilter> filters,
			final Canceller canceller) {
		//first, walk through the filters to see there is actually one enabled
		List<HighlightingFilter> enabledColouringFilters = new LinkedList<>();
		for (HighlightingFilter filter : filters) {
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
			for (HighlightingFilter filter : enabledColouringFilters) {
				if (!filter.countInColouring(trace)) {
					it.remove();
					break;
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
	private static void filterOnSelection(IvMLogFilteredImpl log, Selection selection) {

		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			boolean keepTrace = false;
			for (IvMMove move : trace) {
				if (selection.isSelected(move)) {
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