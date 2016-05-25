package org.processmining.plugins.inductiveVisualMiner.visualisation;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class ProcessTreeVisualisationInfo {
	private final Set<LocalDotNode> nodes;
	private final TIntObjectMap<List<LocalDotNode>> unodes;
	private final TIntObjectMap<LocalDotNode> activityNodes;
	private LocalDotNode source;
	private LocalDotNode sink;

	private final Set<LocalDotEdge> edges;
	private final TIntObjectMap<List<LocalDotEdge>> modelEdges;
	private final TIntObjectMap<List<LocalDotEdge>> modelMoveEdges;
	private final Map<Pair<Integer, Integer>, LocalDotEdge> logMoveEdges;
	private final Set<LocalDotEdge> allModelEdges;
	private final Set<LocalDotEdge> allLogMoveEdges;
	private final Set<LocalDotEdge> allModelMoveEdges;
	private final Set<LocalDotEdge> allTauEdges;
	
	private final Map<LocalDotNode, LocalDotNode> join2split;

	public ProcessTreeVisualisationInfo() {
		nodes = new THashSet<>();
		unodes = new TIntObjectHashMap<>(10, 0.5f, -1);
		activityNodes = new TIntObjectHashMap<>(10, 0.5f, -1);

		edges = new THashSet<>();
		modelEdges = new TIntObjectHashMap<>(10, 0.5f, -1);
		modelMoveEdges = new TIntObjectHashMap<>(10, 0.5f, -1);
		logMoveEdges = new THashMap<>();
		allModelEdges = new THashSet<>();
		allLogMoveEdges = new THashSet<>();
		allModelMoveEdges = new THashSet<>();
		allTauEdges = new THashSet<>();
		
		join2split = new THashMap<>();
	}

	public void setRoot(LocalDotNode source, LocalDotNode sink) {
		this.source = source;
		this.sink = sink;
		nodes.add(source);
		nodes.add(sink);
	}

	public void addNode(int unode, LocalDotNode node, LocalDotNode correspondingSplit) {
		if (!unodes.containsKey(unode)) {
			unodes.put(unode, new ArrayList<LocalDotNode>());
		}
		unodes.get(unode).add(node);

		if (node.getType() == NodeType.activity) {
			activityNodes.put(unode, node);
		}

		nodes.add(node);
		
		if (correspondingSplit != null) {
			join2split.put(node, correspondingSplit);
		}
	}

	public void addEdge(IvMEfficientTree tree, int unode1, int unode2, LocalDotEdge edge) {
		switch (edge.getType()) {
			case logMove :
				allLogMoveEdges.add(edge);
				assert (!logMoveEdges.containsKey(Pair.of(unode1, unode2)));
				logMoveEdges.put(Pair.of(unode1, unode2), edge);
				break;
			case model :
				if (!modelEdges.containsKey(unode1)) {
					modelEdges.put(unode1, new ArrayList<LocalDotEdge>());
				}
				modelEdges.get(unode1).add(edge);
				allModelEdges.add(edge);
				if (tree.isTau(unode1)) {
					allTauEdges.add(edge);
				}
				break;
			case modelMove :
				if (!modelMoveEdges.containsKey(unode1)) {
					modelMoveEdges.put(unode1, new ArrayList<LocalDotEdge>());
				}
				modelMoveEdges.get(unode1).add(edge);
				allModelMoveEdges.add(edge);
				break;
			default :
				break;
		}
		edges.add(edge);
	}

	public void registerExtraEdge(int unode1, int unode2, LocalDotEdge edge) {
		assert (!logMoveEdges.containsKey(Pair.of(unode1, unode2)));
		logMoveEdges.put(Pair.of(unode1, unode2), edge);
	}

	public Set<LocalDotNode> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}

	public List<LocalDotNode> getNodes(int unode) {
		if (!unodes.containsKey(unode)) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(unodes.get(unode));
	}

	public LocalDotNode getActivityDotNode(int unode) {
		return activityNodes.get(unode);
	}

	public Collection<LocalDotNode> getAllActivityNodes() {
		return activityNodes.valueCollection();
	}

	public LocalDotNode getSource() {
		return source;
	}

	public LocalDotNode getSink() {
		return sink;
	}

	public Set<LocalDotEdge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	public List<LocalDotEdge> getModelEdges(int unode) {
		if (!modelEdges.containsKey(unode)) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(modelEdges.get(unode));
	}

	public List<LocalDotEdge> getModelMoveEdges(int unode) {
		if (!modelMoveEdges.containsKey(unode)) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(modelMoveEdges.get(unode));
	}

	public LocalDotEdge getLogMoveEdge(int unode1, int unode2) {
		return logMoveEdges.get(Pair.of(unode1, unode2));
	}

	public Set<LocalDotEdge> getAllModelEdges() {
		return Collections.unmodifiableSet(allModelEdges);
	}

	public Set<LocalDotEdge> getAllLogMoveEdges() {
		return Collections.unmodifiableSet(allLogMoveEdges);
	}

	public Set<LocalDotEdge> getAllModelMoveEdges() {
		return Collections.unmodifiableSet(allModelMoveEdges);
	}

	public Set<LocalDotEdge> getAllTauEdges() {
		return Collections.unmodifiableSet(allTauEdges);
	}

	public LocalDotNode getSplitCorrespondingToJoin(LocalDotNode join) {
		return join2split.get(join);
	}
}
