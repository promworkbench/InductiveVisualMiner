package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningEventFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class IvMPreMiningFiltersController {

	private final List<PreMiningFilter> preMiningFilters;
	private final IvMFiltersView filtersPanel;

	public IvMPreMiningFiltersController(List<PreMiningFilter> preMiningFilters, IvMFiltersView filtersPanel) {
		this.preMiningFilters = preMiningFilters;
		this.filtersPanel = filtersPanel;
	}

	/**
	 * Add the attributes to the filters
	 * 
	 * @param attributesInfo
	 */
	public void setAttributesInfo(AttributesInfo attributesInfo) {
		for (PreMiningFilter filter : preMiningFilters) {
			filter.setAttributesInfo(attributesInfo);
		}
		filtersPanel.getOnOffPanel().set(attributesInfo != null);
	}

	public boolean isAPreMiningFilterEnabled() {
		for (IvMFilter filter : preMiningFilters) {
			if (filter.isEnabledFilter()) {
				return true;
			}
		}
		return false;
	}

	public void applyPreMiningFilters(IMLog log, final IvMCanceller canceller) {
		//first, walk through the filters to see there is actually one enabled
		List<PreMiningTraceFilter> enabledTraceFilters = new ArrayList<>();
		List<PreMiningEventFilter> enabledEventFilters = new ArrayList<>();
		for (IvMFilter filter : preMiningFilters) {
			if (filter instanceof PreMiningTraceFilter && filter.isEnabledFilter()) {
				enabledTraceFilters.add((PreMiningTraceFilter) filter);
			}
			if (filter instanceof PreMiningEventFilter && filter.isEnabledFilter()) {
				enabledEventFilters.add((PreMiningEventFilter) filter);
			}
		}
		if (enabledTraceFilters.isEmpty() && enabledEventFilters.isEmpty()) {
			//no filter is enabled, just return
			return;
		}

		for (Iterator<IMTrace> it = log.iterator(); it.hasNext();) {
			IMTrace trace = it.next();

			//feed this trace to each enabled trace filter
			boolean removed = false;
			for (PreMiningTraceFilter filter : enabledTraceFilters) {
				if (!filter.staysInLog(trace)) {
					it.remove();
					removed = true;
					break;
				}
			}

			if (!removed) {
				for (Iterator<XEvent> it1 = trace.iterator(); it1.hasNext();) {
					XEvent event = it1.next();

					//feed this trace to each enabled event filter
					for (PreMiningEventFilter filter : enabledEventFilters) {
						if (!filter.staysInLog(event)) {
							it1.remove();
							break;
						}
					}

					if (canceller.isCancelled()) {
						return;
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