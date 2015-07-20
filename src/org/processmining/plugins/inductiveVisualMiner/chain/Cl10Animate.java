package org.processmining.plugins.inductiveVisualMiner.chain;

import java.awt.geom.NoninvertibleTransformException;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.colouringmode.ColouringMode;

import com.kitfox.svg.SVGDiagram;

public class Cl10Animate
		extends
		ChainLink<Quadruple<IvMLog, ColouringMode, AlignedLogVisualisationInfo, SVGDiagram>, Pair<Scaler, GraphVizTokens>> {

	protected Quadruple<IvMLog, ColouringMode, AlignedLogVisualisationInfo, SVGDiagram> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getIvMLog(), state.getColourMode(), state.getVisualisationInfo(),
				state.getSVGDiagram());
	}

	protected Pair<Scaler, GraphVizTokens> executeLink(
			Quadruple<IvMLog, ColouringMode, AlignedLogVisualisationInfo, SVGDiagram> input) throws NoninvertibleTransformException {
		return ComputeAnimation.computeAnimation(input.getA(), input.getB(), input.getC(), input.getD(), canceller);
	}

	protected void processResult(Pair<Scaler, GraphVizTokens> result, InductiveVisualMinerState state) {
		state.setAnimation(result.getA(), result.getB());
	}

}
