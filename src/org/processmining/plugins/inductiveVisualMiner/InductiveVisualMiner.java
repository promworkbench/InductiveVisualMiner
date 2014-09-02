package org.processmining.plugins.inductiveVisualMiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class InductiveVisualMiner {

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Event log" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final UIPluginContext context, XLog xLog) {

		List<XEventClassifier> classifiers = getClassifiers(xLog);

		InductiveVisualMinerState state = new InductiveVisualMinerState(xLog);
		InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, classifiers);
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
		
		List<XEventClassifier> classifiers = getClassifiers(launcher.xLog);

		final InductiveVisualMinerState state = new InductiveVisualMinerState(launcher.xLog);
		
		final InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, classifiers);
		new InductiveVisualMinerController(context, panel, state);

		return panel;
	}

	public class InteractiveMinerLauncher {
		public XLog xLog;

		public InteractiveMinerLauncher(XLog xLog) {
			this.xLog = xLog;
		}
	}

	@Plugin(name = "Mine with Inductive visual Miner", returnLabels = { "Interactive Miner launcher" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine, dialog", requiredParameterLabels = { 0 })
	public InteractiveMinerLauncher mineGuiProcessTree(UIPluginContext context, XLog xLog) {
		return new InteractiveMinerLauncher(xLog);
	}

	//make xloginfo to obtain a list of classifiers
	public List<XEventClassifier> getClassifiers(XLog xLog) {
		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(xLog);
		List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>(xLogInfo.getEventClassifiers());
		classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
		Collections.sort(classifiers, new Comparator<XEventClassifier>() {
			public int compare(XEventClassifier o1, XEventClassifier o2) {
				return o1.name().compareTo(o2.name());
			}
		});
		return classifiers;
	}
}
