package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;

public class Cl09AnimationScaler extends ChainLink<IvMLog, Scaler> {

	protected IvMLog generateInput(InductiveVisualMinerState state) {
		return state.getIvMLog();
	}

	protected Scaler executeLink(IvMLog input, IvMCanceller canceller) throws Exception {
		Scaler scaler = Scaler.fromLog(input, ComputeAnimation.initDuration, ComputeAnimation.animationDuration,
				canceller);
		if (scaler == null) {
			return Scaler.fromValues(ComputeAnimation.animationDuration);
		}
		return scaler;
	}

	protected void processResult(Scaler result, InductiveVisualMinerState state) {
		state.setAnimationScaler(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setAnimationScaler(null);
	}

}
