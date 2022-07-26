package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class BinariseCausalGraphAndChoiceData {

	public static Quadruple<CausalGraph, CausalDataTable, THashMap<Choice, Set<Choice>>, THashMap<Choice, Choice>> binarise(
			CausalGraph oldGraph, CausalDataTable oldData, IvMCanceller canceller) {
		//create new choices

		THashMap<Choice, Set<Choice>> oldChoice2newChoices = new THashMap<>();
		THashMap<Choice, Choice> newChoice2oldChoice = new THashMap<>();
		{
			for (Choice oldChoice : oldGraph.getNodes()) {
				Set<Choice> newChoices = new THashSet<>();
				if (oldChoice.nodes.size() == 1) {
					//choice between doing or skipping one node; do nothing
					newChoices.add(oldChoice);
				} else if (oldChoice.nodes.size() == 2) {
					//choice is already binary; do nothing
					newChoices.add(oldChoice);
				} else {
					//choice must be binarised
					for (TIntIterator it = oldChoice.nodes.iterator(); it.hasNext();) {
						int node = it.next();

						Choice newChoice = new Choice();
						newChoice.nodes.add(node);
						newChoice.ids.addAll(oldChoice.ids);
						newChoice.ids.add(CausalDataTable.NO_VALUE);
						newChoice.ids.addAll(oldChoice.nodes);
						newChoices.add(newChoice);
					}
				}

				for (Choice newChoice : newChoices) {
					newChoice2oldChoice.put(newChoice, oldChoice);
				}
				oldChoice2newChoices.put(oldChoice, newChoices);
			}
		}

		//create graph
		CausalGraph newGraph = new CausalGraph();
		{
			for (Pair<Choice, Choice> oldEdge : oldGraph.getEdges()) {
				Choice oldSource = oldEdge.getA();
				Choice oldTarget = oldEdge.getB();

				for (Choice newSource : oldChoice2newChoices.get(oldSource)) {
					for (Choice newTarget : oldChoice2newChoices.get(oldTarget)) {
						newGraph.addEdge(newSource, newTarget);
					}
				}
			}
		}

		//create data
		CausalDataTable newData;
		{
			//create a map of new choices and columns
			List<Choice> newChoices = new ArrayList<>(newChoice2oldChoice.keySet());
			TObjectIntHashMap<Choice> newChoice2newColumn = new TObjectIntHashMap<>(10, 0.5f,
					DirectlyFollowsModel2UpperBoundCausalGraph.NO_NODE);
			int i = 0;
			for (Choice newChoice : newChoices) {
				newChoice2newColumn.put(newChoice, i);
				i++;
			}

			newData = new CausalDataTable(newChoices);

			for (TObjectIntIterator<int[]> it = oldData.iterator(); it.hasNext();) {
				it.advance();
				int[] oldRow = it.key();
				int cardinality = it.value();

				int[] newRow = new int[newChoices.size()];
				Arrays.fill(newRow, CausalDataTable.NO_VALUE);

				for (int oldColumn = 0; oldColumn < oldRow.length; oldColumn++) {
					Choice oldChoice = oldData.getColumns().get(oldColumn);
					int chosenNode = oldRow[oldColumn];

					for (Choice newChoice : oldChoice2newChoices.get(oldChoice)) {
						int newColumn = newChoice2newColumn.get(newChoice);

						if (chosenNode == CausalDataTable.NO_VALUE) {
							//the old choice was not encountered, so the binary new choices was also not encountered
							newRow[newColumn] = CausalDataTable.NO_VALUE;
						} else if (oldChoice.nodes.size() < 3) {
							//the old choice was not changed as it is already binary; simply copy
							newRow[newColumn] = chosenNode;
						} else {
							//the old choice is binarised
							int newChoiceNode = newChoice.nodes.iterator().next();
							if (chosenNode == newChoiceNode) {
								newRow[newColumn] = newChoiceNode;
							} else {
								newRow[newColumn] = CausalDataTable.getSkipNode(newChoiceNode);
							}
						}
					}
				}

				newData.addRow(newRow, cardinality);
			}
		}

		return Quadruple.of(newGraph, newData, oldChoice2newChoices, newChoice2oldChoice);
	}
}