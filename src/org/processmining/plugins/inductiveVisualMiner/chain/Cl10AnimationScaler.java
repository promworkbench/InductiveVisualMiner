package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;

public class Cl10AnimationScaler extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	@Override
	public String getName() {
		return "scale animation";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Scaling animation..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.animation_scaler };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMLogNotFiltered aLog = inputs.get(IvMObject.aligned_log);

		Scaler scaler = Scaler.fromLog(aLog, ComputeAnimation.initDuration, ComputeAnimation.animationDuration,
				canceller);
		if (scaler == null) {
			scaler = Scaler.fromValues(ComputeAnimation.animationDuration);
		}

		return new IvMObjectValues().//
				s(IvMObject.animation_scaler, scaler);
	}
}