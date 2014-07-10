package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class ComputeAlignment {
	public static Pair<AlignmentResult, Map<UnfoldedNode, AlignedLogInfo>> computeAlignment(ProcessTree tree,
			XEventClassifier classifier, XLog xLog, Set<XEventClass> filteredActivities, IMLogInfo logInfo,
			Canceller canceller) {

		//ETM
		AlignmentResult alignment = AlignmentETM.alignTree(tree, classifier, xLog, filteredActivities, canceller);

		//		if (alignment.log.size() == 0) {
		//			//Felix
		//			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(xLog, classifier);
		//			alignment = AlignmentFelix.alignTree(tree, classifier, logInfo, xLog, xLogInfo, filteredActivities);
		//		}

		//Arya
		//		AlignmentResult alignment = AlignmentArya.alignTree(tree, classifier, logInfo, null, xLog, filteredActivities);

		Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos = computeDfgAlignment(alignment.log, tree);

		return Pair.of(alignment, dfgLogInfos);
	}
	
	public static Map<UnfoldedNode, AlignedLogInfo> computeDfgAlignment(AlignedLog log, Set<UnfoldedNode> dfgNodes) {
		Map<UnfoldedNode, AlignedLogInfo> result = new HashMap<ProcessTree2Petrinet.UnfoldedNode, AlignedLogInfo>();

		for (UnfoldedNode unode : dfgNodes) {
			result.put(unode, new AlignedLogInfo(AlignedLogSplitter.getLog(unode, log)));
		}

		return result;
	}
	
	public static Map<UnfoldedNode, AlignedLogInfo> computeDfgAlignment(AlignedLog log, ProcessTree tree) {
		Map<UnfoldedNode, AlignedLogInfo> result = new HashMap<ProcessTree2Petrinet.UnfoldedNode, AlignedLogInfo>();

		for (UnfoldedNode unode : AlignedLogMetrics.getAllDfgNodes(new UnfoldedNode(tree.getRoot()))) {
			result.put(unode, new AlignedLogInfo(AlignedLogSplitter.getLog(unode, log)));
		}

		return result;
	}
}
