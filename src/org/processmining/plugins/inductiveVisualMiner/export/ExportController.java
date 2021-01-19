package org.processmining.plugins.inductiveVisualMiner.export;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.GetExporters;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

import gnu.trove.set.hash.THashSet;

public class ExportController {
	public static void initialise(final DataChain chain, InductiveVisualMinerConfiguration configuration,
			InductiveVisualMinerPanel panel) {

		//compile a list of input objects
		final IvMObject<?>[] objects;
		{
			Set<IvMObject<?>> set = new THashSet<>();
			for (IvMExporter exporter : configuration.getExporters()) {
				set.addAll(Arrays.asList(exporter.getInputObjects()));
				set.addAll(Arrays.asList(exporter.getTriggerObjects()));
			}
			objects = new IvMObject<?>[set.size()];
			set.toArray(objects);
		}

		//add animation and statistics to export
		panel.getGraph().setGetExporters(new GetExporters() {
			public List<Exporter> getExporters(List<Exporter> exporters) {
				//this method is called whenever the uses clicks on the export button

				IvMObjectValues inputs = null;
				try {
					inputs = chain.getObjectValues(objects).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}

				//for each exporter, see whether its requirements are met
				for (IvMExporter exporter : configuration.getExporters()) {
					if (inputs.has(exporter.getInputObjects())) {
						IvMObjectValues subInputs = inputs.getIfPresent(exporter.getInputObjects(),
								exporter.getTriggerObjects());
						exporter.setInputs(subInputs);
						exporters.add(exporter);
					}
				}

				return exporters;
			}
		});
	}
}
