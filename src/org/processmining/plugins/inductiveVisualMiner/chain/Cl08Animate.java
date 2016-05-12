package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.FunctionCancellable;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ThreadedComputer;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;

public class Cl08Animate
		extends
		ChainLink<Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree>, Double> {

	private final ThreadedComputer<Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree>, GraphVizTokens> pool;

	public Cl08Animate(final Executor executor, final InductiveVisualMinerState state,
			final InductiveVisualMinerPanel panel) {

		pool = new ThreadedComputer<Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree>, GraphVizTokens>(
				executor,
				new FunctionCancellable<Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree>, GraphVizTokens>() {

					//this function performs the computation
					public GraphVizTokens call(
							Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree> input,
							IvMCanceller canceller) throws Exception {
						IvMLog log = input.getA();
						Mode colourMode = input.getB();
						ProcessTreeVisualisationInfo info = input.getC();
						SVGDiagram svg = input.getD();
						Scaler scaler = input.getE();
						IvMEfficientTree tree = input.getF();
						return ComputeAnimation.computeAnimation(tree, log, colourMode, info, scaler, svg, canceller);
					}

				}, new InputFunction<GraphVizTokens>() {

					//this function is called on completion
					public void call(final GraphVizTokens result) throws Exception {

						//update the state and gui (in the main thread)
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								state.setAnimation(result);
								panel.getGraph().setTokens(state.getAnimationGraphVizTokens());
								panel.getGraph().setAnimationExtremeTimes(
										state.getAnimationScaler().getMinInUserTime(),
										state.getAnimationScaler().getMaxInUserTime());
								panel.getGraph().setAnimationEnabled(true);
							}
						});
					}

				});
	}

	protected Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree> generateInput(
			InductiveVisualMinerState state) {
		return Sextuple.of(state.getIvMLog(), state.getMode(), state.getVisualisationInfo(), state.getSVGDiagram(),
				state.getAnimationScaler(), state.getTree());
	}

	protected Double executeLink(
			Sextuple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram, Scaler, IvMEfficientTree> input,
			IvMCanceller canceller) {
		/*
		 * The animation is independent of all other chainlinks. Therefore,
		 * compute it asynchronously.
		 */
		pool.compute(input, canceller);

		return null;
	}

	protected void processResult(Double result, InductiveVisualMinerState state) {

	}

}
