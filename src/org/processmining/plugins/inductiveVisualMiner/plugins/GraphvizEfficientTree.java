package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
//import org.processmining.plugins.InductiveMiner.mining.operators.Interleaved;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizProcessTree.NotYetImplementedException;

@Plugin(name = "Graphviz tree visualisation", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Process Tree" }, userAccessible = false)
@Visualizer
public class GraphvizEfficientTree {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, EfficientTree tree) throws NotYetImplementedException {
		return new DotPanel(GraphvizProcessTree.convert(EfficientTree2processTree.convert(tree)));
	}

}
