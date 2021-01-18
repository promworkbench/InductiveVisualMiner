package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMHighlightingFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class Cl13FilterNodeSelection extends DataChainLinkComputationAbstract {

	public String getName() {
		return "highlight selection";
	}

	public String getStatusBusyMessage() {
		return "Highlighting selection..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log, IvMObject.selected_model_selection,
				IvMObject.controller_highlighting_filters, IvMObject.aligned_log_info, IvMObject.model };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log_filtered, IvMObject.aligned_log_info_filtered };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMLogNotFiltered logBase = inputs.get(IvMObject.aligned_log);
		Selection selection = inputs.get(IvMObject.selected_model_selection);
		IvMHighlightingFiltersController highLightingFilters = inputs.get(IvMObject.controller_highlighting_filters);
		IvMLogInfo oldLogInfo = inputs.get(IvMObject.aligned_log_info);
		IvMModel model = inputs.get(IvMObject.model);

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

		return new IvMObjectValues().//
				s(IvMObject.aligned_log_filtered, logFiltered).//
				s(IvMObject.aligned_log_info_filtered, resultLogInfo);
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
					break;
				}
			}

			if (!keepTrace) {
				it.remove();
			}

		}
	}
}
