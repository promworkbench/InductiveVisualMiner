package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl03MakeLog extends
		ChainLink<Quadruple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo, XLifeCycleClassifier>, Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo>> {

	protected Quadruple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo, XLifeCycleClassifier> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getSortedXLog(), state.getPerformanceClassifier(), state.getLog2logInfo(),
				state.getMiner().getLifeCycleClassifier());
	}

	protected Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo> executeLink(
			Quadruple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo, XLifeCycleClassifier> input,
			IvMCanceller canceller) {
		IMLog imLog = new IMLogImpl(input.getA(), input.getB().getActivityClassifier(), input.getD());
		IMLogInfo imLogInfo = input.getC().createLogInfo(imLog);
		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(input.getA(), input.getB().getActivityClassifier());
		XLogInfo xLogInfoPerformance = XLogInfoFactory.createLogInfo(input.getA(), input.getB());

		return Quadruple.of(xLogInfo, xLogInfoPerformance, imLog, imLogInfo);
	}

	protected void processResult(Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo> result,
			InductiveVisualMinerState state) {
		state.setLog(result.getA(), result.getB(), result.getC(), result.getD());
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setLog(null, null, null, null);
	}

	public String getName() {
		return "make log";
	}
}
