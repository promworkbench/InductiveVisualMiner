package org.processmining.plugins.inductiveVisualMiner;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughDirectlyFollowsGraph;
import org.processmining.plugins.graphviz.colourMaps.ColourMapBlue;
import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapLightBlue;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState.ColourMode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;

public class InductiveVisualMinerPanel extends JPanel {

	private static final long serialVersionUID = -1078786029763735572L;

	//gui elements
	private final DotPanel graphPanel;
	private final JComboBox colourSelection;
	private final JLabel statusLabel;
	private final JTextArea selectionLabel;
	private final JCheckBox showDirectlyFollowsGraphs;
	private final NiceDoubleSlider activitiesSlider;
	private final NiceDoubleSlider noiseSlider;
	private JComboBox classifiersCombobox;
	private JButton exitButton;

	private final AlignedLogVisualisation visualiser;

	private InputFunction<Set<UnfoldedNode>> onSelectionChanged = null;

	@SuppressWarnings("unchecked")
	public InductiveVisualMinerPanel(final PluginContext context, InductiveVisualMinerState state,
			Collection<XEventClassifier> classifiers, boolean enableExitButton) throws IOException {
		visualiser = new AlignedLogVisualisation();
		initVisualisationParameters();

		int gridy = 0;

		setLayout(new GridBagLayout());

		activitiesSlider = SlickerFactory.instance().createNiceDoubleSlider("activities", 0, 1.0, 1.0,
				Orientation.VERTICAL);
		GridBagConstraints cActivitiesSlider = new GridBagConstraints();
		cActivitiesSlider.gridx = 1;
		cActivitiesSlider.gridy = gridy;
		cActivitiesSlider.fill = GridBagConstraints.VERTICAL;
		cActivitiesSlider.anchor = GridBagConstraints.EAST;
		add(getActivitiesSlider(), cActivitiesSlider);

		noiseSlider = SlickerFactory.instance().createNiceDoubleSlider("paths", 0, 1.0,
				1 - state.getMiningParameters().getNoiseThreshold(), Orientation.VERTICAL);
		GridBagConstraints cNoiseSlider = new GridBagConstraints();
		cNoiseSlider.gridx = 2;
		cNoiseSlider.gridy = gridy;
		cNoiseSlider.weighty = 1;
		cNoiseSlider.fill = GridBagConstraints.VERTICAL;
		cNoiseSlider.anchor = GridBagConstraints.WEST;
		add(getNoiseSlider(), cNoiseSlider);

		gridy++;

		{
			boolean dfg = false;
			for (FallThrough f : state.getMiningParameters().getFallThroughs()) {
				dfg = dfg || f instanceof FallThroughDirectlyFollowsGraph;
			}
			showDirectlyFollowsGraphs = SlickerFactory.instance().createCheckBox(
					"Fall back to directly-follows graphs", dfg);
			showDirectlyFollowsGraphs.setEnabled(false);
			GridBagConstraints cShowDirectlyFollowsGraphs = new GridBagConstraints();
			cShowDirectlyFollowsGraphs.gridx = 1;
			cShowDirectlyFollowsGraphs.gridy = gridy++;
			cShowDirectlyFollowsGraphs.gridwidth = 2;
			cShowDirectlyFollowsGraphs.anchor = GridBagConstraints.NORTHWEST;
			add(showDirectlyFollowsGraphs, cShowDirectlyFollowsGraphs);
		}

		{
			JLabel classifierLabel = SlickerFactory.instance().createLabel("Classifier");
			GridBagConstraints cClassifierLabel = new GridBagConstraints();
			cClassifierLabel.gridx = 1;
			cClassifierLabel.gridy = gridy;
			cClassifierLabel.gridwidth = 1;
			cClassifierLabel.anchor = GridBagConstraints.WEST;
			add(classifierLabel, cClassifierLabel);

			classifiersCombobox = SlickerFactory.instance().createComboBox(classifiers.toArray());
			GridBagConstraints cClassifiers = new GridBagConstraints();
			cClassifiers.gridx = 2;
			cClassifiers.gridy = gridy++;
			cClassifiers.gridwidth = 1;
			cClassifiers.fill = GridBagConstraints.HORIZONTAL;
			add(classifiersCombobox, cClassifiers);
			classifiersCombobox.setSelectedItem(state.getMiningParameters().getClassifier());
		}

		{
			JLabel colourLabel = SlickerFactory.instance().createLabel("Colour");
			GridBagConstraints cColourLabel = new GridBagConstraints();
			cColourLabel.gridx = 1;
			cColourLabel.gridy = gridy;
			cColourLabel.gridwidth = 1;
			cColourLabel.anchor = GridBagConstraints.WEST;
			add(colourLabel, cColourLabel);

			colourSelection = SlickerFactory.instance().createComboBox(ColourMode.values());
			GridBagConstraints ccolourSelection = new GridBagConstraints();
			ccolourSelection.gridx = 2;
			ccolourSelection.gridy = gridy++;
			ccolourSelection.gridwidth = 1;
			ccolourSelection.fill = GridBagConstraints.HORIZONTAL;
			add(colourSelection, ccolourSelection);
		}

		if (enableExitButton) {
			exitButton = SlickerFactory.instance().createButton("Mine this model (exit)");
			GridBagConstraints cExitButton = new GridBagConstraints();
			cExitButton.gridx = 2;
			cExitButton.gridy = gridy++;
			cExitButton.gridwidth = 1;
			cExitButton.fill = GridBagConstraints.HORIZONTAL;
			add(exitButton, cExitButton);
		}

		{
			selectionLabel = new JTextArea(" \n ");
			selectionLabel.setWrapStyleWord(true);
			selectionLabel.setLineWrap(true);
			selectionLabel.setEditable(false);
			selectionLabel.setOpaque(false);
			GridBagConstraints cSelectionLabel = new GridBagConstraints();
			cSelectionLabel.gridx = 1;
			cSelectionLabel.gridy = gridy++;
			cSelectionLabel.gridwidth = 2;
			cSelectionLabel.anchor = GridBagConstraints.NORTH;
			cSelectionLabel.fill = GridBagConstraints.HORIZONTAL;
			add(selectionLabel, cSelectionLabel);
		}

		{
			statusLabel = SlickerFactory.instance().createLabel(" ");
			GridBagConstraints cStatus = new GridBagConstraints();
			cStatus.gridx = 1;
			cStatus.gridy = gridy++;
			cStatus.gridwidth = 2;
			cStatus.anchor = GridBagConstraints.SOUTH;
			add(statusLabel, cStatus);
		}

		Dot dot = new Dot();
		dot.addNode("Inductive Visual Miner", "");
		dot.addNode("Mining model...", "");
		graphPanel = new DotPanel(dot) {
			private static final long serialVersionUID = -3112819390640390685L;

			public void selectionChanged() {
				//selection of nodes changed; keep track of them

				Set<UnfoldedNode> result = new HashSet<UnfoldedNode>();
				for (DotElement dotElement : graphPanel.getSelectedElements()) {
					result.add(((LocalDotNode) dotElement).node);
				}

				if (onSelectionChanged != null) {
					try {
						onSelectionChanged.call(result);
					} catch (Exception e) {
					}
				}
			}
		};
		GridBagConstraints cGraphPanel = new GridBagConstraints();
		cGraphPanel.gridx = 0;
		cGraphPanel.gridy = 0;
		cGraphPanel.gridheight = gridy;
		cGraphPanel.gridwidth = 1;
		cGraphPanel.weightx = 1;
		cGraphPanel.weighty = 1;
		cGraphPanel.fill = GridBagConstraints.BOTH;
		add(graphPanel, cGraphPanel);
	}

