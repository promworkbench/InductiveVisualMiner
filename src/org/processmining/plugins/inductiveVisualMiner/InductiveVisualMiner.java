package org.processmining.plugins.inductiveVisualMiner;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.IM;
import org.processmining.processtree.ProcessTree;

public class InductiveVisualMiner {

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Event log" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final UIPluginContext context, XLog xLog) {

		InductiveVisualMinerState state = new InductiveVisualMinerState(xLog, null);
		InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, IM.getClassifiers(xLog), true);
		new InductiveVisualMinerController(context, panel, state);

		return panel;
	}

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Interactive Miner launcher" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final UIPluginContext context, final InteractiveMinerLauncher launcher) {

		//remove launcher
		context.getGlobalContext().getResourceManager().getResourceForInstance(launcher).destroy();

		final InductiveVisualMinerState state = new InductiveVisualMinerState(launcher.xLog, launcher.preMinedTree);
		
//		final InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, Classifiers.getClassifiers(launcher.xLog), launcher.preMinedTree == null);
		final InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, new XEventClassifier[]{}, launcher.preMinedTree == null);
		new InductiveVisualMinerController(context, panel, state);

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

	@Plugin(name = "Mine with Inductive visual Miner", returnLabels = { "Inductive visual Miner" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Event log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0 })
	public InteractiveMinerLauncher mineGuiProcessTree(UIPluginContext context, XLog xLog) {
		return new InteractiveMinerLauncher(xLog);
	}
	
	@Plugin(name = "Visualise deviations on process tree", returnLabels = { "Deviations visualisation" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Event log", "Process tree" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0, 1 })
	public InteractiveMinerLauncher mineGuiProcessTree(UIPluginContext context, XLog xLog, ProcessTree preMinedTree) {
		return new InteractiveMinerLauncher(xLog, preMinedTree);
	}
}
