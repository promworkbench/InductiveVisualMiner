package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner.InductiveVisualMinerLauncher;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfigurationDefault;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.DfgMiner;

public class DirectlyFollowsVisualMiner {
	@Plugin(name = "Mine with Directly Follows visual Miner", level = PluginLevel.PeerReviewed, returnLabels = {
			"Inductive visual Miner" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = {
					"Event log" }, userAccessible = true, categories = { PluginCategory.Discovery,
							PluginCategory.Analytics,
							PluginCategory.ConformanceChecking }, help = "Discover a directly follows model interactively. (DFvM)")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0 })
	public InductiveVisualMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog) {
		InductiveVisualMinerLauncher launcher = InductiveVisualMinerLauncher.launcher(xLog);
		launcher.setMiner(new DfgMiner());
		return launcher;
	}

	@Plugin(name = "Visualise deviations on directly follows model (Directly Follows visual Miner)", returnLabels = {
			"Deviations visualisation" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = {
					"Event log", "Directly follows model" }, userAccessible = true, categories = {
							PluginCategory.Analytics,
							PluginCategory.ConformanceChecking }, help = "Perform an alignment on a log and a directly follows graph and visualise the results as Directly Follows visual Miner (DFvM), including its filtering options.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0, 1 })
	public InductiveVisualMinerLauncher mineGuiDfg(PluginContext context, XLog xLog, DirectlyFollowsModel dfg) {
		return InductiveVisualMinerLauncher.launcher(xLog, dfg);
	}

	public static JComponent getDirectlyFollowsVisualMiner(final PluginContext context, XLog xLog,
			ProMCanceller canceller) throws UnknownTreeNodeException {

		InductiveVisualMinerConfiguration configuration = new InductiveVisualMinerConfigurationDefault(canceller,
				context.getExecutor());
		InductiveVisualMinerController controller = new InductiveVisualMinerController(context, configuration, xLog,
				canceller);
		controller.setFixedObject(IvMObject.selected_miner, new DfgMiner());

		return configuration.getPanel();
	}
}
