package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfoG;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.AbstractTask.Manual;


public class AlignedLogInfo extends IMLogInfoG<Move> {

	private MultiSet<UnfoldedNode> modelMoves;
	private MultiSet<String> unlabeledLogMoves;

	//for each position in the tree, the xeventclasses that were moved
	//position:
	// (node1, node1) on node1
	// (node1, node2) on node1, before node2 (only in case of node1 sequence or loop)
	// (root, null) at end of trace
	// (null, root) at start of trace
	private Map<Pair<UnfoldedNode, UnfoldedNode>, MultiSet<XEventClass>> logMoves;
	private MultiSet<Pair<UnfoldedNode, UnfoldedNode>> dfg;

	public AlignedLogInfo(MultiSet<? extends IMTraceG<Move>> log) {
		super(log);

		modelMoves = new MultiSet<UnfoldedNode>();
		logMoves = new HashMap<Pair<UnfoldedNode, UnfoldedNode>, MultiSet<XEventClass>>();
		unlabeledLogMoves = new MultiSet<String>();
		dfg = new MultiSet<Pair<UnfoldedNode, UnfoldedNode>>();
		for (IMTraceG<Move> trace : log) {
			UnfoldedNode lastUnode = null;
			long cardinality = log.getCardinalityOf(trace);
			for (int i = 0; i < trace.size(); i++) {
				Move move = trace.get(i);
				if (move.type == Type.model) {
					modelMoves.add(move.unode, log.getCardinalityOf(trace));
				} else if (move.type == Type.log) {
					//position the log move
					positionLogMove(lastUnode, i, trace, cardinality);

					unlabeledLogMoves.add(move.eventClass.toString(), cardinality);
				}

				if (move.unode != null && move.unode.getNode() instanceof Manual) {
					dfg.add(new Pair<UnfoldedNode, UnfoldedNode>(lastUnode, move.unode), log.getCardinalityOf(trace));
					lastUnode = move.unode;
				}
			}
			if (lastUnode != null) {
				dfg.add(new Pair<UnfoldedNode, UnfoldedNode>(lastUnode, null), log.getCardinalityOf(trace));
			}
		}
	}

	/*
	 * find the child that contains a particular grandchild
	 */
	private UnfoldedNode findChildWith(UnfoldedNode parent, UnfoldedNode grandChild) {
		//first phase: synchronous moves trough the paths
		Iterator<Node> itParent = parent.getPath().iterator();
		Iterator<Node> itGrandChild = grandChild.getPath().iterator();
		while (itParent.hasNext() && itGrandChild.hasNext() && itParent.next().equals(itGrandChild.next())) {

		}
		return parent.unfoldChild(itGrandChild.next());
	}

	private void positionLogMove(UnfoldedNode lastKnownPosition, int position, IMTraceG<Move> trace,
			long cardinality) {
		UnfoldedNode nextKnownPosition = null;
		for (int i = position + 1; i < trace.size(); i++) {
			if (trace.get(i).unode != null) {
				nextKnownPosition = trace.get(i).unode;
				break;
			}
		}

		//exception cases: repeated forbidden execution of activity
		//add log move on that node
		XEventClass e = trace.get(position).eventClass;
		if (lastKnownPosition != null && e.toString().equals(((AbstractTask) lastKnownPosition.getNode()).getName())) {
			addLogMove(lastKnownPosition, lastKnownPosition, e, cardinality);
			return;
		}
		if (nextKnownPosition != null && e.toString().equals(((AbstractTask) nextKnownPosition.getNode()).getName())) {
			addLogMove(nextKnownPosition, nextKnownPosition, e, cardinality);
			return;
		}

		//case: log move at begin or end of trace
		//add log move to root
		if (lastKnownPosition == null && nextKnownPosition == null) {
			//we have an empty model, do nothing;
			return;
		} else if (lastKnownPosition == null) {
			UnfoldedNode root = new UnfoldedNode(nextKnownPosition.getPath().get(0));
			addLogMove(null, root, e, cardinality);
			return;
		} else if (nextKnownPosition == null) {
			UnfoldedNode root = new UnfoldedNode(lastKnownPosition.getPath().get(0));
			addLogMove(root, null, e, cardinality);
			return;
		}

		//we have a lowest common parent that we will put the log move on
		UnfoldedNode logMoveNode = getLowestCommonParent(nextKnownPosition, lastKnownPosition);
		if (logMoveNode.getNode() instanceof Seq) {
			//for a sequence, find the child of lowestCommonParent in which lastUnode is
			addLogMove(logMoveNode, findChildWith(logMoveNode, lastKnownPosition), e, cardinality);
		} else if (logMoveNode.getNode() instanceof DefLoop || logMoveNode.getNode() instanceof XorLoop) {
			//for loop, find the child of lowestcommonparent in which lastUnode is
			addLogMove(logMoveNode, findChildWith(logMoveNode, lastKnownPosition), e, cardinality);
		} else {
			//for all other nodes, put on the node itself
			addLogMove(logMoveNode, logMoveNode, e, cardinality);
		}

	}

	public static UnfoldedNode getLowestCommonParent(UnfoldedNode unode1, UnfoldedNode unode2) {
		UnfoldedNode result = new UnfoldedNode(unode1.getPath().get(0));
		int i = 1;
		while (i < unode1.getPath().size() && i < unode2.getPath().size()
				&& unode1.getPath().get(i).equals(unode2.getPath().get(i))) {
			result = result.unfoldChild(unode1.getPath().get(i));
			i++;
		}
		return result;
	}

	private void addLogMove(UnfoldedNode unode, UnfoldedNode beforeChild, XEventClass e, long cardinality) {
		Pair<UnfoldedNode, UnfoldedNode> key = new Pair<UnfoldedNode, UnfoldedNode>(unode, beforeChild);
		if (!logMoves.containsKey(key)) {
			logMoves.put(key, new MultiSet<XEventClass>());
		}
		logMoves.get(key).add(e, cardinality);
	}

	public MultiSet<UnfoldedNode> getModelMoves() {
		return modelMoves;
	}

	public Map<Pair<UnfoldedNode, UnfoldedNode>, MultiSet<XEventClass>> getLogMoves() {
		return logMoves;
	}

	public MultiSet<String> getUnlabeledLogMoves() {
		return unlabeledLogMoves;
	}

	public long getDfg(UnfoldedNode unode1, UnfoldedNode unode2) {
		return dfg.getCardinalityOf(new Pair<UnfoldedNode, UnfoldedNode>(unode1, unode2));
	}

}
