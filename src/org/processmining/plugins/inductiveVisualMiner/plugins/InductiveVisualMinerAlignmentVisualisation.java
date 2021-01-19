package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfigurationDefault;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class InductiveVisualMinerAlignmentVisualisation {
	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = {
			JComponent.class }, parameterLabels = { "IvM Alignment",
					"canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, InductiveVisualMinerAlignment ivmAlignment,
			ProMCanceller canceller) throws UnknownTreeNodeException {

		XLog xLog = ivmAlignment.getXLog();
		IvMModel model = ivmAlignment.getModel();
		AttributeClassifier[] classifier = ivmAlignment.getClassifier();

		if (xLog == null || model == null || classifier == null) {
			return new JLabel(
					" Unfortunately, this Inductive visual Miner alignment does not have the fields necessary to be visualised.");
		}

		InductiveVisualMinerConfiguration configuration = new InductiveVisualMinerConfigurationDefault(canceller,
				context.getExecutor());
		InductiveVisualMinerController controller = new InductiveVisualMinerController(context, configuration, xLog,
				canceller);

		controller.setFixedObject(IvMObject.model, model);
		controller.setFixedObject(IvMObject.selected_classifier, classifier);
		controller.setFixedObject(IvMObject.imported_alignment, ivmAlignment);

		return configuration.getPanel();
	}

}
