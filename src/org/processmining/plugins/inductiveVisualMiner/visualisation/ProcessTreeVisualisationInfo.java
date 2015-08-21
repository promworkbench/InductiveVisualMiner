package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class ProcessTreeVisualisationInfo {
	private final Set<LocalDotNode> nodes;
	private final Map<UnfoldedNode, List<LocalDotNode>> unodes;
	private final Map<UnfoldedNode, LocalDotNode> activityNodes;
	private LocalDotNode source;
	private LocalDotNode sink;
	
	private final Set<LocalDotEdge> edges;
	private final Map<UnfoldedNode, List<LocalDotEdge>> modelEdges;
	private final Map<UnfoldedNode, List<LocalDotEdge>> modelMoveEdges;
	private final Map<Pair<UnfoldedNode, UnfoldedNode>, LocalDotEdge> logMoveEdges;
	private final Set<LocalDotEdge> allModelEdges;
	private final Set<LocalDotEdge> allLogMoveEdges;
	private final Set<LocalDotEdge> allModelMoveEdges;
	
	public ProcessTreeVisualisationInfo() {
		nodes = new HashSet<>();
		unodes = new HashMap<>();
		activityNodes = new HashMap<>();
		
		edges = new HashSet<>();
		modelEdges = new HashMap<>();
		modelMoveEdges = new HashMap<>();
		logMoveEdges = new HashMap<>();
		allModelEdges = new HashSet<>();
		allLogMoveEdges = new HashSet<>();
		allModelMoveEdges = new HashSet<>();
	}
	
	public void setRoot(LocalDotNode source, LocalDotNode sink) {
		this.source = source;
		this.sink = sink;
		nodes.add(source);
		nodes.add(sink);
	}
	
	public void addNode(UnfoldedNode unode, LocalDotNode node) {
		if (!unodes.containsKey(unode)) {
			unodes.put(unode, new ArrayList<LocalDotNode>());
		}
		unodes.get(unode).add(node);
		
		if (node.getType() == NodeType.activity) {
			activityNodes.put(unode, node);
		}
		
		nodes.add(node);
	}
	
	public void addEdge(UnfoldedNode unode1, UnfoldedNode unode2, LocalDotEdge edge) {
		switch (edge.getType()) {
			case logMove :
				allLogMoveEdges.add(edge);
				assert(!logMoveEdges.containsKey(Pair.of(unode1, unode2)));
				logMoveEdges.put(Pair.of(unode1, unode2), edge);
				break;
			case model :
				if (!modelEdges.containsKey(unode1)) {
					modelEdges.put(unode1, new ArrayList<LocalDotEdge>());
				}
				modelEdges.get(unode1).add(edge);
				allModelEdges.add(edge);
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
	
	public void registerExtraEdge(UnfoldedNode unode1, UnfoldedNode unode2, LocalDotEdge edge) {
		assert(!logMoveEdges.containsKey(Pair.of(unode1, unode2)));
		logMoveEdges.put(Pair.of(unode1, unode2), edge);
	}
	
	public Set<LocalDotNode> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}
	
	public List<LocalDotNode> getNodes(UnfoldedNode unode) {
		if (!unodes.containsKey(unode)) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(unodes.get(unode));
	}
	
	public LocalDotNode getActivityNode(UnfoldedNode unode) {
		return activityNodes.get(unode);
	}
	
	public Collection<LocalDotNode> getAllActivityNodes() {
		return activityNodes.values();
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
	
	public List<LocalDotEdge> getModelEdges(UnfoldedNode unode) {
		if (!modelEdges.containsKey(unode)) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(modelEdges.get(unode));
	}
	
	public List<LocalDotEdge> getModelMoveEdges(UnfoldedNode unode) {
		if (!modelMoveEdges.containsKey(unode)) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(modelMoveEdges.get(unode));
	}
	
	public LocalDotEdge getLogMoveEdge(UnfoldedNode unode1, UnfoldedNode unode2) {
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
}
