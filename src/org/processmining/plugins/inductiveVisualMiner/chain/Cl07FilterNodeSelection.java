package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.info.XLogInfo;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ComputeColouringFilter;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Cl07FilterNodeSelection
		extends
		ChainLink<Septuple<AlignedLog, Set<UnfoldedNode>, Set<LogMovePosition>, AlignedLogInfo, IMLog, XLogInfo, List<ColouringFilter>>, Triple<AlignedLog, AlignedLogInfo, IMLog>> {

	protected Septuple<AlignedLog, Set<UnfoldedNode>, Set<LogMovePosition>, AlignedLogInfo, IMLog, XLogInfo, List<ColouringFilter>> generateInput(
			InductiveVisualMinerState state) {
		return Septuple.of(state.getAlignedLog(), state.getSelectedNodes(), state.getSelectedLogMoves(),
				state.getAlignedLogInfo(), new IMLog(state.getXLog(), state.getActivityClassifier()),
				state.getXLogInfoPerformance(), state.getColouringFilters());
	}

	protected Triple<AlignedLog, AlignedLogInfo, IMLog> executeLink(
			Septuple<AlignedLog, Set<UnfoldedNode>, Set<LogMovePosition>, AlignedLogInfo, IMLog, XLogInfo, List<ColouringFilter>> input) {

		canceller.reset();

		//apply colouring filters
		Triple<AlignedLog, AlignedLogInfo, IMLog> colouringFilteredAlignment = ComputeColouringFilter
				.applyColouringFilter(input.getA(), input.getD(), input.getE(), input.getF(), input.getG(), canceller);

		//apply node/edge selection filters
		if (!input.getB().isEmpty() || !input.getC().isEmpty()) {
			return filterOnSelection(colouringFilteredAlignment.getA(), input.getB(), input.getC(),
					colouringFilteredAlignment.getC());
		} else {
			return colouringFilteredAlignment;
		}

	}

	protected void processResult(Triple<AlignedLog, AlignedLogInfo, IMLog> result, InductiveVisualMinerState state) {
		state.setAlignedFilteredLog(result.getA(), result.getB(), result.getC());
	}

	private static Triple<AlignedLog, AlignedLogInfo, IMLog> filterOnSelection(AlignedLog alignedLog,
			Set<UnfoldedNode> selectedNodes, Set<LogMovePosition> selectedLogMoves, IMLog xLog) {

		AlignedLog fl = new AlignedLog();
		boolean useNodes = !selectedNodes.isEmpty();
		boolean useLogMoves = !selectedLogMoves.isEmpty();
		for (AlignedTrace trace : alignedLog) {
			for (Move move : trace) {
				if (useNodes && move.isModelSync() && selectedNodes.contains(move.getUnode())) {
					fl.add(trace, alignedLog.getCardinalityOf(trace));
					break;
				}
				if (useLogMoves && move.isLogMove() && selectedLogMoves.contains(LogMovePosition.of(move))) {
					fl.add(trace, alignedLog.getCardinalityOf(trace));
					break;
				}
			}

		}

		AlignedLogInfo fli = new AlignedLogInfo(fl);
		return Triple.of(fl, fli, xLog);
	}
}