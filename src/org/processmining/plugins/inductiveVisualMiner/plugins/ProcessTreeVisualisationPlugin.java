package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.processtree.ProcessTree;

public class ProcessTreeVisualisationPlugin {
	
	@Plugin(name = "Process tree visualisation (Inductive visual Miner)", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Process tree" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Visualise process tree", requiredParameterLabels = { 0 })
	public JComponent fancy(PluginContext context, ProcessTree tree) {
		ProcessTreeVisualisation visualisation = new ProcessTreeVisualisation();
		return new DotPanel(visualisation.fancy(tree, null, new ProcessTreeVisualisationParameters()).getA());
	}
	
}