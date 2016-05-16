package org.processmining.plugins.inductiveVisualMiner;

import java.lang.ref.SoftReference;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapperPluginFinder;
import org.processmining.processtree.ProcessTree;

public class InductiveVisualMiner {

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"Event log", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, XLog xLog, ProMCanceller canceller)
			throws UnknownTreeNodeException {

		InductiveVisualMinerState state = new InductiveVisualMinerState(xLog, null);
		InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state,
				VisualMinerWrapperPluginFinder.find(context, state.getMiner()), true, canceller);
		new InductiveVisualMinerController(context, panel, state, canceller);

		return panel;
	}

	@Plugin(name = "Inductive visual Miner", level = PluginLevel.PeerReviewed, returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"Inductive visual Miner launcher", "canceller" }, userAccessible = true)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, final InductiveVisualMinerLauncher launcher,
			ProMCanceller canceller) throws UnknownTreeNodeException {

		//set launcher non-favourite
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(launcher).setFavorite(false);
		}

		XLog log = launcher.xLog.get();
		final InductiveVisualMinerState state;
		if (log == null) {
			throw new RuntimeException("The log has been removed by garbage collection.");
		}
		if (launcher.preMinedTree == null) {
			state = new InductiveVisualMinerState(log, null);
		} else {
			ProcessTree preMinedTree = launcher.preMinedTree.get();
			if (preMinedTree == null) {
				throw new RuntimeException("The pre-mined tree has been removed by garbage collection.");
			}
			state = new InductiveVisualMinerState(log, preMinedTree);
		}

		final InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state,
				VisualMinerWrapperPluginFinder.find(context, state.getMiner()), launcher.preMinedTree == null,
				canceller);
		new InductiveVisualMinerController(context, panel, state, canceller);

		return panel;
	}

	public class InductiveVisualMinerLauncher {
		public SoftReference<XLog> xLog;
		public SoftReference<ProcessTree> preMinedTree;

		public InductiveVisualMinerLauncher(XLog xLog) {
			this.xLog = new SoftReference<>(xLog);
			this.preMinedTree = null;
		}

		public InductiveVisualMinerLauncher(XLog xLog, ProcessTree preMinedTree) {
			this.xLog = new SoftReference<>(xLog);
			this.preMinedTree = new SoftReference<>(preMinedTree);
		}
	}

	@Plugin(name = "Mine with Inductive visual Miner", level = PluginLevel.PeerReviewed, returnLabels = { "Inductive visual Miner" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = { "Event log" }, userAccessible = true, categories = {
			PluginCategory.Discovery, PluginCategory.Analytics, PluginCategory.ConformanceChecking }, help = "Discover a process tree or a Petri net interactively using Inductive Miner.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl", pack = "InductiveVisualMiner")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0 })
	public InductiveVisualMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog) {
		return new InductiveVisualMinerLauncher(xLog);
	}

	@Plugin(name = "Visualise deviations on process tree", returnLabels = { "Deviations visualisation" }, returnTypes = { InductiveVisualMinerLauncher.class }, parameterLabels = {
			"Event log", "Process tree" }, userAccessible = true, categories = { PluginCategory.Analytics,
			PluginCategory.ConformanceChecking }, help = "Perform an alignment on a log and a process tree and visualise the results as Inductive visual Miner, including its filtering options.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl", pack = "InductiveVisualMiner")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0, 1 })
	public InductiveVisualMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog, ProcessTree preMinedTree) {
		return new InductiveVisualMinerLauncher(xLog, preMinedTree);
	}
}
