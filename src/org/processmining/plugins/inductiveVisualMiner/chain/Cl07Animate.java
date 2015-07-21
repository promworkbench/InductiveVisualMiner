package org.processmining.plugins.inductiveVisualMiner.chain;

import java.awt.geom.NoninvertibleTransformException;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogBase;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;

public class Cl07Animate
		extends
		ChainLink<Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram>, Pair<Scaler, GraphVizTokens>> {

	protected Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getIvMLog(), state.getMode(), state.getVisualisationInfo(),
				state.getSVGDiagram());
	}

	protected Pair<Scaler, GraphVizTokens> executeLink(
			Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram> input) throws NoninvertibleTransformException {
		return ComputeAnimation.computeAnimation(input.getA(), input.getB(), input.getC(), input.getD(), canceller);
	}

	protected void processResult(Pair<Scaler, GraphVizTokens> result, InductiveVisualMinerState state) {
		state.setAnimation(result.getA(), result.getB());
	}

}
