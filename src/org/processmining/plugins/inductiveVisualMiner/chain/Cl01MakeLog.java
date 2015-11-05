package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

// make an IMlog out of an XLog
public class Cl01MakeLog
		extends
		ChainLink<Triple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo>, Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo>> {

	public Cl01MakeLog(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Triple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo> generateInput(InductiveVisualMinerState state) {
		return Triple.of(state.getXLog(), state.getPerformanceClassifier(), state.getLog2logInfo());
	}

	protected Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo> executeLink(
			Triple<XLog, XEventPerformanceClassifier, IMLog2IMLogInfo> input) {
		IMLog imLog = new IMLog(input.getA(), input.getB().getActivityClassifier());
		IMLogInfo imLogInfo = input.getC().createLogInfo(imLog);
		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(input.getA(), input.getB().getActivityClassifier());
		XLogInfo xLogInfoPerformance = XLogInfoFactory.createLogInfo(input.getA(), input.getB());

		return Quadruple.of(xLogInfo, xLogInfoPerformance, imLog, imLogInfo);
	}

	protected void processResult(Quadruple<XLogInfo, XLogInfo, IMLog, IMLogInfo> result, InductiveVisualMinerState state) {
		state.setLog(result.getA(), result.getB(), result.getC(), result.getD());
	}
}