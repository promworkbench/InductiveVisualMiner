package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class EfficientTree2CausalGraph {
	public static Pair<CausalGraph, CausalDataTable> convert(EfficientTree tree, IvMLogFiltered log) {

		//get index of maximum unfolding
		int[] k = EfficientTree2Choices.createK(tree, log);

		//get choices
		List<Choice> choices = EfficientTree2Choices.getChoices(tree, k);

		CausalGraph causalGraph = new CausalGraph();

		//create dot edges
		createEdges(causalGraph, tree, tree.getRoot(), new TIntArrayList(), k);

		//create table
		CausalDataTable table = EfficientTree2CausalDataTable.create(tree, log, choices);

		return Pair.of(causalGraph, table);
	}

	public static void createEdges(CausalGraph causalGraph, EfficientTree tree, int node, TIntList ids, int[] k) {
		if (tree.isActivity(node) || tree.isTau(node)) {
			return;
		}

		assert tree.isOperator(node);

		if (tree.isConcurrent(node) || tree.isInterleaved(node)) {
			//simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(causalGraph, tree, child, ids, k);
			}
		} else if (tree.isXor(node)) {
			//first simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(causalGraph, tree, child, ids, k);
			}

			//for xor, the choices of the children depend on the choice made in the xor (with a 1 causality, but it is a causal relation)
			Choice choiceA = EfficientTree2Choices.getXorChoice(tree, node, ids);
			for (int childB : tree.getChildren(node)) {
				List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, ids, k);

				for (Choice choiceB : choicesChildB) {
					causalGraph.addEdge(choiceA, choiceB);
				}
			}

		} else if (tree.isSequence(node)) {
			//first simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(causalGraph, tree, child, ids, k);
			}

			//for sequence, every choice depends on all choices before it
			for (int childIndexA = 0; childIndexA < tree.getNumberOfChildren(node); childIndexA++) {
				int childA = tree.getChild(node, childIndexA);
				List<Choice> choicesChildA = EfficientTree2Choices.getChoices(tree, childA, ids, k);

				if (choicesChildA.size() > 0) {
					for (int childIndexB = childIndexA + 1; childIndexB < tree
							.getNumberOfChildren(node); childIndexB++) {
						int childB = tree.getChild(node, childIndexB);
						List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, ids, k);

						for (Choice choiceA : choicesChildA) {
							for (Choice choiceB : choicesChildB) {
								causalGraph.addEdge(choiceA, choiceB);
							}
						}
					}
				}
			}
		} else if (tree.isOr(node)) {

			//first simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(causalGraph, tree, child, ids, k);
			}

			//for or, the choices of the first child are mutually exclusive, so not connected.

			//for or, the choices of the non-first children are independent, so not connected.

			/**
			 * The choice for the first child may influence the choices within
			 * all children.
			 */
			{
				Choice choiceA = EfficientTree2Choices.getOrChoiceFirst(tree, node, ids);

				for (int childB : tree.getChildren(node)) {
					List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, ids, k);

					for (Choice choiceB : choicesChildB) {
						causalGraph.addEdge(choiceA, choiceB);
					}
				}
			}

			/**
			 * The choice for a first child may influence the choices for
			 * non-first children.
			 */
			{
				Choice choiceA = EfficientTree2Choices.getOrChoiceFirst(tree, node, ids);

				for (int childB : tree.getChildren(node)) {
					Choice choiceB = EfficientTree2Choices.getOrChoiceSecond(tree, node, ids, childB);
					causalGraph.addEdge(choiceA, choiceB);
				}
			}

			/**
			 * The choice for a non-first child may influence the choices within
			 * that child
			 */
			for (int childA : tree.getChildren(node)) {
				Choice choiceA = EfficientTree2Choices.getOrChoiceSecond(tree, node, ids, childA);

				List<Choice> choicesB = EfficientTree2Choices.getChoices(tree, childA, ids, k);
				for (Choice choiceB : choicesB) {
					causalGraph.addEdge(choiceA, choiceB);
				}
			}

		} else if (tree.isLoop(node))

		{

			//unfold and recurse the children's choices
			for (int j = 0; j < k[node]; j++) {
				TIntArrayList childIds = new TIntArrayList(ids);
				childIds.add(node);
				childIds.add(j);

				for (int child : tree.getChildren(node)) {
					createEdges(causalGraph, tree, child, childIds, k);
				}
			}

			//second, add dependencies between unfoldings
			{
				//loop-loop
				for (int jA = 0; jA < k[node] - 1; jA++) {
					Choice choiceA = EfficientTree2Choices.getLoopChoice(tree, node, ids, jA);
					for (int jB = jA + 1; jB < k[node]; jB++) {
						Choice choiceB = EfficientTree2Choices.getLoopChoice(tree, node, ids, jB);
						causalGraph.addEdge(choiceA, choiceB);
					}
				}

				//loop-node
				for (int jA = 0; jA < k[node]; jA++) {
					Choice choiceA = EfficientTree2Choices.getLoopChoice(tree, node, ids, jA);
					for (int jB = jA + 1; jB < k[node]; jB++) {
						for (int childB : tree.getChildren(node)) {
							TIntArrayList childIdsB = new TIntArrayList(ids);
							childIdsB.add(node);
							childIdsB.add(jB);
							List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, childIdsB, k);
							for (Choice choiceB : choicesChildB) {
								causalGraph.addEdge(choiceA, choiceB);
							}
						}
					}
				}

				//node-loop
				for (int jA = 0; jA < k[node] - 1; jA++) {
					for (int numberOfChild = 0; numberOfChild <= 1; numberOfChild++) {
						int childA = tree.getChild(node, numberOfChild);
						TIntArrayList childIdsA = new TIntArrayList(ids);
						childIdsA.add(node);
						childIdsA.add(jA);
						List<Choice> choicesChildA = EfficientTree2Choices.getChoices(tree, childA, childIdsA, k);

						for (int jB = jA + 1; jB < k[node]; jB++) {
							Choice choiceB = EfficientTree2Choices.getLoopChoice(tree, node, ids, jB);

							for (Choice choiceA : choicesChildA) {
								causalGraph.addEdge(choiceA, choiceB);
							}
						}
					}
				}

				//node-node
				for (int jA = 0; jA < k[node] - 1; jA++) {
					for (int numberOfChild = 0; numberOfChild <= 1; numberOfChild++) {
						int childA = tree.getChild(node, numberOfChild);
						TIntArrayList childIdsA = new TIntArrayList(ids);
						childIdsA.add(node);
						childIdsA.add(jA);
						List<Choice> choicesChildA = EfficientTree2Choices.getChoices(tree, childA, childIdsA, k);

						for (int jB = jA + 1; jB < k[node]; jB++) {
							for (int childB : tree.getChildren(node)) {
								TIntArrayList childIdsB = new TIntArrayList(ids);
								childIdsB.add(node);
								childIdsB.add(jB);
								List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, childIdsB,
										k);
								for (Choice choiceA : choicesChildA) {
									for (Choice choiceB : choicesChildB) {
										causalGraph.addEdge(choiceA, choiceB);
									}
								}
							}
						}
					}
				}
			}

			//interal-internal
			{
				//body-choice
				for (int j = 0; j < k[node]; j++) {
					TIntArrayList childIdsA = new TIntArrayList(ids);
					childIdsA.add(node);
					childIdsA.add(j);

					int childA = tree.getChild(node, 0);
					List<Choice> choicesChildA = EfficientTree2Choices.getChoices(tree, childA, childIdsA, k);

					Choice choiceB = EfficientTree2Choices.getLoopChoice(tree, node, ids, j);
					for (Choice choiceA : choicesChildA) {
						causalGraph.addEdge(choiceA, choiceB);
					}
				}

				//body-redo
				for (int j = 0; j < k[node]; j++) {
					TIntArrayList childIdsA = new TIntArrayList(ids);
					childIdsA.add(node);
					childIdsA.add(j);
					int childA = tree.getChild(node, 0);
					List<Choice> choicesChildA = EfficientTree2Choices.getChoices(tree, childA, childIdsA, k);

					TIntArrayList childIdsB = new TIntArrayList(ids);
					childIdsB.add(node);
					childIdsB.add(j);
					int childB = tree.getChild(node, 1);
					List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, childIdsB, k);

					for (Choice choiceA : choicesChildA) {
						for (Choice choiceB : choicesChildB) {
							causalGraph.addEdge(choiceA, choiceB);
						}
					}
				}

				//body-exit
				for (int j = 0; j < k[node]; j++) {
					TIntArrayList childIdsA = new TIntArrayList(ids);
					childIdsA.add(node);
					childIdsA.add(j);
					int childA = tree.getChild(node, 0);
					List<Choice> choicesChildA = EfficientTree2Choices.getChoices(tree, childA, childIdsA, k);

					TIntArrayList childIdsB = new TIntArrayList(ids);
					childIdsB.add(node);
					childIdsB.add(j);
					int childB = tree.getChild(node, 2);
					List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, childIdsB, k);

					for (Choice choiceA : choicesChildA) {
						for (Choice choiceB : choicesChildB) {
							causalGraph.addEdge(choiceA, choiceB);
						}
					}
				}

				//choice-redo
				for (int j = 0; j < k[node]; j++) {
					Choice choiceA = EfficientTree2Choices.getLoopChoice(tree, node, ids, j);

					TIntArrayList childIdsB = new TIntArrayList(ids);
					childIdsB.add(node);
					childIdsB.add(j);
					int childB = tree.getChild(node, 1);
					List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, childIdsB, k);

					for (Choice choiceB : choicesChildB) {
						causalGraph.addEdge(choiceA, choiceB);
					}
				}

				//choice-exit
				for (int j = 0; j < k[node]; j++) {
					Choice choiceA = EfficientTree2Choices.getLoopChoice(tree, node, ids, j);

					TIntArrayList childIdsB = new TIntArrayList(ids);
					childIdsB.add(node);
					childIdsB.add(j);
					int childB = tree.getChild(node, 2);
					List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, childIdsB, k);

					for (Choice choiceB : choicesChildB) {
						causalGraph.addEdge(choiceA, choiceB);
					}
				}
			}
		} else {
			assert false;
		}
	}

}