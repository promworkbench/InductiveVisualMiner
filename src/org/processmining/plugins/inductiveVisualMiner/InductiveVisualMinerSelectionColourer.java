package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMap;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Manual;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;

public class InductiveVisualMinerSelectionColourer {

	public static void colourSelection(SVGDiagram diagram, Set<UnfoldedNode> selectedNodes,
			Set<LogMovePosition> selectedLogMoves, ProcessTreeVisualisationInfo visualisationInfo) {
		for (UnfoldedNode unode : selectedNodes) {
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

	public static TraceViewColourMap colourHighlighting(SVGDiagram svg, ProcessTreeVisualisationInfo info,
			ProcessTree tree, AlignedLogVisualisationData data,
			ProcessTreeVisualisationParameters visualisationParameters) {

		UnfoldedNode uroot = new UnfoldedNode(tree.getRoot());
		TraceViewColourMap traceViewColourMap = new TraceViewColourMap();

		//compute extreme cardinalities
		Pair<Long, Long> extremes = data.getExtremeCardinalities();
		long minCardinality = extremes.getLeft();
		long maxCardinality = extremes.getRight();

		try {
			//style nodes
			for (UnfoldedNode unode : TreeUtils.unfoldAllNodes(uroot)) {
				Pair<String, Long> cardinality = data.getNodeLabel(unode, false);
				Pair<Color, Color> colour = styleUnfoldedNode(unode, svg, info, cardinality, minCardinality,
						maxCardinality, visualisationParameters);

				if (unode.getNode() instanceof Manual) {
					traceViewColourMap.set(unode, colour.getA(), colour.getB());
				}
			}

			//style edges
			styleEdges(svg, info, data, visualisationParameters, traceViewColourMap, minCardinality, maxCardinality);

		} catch (SVGException e) {
			e.printStackTrace();
		}

		return traceViewColourMap;
	}

	public static Pair<Color, Color> styleUnfoldedNode(UnfoldedNode unode, SVGDiagram svg,
			ProcessTreeVisualisationInfo info, Pair<String, Long> cardinality, long minCardinality,
			long maxCardinality, ProcessTreeVisualisationParameters visualisationParameters) throws SVGException {
		if (unode.getNode() instanceof Manual) {
			return styleManual(unode, svg, info, cardinality, minCardinality, maxCardinality, visualisationParameters);
		} else {
			styleNonManualNode(unode, svg, info, cardinality);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static Pair<Color, Color> styleManual(UnfoldedNode unode, SVGDiagram svg,
			ProcessTreeVisualisationInfo info, Pair<String, Long> cardinality, long minCardinality,
			long maxCardinality, ProcessTreeVisualisationParameters visualisationParameters) throws SVGException {

		LocalDotNode dotNode = info.getActivityNode(unode);

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

	private static void styleNonManualNode(UnfoldedNode unode, SVGDiagram svg, ProcessTreeVisualisationInfo info,
			Pair<String, Long> cardinality) {
		//colour non-activity nodes
		for (LocalDotNode dotNode : info.getNodes(unode)) {
			if (cardinality.getB() > 0) {
				DotPanel.setCSSAttributeOf(svg, dotNode, "opacity", "1.0");
			} else {
				DotPanel.setCSSAttributeOf(svg, dotNode, "opacity", "0.2");
			}
		}
	}

	private static void styleEdges(SVGDiagram svg, ProcessTreeVisualisationInfo info, AlignedLogVisualisationData data,
			ProcessTreeVisualisationParameters parameters, TraceViewColourMap traceViewColourMap, long minCardinality,
			long maxCardinality) throws SVGException {
		styleModelEdges(svg, info, data, parameters, traceViewColourMap, minCardinality, maxCardinality);
		styleMoveEdges(svg, info, data, parameters, minCardinality, maxCardinality);
	}

	private static void styleModelEdges(SVGDiagram svg, ProcessTreeVisualisationInfo info,
			AlignedLogVisualisationData data, ProcessTreeVisualisationParameters parameters,
			TraceViewColourMap traceViewColourMap, long minCardinality, long maxCardinality) throws SVGException {
		for (LocalDotEdge dotEdge : info.getAllModelEdges()) {
			Pair<String, Long> cardinality = data.getEdgeLabel(dotEdge.getUnode(), false);
			Color edgeColour = styleEdge(dotEdge, svg, cardinality, minCardinality, maxCardinality,
					parameters.getColourModelEdges(), parameters.isShowFrequenciesOnModelEdges(),
					parameters.getModelEdgesWidth());
			if (dotEdge.getUnode().getNode() instanceof Automatic) {
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
