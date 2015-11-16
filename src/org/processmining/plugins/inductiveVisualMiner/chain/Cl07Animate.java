package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

import org.processmining.framework.plugin.ProMCanceller;
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
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;

public class Cl07Animate extends ChainLink<Double, Double> {

	private final ProMCanceller globalCanceller;

	private final ThreadedComputer<Quadruple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram>, Pair<Scaler, GraphVizTokens>> pool;

	public Cl07Animate(final Executor executor, final InductiveVisualMinerState state,
			final InductiveVisualMinerPanel panel, final ProMCanceller canceller) {
		super(canceller);
		this.globalCanceller = canceller;

		pool = new ThreadedComputer<Quadruple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram>, Pair<Scaler, GraphVizTokens>>(
				executor,
				new Function<Pair<ResettableCanceller, Quadruple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram>>, Pair<Scaler, GraphVizTokens>>() {

					//this function performs the computation
					public Pair<Scaler, GraphVizTokens> call(
							Pair<org.processmining.plugins.inductiveVisualMiner.chain.ChainLink.ResettableCanceller, Quadruple<IvMLogNotFiltered, Mode, ProcessTreeVisualisationInfo, SVGDiagram>> input)
							throws Exception {
						return ComputeAnimation.computeAnimation(input.getB().getA(), input.getB().getB(), input.getB()
								.getC(), input.getB().getD(), input.getA());
					}

				}, new InputFunction<Pair<Scaler, GraphVizTokens>>() {

					//this function is called on completion
					public void call(Pair<Scaler, GraphVizTokens> result) throws Exception {
						state.setAnimation(result.getA(), result.getB());

						//update the gui (in the main thread)
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								panel.getSaveImageButton().setText("image/animation");
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

	protected Double generateInput(InductiveVisualMinerState state) {

		/*
		 * The animation is independent of all other chainlinks. Therefore,
		 * compute it asynchronously.
		 */
		pool.compute(
				Quadruple.of(state.getIvMLog(), state.getMode(), state.getVisualisationInfo(), state.getSVGDiagram()),
				globalCanceller);

		return null;
	}

	protected Double executeLink(Double input) {
		return null;
	}

	protected void processResult(Double result, InductiveVisualMinerState state) {

	}

}
