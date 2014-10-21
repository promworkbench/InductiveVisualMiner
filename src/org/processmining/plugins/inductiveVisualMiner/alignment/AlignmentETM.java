package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nl.tue.astar.AStarThread.Canceller;
import nl.tue.astar.Trace;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XEventImpl;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.BehaviorCounter;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.ProcessTreeToNAryTree;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;
import org.processmining.plugins.etm.termination.ProMCancelTerminationCondition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class AlignmentETM {

	@Plugin(name = "Replay log on process tree using ETM", returnLabels = { "Aligned log" }, returnTypes = { AlignedLog.class }, parameterLabels = {
			"Process tree", "log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Batch compare miners, default", requiredParameterLabels = { 0, 1 })
	public AlignedLog alignTree(PluginContext context, ProcessTree tree, XLog log) {
		return alignTree(tree, MiningParametersIM.getDefaultClassifier(), log, new HashSet<XEventClass>(),
				ProMCancelTerminationCondition.buildDummyCanceller()).log;
	}

	public static String debug(XEventClasses ev) {
		String result = "";
		for (int i = 0; i < ev.size(); i++) {
			result += ev.getByIndex(i) + ", ";
		}
		System.out.println(ev.getByIdentity("a"));
		System.out.println(ev.getByIdentity("_"));
		return result;
	}
	
	public static AlignmentResult alignTree(ProcessTree tree, XEventClassifier classifier, XLog log,
			Set<XEventClass> skipActivities, Canceller canceller) {

		XEventClasses ev = XLogInfoFactory.createLogInfo(log, classifier).getEventClasses();

		CentralRegistry registry = new CentralRegistry(log, classifier, new Random());
		
		//add the event classes of the tree manually
		addAllLeaves(registry.getEventClasses(), tree.getRoot());

		ProcessTreeToNAryTree pt2nt = new ProcessTreeToNAryTree(registry.getEventClasses());
		NAryTree nTree = pt2nt.convert(tree);

		registry.updateLogDerived();

		FitnessReplay fr = new FitnessReplay(registry, canceller);
		fr.setNrThreads(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
		fr.setDetailedAlignmentInfoEnabled(true);
		fr.getFitness(nTree, null);
		BehaviorCounter behC = registry.getFitness(nTree).behaviorCounter;

		//create mapping int->unfoldedNode
		List<UnfoldedNode> l = AlignedLogMetrics.unfoldAllNodes(new UnfoldedNode(tree.getRoot()));
		UnfoldedNode[] nodes = l.toArray(new UnfoldedNode[l.size()]);

		//read moves and create aligned log
		AlignedLog alignedLog = new AlignedLog();
		for (Trace naryTrace : behC.getAlignments().keySet()) {

			AlignedTrace trace = new AlignedTrace();

			TreeRecord tr = behC.getAlignment(naryTrace);
			List<TreeRecord> naryMoves = TreeRecord.getHistory(tr);
			long cardinality = registry.getaStarAlgorithm().getTraceFreq(naryTrace);

			for (TreeRecord naryMove : naryMoves) {

				UnfoldedNode unode = null;
				if (naryMove.getModelMove() >= 0) {
					//an ETM-model-move happened
					unode = nodes[naryMove.getModelMove()];
					if (!(unode.getNode() instanceof Manual) && !(unode.getNode() instanceof Automatic)) {
						unode = null;
					}
				}

				XEventClass activity = null;
				if (naryMove.getMovedEvent() >= 0) {
					//an ETM-log-move happened
					activity = registry.getEventClassByID(naryTrace.get(naryMove.getMovedEvent()));
				}

				if (unode != null || activity != null) {
					Move move;
					if ((unode != null && activity != null) || (unode != null && unode.getNode() instanceof Automatic)) {
						//synchronous move
						move = new Move(Type.synchronous, unode, activity);
					} else if (unode != null) {
						//model move
						move = new Move(Type.model, unode, activity);
					} else {
						//log move
						move = new Move(Type.log, unode, activity);
					}
					trace.add(move);
				}
			}
			alignedLog.add(trace, cardinality);
		}

		AlignedLogInfo alignedLogInfo = new AlignedLogInfo(alignedLog);


		return new AlignmentResult(alignedLog, alignedLogInfo);
	}
	
	public static void addAllLeaves(XEventClasses classes, Node node) {
		if (node instanceof Manual) {
			XEvent event = new XEventImpl();
			XConceptExtension.instance().assignName(event, node.getName());
			classes.register(event);
		} else if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				addAllLeaves(classes, child);
			}
		}
		classes.harmonizeIndices();
	}
}
