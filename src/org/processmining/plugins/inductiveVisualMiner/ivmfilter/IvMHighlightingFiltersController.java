package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class IvMHighlightingFiltersController {

	private final List<HighlightingFilter> highlightingFilters;
	private final IvMFiltersView filtersPanel;

	public IvMHighlightingFiltersController(List<HighlightingFilter> highlightingFilters, IvMFiltersView filtersPanel) {
		this.highlightingFilters = highlightingFilters;
		this.filtersPanel = filtersPanel;
	}

	/**
	 * Add the attributes to the filters
	 * 
	 * @param attributesInfo
	 */
	public void setAttributesInfo(IvMAttributesInfo attributesInfo) {
		for (HighlightingFilter filter : highlightingFilters) {
			filter.setAttributesInfo(attributesInfo);
		}
		filtersPanel.getOnOffPanel().set(attributesInfo != null);
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
					if (!((HighlightingFilter) filter).staysInLog(trace)) {
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
