package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

public class CausalDataTable {
	//result variables
	private final List<Choice> columns;
	private final List<int[]> rows;

	//intermediate state variables
	private EfficientTree tree;
	private final EfficientTreeWalk walker;
	private int[] currentRow;
	private int[] j; //for loop nodes: the number of unfoldings
	private TObjectIntMap<Choice> choice2column;

	public CausalDataTable(final EfficientTree tree, final IvMLogFiltered log, final List<Choice> choices) {
		rows = new ArrayList<>();
		this.columns = choices;

		//initialise intermediate state variables
		{
			this.tree = tree;
			j = new int[tree.getMaxNumberOfNodes()];

			//create map to find columns by choices
			choice2column = new TObjectIntHashMap<>(10, 0.5f, -1);
			int i = 0;
			for (Choice choice : choices) {
				choice2column.put(choice, i);
				i++;
			}
		}

		/**
		 * The walker needs to do two things: keep trace of in which unfolding
		 * we are of loop nodes, and report the made choices.
		 */
		walker = new EfficientTreeWalk() {

			public void nodeEntered(IvMTrace trace, int node, int eventIndex) {
				//				System.out.println("node entered " + node + " at " + eventIndex);

				if (node != tree.getRoot()) {
					int parent = EfficientTreeUtils.getParent(tree, node);
					if (tree.isLoop(parent)) {
						int childNumber = EfficientTreeUtils.getChildNumberWith(tree, parent, node);
						if (childNumber == 0) {
							//if we entered the body (=first) child of a loop, then we are entering a new unfolding
							j[parent]++;
						} else if (childNumber == 1) {
							//if we entered the redo (=second) child of a loop, then we made a choice to do that redo
							Choice choice = EfficientTree2CausalGraph.getLoopChoice(tree, parent, createIds(node));
							reportChoice(choice, node);
						} else if (childNumber == 2) {
							//if we entered the exit (=third) child of a loop, then we made a choice to exit the loop
							Choice choice = EfficientTree2CausalGraph.getLoopChoice(tree, parent, createIds(node));
							reportChoice(choice, node);
						}
					} else if (tree.isOr(node)) {
						//TODO
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
								Choice choice = EfficientTree2CausalGraph.getXorChoice(tree, node, createIds(node));
								reportChoice(choice, child);
								return;
							}
						}
					}
				} else if (tree.isOr(node)) {
					/**
					 * For or, find the first node executed, as well as all
					 * children that have been executed.
					 */
					int firstChild = -1;
					boolean[] childrenExecuted = new boolean[tree.getMaxNumberOfNodes()];
					for (int eventIndex = startEventIndex; eventIndex <= lastEventIndex; eventIndex++) {
						IvMMove move = trace.get(eventIndex);
						if (move.isModelSync()) {
							int child = EfficientTreeUtils.getChildWith(tree, node, move.getTreeNode());
							if (child != -1) {
								if (firstChild == -1) {
									firstChild = child;
								}

								childrenExecuted[child] = true;
							}
						}
					}

					//second, report the appropriate choices
					for (int child : tree.getChildren(node)) {
						if (firstChild == child) {
							//TODO
//							Choice choice = EfficientTree2CausalGraph.getOrChoice(tree, node, createIds(node), true,
//									child);
//							reportChoice(choice, child);
						} else {
							//TODO
						}
					}

				} else if (tree.isLoop(node)) {
					//a loop node is done; reset the unfoldings
					j[node] = -1;
				}
			}
		};

		//walk through traces
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			currentRow = new int[choices.size()];
			Arrays.fill(j, -1);
			Arrays.fill(currentRow, -1);

			IvMTrace trace = it.next();
			//			System.out.println(trace);
			walker.walk(tree, trace);

			rows.add(currentRow);
		}
	}

	private void reportChoice(Choice choice, int chosenNode) {
		assert choice.nodes.contains(chosenNode);

		int columnNumber = choice2column.get(choice);
		if (columnNumber >= 0) {
			assert currentRow[columnNumber] < 0;
			currentRow[columnNumber] = chosenNode;
			return;
		} else {
			assert false;
			/**
			 * The choice was not a column, which means that a loop was not
			 * unfolded far enough; do nothing.
			 */
		}
	}

	private TIntArrayList createIds(int node) {
		TIntArrayList result = new TIntArrayList();
		int parent = tree.getRoot();
		while (parent != -1) {
			if (tree.isLoop(parent)) {
				if (j[parent] >= 0) {
					result.add(parent);
					result.add(j[parent]);
				}
			} else if (tree.isOr(parent)) {
				int child = EfficientTreeUtils.getChildWith(tree, parent, node);
				if (j[parent] >= 0) {
					if (j[parent] == child) {
						result.add(parent);
						result.add(0);
					} else {
						result.add(parent);
						result.add(1);
					}
				}
			}
			parent = EfficientTreeUtils.getChildWith(tree, parent, node);
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		//header
		for (Iterator<Choice> it = columns.iterator(); it.hasNext();) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append(",");
			}
		}
		result.append("\n");

		//data
		int count = 10;
		for (int i = 0; i < count && i < rows.size(); i++) {
			for (int j = 0; j < columns.size(); j++) {
				result.append(rows.get(i)[j]);
				if (j < columns.size() - 1) {
					result.append(",");
				}
			}
			if (i < count - 1 && i < rows.size() - 1) {
				result.append("\n");
			}
		}
		return result.toString();
	}
}