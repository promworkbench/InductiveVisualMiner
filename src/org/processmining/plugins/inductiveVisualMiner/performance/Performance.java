package org.processmining.plugins.inductiveVisualMiner.performance;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Performance {
	
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
		}
		return eventClasses.getByIdentity(s);
	}
}
