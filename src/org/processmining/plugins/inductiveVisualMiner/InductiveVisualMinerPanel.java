package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.Classifiers.ClassifierWrapper;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.colourMaps.ColourMapBlue;
import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapLightBlue;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState.ColourMode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFiltersView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class InductiveVisualMinerPanel extends JPanel {

	private static final long serialVersionUID = -1078786029763735572L;

	//gui elements
	private final DotPanel graphPanel;
	private final JComboBox<?> colourSelection;
	private final JLabel colourLabel;
	private final JLabel statusLabel;
	private final JTextArea selectionLabel;
	private final NiceDoubleSlider activitiesSlider;
	private final NiceDoubleSlider pathsSlider;
	private final JLabel classifierLabel;
	private JComboBox<?> classifiersCombobox;
	private final JLabel minerLabel;
	private JComboBox<?> minerCombobox;
	private final JButton saveModelButton;
	private final JButton saveImageButton;
	private final JButton traceViewButton;
	private final TraceView traceView;
	private final JButton colouringFiltersViewButton;
	private final ColouringFiltersView colouringFiltersView;

	private final AlignedLogVisualisation visualiser;

	private InputFunction<Pair<Set<UnfoldedNode>, Set<LogMovePosition>>> onSelectionChanged = null;

	public InductiveVisualMinerPanel(final PluginContext context, InductiveVisualMinerState state, ClassifierWrapper[] classifiers,
			VisualMinerWrapper[] miners, boolean enableMining) {	
		visualiser = new AlignedLogVisualisation();
		initVisualisationParameters();

		int gridy = 0;

		setLayout(new GridBagLayout());

		{
			activitiesSlider = SlickerFactory.instance().createNiceDoubleSlider("activities", 0, 1.0, 1.0,
					Orientation.VERTICAL);
			GridBagConstraints cActivitiesSlider = new GridBagConstraints();
			cActivitiesSlider.gridx = 1;
			cActivitiesSlider.gridy = gridy;
			cActivitiesSlider.fill = GridBagConstraints.VERTICAL;
			cActivitiesSlider.anchor = GridBagConstraints.EAST;
			add(getActivitiesSlider(), cActivitiesSlider);
		}

		{
			pathsSlider = SlickerFactory.instance().createNiceDoubleSlider("paths", 0, 1.0,
					state.getPaths(), Orientation.VERTICAL);
			GridBagConstraints cNoiseSlider = new GridBagConstraints();
			cNoiseSlider.gridx = 2;
			cNoiseSlider.gridy = gridy++;
			cNoiseSlider.weighty = 1;
			cNoiseSlider.fill = GridBagConstraints.VERTICAL;
			cNoiseSlider.anchor = GridBagConstraints.WEST;
			add(getPathsSlider(), cNoiseSlider);
		}

		{
			classifierLabel = SlickerFactory.instance().createLabel("Classifier");
			GridBagConstraints cClassifierLabel = new GridBagConstraints();
			cClassifierLabel.gridx = 1;
			cClassifierLabel.gridy = gridy;
			cClassifierLabel.gridwidth = 1;
			cClassifierLabel.anchor = GridBagConstraints.WEST;
			add(classifierLabel, cClassifierLabel);

			classifiersCombobox = SlickerFactory.instance().createComboBox(classifiers);
			GridBagConstraints cClassifiers = new GridBagConstraints();
			cClassifiers.gridx = 2;
			cClassifiers.gridy = gridy++;
			cClassifiers.gridwidth = 1;
			cClassifiers.fill = GridBagConstraints.HORIZONTAL;
			add(classifiersCombobox, cClassifiers);
			classifiersCombobox.setSelectedItem(state.getActivityClassifier());
		}

		{
			minerLabel = SlickerFactory.instance().createLabel("Mine");
			GridBagConstraints cMinerLabel = new GridBagConstraints();
			cMinerLabel.gridx = 1;
			cMinerLabel.gridy = gridy;
			cMinerLabel.gridwidth = 1;
			cMinerLabel.anchor = GridBagConstraints.WEST;
			add(minerLabel, cMinerLabel);

			minerCombobox = SlickerFactory.instance().createComboBox(miners);
			GridBagConstraints cMiners = new GridBagConstraints();
			cMiners.gridx = 2;
			cMiners.gridy = gridy++;
			cMiners.gridwidth = 1;
			cMiners.fill = GridBagConstraints.HORIZONTAL;
			add(minerCombobox, cMiners);
			minerCombobox.setSelectedItem(state.getMiner());
		}

		{
			colourLabel = SlickerFactory.instance().createLabel("Show");
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

		//trace view
		{
			traceView = new TraceView(this);
			traceViewButton = SlickerFactory.instance().createButton("traces");
			GridBagConstraints cTraceViewButton = new GridBagConstraints();
			cTraceViewButton.gridx = 2;
			cTraceViewButton.gridy = gridy++;
			cTraceViewButton.gridwidth = 1;
			cTraceViewButton.fill = GridBagConstraints.HORIZONTAL;
			add(traceViewButton, cTraceViewButton);
		}

		//colouring filters view
		{
			colouringFiltersView = new ColouringFiltersView(this);
			colouringFiltersViewButton = SlickerFactory.instance().createButton("highlighting filters");
			GridBagConstraints cColouringFiltersViewButton = new GridBagConstraints();
			cColouringFiltersViewButton.gridx = 2;
			cColouringFiltersViewButton.gridy = gridy++;
			cColouringFiltersViewButton.gridwidth = 1;
			cColouringFiltersViewButton.fill = GridBagConstraints.HORIZONTAL;
			add(colouringFiltersViewButton, cColouringFiltersViewButton);
		}

		{
			JLabel saveLabel = SlickerFactory.instance().createLabel("Save");
			GridBagConstraints cExitButton = new GridBagConstraints();
			cExitButton.gridx = 1;
			cExitButton.gridy = gridy;
			cExitButton.gridwidth = 1;
			cExitButton.fill = GridBagConstraints.HORIZONTAL;
			add(saveLabel, cExitButton);
		}

		{
			saveModelButton = SlickerFactory.instance().createButton("model");
			GridBagConstraints cExitButton = new GridBagConstraints();
			cExitButton.gridx = 2;
			cExitButton.gridy = gridy++;
			cExitButton.gridwidth = 1;
			cExitButton.fill = GridBagConstraints.HORIZONTAL;
			add(saveModelButton, cExitButton);
		}

		{
			saveImageButton = SlickerFactory.instance().createButton("image");
			GridBagConstraints cExitButton = new GridBagConstraints();
			cExitButton.gridx = 2;
			cExitButton.gridy = gridy++;
			cExitButton.gridwidth = 1;
			cExitButton.fill = GridBagConstraints.HORIZONTAL;
			add(saveImageButton, cExitButton);
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

		//graph panel
		{
			Dot dot = new Dot();
			dot.addNode("Inductive visual Miner", "");
			dot.addNode("Mining model...", "");
			graphPanel = new DotPanel(dot) {
				private static final long serialVersionUID = -3112819390640390685L;

				public void selectionChanged() {
					//selection of nodes changed; keep track of them

					Set<UnfoldedNode> resultNodes = new HashSet<>();
					Set<LogMovePosition> resultLogMoveEdges = new HashSet<>();
					for (DotElement dotElement : graphPanel.getSelectedElements()) {
						if (dotElement instanceof LocalDotNode) {
							resultNodes.add(((LocalDotNode) dotElement).getUnode());
						} else if (dotElement instanceof LocalDotEdge) {
							resultLogMoveEdges.add(LogMovePosition.of(((LocalDotEdge) dotElement)));
						}
					}

					if (onSelectionChanged != null) {
						try {
							onSelectionChanged.call(Pair.of(resultNodes, resultLogMoveEdges));
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

		//handle pre-mined tree case
		if (!enableMining) {
			activitiesSlider.setVisible(false);
			pathsSlider.setVisible(false);
			classifierLabel.setVisible(false);
			classifiersCombobox.setVisible(false);
		}
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

		moves.setColourModelEdges(new ColourMapFixed(new Color(187, 187, 255)));
		moves.setColourNodes(new ColourMapLightBlue());

		both.setShowFrequenciesOnModelEdges(true);
		both.setShowFrequenciesOnMoveEdges(true);
		both.setColourModelEdges(new ColourMapFixed(new Color(153, 153, 255)));
		both.setColourMoves(new ColourMapFixed(new Color(255, 0, 0)));
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

	public void removeNotify() {
		super.removeNotify();
		traceView.setVisible(false);
		colouringFiltersView.setVisible(false);
	}

	public void makeNodeSelectable(final LocalDotNode dotNode, boolean select) {
		dotNode.setSelectable(true);
		dotNode.addSelectionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotPanel panel = (DotPanel) e.getSource();

				InductiveVisualMinerSelectionColourer.colourSelectedNode(panel.getSVG(), dotNode, true);

				panel.repaint();
			}
		});
		dotNode.addDeselectionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotPanel panel = (DotPanel) e.getSource();

				InductiveVisualMinerSelectionColourer.colourSelectedNode(panel.getSVG(), dotNode, false);

				panel.repaint();
			}
		});
		if (select) {
			graphPanel.select(dotNode);
		}
	}

	public void makeEdgeSelectable(final LocalDotEdge dotEdge, boolean select) {
		dotEdge.setSelectable(true);
		dotEdge.addSelectionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotPanel panel = (DotPanel) e.getSource();
				InductiveVisualMinerSelectionColourer.colourSelectedEdge(panel.getSVG(), dotEdge, true);
				panel.repaint();
			}
		});
		dotEdge.addDeselectionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotPanel panel = (DotPanel) e.getSource();
				InductiveVisualMinerSelectionColourer.colourSelectedEdge(panel.getSVG(), dotEdge, false);
				panel.repaint();
			}
		});
	}

	public DotPanel getGraph() {
		return graphPanel;
	}

	public JComboBox<?> getMinerSelection() {
		return minerCombobox;
	}

	public JComboBox<?> getColourModeSelection() {
		return colourSelection;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JComboBox<?> getClassifiers() {
		return classifiersCombobox;
	}

	public JComboBox<?> getColourSelection() {
		return colourSelection;
	}

	public JTextArea getSelectionLabel() {
		return selectionLabel;
	}

	public NiceDoubleSlider getPathsSlider() {
		return pathsSlider;
	}

	public NiceDoubleSlider getActivitiesSlider() {
		return activitiesSlider;
	}

	public JButton getSaveModelButton() {
		return saveModelButton;
	}

	public JButton getSaveImageButton() {
		return saveImageButton;
	}

	public void setOnSelectionChanged(InputFunction<Pair<Set<UnfoldedNode>, Set<LogMovePosition>>> onSelectionChanged) {
		this.onSelectionChanged = onSelectionChanged;
	}

	public TraceView getTraceView() {
		return traceView;
	}

	public JButton getTraceViewButton() {
		return traceViewButton;
	}

	public ColouringFiltersView getColouringFiltersView() {
		return colouringFiltersView;
	}

	public JButton getColouringFiltersViewButton() {
		return colouringFiltersViewButton;
	}
}
