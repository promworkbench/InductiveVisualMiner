package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMap;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;

public class InductiveVisualMinerSelectionColourer {

	public static void colourSelection(SVGDiagram diagram, TIntSet selectedNodes, Set<LogMovePosition> selectedLogMoves,
			ProcessTreeVisualisationInfo visualisationInfo) {
		for (TIntIterator it = selectedNodes.iterator(); it.hasNext();) {
			int unode = it.next();
			LocalDotNode dotNode = Animation.getDotNodeFromActivity(unode, visualisationInfo);
			colourSelectedNode(diagram, dotNode, true);
		}
		//re-colour the selected log moves
		for (LogMovePosition logMove : selectedLogMoves) {
			LocalDotEdge dotEdge = Animation.getDotEdgeFromLogMove(logMove, visualisationInfo);
			colourSelectedEdge(diagram, dotEdge, true);
		}
	}

	public static void colourSelectedNode(SVGDiagram svg, LocalDotNode dotNode, boolean selected) {
		Group svgGroup = DotPanel.getSVGElementOf(svg, dotNode);
		SVGElement shape = svgGroup.getChild(1);

		if (selected) {
			dotNode.unselectedAppearance.stroke = DotPanel.setCSSAttributeOf(shape, "stroke", "red");
			dotNode.unselectedAppearance.strokeWidth = DotPanel.setCSSAttributeOf(shape, "stroke-width", "3");
			dotNode.unselectedAppearance.strokeDashArray = DotPanel.setCSSAttributeOf(shape, "stroke-dasharray", "5,5");
		} else {
			DotPanel.setCSSAttributeOf(shape, "stroke", dotNode.unselectedAppearance.stroke);
			DotPanel.setCSSAttributeOf(shape, "stroke-width", dotNode.unselectedAppearance.strokeWidth);
			DotPanel.setCSSAttributeOf(shape, "stroke-dasharray", dotNode.unselectedAppearance.strokeDashArray);
		}
	}

	public static void colourSelectedEdge(SVGDiagram svg, LocalDotEdge dotEdge, boolean selected) {
		Group svgGroup = DotPanel.getSVGElementOf(svg, dotEdge);
		SVGElement line = svgGroup.getChild(1);
		SVGElement text = svgGroup.getChild(3);

		if (selected) {
			dotEdge.unselectedAppearance.textFill = DotPanel.setCSSAttributeOf(text, "fill", "none");
			if (dotEdge.getType() != EdgeType.model) {
				dotEdge.unselectedAppearance.textStroke = DotPanel.setCSSAttributeOf(text, "stroke", "red");
				dotEdge.unselectedAppearance.lineStrokeDashArray = DotPanel.setCSSAttributeOf(line, "stroke-dasharray",
						"2,5");
			} else {
				dotEdge.unselectedAppearance.textStroke = DotPanel.setCSSAttributeOf(text, "stroke", "black");
				dotEdge.unselectedAppearance.lineStrokeDashArray = DotPanel.setCSSAttributeOf(line, "stroke-dasharray",
						"2,2");
			}
			dotEdge.unselectedAppearance.textStrokeWidth = DotPanel.setCSSAttributeOf(text, "stroke-width", "0.55");

		} else {
			DotPanel.setCSSAttributeOf(text, "fill", dotEdge.unselectedAppearance.textFill);
			DotPanel.setCSSAttributeOf(text, "stroke", dotEdge.unselectedAppearance.textStroke);
			DotPanel.setCSSAttributeOf(text, "stroke-width", dotEdge.unselectedAppearance.textStrokeWidth);
			DotPanel.setCSSAttributeOf(line, "stroke-dasharray", dotEdge.unselectedAppearance.lineStrokeDashArray);
		}
	}

