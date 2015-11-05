package org.processmining.plugins.inductiveVisualMiner;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.Classifiers;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapperPluginFinder;
import org.processmining.processtree.ProcessTree;

public class InductiveVisualMiner {

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"Event log", "canceller" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final UIPluginContext context, XLog xLog, ProMCanceller canceller) {

		InductiveVisualMinerState state = new InductiveVisualMinerState(xLog, null);
		InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state,
				Classifiers.getClassifiers(xLog), VisualMinerWrapperPluginFinder.find(context, state.getMiner()), true,
				canceller);
		new InductiveVisualMinerController(context, panel, state, canceller);

		return panel;
	}

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"Interactive Miner launcher", "canceller" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, final InteractiveMinerLauncher launcher,
			ProMCanceller canceller) {

		//remove launcher
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(launcher)
					.destroy();
		}

		final InductiveVisualMinerState state = new InductiveVisualMinerState(launcher.xLog, launcher.preMinedTree);

		final InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state,
				Classifiers.getClassifiers(launcher.xLog), VisualMinerWrapperPluginFinder.find(context,
						state.getMiner()), launcher.preMinedTree == null, canceller);
		new InductiveVisualMinerController(context, panel, state, canceller);

		return panel;
	}

	public class InteractiveMinerLauncher {
		public XLog xLog;
		public ProcessTree preMinedTree;

		public InteractiveMinerLauncher(XLog xLog) {
			this.xLog = xLog;
			this.preMinedTree = null;
		}

		public InteractiveMinerLauncher(XLog xLog, ProcessTree preMinedTree) {
			this.xLog = xLog;
			this.preMinedTree = preMinedTree;
		}
	}

	@Plugin(name = "Mine with Inductive visual Miner", returnLabels = { "Inductive visual Miner" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Event log" }, userAccessible = true, categories = {
			PluginCategory.Discovery, PluginCategory.Analytics, PluginCategory.ConformanceChecking }, help = "Discover a process tree or a Petri net interactively using Inductive Miner.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl", pack = "InductiveVisualMiner")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0 })
	public InteractiveMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog) {
		return new InteractiveMinerLauncher(xLog);
	}

	@Plugin(name = "Visualise deviations on process tree", returnLabels = { "Deviations visualisation" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = {
			"Event log", "Process tree" }, userAccessible = true, categories = { PluginCategory.Analytics,
			PluginCategory.ConformanceChecking }, help = "Perform an alignment on a log and a process tree and visualise the results as Inductive visual Miner, including its filtering options.")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl", pack = "InductiveVisualMiner")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0, 1 })
	public InteractiveMinerLauncher mineGuiProcessTree(PluginContext context, XLog xLog, ProcessTree preMinedTree) {
		return new InteractiveMinerLauncher(xLog, preMinedTree);
	}
}
