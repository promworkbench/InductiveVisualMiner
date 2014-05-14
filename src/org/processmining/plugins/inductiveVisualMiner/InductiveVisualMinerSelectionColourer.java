package org.processmining.plugins.inductiveVisualMiner;

import java.util.Map;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.EdgeType;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMap;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Manual;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;

public class InductiveVisualMinerSelectionColourer {
	public static void colour(InductiveVisualMinerPanel panel, ProcessTree tree, AlignedLogInfo alignedFilteredLogInfo,
			Map<UnfoldedNode, AlignedLogInfo> alignedFilteredDfgLogInfos,
			AlignedLogVisualisationParameters visualisationParameters) {

		UnfoldedNode uroot = new UnfoldedNode(tree.getRoot());

		//compute extreme cardinalities
		Pair<Long, Long> extremes = AlignedLogMetrics.getExtremes(uroot, alignedFilteredLogInfo, true);
		long minCardinality = extremes.getLeft();
		long maxCardinality = extremes.getRight();

		try {

			//style nodes
			for (UnfoldedNode unode : AlignedLogMetrics.unfoldAllNodes(uroot)) {
				long cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, alignedFilteredLogInfo);
				styleUnfoldedNode(unode, panel, cardinality, minCardinality, maxCardinality, visualisationParameters);
			}

			//style edges
			styleEdges(panel, alignedFilteredLogInfo, visualisationParameters, alignedFilteredDfgLogInfos,
					minCardinality, maxCardinality);

		} catch (SVGException e) {
			e.printStackTrace();
		}

