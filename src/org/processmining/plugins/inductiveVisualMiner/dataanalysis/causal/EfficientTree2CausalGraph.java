package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

import cern.colt.Arrays;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

public class EfficientTree2CausalGraph {
	public static Dot convert(EfficientTree tree, IvMLogFiltered log) {

		//get choices
		int[] k = EfficientTree2Choices.createK(tree, log);
		System.out.println(Arrays.toString(k));
		List<Choice> choices = EfficientTree2Choices.getChoices(tree, k);

		Dot dot = new Dot();

		//create dot nodes
		THashMap<Choice, DotNode> choice2dotNode = new THashMap<>();
		for (Choice choice : choices) {
			DotNode dotNode = dot.addNode(choice.getId());
			choice2dotNode.put(choice, dotNode);
		}

		//create dot edges
		createEdges(dot, tree, tree.getRoot(), new TIntArrayList(), k, choice2dotNode);

		try {
			FileUtils.writeStringToFile(
					new File("/home/sander/Documents/svn/49 - causality in process mining - niek/bpic12a.dot"),
					dot.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//create table
		CausalDataTable table = new CausalDataTable(tree, log, choices);

		try {
			FileUtils.writeStringToFile(
					new File("/home/sander/Documents/svn/49 - causality in process mining - niek/bpic12a.csv"),
					table.toString(-1));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dot;
	}

	public static void createEdges(Dot dot, EfficientTree tree, int node, TIntList ids, int[] k,
			THashMap<Choice, DotNode> choice2dotNode) {
		if (tree.isActivity(node) || tree.isTau(node)) {
			return;
		}

		assert tree.isOperator(node);

		if (tree.isConcurrent(node) || tree.isInterleaved(node)) {
			//simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(dot, tree, child, ids, k, choice2dotNode);
			}
		} else if (tree.isXor(node)) {
			//first simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(dot, tree, child, ids, k, choice2dotNode);
			}

			//for xor, the choices of the children depend on the choice made in the xor (with a 1 causality, but it is a causal relation)
			Choice choiceA = EfficientTree2Choices.getXorChoice(tree, node, ids);
			DotNode dotNodeA = choice2dotNode.get(choiceA);
			for (int childB : tree.getChildren(node)) {
				List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, ids, k);

				for (Choice choiceB : choicesChildB) {
					DotNode dotNodeB = choice2dotNode.get(choiceB);
					assert dotNodeA != null && dotNodeB != null;
					dot.addEdge(dotNodeA, dotNodeB);
				}
			}

		} else if (tree.isSequence(node)) {
			//first simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(dot, tree, child, ids, k, choice2dotNode);
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
								DotNode dotNodeA = choice2dotNode.get(choiceA);
								DotNode dotNodeB = choice2dotNode.get(choiceB);
								assert dotNodeA != null && dotNodeB != null;
								dot.addEdge(dotNodeA, dotNodeB);
							}
						}
					}
				}
			}
		} else if (tree.isOr(node)) {

			//first simply recurse
			for (int child : tree.getChildren(node)) {
				createEdges(dot, tree, child, ids, k, choice2dotNode);
			}

			//for or, the choices of the first child are mutually exclusive, so not connected.

			//for or, the choices of the non-first children are independent, so not connected.

			/**
			 * The choice for the first child may influence the choices within
			 * all children.
			 */
			{
				Choice choiceA = EfficientTree2Choices.getOrChoiceFirst(tree, node, ids);
				DotNode dotNodeA = choice2dotNode.get(choiceA);

				for (int childB : tree.getChildren(node)) {
					List<Choice> choicesChildB = EfficientTree2Choices.getChoices(tree, childB, ids, k);

					for (Choice choiceB : choicesChildB) {
						DotNode dotNodeB = choice2dotNode.get(choiceB);
						assert dotNodeA != null && dotNodeB != null;
						dot.addEdge(dotNodeA, dotNodeB);
					}
				}
			}

			/**
			 * The choice for a first child may influence the choices for
			 * non-first children.
			 */
			{
				Choice choiceA = EfficientTree2Choices.getOrChoiceFirst(tree, node, ids);
				DotNode dotNodeA = choice2dotNode.get(choiceA);

				for (int childB : tree.getChildren(node)) {
					Choice choiceB = EfficientTree2Choices.getOrChoiceSecond(tree, node, ids, childB);
					DotNode dotNodeB = choice2dotNode.get(choiceB);
					assert dotNodeA != null && dotNodeB != null;
					dot.addEdge(dotNodeA, dotNodeB);
				}
			}

			/**
			 * The choice for a non-first child may influence the choices within
			 * that child
			 */
			for (int childA : tree.getChildren(node)) {
				Choice choiceA = EfficientTree2Choices.getOrChoiceSecond(tree, node, ids, childA);
				DotNode dotNodeA = choice2dotNode.get(choiceA);

				List<Choice> choicesB = EfficientTree2Choices.getChoices(tree, childA, ids, k);
				for (Choice choiceB : choicesB) {
					DotNode dotNodeB = choice2dotNode.get(choiceB);
					assert dotNodeA != null && dotNodeB != null;
					dot.addEdge(dotNodeA, dotNodeB);
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
					createEdges(dot, tree, child, childIds, k, choice2dotNode);
				}
			}

			//second, add dependencies between unfoldings
			{
				//loop-loop
				for (int jA = 0; jA < k[node] - 1; jA++) {
					Choice choiceA = EfficientTree2Choices.getLoopChoice(tree, node, ids, jA);
					for (int jB = jA + 1; jB < k[node]; jB++) {
						Choice choiceB = EfficientTree2Choices.getLoopChoice(tree, node, ids, jB);
						dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
								dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
								dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
										dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
						dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
							dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
							dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
						dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
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
						dot.addEdge(choice2dotNode.get(choiceA), choice2dotNode.get(choiceB));
					}
				}
			}
		} else {
			assert false;
		}
	}

}