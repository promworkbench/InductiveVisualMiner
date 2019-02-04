package org.processmining.plugins.inductiveVisualMiner.export;

import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.inductiveVisualMiner.alignment.Dfm2AcceptingPetriNet;
import org.processmining.plugins.inductiveVisualMiner.alignment.ExpandProcessTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

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
			net = Dfm2AcceptingPetriNet.convert(model.getDfg());
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

	public static void exportExpandedAcceptingPetriNet(PluginContext context, IvMModel model, String name,
			final ProMCanceller canceller) {
		AcceptingPetriNet net;
		if (model.isTree()) {
			Triple<ProcessTree, Map<UnfoldedNode, UnfoldedNode>, Set<UnfoldedNode>> t = ExpandProcessTree
					.expand(model.getTree().getDTree());
			IvMEfficientTree performanceTree = new IvMEfficientTree(t.getA());
			net = EfficientTree2AcceptingPetriNet.convert(performanceTree);
		} else {
			net = Dfm2AcceptingPetriNet.convertForPerformance(model.getDfg()).getA();
		}
		ReduceAcceptingPetriNetKeepLanguage.reduce(net, new Canceller() {
			public boolean isCancelled() {
				return canceller.isCancelled();
			}
		});

		context.getProvidedObjectManager().createProvidedObject("Expanded accepting Petri net of " + name, net,
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
			pnwm = Dfm2AcceptingPetriNet.convert(model.getDfg());
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

	public static void exportDirectlyFollowsModel(PluginContext context, IvMModel model, String name) {
		context.getProvidedObjectManager().createProvidedObject("Directly follows model of " + name, model.getDfg(),
				DirectlyFollowsModel.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(model.getDfg())
					.setFavorite(true);
		}
	}

}