	public synchronized Dot updateModel(InductiveVisualMinerState state) throws IOException {
		AlignedLogVisualisationParameters parameters = getViewParameters(state);
		Dot dot = visualiser.fancy(state.getTree(), state.getAlignedFilteredLogInfo(), state.getDfgFilteredLogInfos(), parameters);
		graphPanel.changeDot(dot, true);
		return dot;
	}

	//==visualisation parameters==

	private static AlignedLogVisualisationParameters both = new AlignedLogVisualisationParameters();
	private static AlignedLogVisualisationParameters moves = new AlignedLogVisualisationParameters();
	private static AlignedLogVisualisationParameters paths = new AlignedLogVisualisationParameters();
	private static AlignedLogVisualisationParameters withoutAlignment = new AlignedLogVisualisationParameters();

	private static void initVisualisationParameters() {
		withoutAlignment.setColourModelEdges(null);
		withoutAlignment.setShowFrequenciesOnModelEdges(false);
		withoutAlignment.setShowFrequenciesOnNodes(false);
		withoutAlignment.setModelEdgesWidth(new SizeMapFixed(1));

		paths.setShowFrequenciesOnModelEdges(true);
		paths.setColourModelEdges(new ColourMapBlue());
		paths.setModelEdgesWidth(new SizeMapLinear(1, 3));
		paths.setShowFrequenciesOnMoveEdges(false);
		paths.setShowLogMoves(false);
		paths.setShowModelMoves(false);

		moves.setColourModelEdges(new ColourMapFixed("#BBBBFF"));
		moves.setColourNodes(new ColourMapLightBlue());

		both.setShowFrequenciesOnModelEdges(true);
		both.setShowFrequenciesOnMoveEdges(true);
		both.setColourModelEdges(new ColourMapFixed("#9999FF"));
		both.setColourMoves(new ColourMapFixed("#FF0000"));
		both.setRepairLogMoves(false);
	}

