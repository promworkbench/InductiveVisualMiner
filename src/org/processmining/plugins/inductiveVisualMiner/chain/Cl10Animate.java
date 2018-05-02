package org.processmining.plugins.inductiveVisualMiner.chain;

import java.awt.geom.NoninvertibleTransformException;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class Cl10Animate extends
		ChainLink<Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMModel>, GraphVizTokens> {

	protected Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMModel> generateInput(
			InductiveVisualMinerState state) {
		if (state.isAnimationGlobalEnabled() && !state.isIllogicalTimeStamps()) {
			return Sextuple.of(state.getIvMLog(), state.getMode(), state.getVisualisationInfo(), state.getSVGDiagram(),
					state.getAnimationScaler(), state.getModel());
		} else {
			return null;
		}
	}

	protected GraphVizTokens executeLink(
			Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMModel> input,
			IvMCanceller canceller) throws NoninvertibleTransformException, SVGException {
		if (input != null) {
			IvMLog log = input.getA();
			Mode colourMode = input.getB();
			ProcessTreeVisualisationInfo info = input.getC();
			SVGDiagram svg = input.getD();
			Scaler scaler = input.getE();
			IvMModel model = input.getF();
			return ComputeAnimation.computeAnimation(model, log, colourMode, info, scaler, svg, canceller);
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

	public String getName() {
		return "animate";
	}
}
