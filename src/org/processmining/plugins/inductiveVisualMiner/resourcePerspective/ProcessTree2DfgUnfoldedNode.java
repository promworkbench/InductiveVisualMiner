package org.processmining.plugins.inductiveVisualMiner.resourcePerspective;

import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class ProcessTree2DfgUnfoldedNode {

	public static class DfgUnfoldedNode {
		final Graph<UnfoldedNode> directlyFollowsGraph;
		final MultiSet<UnfoldedNode> startActivities;
		final MultiSet<UnfoldedNode> endActivities;
		boolean allowsEmptyTrace;

		public DfgUnfoldedNode() {
			directlyFollowsGraph = GraphFactory.create(UnfoldedNode.class, 1);
			startActivities = new MultiSet<>();
			endActivities = new MultiSet<>();
		}

		public void absorb(Graph<UnfoldedNode> other) {
			directlyFollowsGraph.addVertices(other.getVertices());
			for (long e : other.getEdges()) {
				directlyFollowsGraph.addEdge(other.getEdgeSource(e), other.getEdgeTarget(e), other.getEdgeWeight(0));
			}
		}
	}

	public static DfgUnfoldedNode makeDfg(UnfoldedNode unode, boolean collapseAnd) {
		if (unode.getNode() instanceof Manual) {
			return makeDfgActivity(unode);
		} else if (unode.getNode() instanceof Automatic) {
			return makeDfgTau(unode);
		} else if (unode.getNode() instanceof Xor || unode.getNode() instanceof Def) {
			return makeDfgXor(unode, collapseAnd);
		} else if (unode.getNode() instanceof Seq) {
			return makeDfgSeq(unode, collapseAnd);
		} else if (unode.getNode() instanceof And) {
			return makeDfgAnd(unode, collapseAnd);
		} else if (unode.getNode() instanceof XorLoop || unode.getNode() instanceof DefLoop) {
			return makeDfgLoop(unode, collapseAnd);
		}
		return null;
	}

	private static DfgUnfoldedNode makeDfgActivity(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Manual);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.directlyFollowsGraph.addVertex(unode);
		result.startActivities.add(unode);
		result.endActivities.add(unode);
		result.allowsEmptyTrace = false;
		return result;
	}

	private static DfgUnfoldedNode makeDfgTau(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Automatic);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.allowsEmptyTrace = true;
		return result;
	}

	private static DfgUnfoldedNode makeDfgXor(UnfoldedNode unode, boolean collapseAnd) {
		assert (unode.getNode() instanceof Def || unode.getNode() instanceof Xor);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.allowsEmptyTrace = false;

		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uChild = unode.unfoldChild(child);

			DfgUnfoldedNode subResult = makeDfg(uChild, collapseAnd);

			result.allowsEmptyTrace = result.allowsEmptyTrace || subResult.allowsEmptyTrace;

			result.absorb(subResult.directlyFollowsGraph);
			result.startActivities.addAll(subResult.startActivities);
			result.endActivities.addAll(subResult.endActivities);
		}
		return result;
	}

	private static DfgUnfoldedNode makeDfgSeq(UnfoldedNode unode, boolean collapseAnd) {
		assert (unode.getNode() instanceof Seq);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.allowsEmptyTrace = true;
		int i = 0;
		MultiSet<UnfoldedNode> childEndActivities = new MultiSet<>();
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uChild = unode.unfoldChild(child);
			DfgUnfoldedNode subResult = makeDfg(uChild, collapseAnd);
			result.allowsEmptyTrace = result.allowsEmptyTrace && subResult.allowsEmptyTrace;

			//copy the dfg
			result.absorb(subResult.directlyFollowsGraph);

			//make the connections between the last child and this child (the multiset takes care of taus)
			for (UnfoldedNode from : childEndActivities) {
				for (UnfoldedNode to : subResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}

			//if this child can yield the empty trace, keep the child's start activities
			if (!subResult.allowsEmptyTrace) {
				childEndActivities = new MultiSet<>();
			}
			childEndActivities.addAll(subResult.endActivities);

			//if this is the first child, copy the start activities
			if (i == 0) {
				result.startActivities.addAll(subResult.startActivities);
			}

			//if this is the last child, copy the end activities
			if (i == unode.getBlock().getChildren().size() - 1) {
				result.endActivities.addAll(subResult.endActivities);
			}

			i++;
		}

		return result;
	}

	private static DfgUnfoldedNode makeDfgAnd(UnfoldedNode unode, boolean collapseAnd) {
		assert (unode.getNode() instanceof And);
		DfgUnfoldedNode result = new DfgUnfoldedNode();

		if (collapseAnd) {
			result.directlyFollowsGraph.addVertex(unode);
			result.startActivities.add(unode);
			result.endActivities.add(unode);
			result.allowsEmptyTrace = false;
		} else {
			result.allowsEmptyTrace = true;
			for (Node child : unode.getBlock().getChildren()) {
				UnfoldedNode uChild = unode.unfoldChild(child);
				DfgUnfoldedNode subResult = makeDfg(uChild, collapseAnd);
				result.allowsEmptyTrace = result.allowsEmptyTrace && subResult.allowsEmptyTrace;

				//make all intra-child connections
				for (UnfoldedNode from : result.directlyFollowsGraph.getVertices()) {
					for (UnfoldedNode to : subResult.directlyFollowsGraph.getVertices()) {
						result.directlyFollowsGraph.addEdge(from, to, 1);
						result.directlyFollowsGraph.addEdge(to, from, 1);
					}
				}

				//copy the dfg
				result.absorb(subResult.directlyFollowsGraph);
				result.startActivities.addAll(subResult.startActivities);
				result.endActivities.addAll(subResult.endActivities);
			}
		}

		return result;
	}

	private static DfgUnfoldedNode makeDfgLoop(UnfoldedNode unode, boolean collapseAnd) {
		assert (unode.getNode() instanceof XorLoop || unode.getNode() instanceof DefLoop);
		DfgUnfoldedNode result = new DfgUnfoldedNode();

		//process the first child
		UnfoldedNode body = unode.unfoldChild(unode.getBlock().getChildren().get(0));
		DfgUnfoldedNode bodyResult = makeDfg(body, collapseAnd);
		result.allowsEmptyTrace = bodyResult.allowsEmptyTrace;
		result.startActivities.addAll(bodyResult.startActivities);
		result.endActivities.addAll(bodyResult.endActivities);
		result.absorb(bodyResult.directlyFollowsGraph);

		//process the redo child
		UnfoldedNode redo = unode.unfoldChild(unode.getBlock().getChildren().get(1));
		DfgUnfoldedNode redoResult = makeDfg(redo, collapseAnd);
		result.absorb(redoResult.directlyFollowsGraph);

		//connect to the body
		for (UnfoldedNode from : bodyResult.endActivities) {
			for (UnfoldedNode to : redoResult.startActivities) {
				result.directlyFollowsGraph.addEdge(from, to, 1);
			}
		}
		for (UnfoldedNode from : redoResult.endActivities) {
			for (UnfoldedNode to : bodyResult.startActivities) {
				result.directlyFollowsGraph.addEdge(from, to, 1);
			}
		}

		//if the body allows for the empty trace, the start/end activities of the redo children are global
		if (bodyResult.allowsEmptyTrace) {
			result.startActivities.addAll(redoResult.startActivities);
			result.endActivities.addAll(redoResult.endActivities);

			//moreover, connect all redo end to start
			for (UnfoldedNode from : redoResult.endActivities) {
				for (UnfoldedNode to : redoResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}
		}

		//if one redo allows for the empty trace,
		if (redoResult.allowsEmptyTrace) {
			//connect all body ends to starts
			for (UnfoldedNode from : bodyResult.endActivities) {
				for (UnfoldedNode to : bodyResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}
		}

		return result;
	}
}
