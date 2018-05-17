package org.processmining.plugins.inductiveVisualMiner.export;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.inductiveVisualMiner.alignment.Dfg2AcceptingPetriNet;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.processtree.ProcessTree;

public class ExportModel {

	/*
	 * Store process tree
	 */
	public static void exportProcessTree(PluginContext context, ProcessTree tree, String name) {
		context.getProvidedObjectManager().createProvidedObject("Process tree of " + name, tree, ProcessTree.class,
				context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(tree)
					.setFavorite(true);
		}
	}

	public static void exportEfficientTree(PluginContext context, EfficientTree tree, String name) {
		context.getProvidedObjectManager().createProvidedObject("Efficient tree of " + name, tree, EfficientTree.class,
				context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(tree)
					.setFavorite(true);
		}
	}

	public static void exportAcceptingPetriNet(PluginContext context, IvMModel model, String name,
			final ProMCanceller canceller) {
		AcceptingPetriNet net;
		if (model.isTree()) {
			net = EfficientTree2AcceptingPetriNet.convert(model.getTree());
		} else {
			net = Dfg2AcceptingPetriNet.convertForPerformance(model.getDfg()).getA();
		}
		ReduceAcceptingPetriNetKeepLanguage.reduce(net, new Canceller() {
			public boolean isCancelled() {
				return canceller.isCancelled();
			}
		});

		context.getProvidedObjectManager().createProvidedObject("Accepting Petri net of " + name, net,
				AcceptingPetriNet.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(net)
					.setFavorite(true);
		}
	}

	/*
	 * Store Petri net
	 */
	public static void exportPetrinet(PluginContext context, IvMModel model, String name,
			final ProMCanceller canceller) {
		AcceptingPetriNet pnwm;
		if (model.isTree()) {
			pnwm = EfficientTree2AcceptingPetriNet.convert(model.getTree());
		} else {
			pnwm = Dfg2AcceptingPetriNet.convert(model.getDfg());
		}

		ReduceAcceptingPetriNetKeepLanguage.reduce(pnwm, new Canceller() {
			public boolean isCancelled() {
				return canceller.isCancelled();
			}
		});

		context.getProvidedObjectManager().createProvidedObject("Petri net of " + name, pnwm.getNet(), Petrinet.class,
				context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(pnwm.getNet())
					.setFavorite(true);
		}
		context.getProvidedObjectManager().createProvidedObject("Initial marking of " + name, pnwm.getInitialMarking(),
				Marking.class, context);
		context.getProvidedObjectManager().createProvidedObject("Final marking of " + name,
				pnwm.getFinalMarkings().iterator().next(), Marking.class, context);
		context.addConnection(new InitialMarkingConnection(pnwm.getNet(), pnwm.getInitialMarking()));
		context.addConnection(new FinalMarkingConnection(pnwm.getNet(), pnwm.getFinalMarkings().iterator().next()));
	}

}
