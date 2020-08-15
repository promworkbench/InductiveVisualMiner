package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class IvMHighlightingFiltersController {

	private final List<IvMFilter> highlightingFilters;

	/**
	 * Stage 1: set up the list of filters.
	 * 
	 * @param context
	 * @param panel
	 * @param state
	 * @param attributesInfo
	 */
	public IvMHighlightingFiltersController(List<IvMFilter> highlightingFilters, InductiveVisualMinerState state,
			InductiveVisualMinerPanel panel, final Runnable onHighlightingUpdate) {
		this.highlightingFilters = highlightingFilters;
		//filter meta-filters
		for (Iterator<IvMFilter> it = highlightingFilters.iterator(); it.hasNext();) {
			if (!(it.next() instanceof HighlightingFilter)) {
				it.remove();
			}
		}

		panel.getColouringFiltersView().initialise(highlightingFilters);
		for (IvMFilter filter : highlightingFilters) {
			filter.createFilterGui(onHighlightingUpdate, state.getAttributesInfoIvM());
			panel.getColouringFiltersView().setPanel(filter, onHighlightingUpdate);
		}
	}

	public void updateFiltersWithIvMLog(InductiveVisualMinerPanel panel, final IvMLogNotFiltered ivmLog,
			Executor executor) {
		for (IvMFilter filter : highlightingFilters) {
			final IvMFilter filter2 = filter;
			executor.execute(new Runnable() {
				public void run() {
					try {
						filter2.fillGuiWithLog(null, null, ivmLog);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public boolean isAHighlightingFilterEnabled() {
		for (IvMFilter filter : highlightingFilters) {
			if (filter.isEnabledFilter()) {
				return true;
			}
		}
		return false;
	}

	public void applyHighlightingFilters(IvMLogFilteredImpl log, final IvMCanceller canceller) {
		//first, walk through the filters to see there is actually one enabled
		List<IvMFilter> enabledColouringFilters = new ArrayList<>();
		for (IvMFilter filter : highlightingFilters) {
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
}
