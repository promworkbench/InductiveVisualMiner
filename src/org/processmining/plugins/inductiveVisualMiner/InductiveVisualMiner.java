package org.processmining.plugins.inductiveVisualMiner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class InductiveVisualMiner {

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Event log" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final PluginContext context, XLog xLog) throws IOException {

		List<XEventClassifier> classifiers = getClassifiers(xLog);

		InductiveVisualMinerState state = new InductiveVisualMinerState(xLog);
		InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, classifiers, false);
		new InductiveVisualMinerController(context, panel, state);

		return panel;
	}

	@Plugin(name = "Inductive visual Miner", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Interactive Miner launcher" }, userAccessible = false)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualise(final UIPluginContext context, final InteractiveMinerLauncher launcher)
			throws IOException {

		//remove launcher
		context.getGlobalContext().getResourceManager().getResourceForInstance(launcher).destroy();
		
		List<XEventClassifier> classifiers = getClassifiers(launcher.xLog);

		final InductiveVisualMinerState state = new InductiveVisualMinerState(launcher.xLog);
		final InductiveVisualMinerPanel panel = new InductiveVisualMinerPanel(context, state, classifiers, true);
		new InductiveVisualMinerController(context, panel, state);

		//set up action listener to store model afterwards
		panel.getExitButton().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				//store the resulting Process tree or Petri net
				String name = XConceptExtension.instance().extractName(launcher.xLog);
				ProcessTree tree = state.getTree();
				if (launcher.returnType.equals(ReturnType.processTree)) {
					//store Process tree

					context.getProvidedObjectManager().createProvidedObject("Process tree of " + name, tree,
							ProcessTree.class, context);
					context.getGlobalContext().getResourceManager().getResourceForInstance(tree).setFavorite(true);
				} else {
					//store Petri net
					try {
						PetrinetWithMarkings pnwm = ProcessTree2Petrinet.convert(tree, false);
						context.getProvidedObjectManager().createProvidedObject("Petri net of " + name, pnwm.petrinet,
								Petrinet.class, context);
						context.getGlobalContext().getResourceManager().getResourceForInstance(pnwm.petrinet).setFavorite(true);
						context.getProvidedObjectManager().createProvidedObject("Initial marking of " + name, pnwm.initialMarking,
								Marking.class, context);
						context.getProvidedObjectManager().createProvidedObject("Final marking of " + name, pnwm.finalMarking,
								Marking.class, context);
					} catch (NotYetImplementedException e) {
						e.printStackTrace();
					} catch (InvalidProcessTreeException e) {
						e.printStackTrace();
					}
				}

				//todo: cancel/exit the plugin
			}
		});

		return panel;
	}

	public enum ReturnType {
		processTree, PetriNet
	}

	public class InteractiveMinerLauncher {
		public XLog xLog;
		public ReturnType returnType;

		public InteractiveMinerLauncher(XLog xLog, ReturnType returnType) {
			this.xLog = xLog;
			this.returnType = returnType;
		}
	}

	@Plugin(name = "Mine Process Tree with Inductive Visual Miner", returnLabels = { "Interactive Miner launcher" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process tree, dialog", requiredParameterLabels = { 0 })
	public InteractiveMinerLauncher mineGuiProcessTree(UIPluginContext context, XLog xLog) {
		return new InteractiveMinerLauncher(xLog, ReturnType.processTree);
	}

	@Plugin(name = "Mine Petri net with Inductive Visual Miner", returnLabels = { "Interactive Miner launcher" }, returnTypes = { InteractiveMinerLauncher.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process tree, dialog", requiredParameterLabels = { 0 })
	public InteractiveMinerLauncher mineGuiPetriNet(UIPluginContext context, XLog xLog) {
		return new InteractiveMinerLauncher(xLog, ReturnType.PetriNet);
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
