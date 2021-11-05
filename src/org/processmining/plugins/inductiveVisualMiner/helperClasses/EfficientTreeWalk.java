package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public abstract class EfficientTreeWalk {
	public void walk(EfficientTree tree, IvMTrace trace) {
		int[] enteredNodeAt = new int[tree.getMaxNumberOfNodes()];
		int[] lastSeenNodeAt = new int[tree.getMaxNumberOfNodes()];
		Arrays.fill(enteredNodeAt, -1);
		Arrays.fill(lastSeenNodeAt, -1);

		for (int eventIndex = 0; eventIndex < trace.size(); eventIndex++) {
			//only model moves
			IvMMove move = trace.get(eventIndex);
			if (move.isModelSync()) {
				int eventNode = move.getTreeNode();

				//process exiting nodes
				{
					for (int node = enteredNodeAt.length - 1; node >= 0; node--) {
						if (enteredNodeAt[node] >= 0) {
							//we were in node, see whether we're still in it
							int lowestCommonParent = EfficientTreeUtils.getLowestCommonParent(tree, eventNode, node);
							if (lowestCommonParent == node) {
								//we are still in node; do nothing
							} else if (tree.isConcurrent(lowestCommonParent) || tree.isOr(lowestCommonParent)) {
								//we are still in node; do nothing
							} else {
								//we have left node
								nodeExecuted(trace, node, enteredNodeAt[node], lastSeenNodeAt[node]);
								enteredNodeAt[node] = -1;
								lastSeenNodeAt[node] = -1;
							}
						}
					}
				}

				//process entering nodes
				{
					int parent = tree.getRoot();
					while (parent != -1) {
						if (enteredNodeAt[parent] < 0) {
							enteredNodeAt[parent] = eventIndex;
							nodeEntered(trace, parent, eventIndex);
						}
						lastSeenNodeAt[parent] = eventIndex;
						parent = EfficientTreeUtils.getChildWith(tree, parent, eventNode);
					}
				}
			}
		}

		//exit all the remaining nodes
		for (int node = enteredNodeAt.length - 1; node >= 0; node--) {
			if (enteredNodeAt[node] >= 0) {
				//we were in node, so now we've left it as the trace is over
				nodeExecuted(trace, node, enteredNodeAt[node], lastSeenNodeAt[node]);
			}
		}
	}

	/**
	 * Called whenever a node is entered. Parents are entered before their
	 * children.
	 * 
	 * @param trace
	 * @param node
	 * @param eventIndex
	 */
	public abstract void nodeEntered(IvMTrace trace, int node, int eventIndex);

	/**
	 * Called whenever the execution of a node is completed. Parents are
	 * reported after their children.
	 * 
	 * @param trace
	 * 
	 * @param node
	 * @param startEventIndex
	 *            (inclusive)
	 * @param lastEventIndex
	 *            (inclusive)
	 */
	public abstract void nodeExecuted(IvMTrace trace, int node, int startEventIndex, int lastEventIndex);
}