	public static TraceViewEventColourMap colourHighlighting(SVGDiagram svg, ProcessTreeVisualisationInfo info,
			IvMEfficientTree tree, AlignedLogVisualisationData data,
			ProcessTreeVisualisationParameters visualisationParameters) throws UnknownTreeNodeException {

		TraceViewEventColourMap traceViewColourMap = new TraceViewEventColourMap(tree);

		//compute extreme cardinalities
		Pair<Long, Long> extremes = data.getExtremeCardinalities();
		long minCardinality = extremes.getLeft();
		long maxCardinality = extremes.getRight();

		try {
			//style nodes
			for (int unode : EfficientTreeUtils.getAllNodes(tree)) {
				Triple<String, Long, Long> cardinality = data.getNodeLabel(unode, false);
				Pair<Color, Color> colour = styleUnfoldedNode(tree, unode, svg, info, cardinality, minCardinality,
						maxCardinality, visualisationParameters);

				if (tree.isActivity(unode)) {
					traceViewColourMap.set(unode, colour.getA(), colour.getB());
				}
			}

			//style edges
			styleEdges(tree, svg, info, data, visualisationParameters, traceViewColourMap, minCardinality,
					maxCardinality);

		} catch (SVGException e) {
			e.printStackTrace();
		}

		return traceViewColourMap;
	}

