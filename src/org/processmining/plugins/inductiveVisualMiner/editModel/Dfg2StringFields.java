package org.processmining.plugins.inductiveVisualMiner.editModel;

import org.apache.commons.lang3.StringEscapeUtils;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;

public class Dfg2StringFields {

	public static String getStartActivities(Dfg dfg) {
		StringBuilder result = new StringBuilder();
		for (XEventClass activity : dfg.getStartActivities()) {
			result.append(activity);
			result.append("\n");
		}
		return result.toString();
	}

	public static String getEndActivities(Dfg dfg) {
		StringBuilder result = new StringBuilder();
		for (XEventClass activity : dfg.getEndActivities()) {
			result.append(activity);
			result.append("\n");
		}
		return result.toString();
	}

	public static String getEdges(Dfg dfg) {
		StringBuilder result = new StringBuilder();
		for (long edge : dfg.getDirectlyFollowsEdges()) {
			result.append(escapeNode(dfg.getDirectlyFollowsEdgeSource(edge).getId()));
			result.append(" -> ");
			result.append(escapeNode(dfg.getDirectlyFollowsEdgeTarget(edge).getId()));
			result.append("\n");
		}
		return result.toString();
	}

	public static String escapeNode(String name) {
		name = StringEscapeUtils.escapeCsv(name);
		if (name.contains("->") || name.contains("'")) {
			return ("\"" + name + "\"");
		} else {
			return name;
		}
	}
}
