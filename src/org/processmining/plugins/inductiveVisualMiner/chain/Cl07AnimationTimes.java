package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class Cl07AnimationTimes extends ChainLink<Pair<IvMLog, ProcessTreeVisualisationInfo>, Scaler> {

	public Cl07AnimationTimes(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Pair<IvMLog, ProcessTreeVisualisationInfo> generateInput(InductiveVisualMinerState state) {
		return Pair.of((IvMLog) state.getIvMLog(), state.getVisualisationInfo());
	}

	protected Scaler executeLink(Pair<IvMLog, ProcessTreeVisualisationInfo> input) throws Exception {
		IvMLog log = input.getA();
		ProcessTreeVisualisationInfo info = input.getB();

		//create the scaler
		Scaler scaler = Scaler.fromLog(log, ComputeAnimation.initDuration, ComputeAnimation.animationDuration,
				canceller);
		if (scaler == null) {
			scaler = Scaler.fromValues(ComputeAnimation.animationDuration);
		}

		//compute a start and end time for every trace
		final ShortestPathGraph graph = new ShortestPathGraph(info.getNodes(), info.getEdges());

		for (IvMTrace trace : log) {
			trace.setStartTime(ComputeAnimation.guessStartTime(trace, graph, info, scaler));
			trace.setEndTime(ComputeAnimation.guessEndTime(trace, trace.getStartTime(), graph, info, scaler));
			
			if (canceller.isCancelled()) {
				return null;
			}
		}

		return scaler;
	}

	protected void processResult(Scaler result, InductiveVisualMinerState state) {
		state.setAnimationScaler(result);
	}

}
