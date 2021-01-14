package org.processmining.plugins.inductiveVisualMiner.popup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class PopupPopulator {

	public static final int popupWidthNodes = 300;
	public static final int popupWidthSourceSink = 350;

	public static final int maxCharactersPerLine = 43;

	/**
	 * This method is intended to be extensible, and thus needs the state. Only
	 * the basic input requirements (model, aligned log etc) are declared.
	 * 
	 * @param state
	 * @param configuration
	 * @param panel
	 * @param model
	 * @param visualisationInfo
	 * @param ivmLogInfoFiltered
	 */
	public static void updatePopup(DataState state, InductiveVisualMinerConfiguration configuration,
			InductiveVisualMinerPanel panel, IvMModel model, ProcessTreeVisualisationInfo visualisationInfo,
			IvMLogInfo ivmLogInfoFiltered) {
		List<String> popup = new ArrayList<>();

		if (model == null || visualisationInfo == null || ivmLogInfoFiltered == null) {
			//data not ready
			panel.getGraph().setShowPopup(false, 10);
		} else if (panel.getGraph().isMouseInLogPopupButton()) {
			//log popup

			PopupItemInputLog input = new PopupItemInputLog();
			List<PopupItemLog> items = configuration.getPopupItemsLog();
			popupProcess(state, input, popup, items);

			panel.getGraph().setPopupActivity(popup, -1);
			panel.getGraph().setShowPopup(true, popupWidthNodes);
			return;
		} else if (panel.getGraph().getMouseInElements().isEmpty()) {
			//not in any element
			panel.getGraph().setShowPopup(false, 10);
		} else {
			//output the popup items about the particular node or edge
			DotElement element = panel.getGraph().getMouseInElements().iterator().next();
			if (element instanceof LocalDotNode) {
				int unode = ((LocalDotNode) element).getUnode();

				if (model.isActivity(unode)) {
					//popup of an activity

					PopupItemInputActivity input = new PopupItemInputActivity(unode);
					List<PopupItemActivity> items = configuration.getPopupItemsActivity();
					popupProcess(state, input, popup, items);

					panel.getGraph().setPopupActivity(popup, unode);
					panel.getGraph().setShowPopup(true, popupWidthNodes);
					return;
				} else if (((LocalDotNode) element).getType() == NodeType.source
						|| ((LocalDotNode) element).getType() == NodeType.sink) {
					//popup at the source or sink

					PopupItemInputStartEnd input = new PopupItemInputStartEnd();
					List<PopupItemStartEnd> items = configuration.getPopupItemsStartEnd();
					popupProcess(state, input, popup, items);

					panel.getGraph().setPopupStartEnd(popup);
					panel.getGraph().setShowPopup(true, popupWidthSourceSink);
					return;
				}
			} else if (visualisationInfo != null && element instanceof LocalDotEdge
					&& visualisationInfo.getAllLogMoveEdges().contains(element)) {
				//log move edge
				LocalDotEdge edge = (LocalDotEdge) element;
				//gather input
				LogMovePosition position = LogMovePosition.of(edge);
				MultiSet<XEventClass> logMoves = IvMLogMetrics.getLogMoves(position, ivmLogInfoFiltered);
				PopupItemInputLogMove input = new PopupItemInputLogMove(position, logMoves);
				List<PopupItemLogMove> items = configuration.getPopupItemsLogMove();

				//get popup items
				popupProcess(state, input, popup, items);

				panel.getGraph().setPopupLogMove(popup, position);
				panel.getGraph().setShowPopup(true, popupWidthNodes);
				return;
			} else if (visualisationInfo != null && element instanceof LocalDotEdge
					&& visualisationInfo.getAllModelMoveEdges().contains(element)) {
				//model move edge
				LocalDotEdge edge = (LocalDotEdge) element;
				int unode = edge.getUnode();
				PopupItemInputModelMove input = new PopupItemInputModelMove(unode);
				List<PopupItemModelMove> items = configuration.getPopupItemsModelMove();

				//get popup items
				popupProcess(state, input, popup, items);

				panel.getGraph().setPopupActivity(popup, -1);
				panel.getGraph().setShowPopup(true, popupWidthNodes);
				return;
			}
		}

		panel.getGraph().setShowPopup(false, 10);
		return;
	}

	public static <T> void popupProcess(DataState state, PopupItemInput<T> input, List<String> popup,
			List<? extends PopupItem<T>> popupItems) {
		//gather the values
		String[][] items = new String[0][0];
		for (PopupItem<T> item : popupItems) {
			String[][] newItems = item.get(state, input);

			//merge arrays
			String[][] result2 = new String[items.length + newItems.length][];
			System.arraycopy(items, 0, result2, 0, items.length);
			System.arraycopy(newItems, 0, result2, items.length, newItems.length);
			items = result2;
		}

		//gather the width of the first column
		int widthColumnA = 0;
		{
			for (String[] item : items) {
				if (item != null && item.length == 2) {
					if (item[0] != null && item[1] != null) {
						widthColumnA = Math.max(widthColumnA, item[0].length());
					}
				}
			}
		}

		for (String[] item : items) {
			if (item != null) {
				if (item.length == 0) {
					//no columns (spacer)
					popup.add(null);
				} else if (item.length == 1) {
					//one column
					if (item[0] != null) {
						popup.add(StringUtils.abbreviate(item[0], maxCharactersPerLine));
					}
				} else {
					//two columns
					if (item[0] != null && item[1] != null) {
						popup.add(//
								padRight(item[0], widthColumnA) + //
										" " + //
										StringUtils.abbreviate(item[1], maxCharactersPerLine - widthColumnA - 1));
					}
				}
			}
		}
		removeDoubleEmpty(popup);
	}

	public static void removeDoubleEmpty(List<String> popup) {
		//post-process: remove double empty lines, and the last one
		{
			boolean seenNull = false;
			for (Iterator<String> it = popup.iterator(); it.hasNext();) {
				if (it.next() == null) {
					if (seenNull || !it.hasNext()) {
						it.remove();
					} else {
						seenNull = true;
					}
				} else {
					seenNull = false;
				}
			}
		}
	}

	public static String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
}