	public static Pair<Color, Color> styleUnfoldedNode(IvMEfficientTree tree, int unode, SVGDiagram svg,
			ProcessTreeVisualisationInfo info, Triple<String, Long, Long> cardinality, long minCardinality,
			long maxCardinality, ProcessTreeVisualisationParameters visualisationParameters) throws SVGException {
		if (tree.isActivity(unode)) {
			return styleManual(tree, unode, svg, info, cardinality, minCardinality, maxCardinality,
					visualisationParameters);
		} else {
			styleNonManualNode(tree, unode, svg, info, cardinality);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static Pair<Color, Color> styleManual(IvMEfficientTree tree, int unode, SVGDiagram svg,
			ProcessTreeVisualisationInfo info, Triple<String, Long, Long> cardinality, long minCardinality,
			long maxCardinality, ProcessTreeVisualisationParameters visualisationParameters) throws SVGException {

		LocalDotNode dotNode = info.getActivityDotNode(unode);

		Group group = DotPanel.getSVGElementOf(svg, dotNode);
		SVGElement polygon = group.getChild(1);
		Text titleName = (Text) group.getChild(group.getChildren(null).size() - 2);
		Text titleCount = (Text) group.getChild(group.getChildren(null).size() - 1);

		//recolour the polygon
		Color fillColour;
		Color fontColour = Color.black;
		if (cardinality.getB() > 0) {
			fillColour = visualisationParameters.getColourNodes().colour(cardinality.getB(), minCardinality,
					maxCardinality);
			if (ColourMaps.getLuma(fillColour) < 128) {
				fontColour = Color.white;
			}
		} else {
			fillColour = visualisationParameters.getColourNodes().colour(1, 0, 2);
		}
		DotPanel.setCSSAttributeOf(polygon, "fill", fillColour);

		//set label colour
		DotPanel.setCSSAttributeOf(titleCount, "fill", fontColour);
		DotPanel.setCSSAttributeOf(titleName, "fill", fontColour);

		if (cardinality.getB() > 0) {
			DotPanel.setCSSAttributeOf(group, "opacity", "1.0");
		} else {
			DotPanel.setCSSAttributeOf(group, "opacity", "0.2");
		}

		//set title
		titleCount.getContent().clear();
		titleCount.getContent().add(cardinality.getA());
		titleCount.rebuild();

		return Pair.of(fillColour, fontColour);
	}

	private static void styleNonManualNode(IvMEfficientTree tree, int node, SVGDiagram svg,
			ProcessTreeVisualisationInfo info, Triple<String, Long, Long> cardinality) {
		//colour non-activity nodes
		for (LocalDotNode dotNode : info.getNodes(node)) {
			if (cardinality.getB() > 0) {
				DotPanel.setCSSAttributeOf(svg, dotNode, "opacity", "1.0");
			} else {
				DotPanel.setCSSAttributeOf(svg, dotNode, "opacity", "0.2");
			}
		}
	}

	private static void styleEdges(IvMEfficientTree tree, SVGDiagram svg, ProcessTreeVisualisationInfo info,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters,
			TraceViewEventColourMap traceViewColourMap, long minCardinality, long maxCardinality)
			throws SVGException, UnknownTreeNodeException {
		styleModelEdges(tree, svg, info, data, parameters, traceViewColourMap, minCardinality, maxCardinality);
		styleMoveEdges(svg, info, data, parameters, minCardinality, maxCardinality);
	}

	private static void styleModelEdges(IvMEfficientTree tree, SVGDiagram svg, ProcessTreeVisualisationInfo info,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters,
			TraceViewEventColourMap traceViewColourMap, long minCardinality, long maxCardinality)
			throws SVGException, UnknownTreeNodeException {
		for (LocalDotEdge dotEdge : info.getAllModelEdges()) {
			Pair<String, Long> cardinality = data.getEdgeLabel(dotEdge.getUnode(), false);
			Color edgeColour = styleEdge(dotEdge, svg, cardinality, minCardinality, maxCardinality,
					parameters.getColourModelEdges(), parameters.isShowFrequenciesOnModelEdges(),
					parameters.getModelEdgesWidth());
			if (tree.isTau(dotEdge.getUnode())) {
				traceViewColourMap.set(dotEdge.getUnode(), edgeColour, edgeColour);
			}
		}
	}

	private static void styleMoveEdges(SVGDiagram svg, ProcessTreeVisualisationInfo info,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters, long minCardinality,
			long maxCardinality) throws SVGException {
		//style model move edges
		for (LocalDotEdge dotEdge : info.getAllModelMoveEdges()) {
			Pair<String, Long> cardinality = data.getModelMoveEdgeLabel(dotEdge.getUnode());
			styleEdge(dotEdge, svg, cardinality, minCardinality, maxCardinality, parameters.getColourMoves(),
					parameters.isShowFrequenciesOnMoveEdges(), parameters.getMoveEdgesWidth());
		}

		//style log moves
		for (LocalDotEdge dotEdge : info.getAllLogMoveEdges()) {
			LogMovePosition logMovePosition = LogMovePosition.of(dotEdge);
			Pair<String, MultiSet<XEventClass>> cardinality = data.getLogMoveEdgeLabel(logMovePosition);
			styleEdge(dotEdge, svg, Pair.of(cardinality.getA(), cardinality.getB().size()), minCardinality,
					maxCardinality, parameters.getColourMoves(), parameters.isShowFrequenciesOnMoveEdges(),
					parameters.getMoveEdgesWidth());
		}
	}

	@SuppressWarnings("unchecked")
	private static Color styleEdge(DotEdge edge, SVGDiagram svg, Pair<String, Long> cardinality, long minCardinality,
			long maxCardinality, ColourMap colourMap, boolean showFrequency, SizeMap widthMap) throws SVGException {

		//prepare parts of the rendered dot element
		Group group = DotPanel.getSVGElementOf(svg, edge);
		SVGElement line = group.getChild(1);
		SVGElement arrowHead = group.getChild(2);

		//stroke
		Color edgeColour = colourMap.colour(cardinality.getB(), minCardinality, maxCardinality);
		double strokeWidth = widthMap.size(cardinality.getB(), minCardinality, maxCardinality);
		DotPanel.setCSSAttributeOf(line, "stroke", edgeColour);
		DotPanel.setCSSAttributeOf(arrowHead, "stroke", edgeColour);
		DotPanel.setCSSAttributeOf(arrowHead, "fill", edgeColour);
		DotPanel.setCSSAttributeOf(line, "stroke-width", strokeWidth + "");

		//transparency
		if (cardinality.getB() > 0) {
			DotPanel.setCSSAttributeOf(group, "opacity", "1.0");
		} else {
			DotPanel.setCSSAttributeOf(group, "opacity", "0.1");
		}

		//edge label
		if (showFrequency) {
			Text label = (Text) group.getChild(group.getChildren(null).size() - 1);
			label.getContent().clear();
			label.getContent().add(cardinality.getA());
			label.rebuild();
		}

		return edgeColour;
	}
}
