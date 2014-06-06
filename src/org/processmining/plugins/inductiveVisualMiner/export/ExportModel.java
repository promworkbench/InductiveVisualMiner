package org.processmining.plugins.inductiveVisualMiner.export;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class ExportModel {
	
	/*
	 * Store process tree
	 */
	public static void exportProcessTree(UIPluginContext context, ProcessTree tree, String name) {
		context.getProvidedObjectManager().createProvidedObject("Process tree of " + name, tree,
				ProcessTree.class, context);
		context.getGlobalContext().getResourceManager().getResourceForInstance(tree).setFavorite(true);
	}
	
	/*
	 * Store Petri net
	 */
	public static void exportPetrinet(UIPluginContext context, ProcessTree tree, String name) {
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
	
}
