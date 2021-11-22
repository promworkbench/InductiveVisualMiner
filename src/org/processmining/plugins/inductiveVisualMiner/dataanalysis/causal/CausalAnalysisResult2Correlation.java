package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.Map;
import java.util.Map.Entry;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class CausalAnalysisResult2Correlation {

	public static void convert(CausalGraph graph, CausalDataTable table, IvMModel model) {
		for (int c = 0; c < table.getColumns().size() - 1; c++) {
			Map<Pair<Integer, Choice>, TIntIntMap> map = getColumn(graph, table, c);
			System.out.println("for choice " + table.getColumns().get(c).toString(model));
			for (Entry<Pair<Integer, Choice>, TIntIntMap> m : map.entrySet()) {
				int decisionNode = m.getKey().getA();
				if (decisionNode >= 0) {
					System.out.print(" doing " + Choice.node2string(model, decisionNode));
					Choice choice = m.getKey().getB();
					System.out.println(
							" results in choice " + choice.toString(model) + " (" + choice.toString() + ") being ");
					for (int nodeChosen : m.getValue().keys()) {
						if (nodeChosen >= 0) {
							System.out.println("  " + Choice.node2string(model, nodeChosen) + "x"
									+ m.getValue().get(nodeChosen) + " ");
						}
					}
				}
			}
			System.out.println();
		}
	}

	public static Map<Pair<Integer, Choice>, TIntIntMap> getColumn(CausalGraph graph, CausalDataTable table,
			int column) {
		Map<Pair<Integer, Choice>, TIntIntMap> result = new THashMap<>();
		for (int cB = 0; cB < table.getColumns().size(); cB++) {
			Choice choiceB = table.getColumns().get(cB);

			if (graph.getEdges().contains(Pair.of(table.getColumns().get(column), choiceB))) {
				for (int[] row : table.getRows()) {
					Pair<Integer, Choice> p = Pair.of(row[column], choiceB);
					result.putIfAbsent(p, new TIntIntHashMap(10, 0.5f, -1, 0));
					result.get(p).adjustOrPutValue(row[cB], 1, 1);
				}
			}
		}

		return result;
	}
}