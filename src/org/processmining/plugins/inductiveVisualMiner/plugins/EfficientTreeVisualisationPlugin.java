package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class EfficientTreeVisualisationPlugin {
	@Plugin(name = "Efficient tree visualisation (Inductive visual Miner)", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Efficient tree" }, userAccessible = true)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Visualise process tree", requiredParameterLabels = { 0 })
	public DotPanel fancy2(PluginContext context, EfficientTree tree) throws UnknownTreeNodeException {
		Dot dot = fancy(tree);
		return new DotPanel(dot);
	}
	
	public static Dot fancy(EfficientTree tree) throws UnknownTreeNodeException {
		ProcessTreeVisualisation visualisation = new ProcessTreeVisualisation();
		return visualisation.fancy(tree, new AlignedLogVisualisationDataImplEmpty(),
				new ProcessTreeVisualisationParameters()).getA();
	}
}
