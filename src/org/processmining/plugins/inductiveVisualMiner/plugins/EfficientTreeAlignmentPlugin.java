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
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentPerformance;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class EfficientTreeAlignmentPlugin {

	public static class IvMAlignment {
		IvMLog log;
		IvMEfficientTree tree;
	}

	@Plugin(name = "Compute efficient tree alignment", returnLabels = { "Aligned log" }, returnTypes = {
			IvMAlignment.class }, parameterLabels = { "Efficient tree",
					"Log" }, userAccessible = true, level = PluginLevel.Regular, categories = {
							PluginCategory.Enhancement }, help = "Align an efficient tree and an event log using ETM-alignments.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Align efficien tree, default", requiredParameterLabels = { 0, 1 })
	public IvMAlignment align(final UIPluginContext context, EfficientTree tree, XLog log) throws Exception {

		EfficientTreeAlignmentDialog dialog = new EfficientTreeAlignmentDialog(log);
		{
			InteractionResult result = context.showWizard("Align an efficient tree and an event log using ETM", true,
					true, dialog);
			if (result != InteractionResult.FINISHED) {
				context.getFutureResult(0).cancel(false);
				return null;
			}
		}

		context.log("Mining...");

		IvMEfficientTree ivmTree = new IvMEfficientTree(tree);
		IvMLog aLog = align(ivmTree, log, dialog.getClassifier(), new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});

		IvMAlignment result = new IvMAlignment();
		result.log = aLog;
		result.tree = ivmTree;
		return result;
	}

	public static IvMLog align(IvMEfficientTree tree, XLog log, XEventClassifier classifier, ProMCanceller canceller)
			throws Exception {
		XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(classifier);

		XEventClasses activityEventClasses = XLogInfoFactory.createLogInfo(log, classifier).getEventClasses();
		XEventClasses performanceEventClasses = XLogInfoFactory.createLogInfo(log, performanceClassifier)
				.getEventClasses();

		return AlignmentPerformance.align(tree, performanceClassifier, log, activityEventClasses,
				performanceEventClasses, canceller);
	}
}