	public static AlignedLogVisualisationParameters getViewParameters(InductiveVisualMinerState state) {
		if (!state.isAlignmentReady()) {
			return withoutAlignment;
		}
		switch (state.getColourMode()) {
			case both :
				return both;
			case deviations :
				return moves;
			default :
				return paths;
		}
	}

	private class Selected {
		public String stroke;
		public String strokeWidth;
		public String strokeDashArray;
	}

	public void makeNodeSelectable(final LocalDotNode dotNode, boolean select) {
		final Selected oldSelected = new Selected();
		dotNode.setSelectable(true);
		dotNode.addSelectionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotPanel panel = (DotPanel) e.getSource();
				Group svgGroup = panel.getSVGElementOf(dotNode);
				SVGElement shape = svgGroup.getChild(1);

				oldSelected.stroke = panel.setCSSAttributeOf(shape, "stroke", "red");
				oldSelected.strokeWidth = panel.setCSSAttributeOf(shape, "stroke-width", "3");
				oldSelected.strokeDashArray = panel.setCSSAttributeOf(shape, "stroke-dasharray", "5,5");

				panel.repaint();
			}
		});
		dotNode.addDeselectionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotPanel panel = (DotPanel) e.getSource();
				Group svgGroup = panel.getSVGElementOf(dotNode);
				SVGElement shape = svgGroup.getChild(1);

				panel.setCSSAttributeOf(shape, "stroke", oldSelected.stroke);
				panel.setCSSAttributeOf(shape, "stroke-width", oldSelected.strokeWidth);
				panel.setCSSAttributeOf(shape, "stroke-dasharray", oldSelected.strokeDashArray);

				panel.repaint();
			}
		});
		if (select) {
			graphPanel.select(dotNode);
		}
	}

	public DotPanel getGraph() {
		return graphPanel;
	}

	public JComboBox getColourModeSelection() {
		return colourSelection;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JComboBox getClassifiers() {
		return classifiersCombobox;
	}

	public JComboBox getColourSelection() {
		return colourSelection;
	}

	public JTextArea getSelectionLabel() {
		return selectionLabel;
	}

	public JCheckBox getShowDirectlyFollowsGraphs() {
		return showDirectlyFollowsGraphs;
	}

	public NiceDoubleSlider getNoiseSlider() {
		return noiseSlider;
	}

	public NiceDoubleSlider getActivitiesSlider() {
		return activitiesSlider;
	}
	
	public JButton getExitButton() {
		return exitButton;
	}

	public Map<UnfoldedNode, Set<LocalDotNode>> getUnfoldedNode2DfgdotNodes() {
		return visualiser.getUnfoldedNode2DfgdotNodes();
	}

	public Map<UnfoldedNode, Set<LocalDotNode>> getUnfoldedNode2dotNodes() {
		return visualiser.getUnfoldedNode2dotNodes();
	}

	public Map<UnfoldedNode, LocalDotNode> getActivity2dotNode() {
		return visualiser.getActivity2dotNode();
	}

	public void setOnSelectionChanged(InputFunction<Set<UnfoldedNode>> onSelectionChanged) {
		this.onSelectionChanged = onSelectionChanged;
	}

	public Map<UnfoldedNode, Set<LocalDotEdge>> getUnfoldedNode2DfgdotEdges() {
		return visualiser.getUnfoldedNode2DfgdotEdges();
	}

	public Map<UnfoldedNode, Set<LocalDotEdge>> getUnfoldedNode2dotEdgesModel() {
		return visualiser.getUnfoldedNode2dotEdgesModel();
	}

	public Map<UnfoldedNode, Set<LocalDotEdge>> getUnfoldedNode2dotEdgesMove() {
		return visualiser.getUnfoldedNode2dotEdgesMove();
	}
}
