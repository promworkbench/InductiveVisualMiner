package org.processmining.plugins.inductiveVisualMiner.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkComputationAbstract;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class PopupController {

	public static final int popupWidthNodes = 300;
	public static final int popupWidthSourceSink = 350;

	public static final int maxCharactersPerLine = 43;

	public static class ClPopups extends DataChainLinkComputationAbstract {

		private final IvMObject<?>[] triggers;

		public ClPopups(Set<IvMObject<?>> triggers) {
			triggers.add(IvMObject.model);
			triggers.add(IvMObject.aligned_log_info_filtered);

			this.triggers = new IvMObject<?>[triggers.size()];
			triggers.toArray(this.triggers);
		}

		@Override
		public String getName() {
			return "popup computer";
		}

		@Override
		public String getStatusBusyMessage() {
			return "Creating popups..";
		}

		@Override
		public IvMObject<?>[] getOptionalObjects() {
			return triggers;
		}

		@Override
		public IvMObject<?>[] createInputObjects() {
			return new IvMObject<?>[] {};
		}

		@Override
		public IvMObject<?>[] createOutputObjects() {
			return new IvMObject<?>[] { IvMObject.popups };
		}

		@Override
		public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
				IvMCanceller canceller) throws Exception {
			Map<PopupItemInput<?>, List<String[]>> popups = new THashMap<>();

			//log
			{
				PopupItemInputLog itemInput = new PopupItemInputLog();
				List<PopupItemLog> items = configuration.getPopupItemsLog();
				popups.put(itemInput, popupProcess(inputs, itemInput, items));
			}
			//start end
			{
				PopupItemInputStartEnd itemInput = new PopupItemInputStartEnd();
				List<PopupItemStartEnd> items = configuration.getPopupItemsStartEnd();
				popups.put(itemInput, popupProcess(inputs, itemInput, items));
			}
			//activities
			if (inputs.has(IvMObject.model)) {
				IvMModel model = inputs.get(IvMObject.model);
				for (int unode : model.getAllNodes()) {
					if (model.isActivity(unode)) {
						PopupItemInputActivity itemInput = new PopupItemInputActivity(unode);
						List<PopupItemActivity> items = configuration.getPopupItemsActivity();
						popups.put(itemInput, popupProcess(inputs, itemInput, items));
					}
				}
				if (inputs.has(IvMObject.aligned_log_info_filtered)) {
					IvMLogInfo ivmLogInfoFiltered = inputs.get(IvMObject.aligned_log_info_filtered);
					//log moves
					for (LogMovePosition position : ivmLogInfoFiltered.getLogMoves().keySet()) {
						PopupItemInputLogMove itemInput = new PopupItemInputLogMove(position);
						List<PopupItemLogMove> items = configuration.getPopupItemsLogMove();
						popups.put(itemInput, popupProcess(inputs, itemInput, items));
					}
					//model moves
					for (int modelMove : ivmLogInfoFiltered.getModelMoves().keys()) {
						PopupItemInputModelMove itemInput = new PopupItemInputModelMove(modelMove);
						List<PopupItemModelMove> items = configuration.getPopupItemsModelMove();
						popups.put(itemInput, popupProcess(inputs, itemInput, items));
					}
				}
			}

			return new IvMObjectValues().//
					s(IvMObject.popups, popups);
		}
	}

	public PopupController(DataChain chain, InductiveVisualMinerConfiguration configuration) {
		//gather the required triggers
		Set<IvMObject<?>> triggers = new THashSet<>();
		{
			for (PopupItem<?> item : configuration.getPopupItemsLog()) {
				triggers.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsActivity()) {
				triggers.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsLogMove()) {
				triggers.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsModelMove()) {
				triggers.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsStartEnd()) {
				triggers.addAll(Arrays.asList(item.inputObjects()));
			}
		}

		//set up a chain link computer
		chain.register(new ClPopups(triggers));
	}

	public void showPopup(InductiveVisualMinerPanel panel, DataChain chain) {
		IvMObjectValues inputs;
		try {
			inputs = chain.getObjectValues(IvMObject.popups, IvMObject.model, IvMObject.graph_visualisation_info_aligned,
					IvMObject.aligned_log_info_filtered).get();
			showPopup(panel, inputs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void showPopup(InductiveVisualMinerPanel panel, IvMObjectValues inputs) {
		if (inputs.has(IvMObject.popups)) {
			//data ready

			@SuppressWarnings("unchecked")
			Map<PopupItemInput<?>, List<String[]>> popups = inputs.get(IvMObject.popups);

			if (panel.getGraph().isMouseInLogPopupButton()) {
				//log popup
				panel.getGraph().setPopupActivity(prettyPrint(popups.get(new PopupItemInputLog())), -1);
				panel.getGraph().setShowPopup(true, popupWidthNodes);
				return;
			}

			if (!panel.getGraph().getMouseInElements().isEmpty()) {
				//in an element

				//output the popup items about the particular node or edge
				DotElement element = panel.getGraph().getMouseInElements().iterator().next();

				if (element instanceof LocalDotNode && (((LocalDotNode) element).getType() == NodeType.source
						|| ((LocalDotNode) element).getType() == NodeType.sink)) {
					//popup at the source or sink
					panel.getGraph().setPopupStartEnd(prettyPrint(popups.get(new PopupItemInputStartEnd())));
					panel.getGraph().setShowPopup(true, popupWidthSourceSink);
					return;
				}

				if (inputs.has(IvMObject.model)) {
					//model ready
					IvMModel model = inputs.get(IvMObject.model);

					if (element instanceof LocalDotNode && model.isActivity(((LocalDotNode) element).getUnode())) {
						//popup of an activity

						int unode = ((LocalDotNode) element).getUnode();
						PopupItemInputActivity input = new PopupItemInputActivity(unode);
						if (popups.containsKey(input)) {
							panel.getGraph().setPopupActivity(prettyPrint(popups.get(input)), unode);
							panel.getGraph().setShowPopup(true, popupWidthNodes);
							return;
						}
					}

					if (inputs.has(IvMObject.graph_visualisation_info_aligned)) {
						ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info_aligned);

						if (element instanceof LocalDotEdge
								&& visualisationInfo.getAllLogMoveEdges().contains(element)) {
							//log move edge
							LocalDotEdge edge = (LocalDotEdge) element;
							//gather input
							LogMovePosition position = LogMovePosition.of(edge);
							PopupItemInputLogMove input = new PopupItemInputLogMove(position);

							if (popups.containsKey(input)) {
								panel.getGraph().setPopupLogMove(prettyPrint(popups.get(input)), position);
								panel.getGraph().setShowPopup(true, popupWidthNodes);
								return;
							}
						}

						if (element instanceof LocalDotEdge
								&& visualisationInfo.getAllModelMoveEdges().contains(element)) {
							//model move edge
							LocalDotEdge edge = (LocalDotEdge) element;
							int unode = edge.getUnode();
							PopupItemInputModelMove input = new PopupItemInputModelMove(unode);

							if (popups.containsKey(input)) {
								panel.getGraph().setPopupActivity(prettyPrint(popups.get(input)), -1);
								panel.getGraph().setShowPopup(true, popupWidthNodes);
								return;
							}
						}
					}
				}
			}

		}

		//no popup found
		panel.getGraph().setShowPopup(false, 10);
		return;
	}

	public static List<String> prettyPrint(List<String[]> items) {
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

		List<String> result = new ArrayList<>();
		for (String[] item : items) {
			if (item != null) {
				if (item.length == 0) {
					//no columns (spacer)
					result.add(null);
				} else if (item.length == 1) {
					//one column
					if (item[0] != null) {
						result.add(StringUtils.abbreviate(item[0], maxCharactersPerLine));
					}
				} else {
					//two columns
					if (item[0] != null && item[1] != null) {
						result.add(//
								padRight(item[0], widthColumnA) + //
										" " + //
										StringUtils.abbreviate(item[1], maxCharactersPerLine - widthColumnA - 1));
					}
				}
			}
		}

		return result;
	}

	public static <T> List<String[]> popupProcess(IvMObjectValues inputs, PopupItemInput<T> itemInput,
			List<? extends PopupItem<T>> popupItems) {
		List<String[]> popup = new ArrayList<>();

		//gather the values
		for (PopupItem<T> item : popupItems) {
			if (inputs.has(item.inputObjects())) {
				IvMObjectValues subInputs = inputs.getIfPresent(item.inputObjects());
				String[][] newItems = item.get(subInputs, itemInput);

				popup.addAll(Arrays.asList(newItems));
			}
		}

		removeDoubleEmpty(popup);
		return popup;
	}

	public static void removeDoubleEmpty(List<String[]> popup) {
		//post-process: remove double empty lines, and the last one
		{
			boolean seenNull = false;
			for (Iterator<String[]> it = popup.iterator(); it.hasNext();) {
				String[] row = it.next();
				if (row == null || row.length == 0) {
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
