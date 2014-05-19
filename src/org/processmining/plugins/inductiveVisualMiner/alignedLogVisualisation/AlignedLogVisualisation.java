package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.MaybeString;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.metrics.PropertyDirectlyFollowsGraph;
import org.processmining.plugins.InductiveMiner.plugins.IMiProcessTree;
import org.processmining.plugins.etm.termination.ProMCancelTerminationCondition;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentETM;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

@Plugin(name = "Show deviations", returnLabels = { "dot" }, returnTypes = { Dot.class }, parameterLabels = {
		"process tree", "event log" }, userAccessible = true)
public class AlignedLogVisualisation {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Show deviations", requiredParameterLabels = { 0, 1 })
	public Dot fancy(PluginContext context, ProcessTree tree, XLog xLog) {
		AlignmentResult result = AlignmentETM.alignTree(tree, MiningParameters.getDefaultClassifier(), xLog,
				new HashSet<XEventClass>(), ProMCancelTerminationCondition.buildDummyCanceller());
		Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos = InductiveVisualMinerController.computeDfgAlignment(result.log,
				tree);
		return fancy(tree, result.logInfo, dfgLogInfos, new AlignedLogVisualisationParameters());
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Show deviations", requiredParameterLabels = { 0 })
	public Dot fancy(PluginContext context, ProcessTree tree) {
		return fancy(tree, null, null, new AlignedLogVisualisationParameters());
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Show deviations", requiredParameterLabels = { 0 })
	public Dot fancy(PluginContext context, XLog xLog) {
		ProcessTree tree = IMiProcessTree.mineProcessTree(xLog);
		return fancy(context, tree, xLog);
	}

	public enum NodeType {
		source, sink, activity, xor, parallel, logMoveActivity
	}

	public class LocalDotNode extends DotNode {

		public NodeType type;
		public final UnfoldedNode node;

		public LocalDotNode(NodeType type, String label, final UnfoldedNode unode) {
			super(label, "");
			dot.addNode(this);
			if (unfoldedNode2dotNodes.get(unode) == null) {
				unfoldedNode2dotNodes.put(unode, new ArrayList<LocalDotNode>());
			}
			unfoldedNode2dotNodes.get(unode).add(this);
			dotNodes.add(this);
			this.node = unode;
			this.type = type;

			switch (type) {
				case activity :
					setOptions("shape=\"box\", style=\"rounded,filled\", fontsize=9");
					break;
				case logMoveActivity :
					setOptions("shape=\"box\", style=\"rounded,filled\", fontsize=9, fillcolor=\"red\"");
					break;
				case parallel :
					setOptions("shape=\"diamond\", fixedsize=true, height=0.25, width=0.27");
					break;
				case sink :
					setOptions("width=0.2, shape=\"circle\", style=filled, fillcolor=\"red\"");
					break;
				case source :
					setOptions("width=0.2, shape=\"circle\", style=filled, fillcolor=\"green\"");
					break;
				case xor :
					setOptions("width=0.05, shape=\"circle\"");
					break;
			}

			if (parameters.isAddOnClick()) {
				addMouseListener(new MouseListener() {

					public void mouseReleased(MouseEvent arg0) {
					}

					public void mousePressed(MouseEvent arg0) {

					}

					public void mouseExited(MouseEvent arg0) {
					}

					public void mouseEntered(MouseEvent arg0) {
					}

					public void mouseClicked(MouseEvent arg0) {

					}
				});
			}
		}
	}

	public enum EdgeType {
		model, logMove, modelMove
	};

	public class LocalDotEdge extends DotEdge {

		private final EdgeType type;
		private final UnfoldedNode unode;
		private final UnfoldedNode lookupNode1;
		private final UnfoldedNode lookupNode2;
		private final boolean directionForward;

		//constructor for model edge
		public LocalDotEdge(LocalDotNode source, LocalDotNode target, String label, String options, UnfoldedNode unode, boolean directionForward) {
			super(source, target, label, options);
			dot.addEdge(this);
			this.unode = unode;
			this.lookupNode1 = null;
			this.lookupNode2 = null;
			this.type = EdgeType.model;
			this.directionForward = directionForward;

			if (!unfoldedNode2dotEdgesModel.containsKey(unode)) {
				unfoldedNode2dotEdgesModel.put(unode, new ArrayList<LocalDotEdge>());
			}
			unfoldedNode2dotEdgesModel.get(unode).add(this);
			dotEdges.add(this);
		}

		public LocalDotEdge(LocalDotNode source, LocalDotNode target, String label, String options, UnfoldedNode unode,
				EdgeType type, UnfoldedNode lookupNode1, UnfoldedNode lookupNode2, boolean directionForward) {
			super(source, target, label, options);
			dot.addEdge(this);
			this.unode = unode;
			this.lookupNode1 = lookupNode1;
			this.lookupNode2 = lookupNode2;
			this.type = type;
			this.directionForward = directionForward;

			if (!unfoldedNode2dotEdgesMove.containsKey(unode)) {
				unfoldedNode2dotEdgesMove.put(unode, new ArrayList<LocalDotEdge>());
			}
			unfoldedNode2dotEdgesMove.get(unode).add(this);
			dotEdges.add(this);
		}
		
		public LocalDotNode getTarget() {
			if (directionForward) {
				return (LocalDotNode) super.getTarget();
			} else {
				return (LocalDotNode) super.getSource();
			}
		}

		public LocalDotNode getSource() {
			if (directionForward) {
				return (LocalDotNode) super.getSource();
			} else {
				return (LocalDotNode) super.getTarget();
			}
		}

		public EdgeType getType() {
			return type;
		}

		public UnfoldedNode getUnode() {
			return unode;
		}
		
		public boolean isDirectionForward() {
			return directionForward;
		}

		public UnfoldedNode getLookupNode1() {
			return lookupNode1;
		}
		
		public UnfoldedNode getLookupNode2() {
			return lookupNode2;
		}
	}

	private Dot dot;
	private AlignedLogInfo logInfo;
	private Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos;
	private long maxCardinality;
	private long minCardinality;
	private AlignedLogVisualisationParameters parameters;
	private LocalDotNode rootSource;
	private LocalDotNode rootSink;

	private Set<LocalDotEdge> dotEdges;
	private Set<LocalDotNode> dotNodes;
	private Map<UnfoldedNode, List<LocalDotNode>> unfoldedNode2dotNodes;
	private Map<UnfoldedNode, List<LocalDotEdge>> unfoldedNode2dotEdgesModel;
	private Map<UnfoldedNode, List<LocalDotEdge>> unfoldedNode2dotEdgesMove;
	private Map<UnfoldedNode, LocalDotNode> activity2dotNode;
	private Map<LocalDotNode, UnfoldedNode> dotNode2DfgUnfoldedNode;
	private Map<UnfoldedNode, List<LocalDotNode>> unfoldedNode2DfgdotNodes;
	private Map<UnfoldedNode, List<LocalDotEdge>> unfoldedNode2DfgdotEdges;

	public Dot fancy(ProcessTree tree, AlignedLogInfo logInfo, Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos,
			AlignedLogVisualisationParameters parameters) {
		this.parameters = parameters;
		if (logInfo == null) {
			//use empty logInfo
			logInfo = new AlignedLogInfo(new MultiSet<IMTraceG<Move>>());
			//set parameters to not show frequencies
			parameters.setShowFrequenciesOnModelEdges(false);
			parameters.setShowFrequenciesOnMoveEdges(false);
			parameters.setShowFrequenciesOnNodes(false);
		}
		this.logInfo = logInfo;

		if (dfgLogInfos == null) {
			dfgLogInfos = new HashMap<UnfoldedNode, AlignedLogInfo>();
		}
		this.dfgLogInfos = dfgLogInfos;

		//find maximum and mimimum occurrences
		Pair<Long, Long> p = AlignedLogMetrics.getExtremes(new UnfoldedNode(tree.getRoot()), logInfo, true);
		minCardinality = p.getLeft();
		maxCardinality = p.getRight();

		dotNodes = new HashSet<>();
		dotEdges = new HashSet<>();
		unfoldedNode2dotNodes = new HashMap<UnfoldedNode, List<LocalDotNode>>();
		unfoldedNode2dotEdgesModel = new HashMap<UnfoldedNode, List<LocalDotEdge>>();
		unfoldedNode2dotEdgesMove = new HashMap<UnfoldedNode, List<LocalDotEdge>>();
		activity2dotNode = new HashMap<UnfoldedNode, LocalDotNode>();
		dotNode2DfgUnfoldedNode = new HashMap<LocalDotNode, UnfoldedNode>();
		unfoldedNode2DfgdotNodes = new HashMap<ProcessTree2Petrinet.UnfoldedNode, List<LocalDotNode>>();
		unfoldedNode2DfgdotEdges = new HashMap<ProcessTree2Petrinet.UnfoldedNode, List<LocalDotEdge>>();
		dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		UnfoldedNode root = new UnfoldedNode(tree.getRoot());

		//source
		rootSource = new LocalDotNode(NodeType.source, "", root);

		//sink
		rootSink = new LocalDotNode(NodeType.sink, "", root);

		//convert root node
		convertNode(root, rootSource, rootSink, true);

		//add log-move-arcs to source and sink
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(rootSource, rootSource, root, null, root, true);
			visualiseLogMove(rootSink, rootSink, root, root, null, false);
		}

		return dot;
	}

	private void convertNode(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		if (PropertyDirectlyFollowsGraph.isSet(unode.getNode())) {
			convertDirectlyFollowsGraph(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Seq) {
			convertSequence(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof XorLoop) {
			convertLoop(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof And) {
			convertParallel(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Xor) {
			convertXor(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Manual) {
			convertActivity(unode, source, sink, directionForward);
		} else if (unode.getNode() instanceof Automatic) {
			convertTau(unode, source, sink, directionForward);
		}
	}

	private void convertActivity(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, logInfo);
		LocalDotNode dotNode = convertActivity(unode, cardinality);

		addArc(source, dotNode, unode, directionForward);
		addArc(dotNode, sink, unode, directionForward);

		//draw model moves
		if (parameters.isShowModelMoves()) {
			long modelMoves = AlignedLogMetrics.getModelMovesLocal(unode, logInfo);
			if (modelMoves != 0) {
				addMoveArc(source, sink, unode, EdgeType.modelMove, null, null, modelMoves, directionForward);
			}
		}

		//draw self-log moves
		if (parameters.isShowLogMoves()) {
			MultiSet<XEventClass> selfLogMoves = AlignedLogMetrics.getLogMoves(unode, unode, logInfo);
			if (selfLogMoves.size() != 0) {
				visualiseLogMove(dotNode, dotNode, unode, unode, unode, directionForward);
			}
		}
	}

	private LocalDotNode convertActivity(UnfoldedNode unode, long cardinality) {
		//style the activity by the occurrences of it
		String fillColour = "#FFFFFF";
		if (cardinality != 0 && parameters.getColourNodes() != null) {
			fillColour = parameters.getColourNodes().colour((long) (getOccurrenceFactor(cardinality) * 100), 100);
		}

		//determine label colour
		String fontColour = "black";
		if (ColourMaps.getLuma(fillColour) < 128) {
			fontColour = "white";
		}

		String label = unode.getNode().getName();
		if (label.length() == 0) {
			label = " ";
		}
		if (cardinality != -1 && parameters.isShowFrequenciesOnNodes()) {
			label += "\n" + cardinality;
		}

		final LocalDotNode dotNode = new LocalDotNode(NodeType.activity, label, unode);
		dotNode.setOptions(dotNode.getOptions() + ", fillcolor=\"" + fillColour + "\", fontcolor=\"" + fontColour
				+ "\"");

		activity2dotNode.put(unode, dotNode);
		return dotNode;
	}

	private void convertTau(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		addArc(source, sink, unode, directionForward);
	}

	private void convertSequence(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {
		LocalDotNode split;
		LocalDotNode join = source;

		Iterator<Node> it = unode.getBlock().getChildren().iterator();
		while (it.hasNext()) {
			Node child = it.next();

			split = join;
			if (it.hasNext()) {
				join = new LocalDotNode(NodeType.xor, "", unode);
			} else {
				join = sink;
			}

			convertNode(unode.unfoldChild(child), split, join, directionForward);

			//draw log-move-arc if necessary
			if (parameters.isShowLogMoves()) {
				visualiseLogMove(join, join, unode, unode, unode.unfoldChild(child), directionForward);
			}
		}
	}

	private void convertLoop(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(NodeType.xor, "", unode);
		addArc(source, split, unode, directionForward);

		//operator join
		LocalDotNode join = new LocalDotNode(NodeType.xor, "", unode);

		Node bodyChild = unode.getBlock().getChildren().get(0);
		convertNode(unode.unfoldChild(bodyChild), split, join, directionForward);

		Node redoChild = unode.getBlock().getChildren().get(1);
		convertNode(unode.unfoldChild(redoChild), join, split, !directionForward);

		Node exitChild = unode.getBlock().getChildren().get(2);
		convertNode(unode.unfoldChild(exitChild), join, sink, directionForward);

		//put log-moves on children
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(join, join, unode, unode, unode.unfoldChild(bodyChild), directionForward);
			visualiseLogMove(split, split, unode, unode, unode.unfoldChild(redoChild), directionForward);
		}
	}

	private void convertParallel(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(NodeType.parallel, "+", unode);
		addArc(source, split, unode, directionForward);

		//operator join
		LocalDotNode join = new LocalDotNode(NodeType.parallel, "+", unode);
		addArc(join, sink, unode, directionForward);

		for (Node child : unode.getBlock().getChildren()) {
			convertNode(unode.unfoldChild(child), split, join, directionForward);
		}

		//put log-moves on join, if necessary
		if (parameters.isShowLogMoves()) {
			visualiseLogMove(join, join, unode, unode, unode, directionForward);
		}
	}

	private void convertXor(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink, boolean directionForward) {

		//operator split
		LocalDotNode split = new LocalDotNode(NodeType.xor, "", unode);
		addArc(source, split, unode, directionForward);

		//operator join
		LocalDotNode join = new LocalDotNode(NodeType.xor, "", unode);
		addArc(join, sink, unode, directionForward);

		for (Node child : unode.getBlock().getChildren()) {
			convertNode(unode.unfoldChild(child), split, join, directionForward);
		}

		//log-moves
		//are never put on xor
	}

	private void convertDirectlyFollowsGraph(UnfoldedNode unode, LocalDotNode source, LocalDotNode sink,
			boolean directionForward) {
		//make map of activities and add nodes
		Map<String, LocalDotNode> mapName2dotNode = new HashMap<String, LocalDotNode>();
		unfoldedNode2DfgdotNodes.put(unode, new ArrayList<LocalDotNode>());
		for (UnfoldedNode unode2 : AlignedLogMetrics.unfoldAllNodes(unode)) {
			if (unode2.getNode() instanceof Manual) {
				LocalDotNode dotNode = convertActivity(unode2,
						AlignedLogMetrics.getNumberOfTracesRepresented(unode2, logInfo));
				mapName2dotNode.put(unode2.getNode().getName(), dotNode);
				unfoldedNode2DfgdotNodes.get(unode).add(dotNode);
				dotNode2DfgUnfoldedNode.put(dotNode, unode);
			}
		}

		//get the directly-follows graph
		List<Triple<MaybeString, MaybeString, Long>> edges = PropertyDirectlyFollowsGraph.get(unode.getNode());

		//get the logInfo of the directly-follows graph
		AlignedLogInfo dfgLogInfo = dfgLogInfos.get(unode);
		if (dfgLogInfo == null) {
			dfgLogInfo = new AlignedLogInfo(new AlignedLog());
		}

		//add edges
		unfoldedNode2DfgdotEdges.put(unode, new ArrayList<LocalDotEdge>());
		for (Triple<MaybeString, MaybeString, Long> edge : edges) {

			//get endpoints-names
			String fromName = edge.getA().get();
			String toName = edge.getB().get();

			//get dotNodes of the endpoints
			LocalDotNode fromDotNode = mapName2dotNode.get(fromName);
			LocalDotNode toDotNode = mapName2dotNode.get(toName);

			//create the edge
			LocalDotEdge dotEdge;
			if (fromDotNode != null && toDotNode != null) {
				//normal edge
				long cardinality = dfgLogInfo.getDfg(fromDotNode.node, toDotNode.node);
				dotEdge = addModelArc(fromDotNode, toDotNode, unode, directionForward, cardinality);
			} else if (fromDotNode == null) {
				//start activity
				long cardinality = dfgLogInfo.getDfg(null, toDotNode.node);
				dotEdge = addModelArc(source, toDotNode, unode, directionForward, cardinality);
			} else {
				//end activity
				long cardinality = dfgLogInfo.getDfg(fromDotNode.node, null);
				dotEdge = addModelArc(fromDotNode, sink, unode, directionForward, cardinality);
			}

			unfoldedNode2DfgdotEdges.get(unode).add(dotEdge);
		}
	}

	private LocalDotEdge addArc(LocalDotNode from, LocalDotNode to, final UnfoldedNode unode, boolean directionForward) {
		return addModelArc(from, to, unode, directionForward,
				AlignedLogMetrics.getNumberOfTracesRepresented(unode, logInfo));
	}

	public static double getPenWidth(long cardinality, long minCardinality, long maxCardinality, boolean widen) {
		if (widen) {
			return getOccurrenceFactor(cardinality, minCardinality, maxCardinality) * 100 + 1;
		} else {
			return 1;
		}
	}

	private LocalDotEdge addModelArc(LocalDotNode from, LocalDotNode to, final UnfoldedNode unode,
			boolean directionForward, long cardinality) {

		String options = "";

		if (parameters.getColourModelEdges() != null) {
			String lineColour = null;
			lineColour = parameters.getColourModelEdges().colour(cardinality, minCardinality, maxCardinality);
			options += "color=\"" + lineColour + "\", ";
		}

		options += "penwidth=" + parameters.getModelEdgesWidth().size(cardinality, minCardinality, maxCardinality);

		final LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(from, to, "", options, unode, directionForward);
		} else {
			edge = new LocalDotEdge(to, from, "", options + ", dir=\"back\"", unode, directionForward);
		}

		if (parameters.isShowFrequenciesOnModelEdges()) {
			edge.setLabel(cardinality + "");
		}

		return edge;
	}

	private void visualiseLogMove(LocalDotNode from, LocalDotNode to, UnfoldedNode unode, UnfoldedNode lookupNode1,
			UnfoldedNode lookupNode2, boolean directionForward) {
		MultiSet<XEventClass> logMoves = AlignedLogMetrics.getLogMoves(lookupNode1, lookupNode2, logInfo);
		if (logMoves.size() > 0) {
			if (parameters.isRepairLogMoves()) {
				for (XEventClass e : logMoves) {
					long cardinality = logMoves.getCardinalityOf(e);
					LocalDotNode dotNode = new LocalDotNode(NodeType.logMoveActivity, e.toString(), unode);
					addMoveArc(from, dotNode, unode, EdgeType.logMove, lookupNode1, lookupNode2, cardinality,
							directionForward);
					addMoveArc(dotNode, to, unode, EdgeType.logMove, lookupNode1, lookupNode2, cardinality,
							directionForward);
				}
			} else {
				addMoveArc(from, to, unode, EdgeType.logMove, lookupNode1, lookupNode2, logMoves.size(),
						directionForward);
			}
		}
	}

	private LocalDotEdge addMoveArc(LocalDotNode from, LocalDotNode to, UnfoldedNode unode, EdgeType type,
			UnfoldedNode lookupNode1, UnfoldedNode lookupNode2, long cardinality, boolean directionForward) {

		String options = "style=\"dashed\", arrowsize=.5";

		if (parameters.getColourMoves() != null) {
			String lineColour = parameters.getColourMoves().colour(cardinality, minCardinality, maxCardinality);
			options += ", color=\"" + lineColour + "\", fontcolor=\"" + lineColour + "\"";
		}

		options += ", penwidth=" + parameters.getMoveEdgesWidth().size(cardinality, minCardinality, maxCardinality);

		LocalDotEdge edge;
		if (directionForward) {
			edge = new LocalDotEdge(from, to, "", options, unode, type, lookupNode1, lookupNode2, directionForward);
		} else {
			edge = new LocalDotEdge(to, from, "", options + ", dir=\"back\", " + options, unode, type, lookupNode1,
					lookupNode2, directionForward);
		}

		if (parameters.isShowFrequenciesOnMoveEdges()) {
			edge.setLabel(cardinality + "");
		}

		return edge;
	}

	private double getOccurrenceFactor(long cardinality) {
		return getOccurrenceFactor(cardinality, minCardinality, maxCardinality);
	}

	private static double getOccurrenceFactor(long cardinality, long minCardinality, long maxCardinality) {
		if (minCardinality == maxCardinality) {
			return 1;
		}
		if (cardinality != -1 && minCardinality != -1 && maxCardinality != -1) {
			return (cardinality - minCardinality) / ((maxCardinality - minCardinality) * 1.0);
		}

		return 0;
	}

	public Map<UnfoldedNode, List<LocalDotNode>> getUnfoldedNode2dotNodes() {
		return unfoldedNode2dotNodes;
	}

	public Map<UnfoldedNode, List<LocalDotEdge>> getUnfoldedNode2dotEdgesModel() {
		return unfoldedNode2dotEdgesModel;
	}

	public Map<UnfoldedNode, List<LocalDotEdge>> getUnfoldedNode2dotEdgesMove() {
		return unfoldedNode2dotEdgesMove;
	}

	public Map<UnfoldedNode, LocalDotNode> getActivity2dotNode() {
		return activity2dotNode;
	}

	public Map<UnfoldedNode, List<LocalDotNode>> getUnfoldedNode2DfgdotNodes() {
		return unfoldedNode2DfgdotNodes;
	}

	public Map<UnfoldedNode, List<LocalDotEdge>> getUnfoldedNode2DfgdotEdges() {
		return unfoldedNode2DfgdotEdges;
	}

	public Set<LocalDotEdge> getEdges() {
		return dotEdges;
	}
	
	public Set<LocalDotNode> getNodes() {
		return dotNodes;
	}

	public LocalDotNode getRootSource() {
		return rootSource;
	}

	public LocalDotNode getRootSink() {
		return rootSink;
	}
}
