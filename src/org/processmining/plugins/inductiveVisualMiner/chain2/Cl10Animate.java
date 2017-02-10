package org.processmining.plugins.inductiveVisualMiner.chain2;

import java.awt.geom.NoninvertibleTransformException;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class Cl10Animate extends
		ChainLink2<Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree>, GraphVizTokens> {

	protected Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree> generateInput(
			InductiveVisualMinerState state) {
		if (state.isAnimationGlobalEnabled() && !state.isIllogicalTimeStamps()) {
			return Sextuple.of(state.getIvMLog(), state.getMode(), state.getVisualisationInfo(), state.getSVGDiagram(),
					state.getAnimationScaler(), state.getTree());
		} else {
			return null;
		}
	}

	protected GraphVizTokens executeLink(
			Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree> input,
			IvMCanceller canceller) throws NoninvertibleTransformException, SVGException {
		if (input != null) {
			IvMLog log = input.getA();
			Mode colourMode = input.getB();
			ProcessTreeVisualisationInfo info = input.getC();
			SVGDiagram svg = input.getD();
			Scaler scaler = input.getE();
			IvMEfficientTree tree = input.getF();
			return ComputeAnimation.computeAnimation(tree, log, colourMode, info, scaler, svg,
					canceller);
		} else {
			//the animation is disabled
			return null;
		}
	}

	protected void processResult(GraphVizTokens result, InductiveVisualMinerState state) {
		state.setAnimation(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setAnimation(null);
	}

}