		panel.repaint();
	}

	private static void styleUnfoldedNode(UnfoldedNode unode, InductiveVisualMinerPanel panel, long cardinality,
			long minCardinality, long maxCardinality, AlignedLogVisualisationParameters visualisationParameters)
			throws SVGException {
		if (unode.getNode() instanceof Manual) {
			styleManual(unode, panel, cardinality, minCardinality, maxCardinality, visualisationParameters);
		} else {
			styleNonManualNode(unode, panel, cardinality);
		}
	}

	private static void styleManual(UnfoldedNode unode, InductiveVisualMinerPanel panel, long cardinality,
			long minCardinality, long maxCardinality, AlignedLogVisualisationParameters visualisationParameters)
			throws SVGException {
		DotElement dotNode = panel.getUnfoldedNode2dotNodes().get(unode).iterator().next();

		Group group = panel.getGraph().getSVGElementOf(dotNode);
		SVGElement polygon = group.getChild(1);
		Text titleName = (Text) group.getChild(group.getChildren(null).size() - 2);
		Text titleCount = (Text) group.getChild(group.getChildren(null).size() - 1);

		//recolour the polygon
		String fillColour;
		String fontColour = "black";
		if (cardinality > 0) {
			fillColour = visualisationParameters.getColourNodes().colour(cardinality, minCardinality, maxCardinality);
			if (ColourMaps.getLuma(fillColour) < 128) {
				fontColour = "white";
			}
		} else {
			fillColour = visualisationParameters.getColourNodes().colour(1, 0, 2);
		}
		panel.getGraph().setCSSAttributeOf(polygon, "fill", fillColour);

		//set label colour
		panel.getGraph().setCSSAttributeOf(titleCount, "fill", fontColour);
		panel.getGraph().setCSSAttributeOf(titleName, "fill", fontColour);

		if (cardinality > 0) {
			panel.getGraph().setCSSAttributeOf(group, "opacity", "1.0");
		} else {
			panel.getGraph().setCSSAttributeOf(group, "opacity", "0.2");
		}

		//set title
		titleCount.getContent().clear();
		titleCount.getContent().add(cardinality + "");
		titleCount.rebuild();
	}

	private static void styleNonManualNode(UnfoldedNode unode, InductiveVisualMinerPanel panel, long cardinality) {
		//colour non-activity nodes
		if (panel.getUnfoldedNode2dotNodes().containsKey(unode)) {
			for (DotNode dotNode : panel.getUnfoldedNode2dotNodes().get(unode)) {
				if (cardinality > 0) {
					panel.getGraph().setCSSAttributeOf(dotNode, "opacity", "1.0");
				} else {
					panel.getGraph().setCSSAttributeOf(dotNode, "opacity", "0.2");
				}
			}
		}
	}

	private static void styleEdges(InductiveVisualMinerPanel panel, AlignedLogInfo logInfo,
			AlignedLogVisualisationParameters parameters, Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos,
			long minCardinality, long maxCardinality) throws SVGException {
		styleModelEdges(panel, logInfo, parameters, dfgLogInfos, minCardinality, maxCardinality);
		styleMoveEdges(panel, logInfo, parameters, dfgLogInfos, minCardinality, maxCardinality);
	}

	private static void styleModelEdges(InductiveVisualMinerPanel panel, AlignedLogInfo logInfo,
			AlignedLogVisualisationParameters parameters, Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos,
			long minCardinality, long maxCardinality) throws SVGException {
		for (UnfoldedNode unode : panel.getUnfoldedNode2dotEdgesModel().keySet()) {
			for (DotEdge dotEdge : panel.getUnfoldedNode2dotEdgesModel().get(unode)) {
				long cardinality;
				if (!panel.getUnfoldedNode2DfgdotEdges().containsKey(unode)) {
					//normal model edge
					cardinality = AlignedLogMetrics.getNumberOfTracesRepresented(unode, logInfo);
				} else {
					//directly-follows edge
					cardinality = AlignedLogMetrics.getNumberOfTimesDfgEdgeTaken((LocalDotEdge) dotEdge,
							dfgLogInfos.get(unode));
				}
				styleEdge(dotEdge, panel.getGraph(), cardinality, minCardinality, maxCardinality,
						parameters.getColourModelEdges(), parameters.isShowFrequenciesOnModelEdges(),
						parameters.getModelEdgesWidth());
			}
		}
	}

	private static void styleMoveEdges(InductiveVisualMinerPanel panel, AlignedLogInfo logInfo,
			AlignedLogVisualisationParameters parameters, Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos,
			long minCardinality, long maxCardinality) throws SVGException {
		for (UnfoldedNode unode : panel.getUnfoldedNode2dotEdgesMove().keySet()) {
			for (DotEdge dotEdge : panel.getUnfoldedNode2dotEdgesMove().get(unode)) {
				LocalDotEdge lDotEdge = (LocalDotEdge) dotEdge; 
				long cardinality;
				if (lDotEdge.type.equals(EdgeType.logMove)) {
					cardinality = AlignedLogMetrics.getLogMoves(lDotEdge.lookupNode1, lDotEdge.lookupNode2, logInfo).size();
				} else {
					cardinality = AlignedLogMetrics.getModelMovesLocal(lDotEdge.unode, logInfo);
				}
				styleEdge(dotEdge, panel.getGraph(), cardinality, minCardinality, maxCardinality,
						parameters.getColourMoves(), parameters.isShowFrequenciesOnMoveEdges(),
						parameters.getMoveEdgesWidth());
			}
		}
	}

	private static void styleEdge(DotEdge edge, DotPanel panel, long cardinality, long minCardinality,
			long maxCardinality, ColourMap colourMap, boolean showFrequency, SizeMap widthMap) throws SVGException {

		//prepare parts of the rendered dot element
		Group group = panel.getSVGElementOf(edge);
		SVGElement line = group.getChild(1);
		SVGElement arrowHead = group.getChild(2);

		//stroke
		String edgeColour = colourMap.colour(cardinality, minCardinality, maxCardinality);
		double strokeWidth = widthMap.size(cardinality, minCardinality, maxCardinality);
		panel.setCSSAttributeOf(line, "stroke", edgeColour);
		panel.setCSSAttributeOf(arrowHead, "stroke", edgeColour);
		panel.setCSSAttributeOf(arrowHead, "fill", edgeColour);
		panel.setCSSAttributeOf(line, "stroke-width", strokeWidth + "");
		
		//transparency
		if (cardinality > 0) {
			panel.setCSSAttributeOf(group, "opacity", "1.0");
		} else {
			panel.setCSSAttributeOf(group, "opacity", "0.1");
		}

		//edge label
		if (showFrequency) {
			Text label = (Text) group.getChild(group.getChildren(null).size() - 1);
			label.getContent().clear();
			label.getContent().add(cardinality + "");
			label.rebuild();
		}
	}
}
