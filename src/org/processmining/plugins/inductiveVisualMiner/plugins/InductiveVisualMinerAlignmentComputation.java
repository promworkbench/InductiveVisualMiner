package org.processmining.plugins.inductiveVisualMiner.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputerImpl;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentPerformance;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAlignment;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class InductiveVisualMinerAlignmentComputation {

	@Plugin(name = "Compute Inductive visual Miner alignment", returnLabels = {
			"Inductive visual Miner alignment" }, returnTypes = {
					InductiveVisualMinerAlignment.class }, parameterLabels = { "Efficient tree",
							"Log" }, userAccessible = true, level = PluginLevel.Regular, categories = {
									PluginCategory.Enhancement }, help = "Align an efficient tree and an event log using ETM-alignments.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Align efficient tree, default", requiredParameterLabels = { 0, 1 })
	public InductiveVisualMinerAlignment align(final UIPluginContext context, EfficientTree tree, XLog log)
			throws Exception {

		EfficientTreeAlignmentDialog dialog = new EfficientTreeAlignmentDialog(log);
		{
			InteractionResult result = context.showWizard("Align an efficient tree and an event log using ETM", true,
					true, dialog);
			if (result != InteractionResult.FINISHED) {
				context.getFutureResult(0).cancel(false);
				return null;
			}
		}

		context.log("Aligning...");

		IvMEfficientTree ivmTree = new IvMEfficientTree(tree);
		IvMLog aLog = align(ivmTree, log, dialog.getClassifier(), new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});

		return ExportAlignment.exportAlignment(aLog, new IvMModel(ivmTree), dialog.getClassifier());
	}

	public static IvMLog align(IvMEfficientTree tree, XLog log, XEventClassifier classifier, ProMCanceller canceller)
			throws Exception {
		XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(classifier);

		XEventClasses activityEventClasses = XLogInfoFactory.createLogInfo(log, classifier).getEventClasses();
		XEventClasses performanceEventClasses = XLogInfoFactory.createLogInfo(log, performanceClassifier)
				.getEventClasses();
		AlignmentComputer computer = new AlignmentComputerImpl();

		return AlignmentPerformance.align(computer, new IvMModel(tree), performanceClassifier, log,
				activityEventClasses, performanceEventClasses, canceller);
	}
}
