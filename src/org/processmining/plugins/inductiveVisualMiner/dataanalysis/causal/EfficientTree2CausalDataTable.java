package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.Arrays;
import java.util.List;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.EfficientTreeWalk;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class EfficientTree2CausalDataTable {

	//intermediate state variables
	private static class State {
		EfficientTree tree;
		EfficientTreeWalk walker;
		int[] currentRow;
		int[] j; //for loop nodes: the number of unfoldings;
		TObjectIntMap<Choice> choice2column;
	}

	public static CausalDataTable create(final EfficientTree tree, final IvMLogFiltered log,
			final List<Choice> choices) {
		CausalDataTable result = new CausalDataTable(choices);

		//initialise intermediate state variables
		final State state = new State();
		{
			state.tree = tree;
			state.j = new int[tree.getMaxNumberOfNodes()];

			//create map to find columns by choices
			state.choice2column = new TObjectIntHashMap<>(10, 0.5f, -1);
			int i = 0;
			for (Choice choice : choices) {
				state.choice2column.put(choice, i);
				i++;
			}
		}

		/**
		 * The walker needs to do two things: keep trace of in which unfolding
		 * we are of loop nodes, and report the made choices.
		 */
		state.walker = new EfficientTreeWalk() {

			public void nodeEntered(IvMTrace trace, int node, int eventIndex) {
				//				System.out.println("node entered " + node + " at " + eventIndex);

				if (node != tree.getRoot()) {
					int parent = EfficientTreeUtils.getParent(tree, node);
					if (tree.isLoop(parent)) {
						int childNumber = EfficientTreeUtils.getChildNumberWith(tree, parent, node);
						if (childNumber == 0) {
							//if we entered the body (=first) child of a loop, then we are entering a new unfolding
							state.j[parent]++;
						} else if (childNumber == 1) {
							//if we entered the redo (=second) child of a loop, then we made a choice to do that redo
							Choice choice = EfficientTree2Choices.getLoopChoice(tree, parent, createIds(state, node));
							reportChoice(state, choice, node);
						} else if (childNumber == 2) {
							//if we entered the exit (=third) child of a loop, then we made a choice to exit the loop
							Choice choice = EfficientTree2Choices.getLoopChoice(tree, parent, createIds(state, node));
							reportChoice(state, choice, node);
						}
					}
				}
			}

			public void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex) {
				//				System.out.println("node " + node + " executed from " + startEventIndex + " to " + lastEventIndex);

				if (tree.isXor(node)) {
					/**
					 * For xor, find the first event that belongs to a child.
					 */
					for (int eventIndex = startEventIndex; eventIndex <= lastEventIndex; eventIndex++) {
						IvMMove move = trace.get(eventIndex);
						if (move.isModelSync()) {
							int child = EfficientTreeUtils.getChildWith(tree, node, move.getTreeNode());
							if (child != -1) {
								//we found which child was executed for this xor
								Choice choice = EfficientTree2Choices.getXorChoice(tree, node, createIds(state, node));
								reportChoice(state, choice, child);
								return;
							}
						}
					}
				} else if (tree.isOr(node)) {
					/**
					 * For or, find the children that have been executed and the
					 * first child that was executed.
					 */
					boolean[] childrenExecuted = new boolean[tree.getMaxNumberOfNodes()];
					int firstChild = -1;
					for (int eventIndex = startEventIndex; eventIndex <= lastEventIndex; eventIndex++) {
						IvMMove move = trace.get(eventIndex);
						if (move.isModelSync()) {
							int child = EfficientTreeUtils.getChildWith(tree, node, move.getTreeNode());
							if (child != -1) {
								childrenExecuted[child] = true;

								if (firstChild == -1) {
									firstChild = child;
								}
							}
						}
					}

					//report the choice for the first child
					{
						assert firstChild != -1;
						Choice firstChoice = EfficientTree2Choices.getOrChoiceFirst(tree, node, createIds(state, node));
						reportChoice(state, firstChoice, firstChild);
					}

					//second, report the appropriate choices
					for (int child : tree.getChildren(node)) {
						if (firstChild != child) {
							Choice choice = EfficientTree2Choices.getOrChoiceSecond(tree, node, createIds(state, node),
									child);
							if (childrenExecuted[child]) {
								//report that this child was executed as a non-first node
								reportChoice(state, choice, child);
							} else {
								//this child was not executed, which is also a choice
								reportChoice(state, choice, 0);
							}
						}
					}

				} else if (tree.isLoop(node)) {
					//a loop node is done; reset the unfoldings
					state.j[node] = -1;
				}
			}
		};

		//walk through traces
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			state.currentRow = new int[choices.size()];
			Arrays.fill(state.j, -1);
			Arrays.fill(state.currentRow, CausalDataTable.NO_VALUE);

			IvMTrace trace = it.next();
			//			System.out.println(trace);
			state.walker.walk(tree, trace);

			result.addRow(state.currentRow);
		}
		return result;
	}

	private static void reportChoice(State state, Choice choice, int chosenNode) {
		assert choice.nodes.contains(chosenNode) || (choice.nodes.size() == 1 && chosenNode == 0);

		int columnNumber = state.choice2column.get(choice);
		if (columnNumber >= 0) {
			assert state.currentRow[columnNumber] < 0;
			state.currentRow[columnNumber] = chosenNode;
			return;
		} else {
			/**
			 * The choice was not a column, which means that a loop was not
			 * unfolded far enough; do nothing.
			 */
		}
	}

	private static TIntArrayList createIds(State state, int node) {
		TIntArrayList result = new TIntArrayList();
		int parent = state.tree.getRoot();
		while (parent != -1) {
			if (state.tree.isLoop(parent)) {
				if (state.j[parent] >= 0) {
					result.add(parent);
					result.add(state.j[parent]);
				}
			} else if (state.tree.isOr(parent)) {
				int child = EfficientTreeUtils.getChildWith(state.tree, parent, node);
				if (state.j[parent] >= 0) {
					if (state.j[parent] == child) {
						result.add(parent);
						result.add(0);
					} else {
						result.add(parent);
						result.add(1);
					}
				}
			}
			parent = EfficientTreeUtils.getChildWith(state.tree, parent, node);
		}

		return result;
	}
}