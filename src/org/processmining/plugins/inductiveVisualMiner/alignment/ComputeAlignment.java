package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Set;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.processtree.ProcessTree;

public class ComputeAlignment {
	public static AlignmentResult computeAlignment(ProcessTree tree,
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

		return alignment;
	}
}
