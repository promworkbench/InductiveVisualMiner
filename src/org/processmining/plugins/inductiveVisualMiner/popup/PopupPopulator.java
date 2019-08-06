package org.processmining.plugins.inductiveVisualMiner.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeGlobal;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class PopupPopulator {

	public static final int popupWidthNodes = 300;
	public static final int popupWidthSourceSink = 350;

	public static void updatePopup(InductiveVisualMinerPanel panel, InductiveVisualMinerState state)
			throws UnknownTreeNodeException {
		if (panel.getGraph().getMouseInElements().isEmpty()) {
			panel.getGraph().setShowPopup(false, 10);
		} else {
			//output statistics about the node
			DotElement element = panel.getGraph().getMouseInElements().iterator().next();
			if (element instanceof LocalDotNode) {
				int unode = ((LocalDotNode) element).getUnode();
				if (state.isAlignmentReady()) {
					//popup of an activity
					if (state.getModel().isActivity(unode)) {
						List<String> popup = new ArrayList<>();

						//name
						popup.add("activity " + state.getModel().getActivityName(unode));

						//frequencies
						popup.add("number of occurrences  " + IvMLogMetrics.getNumberOfTracesRepresented(
								state.getModel(), unode, false, state.getIvMLogInfoFiltered()));
						//frequencies
						popup.add("occurrences per trace  "
								+ (IvMLogMetrics.getNumberOfTracesRepresented(state.getModel(), unode, false,
										state.getIvMLogInfoFiltered())
										/ (state.getIvMLogInfoFiltered().getNumberOfTraces() * 1.0)));
						popup.add(null);

						//times
						if (state.isPerformanceReady()) {
							for (TypeNode type : TypeNode.values()) {
								for (Gather gather : Gather.values()) {
									long m = state.getPerformance().getNodeMeasure(type, gather, unode);
									if (m > -1) {
										popup.add(gather.toString() + " " + type.toString() + " "
												+ Performance.timeToString(m));
									}
								}
								if (popup.get(popup.size() - 1) != null) {
									popup.add(null);
								}
							}
						}

						popup.remove(popup.size() - 1);
						panel.getGraph().setPopupActivity(popup, unode);
						panel.getGraph().setShowPopup(true, popupWidthNodes);
					} else if (((LocalDotNode) element).getType() == NodeType.source
							|| ((LocalDotNode) element).getType() == NodeType.sink) {
						//popup at the source or sink
						List<String> popup = new ArrayList<>();

						//name
						popup.add("all highlighted traces");

						//frequencies
						popup.add(
								"number of traces                " + state.getIvMLogInfoFiltered().getNumberOfTraces());
						popup.add(null);

						//times
						if (state.isPerformanceReady()) {
							for (TypeGlobal type : TypeGlobal.values()) {
								for (Gather gather : Gather.values()) {
									long m = state.getPerformance().getGlobalMeasure(type, gather);
									if (m > -1) {
										popup.add(gather.toString() + " " + type.toString() + " "
												+ Performance.timeToString(m));
									}
								}
								if (popup.get(popup.size() - 1) != null) {
									popup.add(null);
								}
							}
						}

						popup.remove(popup.size() - 1);
						panel.getGraph().setPopupLog(popup);
						panel.getGraph().setShowPopup(true, popupWidthSourceSink);
					} else {
						panel.getGraph().setShowPopup(false, 10);
					}
				} else {
					panel.getGraph().setShowPopup(false, 10);
				}
			} else if (state.getVisualisationInfo() != null && element instanceof LocalDotEdge
					&& state.getVisualisationInfo().getAllLogMoveEdges().contains(element)) {
				//log move edge
				LocalDotEdge edge = (LocalDotEdge) element;
				int maxNumberOfLogMoves = 10;
				if (state.isAlignmentReady()) {
					List<String> popup = new ArrayList<>();
					LogMovePosition position = LogMovePosition.of(edge);
					MultiSet<XEventClass> logMoves = IvMLogMetrics.getLogMoves(position, state.getIvMLogInfoFiltered());

					popup.add(logMoves.size() + (logMoves.size() <= 1 ? " event" : " events")
							+ " additional to the model:");

					//get digits of the maximum cardinality
					long max = logMoves.getCardinalityOf(logMoves.getElementWithHighestCardinality());
					int maxDigits = (int) (Math.log10(max) + 1);

					if (max == 0) {
						panel.getGraph().setShowPopup(false, 10);
					}

					List<XEventClass> activities = logMoves.sortByCardinality();
					Collections.reverse(activities);
					for (XEventClass activity : activities) {
						if (maxNumberOfLogMoves > 0) {
							popup.add(String.format("%" + maxDigits + "d", logMoves.getCardinalityOf(activity)) + " "
									+ StringUtils.abbreviate(activity.toString(), 40 - maxDigits));
						}
						maxNumberOfLogMoves--;
					}
					if (maxNumberOfLogMoves < 0) {
						popup.add("... and " + Math.abs(maxNumberOfLogMoves) + " more "
								+ (Math.abs(maxNumberOfLogMoves) > 1 ? "activities" : "activity") + " ");
					}

					panel.getGraph().setPopupLogMove(popup, position);
					panel.getGraph().setShowPopup(true, popupWidthNodes);
				} else {
					panel.getGraph().setShowPopup(false, 10);
				}
			} else if (state.getVisualisationInfo() != null && element instanceof LocalDotEdge
					&& state.getVisualisationInfo().getAllModelMoveEdges().contains(element)) {
				//model move edge
				if (state.isAlignmentReady()) {
					LocalDotEdge edge = (LocalDotEdge) element;
					int node = edge.getUnode();
					List<String> popup = new ArrayList<>();
					long t = IvMLogMetrics.getModelMovesLocal(node, state.getIvMLogInfoFiltered());
					popup.add((t > 1 ? (t + " times") : "Once") + ", activity ");
					popup.add(StringUtils.abbreviate(state.getModel().getActivityName(edge.getUnode()), 40));
					popup.add("was not executed.");

					panel.getGraph().setPopupActivity(popup, -1);
					panel.getGraph().setShowPopup(true, popupWidthNodes);
				} else {
					panel.getGraph().setShowPopup(false, 10);
				}
			} else {
				panel.getGraph().setShowPopup(false, 10);
			}
		}
	}
}
