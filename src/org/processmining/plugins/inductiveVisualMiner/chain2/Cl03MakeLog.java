package org.processmining.plugins.inductiveVisualMiner.chain2;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl03MakeLog extends
		ChainLink2<Triple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo>, Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo>> {

	protected Triple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo> generateInput(
			InductiveVisualMinerState state) {
		return Triple.of(state.getSortedXLog(), state.getPerformanceClassifier(), state.getLog2logInfo());
	}

	protected Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo> executeLink(
			Triple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo> input, IvMCanceller canceller) {
		IMLog imLog = new IMLogImpl(input.getA(), input.getB().getActivityClassifier());
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

}
