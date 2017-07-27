package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsDeviations;
import org.processmining.plugins.inductiveVisualMiner.plugins.EfficientTreeAlignmentPlugin.IvMAlignment;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

public class IvMLogVisualisation {
	@Plugin(name = "Aligned log visualisation", returnLabels = { "Dot visualization" }, returnTypes = {
			JComponent.class }, parameterLabels = {
					"Alignment" }, userAccessible = true, level = PluginLevel.PeerReviewed)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Visualise alignment", requiredParameterLabels = { 0 })
	public DotPanel fancy(PluginContext context, IvMAlignment alignment) throws UnknownTreeNodeException {
		ProcessTreeVisualisation visualisation = new ProcessTreeVisualisation();
		ProcessTreeVisualisationParameters parameters = new ModePathsDeviations().visualisationParameters;
		IvMLogInfo logInfo = new IvMLogInfo(alignment.log, alignment.tree);
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplFrequencies(alignment.tree, logInfo);
		Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> t = visualisation.fancy(alignment.tree, data,
				parameters);

		return new DotPanel(t.getA());
	}
}
