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
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
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

		public ClPopups(IvMObject<?>[] triggers) {
			this.triggers = new IvMObject<?>[triggers.length + 2];
			System.arraycopy(triggers, 0, this.triggers, 2, triggers.length);
			this.triggers[0] = IvMObject.model;
			this.triggers[1] = IvMObject.aligned_log_info_filtered;
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
		public IvMObject<?>[] getTriggerObjects() {
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
			Map<PopupItemInput<?>, List<String>> popups = new THashMap<>();

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
		//gather the required inputs
		final IvMObject<?>[] inputs;
		{
			Set<IvMObject<?>> inputSet = new THashSet<>();
			for (PopupItem<?> item : configuration.getPopupItemsLog()) {
				inputSet.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsActivity()) {
				inputSet.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsLogMove()) {
				inputSet.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsModelMove()) {
				inputSet.addAll(Arrays.asList(item.inputObjects()));
			}
			for (PopupItem<?> item : configuration.getPopupItemsStartEnd()) {
				inputSet.addAll(Arrays.asList(item.inputObjects()));
			}
			inputs = new IvMObject<?>[inputSet.size()];
			inputSet.toArray(inputs);
		}

		//set up a chain link computer
		chain.register(new ClPopups(inputs));
	}

	public void showPopup(InductiveVisualMinerPanel panel, DataState state) {
		IvMObjectValues inputs = state.getObject(IvMObject.carte_blanche).getIfPresent(IvMObject.popups,
				IvMObject.model, IvMObject.graph_visualisation_info, IvMObject.aligned_log_info_filtered);
		showPopup(panel, inputs);
	}

	public void showPopup(InductiveVisualMinerPanel panel, IvMObjectValues inputs) {

		if (inputs.has(IvMObject.popups)) {
			//data ready

			@SuppressWarnings("unchecked")
			Map<PopupItemInput<?>, List<String>> popups = inputs.get(IvMObject.popups);

			if (!panel.getGraph().getMouseInElements().isEmpty()) {
				//in an element

				if (panel.getGraph().isMouseInLogPopupButton()) {
					//log popup
					panel.getGraph().setPopupActivity(popups.get(new PopupItemInputLog()), -1);
					panel.getGraph().setShowPopup(true, popupWidthNodes);
					return;
				}

				//output the popup items about the particular node or edge
				DotElement element = panel.getGraph().getMouseInElements().iterator().next();

				if (((LocalDotNode) element).getType() == NodeType.source
						|| ((LocalDotNode) element).getType() == NodeType.sink) {
					//popup at the source or sink
					panel.getGraph().setPopupStartEnd(popups.get(new PopupItemInputStartEnd()));
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
							panel.getGraph().setPopupActivity(popups.get(input), unode);
							panel.getGraph().setShowPopup(true, popupWidthNodes);
							return;
						}
					}

					if (inputs.has(IvMObject.graph_visualisation_info)) {
						ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info);

						if (element instanceof LocalDotEdge
								&& visualisationInfo.getAllLogMoveEdges().contains(element)) {
							//log move edge
							LocalDotEdge edge = (LocalDotEdge) element;
							//gather input
							LogMovePosition position = LogMovePosition.of(edge);
							PopupItemInputLogMove input = new PopupItemInputLogMove(position);

							panel.getGraph().setPopupLogMove(popups.get(input), position);
							panel.getGraph().setShowPopup(true, popupWidthNodes);
							return;
						}

						if (element instanceof LocalDotEdge
								&& visualisationInfo.getAllModelMoveEdges().contains(element)) {
							//model move edge
							LocalDotEdge edge = (LocalDotEdge) element;
							int unode = edge.getUnode();
							PopupItemInputModelMove input = new PopupItemInputModelMove(unode);

							panel.getGraph().setPopupActivity(popups.get(input), -1);
							panel.getGraph().setShowPopup(true, popupWidthNodes);
							return;
						}
					}
				}
			}

		}

		//no popup found
		panel.getGraph().setShowPopup(false, 10);
		return;
	}

	public static <T> List<String> popupProcess(IvMObjectValues inputs, PopupItemInput<T> itemInput,
			List<? extends PopupItem<T>> popupItems) {
		List<String> popup = new ArrayList<>();

		//gather the values
		String[][] items = new String[0][0];
		for (PopupItem<T> item : popupItems) {
			if (inputs.has(item.inputObjects())) {
				IvMObjectValues subInputs = inputs.getIfPresent(item.inputObjects());
				String[][] newItems = item.get(subInputs, itemInput);

				//merge arrays
				String[][] result2 = new String[items.length + newItems.length][];
				System.arraycopy(items, 0, result2, 0, items.length);
				System.arraycopy(newItems, 0, result2, items.length, newItems.length);
				items = result2;
			}
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
		return popup;
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
