package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.EfficientTreeWalk;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class EfficientTree2Choices {
	/**
	 * 
	 * @param tree
	 * @param k
	 *            for each node, how many times it should be unfolded (only used
	 *            for loop nodes)
	 * @return
	 */
	public static List<Choice> getChoices(EfficientTree tree, int[] k) {
		return getChoices(tree, tree.getRoot(), new TIntArrayList(), k);
	}

	public static List<Choice> getChoices(EfficientTree tree, int node, TIntList ids, int[] k) {
		List<Choice> result = new ArrayList<>();
		if (tree.isTau(node) || tree.isActivity(node)) {
			return result;
		}

		assert tree.isOperator(node);
		if (tree.isLoop(node)) {
			//for loop, we need to unfold choices up to a certain length k
			for (int j = 0; j < k[node]; j++) {
				//unfold the children's choices
				{
					TIntArrayList childIds = new TIntArrayList(ids);
					childIds.add(node);
					childIds.add(j);

					for (int child : tree.getChildren(node)) {
						result.addAll(getChoices(tree, child, childIds, k));
					}
				}

				//add a choice for this unfolding
				Choice choice = getLoopChoice(tree, node, ids, j);
				result.add(choice);
			}
		} else {
			//recurse on children
			for (int child : tree.getChildren(node)) {
				result.addAll(getChoices(tree, child, ids, k));
			}

			if (tree.isXor(node)) {
				Choice choice = getXorChoice(tree, node, ids);
				result.add(choice);
			} else if (tree.isOr(node)) {

				/**
				 * For the or, there's a first choice between all children
				 * combined and then a non-first choice for every child
				 * separately
				 */
				Choice firstChoice = getOrChoiceFirst(tree, node, ids);
				result.add(firstChoice);

				for (int child : tree.getChildren(node)) {
					Choice secondChoice = getOrChoiceSecond(tree, node, ids, child);
					result.add(secondChoice);
				}
			} else {
				//the other operators add no choices

			}
		}

		return result;
	}

	public static Choice getOrChoiceFirst(EfficientTree tree, int node, TIntList ids) {
		Choice choice = new Choice();
		for (int child : tree.getChildren(node)) {
			choice.nodes.add(child);
		}
		choice.ids.addAll(ids);
		return choice;
	}

	public static Choice getOrChoiceSecond(EfficientTree tree, int node, TIntList ids, int child) {
		Choice choice = new Choice();
		choice.nodes.add(child);
		choice.ids.addAll(ids);
		return choice;
	}

	public static Choice getLoopChoice(EfficientTree tree, int node, TIntList ids, int j) {
		TIntArrayList childIds = new TIntArrayList(ids);
		childIds.add(node);
		childIds.add(j);
		return getLoopChoice(tree, node, childIds);
	}

	public static Choice getLoopChoice(EfficientTree tree, int node, TIntList ids) {
		Choice choice = new Choice();
		choice.nodes.add(tree.getChild(node, 1));
		choice.nodes.add(tree.getChild(node, 2));
		choice.ids.addAll(ids);
		return choice;
	}

	public static Choice getXorChoice(EfficientTree tree, int node, TIntList ids) {
		//an xor actually adds a choice
		Choice choice = new Choice();
		for (int child : tree.getChildren(node)) {
			choice.nodes.add(child);
		}
		choice.ids.addAll(ids);
		return choice;
	}

	public static int[] createFixedK(EfficientTree tree, int value) {
		//first, figure out how often each node was maximally executed in a trace in the log
		int[] k = new int[tree.getMaxNumberOfNodes()];
		for (int node : EfficientTreeUtils.getAllNodes(tree)) {
			k[node] = value;
		}
		return k;
	}

	public static int[] createK(final EfficientTree tree, IvMLogFiltered log) {
		final int[] k = new int[tree.getMaxNumberOfNodes()];
		final int[] l = new int[tree.getMaxNumberOfNodes()];

		EfficientTreeWalk walk = new EfficientTreeWalk() {
			public void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex) {
				//if this node is the body child of a loop, then the loop has been executed.
				if (node != tree.getRoot()) {
					int parent = EfficientTreeUtils.getParent(tree, node);
					if (tree.isLoop(parent) && EfficientTreeUtils.getChildNumberWith(tree, parent, node) == 0) {
						l[parent]++;
					}
				}
			}

			public void nodeEntered(IvMTrace trace, int node, int eventIndex) {

			}
		};

		for (Iterator<IvMTrace> it = log.iterator(); it.hasNext();) {
			Arrays.fill(l, 0);

			walk.walk(tree, it.next());

			for (int i = 0; i < k.length; i++) {
				k[i] = Math.max(k[i], l[i]);
			}
		}

		return k;
	}
}
