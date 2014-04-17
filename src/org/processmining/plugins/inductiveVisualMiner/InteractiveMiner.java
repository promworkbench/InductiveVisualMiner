package org.processmining.plugins.inductiveVisualMiner;

import java.io.IOException;
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
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class InteractiveMiner {
	
	@Plugin(name = "  Interactive Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Event log" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final PluginContext context, XLog xLog) throws IOException {

		List<XEventClassifier> classifiers = getClassifiers(xLog);

		InteractiveMinerState state = new InteractiveMinerState(xLog);
		InteractiveMinerPanel panel = new InteractiveMinerPanel(context, state, classifiers, false);
		new InteractiveMinerController(context, panel, state);

		return panel;
	}

	@Plugin(name = "Interactive Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Interactive Miner launcher" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final PluginContext context, InteractiveMinerLauncher launcher) throws IOException {

		List<XEventClassifier> classifiers = getClassifiers(launcher.xLog);

		InteractiveMinerState state = new InteractiveMinerState(launcher.xLog);
		InteractiveMinerPanel panel = new InteractiveMinerPanel(context, state, classifiers, true);
		new InteractiveMinerController(context, panel, state);

		return panel;
	}

	public class InteractiveMinerLauncher {
		public XLog xLog;
		
		public InteractiveMinerLauncher(XLog xLog) {
			this.xLog = xLog;
		}
	}

	@Plugin(name = "Mine Process Tree with Interactive Miner", returnLabels = { "Interactive Miner launcher" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process tree, dialog", requiredParameterLabels = { 0 })
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
