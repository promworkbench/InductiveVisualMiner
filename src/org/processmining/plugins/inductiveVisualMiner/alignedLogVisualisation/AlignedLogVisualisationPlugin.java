package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.processtree.ProcessTree;

public class AlignedLogVisualisationPlugin {
	
	@Plugin(name = "Aligned log visualisation (IvM)", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Aligned Log" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Visualise aligned log", requiredParameterLabels = { 0 })
	public JComponent visualise(final PluginContext context, final AlignedLog aLog) {
		ProcessTree tree = getTree(aLog);
		AlignedLogInfo logInfo = new AlignedLogInfo(aLog);
		AlignedLogVisualisation visualisation = new AlignedLogVisualisation();
		AlignedLogVisualisationParameters parameters = new AlignedLogVisualisationParameters();
		parameters.setShowFrequenciesOnModelEdges(true);
		
		Dot dot = visualisation.fancy(tree, logInfo, null, parameters).getLeft();
		return new DotPanel(dot);
	}
	
	@Plugin(name = "Process tree visualisation (IvM)", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Process tree" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Visualise process tree", requiredParameterLabels = { 0 })
	public JComponent fancy(PluginContext context, ProcessTree tree) {
		AlignedLogVisualisation visualisation = new AlignedLogVisualisation();
		return new DotPanel(visualisation.fancy(tree, null, null, new AlignedLogVisualisationParameters()).getLeft());
	}
	
	private static ProcessTree getTree(AlignedLog aLog) {
		//find the process tree
		for (AlignedTrace trace : aLog) {
			for (Move m : trace) {
				if (m.isModelSync()) {
					return m.getUnode().getNode().getProcessTree();
				}
			}
		}
		return null;
	}
}
