package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ThreadedComputer;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogBase;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;

public class Cl07Animate extends ChainLink<Double, Double> {

	private final InductiveVisualMinerState state;
	private final InductiveVisualMinerPanel panel;

	private ThreadedComputer<Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram>, Pair<Scaler, GraphVizTokens>> pool = new ThreadedComputer<Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram>, Pair<Scaler, GraphVizTokens>>(
			new Function<Pair<ResettableCanceller, Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram>>, Pair<Scaler, GraphVizTokens>>() {

				//this function performs the computation
				public Pair<Scaler, GraphVizTokens> call(
						Pair<org.processmining.plugins.inductiveVisualMiner.chain.ChainLink.ResettableCanceller, Quadruple<IvMLogBase, Mode, ProcessTreeVisualisationInfo, SVGDiagram>> input)
						throws Exception {
					return ComputeAnimation.computeAnimation(input.getB().getA(), input.getB().getB(), input.getB()
							.getC(), input.getB().getD(), input.getA());
				}

			}, new InputFunction<Pair<Scaler, GraphVizTokens>>() {

				//this function is called on completion
				public void call(Pair<Scaler, GraphVizTokens> result) throws Exception {
					state.setAnimation(result.getA(), result.getB());
					
					panel.getSaveImageButton().setText("animation");
					panel.getGraph().setTokens(state.getAnimationGraphVizTokens());
					panel.getGraph().setAnimationExtremeTimes(state.getAnimationScaler().getMinInUserTime(),
							state.getAnimationScaler().getMaxInUserTime());
					panel.getGraph().setEnableAnimation(true);
				}

			});

	public Cl07Animate(InductiveVisualMinerState state, InductiveVisualMinerPanel panel) {
		this.state = state;
		this.panel = panel;
	}

	protected Double generateInput(InductiveVisualMinerState state) {

		/*
		 * The animation is independent of all other chainlinks. Therefore,
		 * compute it asynchronously.
		 */
		pool.compute(Quadruple.of(state.getIvMLog(), state.getMode(), state.getVisualisationInfo(),
				state.getSVGDiagram()));

		return null;
	}

	protected Double executeLink(Double input) {
		return null;
	}

	protected void processResult(Double result, InductiveVisualMinerState state) {

	}

}
