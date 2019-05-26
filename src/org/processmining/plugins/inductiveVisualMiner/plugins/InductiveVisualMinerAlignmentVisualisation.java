package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.export.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapperPluginFinder;

public class InductiveVisualMinerAlignmentVisualisation {
	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = {
			JComponent.class }, parameterLabels = { "IvM Alignment",
					"canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, InductiveVisualMinerAlignment ivmAlignment,
			ProMCanceller canceller) throws UnknownTreeNodeException {
		InductiveVisualMinerState state = new InductiveVisualMinerState(ivmAlignment.getXLog());
		state.setPreMinedModel(ivmAlignment.getModel());
		state.setPreMinedClassifier(ivmAlignment.getClassifier());

		InductiveVisualMinerPanel panel = InductiveVisualMinerPanel.panel(context, state,
				VisualMinerWrapperPluginFinder.find(context, state.getMiner()), canceller);
		new InductiveVisualMinerController(context, panel, state, canceller);

		return panel;
	}

}
