package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.tue.astar.AStarThread.Canceller;
import nl.tue.astar.Trace;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.conversion.ExpandProcessTree;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles.Transition;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.BehaviorCounter;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.ProcessTreeToNAryTree;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;
import org.processmining.plugins.etm.termination.ProMCancelTerminationCondition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
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
		XEventClassifier activityClassifier = MiningParametersIM.getDefaultClassifier();
		XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(activityClassifier);

		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, activityClassifier);
		XLogInfo logInfoPerformance = XLogInfoFactory.createLogInfo(log, performanceClassifier);

		return alignTree(tree, new XEventPerformanceClassifier(MiningParametersIM.getDefaultClassifier()), log,
				logInfo.getEventClasses(), logInfoPerformance.getEventClasses(),
				ProMCancelTerminationCondition.buildDummyCanceller()).log;
	}

	public static AlignmentResult alignTree(ProcessTree tree, XEventPerformanceClassifier performanceClassifier,
			XLog log, XEventClasses activityEventClasses, XEventClasses performanceEventClasses, Canceller canceller) {

		CentralRegistry registry = new CentralRegistry(log, performanceClassifier, new Random());

		//transform tree for performance measurement
		Pair<ProcessTree, Map<UnfoldedNode, UnfoldedNode>> p = ExpandProcessTree.expand(tree);
		ProcessTree performanceTree = p.getA();
		Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping = p.getB(); //mapping performance node -> original node

		//add the event classes of the tree manually
		addAllLeaves(registry.getEventClasses(), performanceTree.getRoot());

		ProcessTreeToNAryTree pt2nt = new ProcessTreeToNAryTree(registry.getEventClasses());
		NAryTree nTree = pt2nt.convert(performanceTree);

		//tell ETM that event classes were added
		registry.updateLogDerived();

		//perform the alignment
		FitnessReplay fr = new FitnessReplay(registry, canceller);
		fr.setNrThreads(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
		fr.setDetailedAlignmentInfoEnabled(true);
		fr.getFitness(nTree, null);
		BehaviorCounter behC = registry.getFitness(nTree).behaviorCounter;

		//create mapping int->(performance)unfoldedNode
		List<UnfoldedNode> l = TreeUtils.unfoldAllNodes(new UnfoldedNode(performanceTree.getRoot()));
		UnfoldedNode[] nodeId2performanceNode = l.toArray(new UnfoldedNode[l.size()]);

		//read moves and create aligned log
		AlignedLog alignedLog = new AlignedLog();
		for (Trace naryTrace : behC.getAlignments().keySet()) {

			AlignedTrace trace = new AlignedTrace();

			TreeRecord tr = behC.getAlignment(naryTrace);
			List<TreeRecord> naryMoves = TreeRecord.getHistory(tr);
			long cardinality = registry.getaStarAlgorithm().getTraceFreq(naryTrace);

			for (TreeRecord naryMove : naryMoves) {

				//get log part of move
				XEventClass performanceActivity = null;
				XEventClass activity = null;
				Transition lifeCycleTransition = null;
				if (naryMove.getMovedEvent() >= 0) {
					//an ETM-log-move happened
					performanceActivity = registry.getEventClassByID(naryTrace.get(naryMove.getMovedEvent()));
					activity = Performance.getActivity(performanceActivity, activityEventClasses);
					lifeCycleTransition = Performance.getLifeCycleTransition(performanceActivity);
				}

				//get model part of move
				UnfoldedNode performanceUnode = null;
				UnfoldedNode unode = null;
				if (naryMove.getModelMove() >= 0) {
					//an ETM-model-move happened
					performanceUnode = nodeId2performanceNode[naryMove.getModelMove()];
					unode = performanceNodeMapping.get(performanceUnode);
					lifeCycleTransition = Performance.getLifeCycleTransition(performanceUnode);

					if (performanceUnode.getNode() instanceof Automatic && unode.getNode() instanceof Manual) {
						//this is a tau that represents that the start of an activity is skipped
						lifeCycleTransition = Transition.start;
						activity = activityEventClasses.getByIdentity(unode.getNode().getName());
						performanceActivity = performanceEventClasses.getByIdentity(unode.getNode().getName()
								+ "+" + XLifecycleExtension.StandardModel.START);
					}

					//we are only interested in moves on leaves, not in moves on nodes
					if (!(performanceUnode.getNode() instanceof Manual)
							&& !(performanceUnode.getNode() instanceof Automatic)) {
						continue;
					}
				}

				if (performanceUnode != null || performanceActivity != null) {
					Move move;
					if (performanceUnode != null && performanceUnode.getNode() instanceof Automatic
							&& unode.getNode() instanceof Manual) {
						//tau-start
						move = new Move(Type.tauStart, unode, activity, performanceActivity, lifeCycleTransition);
					} else if ((performanceUnode != null && performanceActivity != null)
							|| (performanceUnode != null && performanceUnode.getNode() instanceof Automatic)) {
						//synchronous move
						move = new Move(Type.synchronous, unode, activity, performanceActivity, lifeCycleTransition);
					} else if (performanceUnode != null) {
						//model move
						move = new Move(Type.model, unode, activity, performanceActivity, lifeCycleTransition);
					} else {
						//log move
						if (lifeCycleTransition == Transition.start) {
							//log moves of start events are ignored
							move = new Move(Type.ignoredLogMove, null, activity, performanceActivity, lifeCycleTransition);
						} else {
							move = new Move(Type.log, unode, activity, performanceActivity, lifeCycleTransition);
						}
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
			classes.register(node.getName());
		} else if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				addAllLeaves(classes, child);
			}
		}
		classes.harmonizeIndices();
	}

}
