package org.processmining.plugins.inductiveVisualMiner.editModel;

import org.apache.commons.lang3.StringEscapeUtils;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;

public class Dfg2StringFields {

	public static String getStartActivities(DirectlyFollowsModel dfg) {
		StringBuilder result = new StringBuilder();
		for (int activity : dfg.getStartActivities()) {
			result.append(dfg.getActivityOfIndex(activity));
			result.append("\n");
		}
		return result.toString();
	}

	public static String getEndActivities(DirectlyFollowsModel dfg) {
		StringBuilder result = new StringBuilder();
		for (int activity : dfg.getEndActivities()) {
			result.append(dfg.getActivityOfIndex(activity));
			result.append("\n");
		}
		return result.toString();
	}

	public static String getEdges(DirectlyFollowsModel dfg) {
		StringBuilder result = new StringBuilder();
		for (long edge : dfg.getDirectlyFollowsGraph().getEdges()) {
			result.append(escapeNode(dfg.getActivityOfIndex(dfg.getDirectlyFollowsGraph().getEdgeSource(edge))));
			result.append(" -> ");
			result.append(escapeNode(dfg.getActivityOfIndex(dfg.getDirectlyFollowsGraph().getEdgeTarget(edge))));
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
