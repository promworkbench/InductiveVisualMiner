package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.inductiveVisualMiner.editModel.DfgEdgeNodiser.NodeType;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsdImpl;

public class DfgParser {

	public static Triple<DirectlyFollowsModel, Integer, String> parse(String startActivities, String edges,
			String endActivities, boolean emptyTraces) throws IOException {
		DirectlyFollowsModel dfg = new DfgMsdImpl();

		//start activities
		DfgActivityNodiser startActivityNodiser = new DfgActivityNodiser(startActivities);
		parseStartActivities(startActivityNodiser, dfg);

		//edges
		DfgEdgeNodiser edgeNodiser = new DfgEdgeNodiser(edges);
		Pair<Integer, String> p = parseEdges(edgeNodiser, dfg);
		if (p.getA() >= 0) {
			return Triple.of(null, p.getA(), p.getB());
		}

		//start activities
		DfgActivityNodiser endActivityNodiser = new DfgActivityNodiser(endActivities);
		parseEndActivities(endActivityNodiser, dfg);

		//empty traces
		if (emptyTraces) {
			dfg.setNumberOfEmptyTraces(1);
		}

		return Triple.of(dfg, -1, null);
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
	public static Pair<Integer, String> parseEdges(DfgEdgeNodiser nodiser, DirectlyFollowsModel dfg)
			throws IOException {

		while (nodiser.nextNode()) {
			if (nodiser.getLastNodeType() != NodeType.activity) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected an activity.");
			}
			String source = nodiser.getLastActivity();
			int sourceIndex = dfg.addActivity(source);

			if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.edgeSymbol) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected ->.");
			}

			if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.activity) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected an activity.");
			}
			String target = nodiser.getLastActivity();
			int targetIndex = dfg.addActivity(target);

			long cardinality;
			if (nodiser.nextNode()) {
				if (nodiser.getLastNodeType() == NodeType.multiplicitySymbol) {
					//multiplicity coming
					if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.activity) {
						return Pair.of(nodiser.getLastLineNumber(), "Expected a cardinality (number).");
					}
					String number = nodiser.getLastActivity();
					if (!NumberUtils.isParsable(number) || StringUtils.contains(number, ".")) {
						return Pair.of(nodiser.getLastLineNumber(), "Expected a cardinality (number).");
					}
					cardinality = Long.parseLong(number);
				} else {
					nodiser.pushBack();
					cardinality = 1;
				}
			} else {
				cardinality = 1;
			}

			dfg.getDirectlyFollowsGraph().addEdge(sourceIndex, targetIndex, cardinality);
		}

		return Pair.of(-1, null);
	}

	public static void parseStartActivities(DfgActivityNodiser nodiser, DirectlyFollowsModel dfg) throws IOException {
		while (nodiser.nextNode()) {
			String source = nodiser.getLastActivity();
			int index = dfg.addActivity(source);
			dfg.getStartActivities().add(index);
		}
	}

	public static void parseEndActivities(DfgActivityNodiser nodiser, DirectlyFollowsModel dfg) throws IOException {
		while (nodiser.nextNode()) {
			String source = nodiser.getLastActivity();
			int index = dfg.addActivity(source);
			dfg.getEndActivities().add(index);
		}
	}
}