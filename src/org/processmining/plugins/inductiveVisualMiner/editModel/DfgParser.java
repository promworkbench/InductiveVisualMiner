package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.io.IOException;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.inductiveVisualMiner.editModel.DfgEdgeNodiser.NodeType;

import gnu.trove.map.hash.THashMap;

public class DfgParser {

	public static Triple<Dfg, Integer, String> parse(String startActivities, String edges, String endActivities)
			throws IOException {
		Map<String, XEventClass> map = new THashMap<>();
		Dfg dfg = new DfgImpl();

		//start activities
		DfgActivityNodiser startActivityNodiser = new DfgActivityNodiser(startActivities);
		parseStartActivities(startActivityNodiser, dfg, map);

		//edges
		DfgEdgeNodiser edgeNodiser = new DfgEdgeNodiser(edges);
		Pair<Integer, String> p = parseEdges(edgeNodiser, dfg, map);
		if (p.getA() >= 0) {
			return Triple.of(null, p.getA(), p.getB());
		}

		//start activities
		DfgActivityNodiser endActivityNodiser = new DfgActivityNodiser(endActivities);
		parseEndActivities(endActivityNodiser, dfg, map);

		return Triple.of(dfg, -1, null);
	}

	public static XEventClass registerActivity(Map<String, XEventClass> map, String activity) {
		if (map.containsKey(activity)) {
			return map.get(activity);
		}
		map.put(activity, new XEventClass(activity, map.size()));
		return map.get(activity);
	}

	/**
	 * Parse the next node using nodiser.
	 * 
	 * @param nodiser
	 * @param dfg
	 * @return A triple, in which the first item denotes the parsed dfg. If
	 *         parsing failed, this is null, and the second element contains the
	 *         line number where parsing failed, and the third element contains
	 *         an error message.
	 * @throws IOException
	 */
	public static Pair<Integer, String> parseEdges(DfgEdgeNodiser nodiser, Dfg dfg, Map<String, XEventClass> map)
			throws IOException {

		while (nodiser.nextNode()) {
			if (nodiser.getLastNodeType() != NodeType.activity) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected an activity.");
			}
			XEventClass source = registerActivity(map, nodiser.getLastActivity());
			dfg.addActivity(source);

			if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.edgeSymbol) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected ->.");
			}

			if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.activity) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected an activity.");
			}
			XEventClass target = registerActivity(map, nodiser.getLastActivity());
			dfg.addActivity(target);

			dfg.addDirectlyFollowsEdge(source, target, 1);
		}

		return Pair.of(-1, null);
	}

	public static void parseStartActivities(DfgActivityNodiser nodiser, Dfg dfg, Map<String, XEventClass> map)
			throws IOException {
		while (nodiser.nextNode()) {
			XEventClass source = registerActivity(map, nodiser.getLastActivity());
			dfg.addActivity(source);
			dfg.addStartActivity(source, 1);
		}
	}

	public static void parseEndActivities(DfgActivityNodiser nodiser, Dfg dfg, Map<String, XEventClass> map)
			throws IOException {
		while (nodiser.nextNode()) {
			XEventClass source = registerActivity(map, nodiser.getLastActivity());
			dfg.addActivity(source);
			dfg.addEndActivity(source, 1);
		}
	}
}