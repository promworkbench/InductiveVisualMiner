package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogImplFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Cl08FilterNodeSelection
		extends
		ChainLink<Quintuple<IvMLogNotFiltered, Set<UnfoldedNode>, Set<LogMovePosition>, List<ColouringFilter>, IvMLogInfo>, Pair<IvMLogImplFiltered, IvMLogInfo>> {

	public Cl08FilterNodeSelection(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Quintuple<IvMLogNotFiltered, Set<UnfoldedNode>, Set<LogMovePosition>, List<ColouringFilter>, IvMLogInfo> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getIvMLog(), state.getSelectedNodes(), state.getSelectedLogMoves(),
				state.getColouringFilters(), state.getIvMLogInfo());
	}

	protected Pair<IvMLogImplFiltered, IvMLogInfo> executeLink(
			Quintuple<IvMLogNotFiltered, Set<UnfoldedNode>, Set<LogMovePosition>, List<ColouringFilter>, IvMLogInfo> input) {

		canceller.reset();

		IvMLogNotFiltered logBase = input.getA();
		Set<UnfoldedNode> selectedNodes = input.getB();
		Set<LogMovePosition> selectedLogMoves = input.getC();
		List<ColouringFilter> colouringFilters = input.getD();
		IvMLogInfo oldLogInfo = input.getE();

		IvMLogImplFiltered logFiltered = new IvMLogImplFiltered(logBase);

		//apply the colouring filters
		applyColouringFilter(logFiltered, colouringFilters, canceller);

		//apply node/edge selection filters
		if (!selectedNodes.isEmpty() || !selectedLogMoves.isEmpty()) {
			filterOnSelection(logFiltered, selectedNodes, selectedLogMoves);
		}

		//create a log info
		IvMLogInfo resultLogInfo = oldLogInfo;
		if (logFiltered.isSomethingFiltered()) {
			resultLogInfo = new IvMLogInfo(logFiltered);
		}
		return Pair.of(logFiltered, resultLogInfo);
	}

	protected void processResult(Pair<IvMLogImplFiltered, IvMLogInfo> result, InductiveVisualMinerState state) {
		state.setIvMLogFiltered(result.getA(), result.getB());
		state.setVisualisationData(state.getMode().getVisualisationData(state));
	}

	public static void applyColouringFilter(IvMLogImplFiltered log, List<ColouringFilter> filters, final Canceller canceller) {
		//first, walk through the filters to see there is actually one enabled
		List<ColouringFilter> enabledColouringFilters = new LinkedList<>();
		for (ColouringFilter filter : filters) {
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
			for (ColouringFilter filter : enabledColouringFilters) {
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
	private static void filterOnSelection(IvMLogImplFiltered log, Set<UnfoldedNode> selectedNodes,
			Set<LogMovePosition> selectedLogMoves) {

		boolean useNodes = !selectedNodes.isEmpty();
		boolean useLogMoves = !selectedLogMoves.isEmpty();
		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {

			boolean keepTrace = false;
			for (IvMMove move : it.next()) {
				if (useNodes && move.isModelSync() && selectedNodes.contains(move.getUnode())) {
					keepTrace = true;
					break;
				}
				if (useLogMoves && move.isLogMove() && selectedLogMoves.contains(LogMovePosition.of(move))) {
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