package org.processmining.plugins.inductiveVisualMiner.performance;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles.Transition;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Performance {

	public static Transition getLifeCycleTransition(XEventClass performanceActivity) {
		return getLifeCycleTransition(performanceActivity.getId());
	}
	
	public static Transition getLifeCycleTransition(UnfoldedNode performanceUnode) {
		return getLifeCycleTransition(performanceUnode.getNode().getName());
	}

	public static Transition getLifeCycleTransition(String performanceActivity) {
		if (performanceActivity.endsWith("+start")) {
			return Transition.start;
		} else if (performanceActivity.endsWith("complete")) {
			return Transition.complete;
		} else {
			return Transition.other;
		}
	}

	public static boolean isStart(UnfoldedNode performanceUnode) {
		return performanceUnode.getNode().getName().endsWith("+start");
	}

	public static boolean isStart(XEventClass performanceActivity) {
		return performanceActivity.getId().endsWith("+start");
	}

	public static String getActivity(UnfoldedNode unode) {
		String s = unode.getNode().getName();
		if (s.endsWith("+start")) {
			return s.substring(0, s.lastIndexOf("+start"));
		} else if (s.endsWith("+complete")) {
			return s.substring(0, s.lastIndexOf("+complete"));
		}
		return s;
	}

	public static XEventClass getActivity(XEventClass performanceActivity, XEventClasses eventClasses) {
		String s = performanceActivity.getId();
		if (s.endsWith("+start")) {
			s = s.substring(0, s.lastIndexOf("+start"));
		} else if (s.endsWith("+complete")) {
			s = s.substring(0, s.lastIndexOf("+complete"));
		} else if (s.contains("+")) {
			s = s.substring(0, s.lastIndexOf("+"));
		}
		return eventClasses.getByIdentity(s);
	}
}
