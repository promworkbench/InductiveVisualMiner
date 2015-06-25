package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMMove.Scaler;
import org.processmining.plugins.inductiveVisualMiner.colouringmode.ColouringMode;

import com.kitfox.svg.SVGDiagram;

public class Cl10Animate
		extends
		ChainLink<Quintuple<IvMLog, ColouringMode, AlignedLogVisualisationInfo, Dot, SVGDiagram>, Pair<SVGDiagram, Scaler>> {

	protected Quintuple<IvMLog, ColouringMode, AlignedLogVisualisationInfo, Dot, SVGDiagram> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getIvMLog(), state.getColourMode(), state.getVisualisationInfo(), state.getDot(),
				state.getSVGDiagram());
	}

	protected Pair<SVGDiagram, Scaler> executeLink(
			Quintuple<IvMLog, ColouringMode, AlignedLogVisualisationInfo, Dot, SVGDiagram> input) {
		return ComputeAnimation.computeAnimation(input.getA(), input.getB(), input.getC(),
				InductiveVisualMinerController.maxAnimatedTraces, input.getD(), input.getE(), canceller);
	}

	protected void processResult(Pair<SVGDiagram, Scaler> result, InductiveVisualMinerState state) {
		state.setAnimation(result.getB(), result.getA());
	}

}
