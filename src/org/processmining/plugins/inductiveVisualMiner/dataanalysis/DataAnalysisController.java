package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGuiAbstract;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

public class DataAnalysisController {
	public static void initialise(InductiveVisualMinerConfiguration configuration,
			DataChain<InductiveVisualMinerConfiguration> chain) {

		for (DataAnalysisTableFactory analysis2 : configuration.getDataAnalysisTables()) {

			final DataAnalysisTableFactory analysis = analysis2;

			//update the analysis view when its inputs are available
			chain.register(
					new DataChainLinkGuiAbstract<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>() {
						public String getName() {
							return "analysis " + analysis.getAnalysisName();
						}

						public IvMObject<?>[] createInputObjects() {
							return analysis.getInputObjects();
						}

						public IvMObject<?>[] createOptionalObjects() {
							return analysis.getOptionalObjects();
						}

						public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs)
								throws Exception {
							panel.getDataAnalysesView().setData(analysis.getAnalysisName(), inputs);
						}

						public void invalidate(InductiveVisualMinerPanel panel) {
							panel.getDataAnalysesView().invalidate(analysis.getAnalysisName());
						}
					});
		}
	}
}