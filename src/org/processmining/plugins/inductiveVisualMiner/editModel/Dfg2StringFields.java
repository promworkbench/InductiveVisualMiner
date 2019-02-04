package org.processmining.plugins.inductiveVisualMiner.editModel;

import org.apache.commons.lang3.StringEscapeUtils;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;

public class Dfg2StringFields {

	public static TIntObjectMap<String> getActivity2Node(DirectlyFollowsModel dfg) {
		TIntObjectHashMap<String> result = new TIntObjectHashMap<>(10, 0.5f, -1);

		for (String activity : dfg.getAllActivities()) {
			TIntSet a = dfg.getIndexOfActivity(activity);

			if (a.size() == 1) {
				result.put(a.iterator().next(), escapeNode(activity));
			} else {
				int count = 0;
				for (TIntIterator it = a.iterator(); it.hasNext();) {
					int index = it.next();
					result.put(index, "\"" + activity + "\"#" + count);
					count++;
				}
			}
		}
		return result;
	}

	public static String getStartActivities(DirectlyFollowsModel dfg, TIntObjectMap<String> activity2node) {
		StringBuilder result = new StringBuilder();
		for (TIntIterator it = dfg.getStartActivities().iterator(); it.hasNext();) {
			int activity = it.next();
			result.append(escapeNode(dfg.getActivityOfIndex(activity)));
			result.append("\n");
		}
		return result.toString();
	}

	public static String getEndActivities(DirectlyFollowsModel dfg, TIntObjectMap<String> activity2node) {
		StringBuilder result = new StringBuilder();
		for (TIntIterator it = dfg.getEndActivities().iterator(); it.hasNext();) {
			int activity = it.next();
			result.append(activity2node.get(activity));
			result.append("\n");
		}
		return result.toString();
	}

	public static String getEdges(DirectlyFollowsModel dfg, TIntObjectMap<String> activity2node) {
		StringBuilder result = new StringBuilder();
		for (long edge : dfg.getEdges()) {
			result.append(activity2node.get(dfg.getEdgeSource(edge)));
			result.append(" -> ");
			result.append(activity2node.get(dfg.getEdgeTarget(edge)));
			result.append("\n");
		}
		return result.toString();
	}

	public static String escapeNode(String name) {
		name = StringEscapeUtils.escapeCsv(name);
		if (name.contains("->") || name.contains("'") || name.contains(" ")) {
			return ("\"" + name + "\"");
		} else {
			return name;
		}
	}
}
