package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningEventFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;

public class IvMPreMiningFiltersController {

	private final List<IvMFilter> preMiningFilters;

	/**
	 * Stage 1: set up the list of filters.
	 * 
	 * @param context
	 * @param panel
	 * @param state
	 * @param attributesInfo
	 */
	public IvMPreMiningFiltersController(List<IvMFilter> preMiningFilters, InductiveVisualMinerState state,
			InductiveVisualMinerPanel panel, final Runnable onPreMiningUpdate) {
		this.preMiningFilters = preMiningFilters;
		panel.getPreMiningFiltersView().initialise(preMiningFilters);
		for (IvMFilter filter : preMiningFilters) {
			filter.createFilterGui(onPreMiningUpdate, state.getAttributesInfo());
			panel.getPreMiningFiltersView().setPanel(filter, onPreMiningUpdate);
		}
	}

	/**
	 * Stage 2: update filters with an IMLog.
	 * 
	 * @param panel
	 * @param xLog
	 * @param attributesInfo
	 * @param executor
	 */
	public void updateFiltersWithIMLog(InductiveVisualMinerPanel panel, final IMLog log, final XLog xLog,
			Executor executor) {
		for (IvMFilter filter : preMiningFilters) {
			final IvMFilter filter2 = filter;
			executor.execute(new Runnable() {
				public void run() {
					try {
						filter2.fillGuiWithLog(log, xLog, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
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