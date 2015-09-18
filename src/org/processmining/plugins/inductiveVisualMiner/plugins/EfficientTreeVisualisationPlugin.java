package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

@Plugin(name = "Tree visualization (Inductive visual Miner)", returnLabels = { "Tree visualization (Inductive visual Miner)" }, returnTypes = { JComponent.class }, parameterLabels = { "Dot" }, userAccessible = false)
@Visualizer
public class EfficientTreeVisualisationPlugin {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, EfficientTree tree) {
		ProcessTreeVisualisation visualisation = new ProcessTreeVisualisation();
		return new DotPanel(visualisation.fancy(EfficientTree2processTree.convert(tree),
				new AlignedLogVisualisationDataImplEmpty(), new ProcessTreeVisualisationParameters()).getA());
	}
}
