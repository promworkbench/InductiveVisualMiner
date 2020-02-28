package org.processmining.plugins.inductiveVisualMiner;

import java.lang.ref.SoftReference;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfigurationDefault;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class InductiveVisualMiner {

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = {
			JComponent.class }, parameterLabels = { "Event log",
					"canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, XLog xLog, ProMCanceller canceller)
			throws UnknownTreeNodeException {

		InductiveVisualMinerConfiguration configuration = new InductiveVisualMinerConfigurationDefault(xLog, canceller,
				context.getExecutor());
		InductiveVisualMinerController controller = new InductiveVisualMinerController(context, configuration,
				canceller);

		return controller.getPanel();
	}

	@Plugin(name = "Inductive visual Miner", level = PluginLevel.PeerReviewed, returnLabels = {
			"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
					"Inductive visual Miner launcher", "canceller" }, userAccessible = true)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, final InductiveVisualMinerLauncher launcher,
			ProMCanceller canceller) throws UnknownTreeNodeException {

		//set launcher non-favourite
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(launcher)
					.setFavorite(false);
		}

		XLog log = launcher.xLog.get();
		if (log == null) {
			throw new RuntimeException("The log has been removed by garbage collection.");
		}
		InductiveVisualMinerConfiguration configuration;
		if (launcher.configuration != null) {
			configuration = launcher.configuration;
		} else {
			configuration = new InductiveVisualMinerConfigurationDefault(log, canceller, context.getExecutor());
		}
		if (launcher.preMinedTree == null && launcher.preMinedDfg == null) {
			//pre-set the miner if necessary
			if (launcher.getMiner() != null) {
				configuration.getState().setMiner(launcher.getMiner());
				configuration.getPanel().getMinerSelection().setSelectedItem(launcher.getMiner());
			}
		} else if (launcher.preMinedTree != null) {
			//launch with pre-mined tree
			EfficientTree preMinedTree = launcher.preMinedTree.get();
			if (preMinedTree == null) {
				throw new RuntimeException("The pre-mined tree has been removed by garbage collection.");
			}
			configuration.getState().setPreMinedModel(new IvMModel(preMinedTree));
		} else {
			//launch with pre-mined dfg
			DirectlyFollowsModel preMinedDfm = launcher.preMinedDfg.get();
			if (preMinedDfm == null) {
				throw new RuntimeException("The pre-mined tree has been removed by garbage collection.");
			}
			configuration.getState().setPreMinedModel(new IvMModel(preMinedDfm));
		}

		InductiveVisualMinerController controller = new InductiveVisualMinerController(context, configuration,
				canceller);

		return controller.getPanel();
	}

	public static class InductiveVisualMinerLauncher {
		public final SoftReference<XLog> xLog;
		public final SoftReference<EfficientTree> preMinedTree;
		public final SoftReference<DirectlyFollowsModel> preMinedDfg;
		private VisualMinerWrapper miner;
		private final InductiveVisualMinerConfiguration configuration;

		private InductiveVisualMinerLauncher(SoftReference<XLog> xLog, SoftReference<EfficientTree> preMinedTree,
				SoftReference<DirectlyFollowsModel> preMinedDfg, InductiveVisualMinerConfiguration configuration) {
			this.xLog = xLog;
			this.preMinedTree = preMinedTree;
			this.preMinedDfg = preMinedDfg;
			this.configuration = configuration;
		}

		public VisualMinerWrapper getMiner() {
			return miner;
		}

		public static InductiveVisualMinerLauncher launcher(InductiveVisualMinerConfiguration configuration) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(configuration.getState().getXLog()), null, null,
					configuration);
		}

		public static InductiveVisualMinerLauncher launcher(XLog xLog) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(xLog), null, null, null);
		}

		@Deprecated
		public static InductiveVisualMinerLauncher launcher(XLog xLog, ProcessTree preMinedTree) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(xLog),
					new SoftReference<>(ProcessTree2EfficientTree.convert(preMinedTree)), null, null);
		}

		public static InductiveVisualMinerLauncher launcher(XLog xLog, EfficientTree preMinedTree) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(xLog), new SoftReference<>(preMinedTree), null,
					null);
		}

		public static InductiveVisualMinerLauncher launcher(XLog xLog, DirectlyFollowsModel preMinedDfg) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(xLog), null, new SoftReference<>(preMinedDfg),
					null);
		}

		@Deprecated
		public static InductiveVisualMinerLauncher launcherPro(XLog xLog) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(xLog), null, null, null);
		}

		@Deprecated
		public static InductiveVisualMinerLauncher launcherPro(XLog xLog, ProcessTree preMinedTree) {
			return new InductiveVisualMinerLauncher(new SoftReference<>(xLog),
					new SoftReference<>(ProcessTree2EfficientTree.convert(preMinedTree)), null, null);
		}

		public void setMiner(VisualMinerWrapper miner) {
			this.miner = miner;
		}
	}

	@Plugin(name = "Mine with Inductive visual Miner", level = PluginLevel.PeerReviewed, returnLabels = {
			"Inductive visual Miner" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = {
					"Event log" }, userAccessible = true, categories = { PluginCategory.Discovery,
							PluginCategory.Analytics,
							PluginCategory.ConformanceChecking }, help = "Discover a process tree or a Petri net interactively using Inductive Miner (IvM).")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0 })
	public InductiveVisualMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog) {
		return InductiveVisualMinerLauncher.launcher(xLog);
	}

	@Plugin(name = "Visualise deviations on Process tree (Inductive visual Miner)", returnLabels = {
			"Deviations visualisation" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = {
					"Event log", "Process tree" }, userAccessible = true, categories = { PluginCategory.Analytics,
							PluginCategory.ConformanceChecking }, help = "Perform an alignment on a log and a process tree and visualise the results as Inductive visual Miner (IvM), including its filtering options.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0, 1 })
	public InductiveVisualMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog, ProcessTree preMinedTree) {
		return InductiveVisualMinerLauncher.launcher(xLog, preMinedTree);
	}

	@Plugin(name = "Visualise deviations on process tree (Inductive visual Miner)", returnLabels = {
			"Deviations visualisation" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = {
					"Event log", "Process tree" }, userAccessible = true, categories = { PluginCategory.Analytics,
							PluginCategory.ConformanceChecking }, help = "Perform an alignment on a log and a process tree and visualise the results as Inductive visual Miner (IvM), including its filtering options.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0, 1 })
	public InductiveVisualMinerLauncher mineGuiEfficientTree(PluginContext context, XLog xLog,
			EfficientTree preMinedTree) {
		return InductiveVisualMinerLauncher.launcher(xLog, preMinedTree);
	}
}
