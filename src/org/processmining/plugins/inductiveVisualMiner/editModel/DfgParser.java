package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.io.IOException;

import org.processmining.directlyfollowsmodelminer.mining.CheckSoundness;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModelImplQuadratic;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.editModel.DfgEdgeNodiser.NodeType;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class DfgParser {

	public static Triple<DirectlyFollowsModel, Integer, String> parse(String startActivities, String edges,
			String endActivities, boolean emptyTraces) throws IOException {
		DirectlyFollowsModel dfg = new DirectlyFollowsModelImplQuadratic();
		TObjectIntMap<Pair<String, Integer>> userIndex2dfmIndex = new TObjectIntHashMap<>(10, 0.5f, -1);

		//start activities
		DfgActivityNodiser startActivityNodiser = new DfgActivityNodiser(startActivities);
		parseStartActivities(startActivityNodiser, dfg, userIndex2dfmIndex);

		//edges
		DfgEdgeNodiser edgeNodiser = new DfgEdgeNodiser(edges);
		Pair<Integer, String> p = parseEdges(edgeNodiser, dfg, userIndex2dfmIndex);
		if (p.getA() >= 0) {
			return Triple.of(dfg, p.getA(), p.getB());
		}

		//start activities
		DfgActivityNodiser endActivityNodiser = new DfgActivityNodiser(endActivities);
		parseEndActivities(endActivityNodiser, dfg, userIndex2dfmIndex);

		//empty traces
		if (emptyTraces) {
			dfg.setEmptyTraces(true);
		}

		String issues = CheckSoundness.findIssues(dfg);
		if (issues != null) {
			return Triple.of(dfg, -1, issues);
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
	public static Pair<Integer, String> parseEdges(DfgEdgeNodiser nodiser, DirectlyFollowsModel dfg,
			TObjectIntMap<Pair<String, Integer>> userIndex2dfgIndex) throws IOException {

		while (nodiser.nextNode()) {
			if (nodiser.getLastNodeType() != NodeType.activity) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected an activity.");
			}

			//get the activity and its index, and add it to the dfm 
			String source = nodiser.getLastActivity();
			Pair<String, Integer> pSource = Pair.of(source, nodiser.getLastActivityIndex());
			int sourceIndex;
			if (userIndex2dfgIndex.containsKey(pSource)) {
				sourceIndex = userIndex2dfgIndex.get(pSource);
			} else {
				sourceIndex = dfg.addNode(source);
				userIndex2dfgIndex.put(pSource, sourceIndex);
			}

			if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.edgeSymbol) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected ->.");
			}

			if (!nodiser.nextNode() || nodiser.getLastNodeType() != NodeType.activity) {
				return Pair.of(nodiser.getLastLineNumber(), "Expected an activity.");
			}

			//get the activity and it's index, and add it to the dfm
			String target = nodiser.getLastActivity();
			Pair<String, Integer> pTarget = Pair.of(target, nodiser.getLastActivityIndex());
			int targetIndex;
			if (userIndex2dfgIndex.containsKey(pTarget)) {
				targetIndex = userIndex2dfgIndex.get(pTarget);
			} else {
				targetIndex = dfg.addNode(target);
				userIndex2dfgIndex.put(pTarget, targetIndex);
			}

			dfg.addEdge(sourceIndex, targetIndex);
		}

		return Pair.of(-1, null);
	}

	public static void parseStartActivities(DfgActivityNodiser nodiser, DirectlyFollowsModel dfg,
			TObjectIntMap<Pair<String, Integer>> userIndex2dfgIndex) throws IOException {
		while (nodiser.nextNode()) {
			String activity = nodiser.getLastActivity();
			int userIndex = nodiser.getLastActivityIndex();
			Pair<String, Integer> p = Pair.of(activity, userIndex);

			int index;
			if (userIndex2dfgIndex.containsKey(p)) {
				index = userIndex2dfgIndex.get(p);
			} else {
				index = dfg.addNode(activity);
				userIndex2dfgIndex.put(p, index);
			}
			dfg.getStartNodes().add(index);
		}
	}

	public static void parseEndActivities(DfgActivityNodiser nodiser, DirectlyFollowsModel dfg,
			TObjectIntMap<Pair<String, Integer>> userIndex2dfgIndex) throws IOException {
		while (nodiser.nextNode()) {
			String activity = nodiser.getLastActivity();
			int userIndex = nodiser.getLastActivityIndex();
			Pair<String, Integer> p = Pair.of(activity, userIndex);

			int index;
			if (userIndex2dfgIndex.containsKey(p)) {
				index = userIndex2dfgIndex.get(p);
			} else {
				index = dfg.addNode(activity);
				userIndex2dfgIndex.put(p, index);
			}
			dfg.getEndNodes().add(index);
		}
	}
}