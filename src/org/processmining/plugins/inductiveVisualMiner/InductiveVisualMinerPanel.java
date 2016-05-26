package org.processmining.plugins.inductiveVisualMiner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.BoundsPopupMenuListener;
import org.processmining.plugins.InductiveMiner.ClassifierChooser;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.SelectionChangedListener;
import org.processmining.plugins.inductiveVisualMiner.editModel.EditModelView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFiltersView;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsDeviations;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsQueueLengths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsService;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsSojourn;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceView;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerPanel extends JPanel {

	private static final long serialVersionUID = -1078786029763735572L;

	private static final int sidePanelWidth = 210;
	private static final int lineHeight = 20;

	//gui elements
	private final InductiveVisualMinerAnimationPanel graphPanel;
	private final JComboBox<?> colourSelection;
	private final JLabel colourLabel;
	private final JLabel statusLabel;
	private final JLabel animationTimeLabel;
	private final JTextArea selectionLabel;
	private final NiceDoubleSlider activitiesSlider;
	private final NiceDoubleSlider pathsSlider;
	private final JLabel classifierLabel;
	private ClassifierChooser classifiersCombobox;
	private final JButton preMiningFiltersButton;
	private final PreMiningFiltersView preMiningFiltersView;
	private final JButton editModelButton;
	private final EditModelView editModelView;
	private final JLabel minerLabel;
	private JComboBox<?> minerCombobox;
	private final JButton saveModelButton;
	private final JButton saveImageButton;
	private final JButton traceViewButton;
	private final TraceView traceView;
	private final JButton highlightingFiltersViewButton;
	private final HighlightingFiltersView highlightingFiltersView;

	private InputFunction<Selection> onSelectionChanged = null;
	private Runnable onGraphDirectionChanged = null;

	public static InductiveVisualMinerPanel panelPro(final PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, boolean enableMining, ProMCanceller canceller) {
		return new InductiveVisualMinerPanel(context, state, miners, enableMining, true, canceller);
	}

	public static InductiveVisualMinerPanel panelBasic(PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, ProMCanceller canceller) {
		return new InductiveVisualMinerPanel(context, state, miners, true, false, canceller);
	}

	private InductiveVisualMinerPanel(final PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, boolean enableMining, boolean pro, ProMCanceller canceller) {
		int gridy = 0;

		setLayout(new BorderLayout());

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.setMaximumSize(new Dimension(sidePanelWidth, 10000));
		sidePanel.setMinimumSize(new Dimension(sidePanelWidth, 100));
		sidePanel.setPreferredSize(new Dimension(sidePanelWidth, 10000));
		add(sidePanel, BorderLayout.LINE_END);

		//sliders panel
		{
			JPanel slidersPanel = new JPanel();
			sidePanel.add(slidersPanel, BorderLayout.CENTER);
			slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.LINE_AXIS));

			//activities slider
			{
				activitiesSlider = SlickerFactory.instance().createNiceDoubleSlider("activities", 0, 1.0, 1.0,
						Orientation.VERTICAL);
				slidersPanel.add(activitiesSlider);
			}

			//paths slider
			{
				pathsSlider = SlickerFactory.instance().createNiceDoubleSlider("paths", 0, 1.0, state.getPaths(),
						Orientation.VERTICAL);
				slidersPanel.add(pathsSlider);
			}
		}

		//other settings
		{
			JPanel otherSettingsPanel = new JPanel();
			sidePanel.add(otherSettingsPanel, BorderLayout.PAGE_END);
			otherSettingsPanel.setLayout(new GridBagLayout());
			{
				classifierLabel = SlickerFactory.instance().createLabel("Classifier");
				if (pro) {
					GridBagConstraints cClassifierLabel = new GridBagConstraints();
					cClassifierLabel.gridx = 0;
					cClassifierLabel.gridy = gridy;
					cClassifierLabel.gridwidth = 1;
					cClassifierLabel.anchor = GridBagConstraints.WEST;
					otherSettingsPanel.add(classifierLabel, cClassifierLabel);
				}

				classifiersCombobox = new ClassifierChooser(null, null, false);
				if (pro) {
					classifiersCombobox.setEnabled(false);
					GridBagConstraints cClassifiers = new GridBagConstraints();
					cClassifiers.gridx = 1;
					cClassifiers.gridy = gridy++;
					cClassifiers.gridwidth = 1;
					cClassifiers.fill = GridBagConstraints.HORIZONTAL;
					otherSettingsPanel.add(classifiersCombobox, cClassifiers);
				}
			}

			//pre-mining filters
			{
				preMiningFiltersView = new PreMiningFiltersView(this);
				preMiningFiltersButton = SlickerFactory.instance().createButton("pre-mining filters");
				if (pro) {
					GridBagConstraints cTraceViewButton = new GridBagConstraints();
					cTraceViewButton.gridx = 1;
					cTraceViewButton.gridy = gridy++;
					cTraceViewButton.gridwidth = 1;
					cTraceViewButton.fill = GridBagConstraints.HORIZONTAL;
					otherSettingsPanel.add(preMiningFiltersButton, cTraceViewButton);
				}
			}

			//miner
			{
				minerLabel = SlickerFactory.instance().createLabel("Miner");
				if (pro) {
					GridBagConstraints cMinerLabel = new GridBagConstraints();
					cMinerLabel.gridx = 0;
					cMinerLabel.gridy = gridy;
					cMinerLabel.gridwidth = 1;
					cMinerLabel.anchor = GridBagConstraints.WEST;
					otherSettingsPanel.add(minerLabel, cMinerLabel);
				}

				minerCombobox = SlickerFactory.instance().createComboBox(miners);
				if (pro) {
					minerCombobox.addPopupMenuListener(new BoundsPopupMenuListener(true, false));
					GridBagConstraints cMiners = new GridBagConstraints();
					cMiners.gridx = 1;
					cMiners.gridy = gridy++;
					cMiners.gridwidth = 1;
					cMiners.fill = GridBagConstraints.HORIZONTAL;
					otherSettingsPanel.add(minerCombobox, cMiners);
					minerCombobox.setSelectedItem(state.getMiner());
				}
			}

			//edit model view
			{
				editModelView = new EditModelView(this);
				editModelButton = SlickerFactory.instance().createButton("edit model");
				if (pro) {
					GridBagConstraints cEditModelButton = new GridBagConstraints();
					cEditModelButton.gridx = 1;
					cEditModelButton.gridy = gridy++;
					cEditModelButton.gridwidth = 1;
					cEditModelButton.fill = GridBagConstraints.HORIZONTAL;
					otherSettingsPanel.add(editModelButton, cEditModelButton);
				}
			}

			{
				colourLabel = SlickerFactory.instance().createLabel("Show");
				GridBagConstraints cColourLabel = new GridBagConstraints();
				cColourLabel.gridx = 0;
				cColourLabel.gridy = gridy;
				cColourLabel.gridwidth = 1;
				cColourLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(colourLabel, cColourLabel);

				if (pro) {
					colourSelection = SlickerFactory.instance().createComboBox(
							new Mode[] { new ModePaths(), new ModePathsDeviations(), new ModePathsQueueLengths(),
									new ModePathsSojourn(), new ModePathsService() });
				} else {
					colourSelection = SlickerFactory.instance().createComboBox(
							new Mode[] { new ModePaths(), new ModePathsDeviations() });
				}
				colourSelection.addPopupMenuListener(new BoundsPopupMenuListener(true, false));
				GridBagConstraints ccolourSelection = new GridBagConstraints();
				ccolourSelection.gridx = 1;
				ccolourSelection.gridy = gridy++;
				ccolourSelection.gridwidth = 1;
				ccolourSelection.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(colourSelection, ccolourSelection);
			}

			//highlighting filters view
			{
				highlightingFiltersView = new HighlightingFiltersView(this);
				highlightingFiltersViewButton = SlickerFactory.instance().createButton("highlighting filters");
				GridBagConstraints cColouringFiltersViewButton = new GridBagConstraints();
				cColouringFiltersViewButton.gridx = 1;
				cColouringFiltersViewButton.gridy = gridy++;
				cColouringFiltersViewButton.gridwidth = 1;
				cColouringFiltersViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(highlightingFiltersViewButton, cColouringFiltersViewButton);
			}

			//trace view
			{
				traceView = new TraceView(this);
				traceViewButton = SlickerFactory.instance().createButton("traces");
				GridBagConstraints cTraceViewButton = new GridBagConstraints();
				cTraceViewButton.gridx = 1;
				cTraceViewButton.gridy = gridy++;
				cTraceViewButton.gridwidth = 1;
				cTraceViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(traceViewButton, cTraceViewButton);
			}

			{
				saveModelButton = SlickerFactory.instance().createButton("export model");
				GridBagConstraints cExitButton = new GridBagConstraints();
				cExitButton.gridx = 1;
				cExitButton.gridy = gridy++;
				cExitButton.gridwidth = 1;
				cExitButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(saveModelButton, cExitButton);
			}

			{
				saveImageButton = SlickerFactory.instance().createButton("export view");
				GridBagConstraints cExitButton = new GridBagConstraints();
				cExitButton.gridx = 1;
				cExitButton.gridy = gridy++;
				cExitButton.gridwidth = 1;
				cExitButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(saveImageButton, cExitButton);
			}

			{
				selectionLabel = new JTextArea(" ");
				selectionLabel.setWrapStyleWord(true);
				selectionLabel.setLineWrap(true);
				selectionLabel.setEditable(false);
				selectionLabel.setOpaque(false);
				GridBagConstraints cSelectionLabel = new GridBagConstraints();
				cSelectionLabel.gridx = 0;
				cSelectionLabel.gridy = gridy++;
				cSelectionLabel.gridwidth = 2;
				cSelectionLabel.anchor = GridBagConstraints.NORTH;
				cSelectionLabel.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(selectionLabel, cSelectionLabel);
			}

			{
				animationTimeLabel = SlickerFactory.instance().createLabel(" ");
				animationTimeLabel.setMinimumSize(new Dimension(10, lineHeight));
				animationTimeLabel.setPreferredSize(new Dimension(sidePanelWidth - 5, lineHeight));
				GridBagConstraints cAnimationTimeLabel = new GridBagConstraints();
				cAnimationTimeLabel.gridx = 0;
				cAnimationTimeLabel.gridy = gridy++;
				cAnimationTimeLabel.gridwidth = 2;
				cAnimationTimeLabel.anchor = GridBagConstraints.SOUTH;
				otherSettingsPanel.add(animationTimeLabel, cAnimationTimeLabel);
			}

			{
				statusLabel = SlickerFactory.instance().createLabel(" ");
				statusLabel.setMinimumSize(new Dimension(10, lineHeight));
				statusLabel.setPreferredSize(new Dimension(sidePanelWidth - 5, lineHeight));
				GridBagConstraints cStatus = new GridBagConstraints();
				cStatus.gridx = 0;
				cStatus.gridy = gridy++;
				cStatus.gridwidth = 2;
				cStatus.anchor = GridBagConstraints.SOUTH;
				otherSettingsPanel.add(statusLabel, cStatus);
			}
		}

		//graph panel
		{
			graphPanel = new InductiveVisualMinerAnimationPanel(canceller);
			graphPanel.setFocusable(true);

			//set the graph changed listener
			//if we are initialised, the dotPanel should not update the layout, as we have to recompute the animation
			graphPanel.addGraphChangedListener(new GraphChangedListener() {
				public void graphChanged(GraphChangedReason reason, Object newState) {
					onGraphDirectionChanged.run();
				}
			});

			//set the node selection change listener
			graphPanel.addSelectionChangedListener(new SelectionChangedListener<DotElement>() {
				public void selectionChanged(Set<DotElement> selectedElements) {
					//selection of nodes changed; keep track of them

					Selection selection = new Selection();
					for (DotElement dotElement : graphPanel.getSelectedElements()) {
						selection.select(dotElement);
					}

					if (onSelectionChanged != null) {
						try {
							onSelectionChanged.call(selection);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					graphPanel.repaint();
				}
			});

			add(graphPanel, BorderLayout.CENTER);
		}

		//handle pre-mined tree case
		if (!enableMining) {
			activitiesSlider.setVisible(false);
			pathsSlider.setVisible(false);
			//classifierLabel.setVisible(false);
			//classifiersCombobox.setVisible(false);
			preMiningFiltersButton.setVisible(false);
			minerLabel.setVisible(false);
			minerCombobox.setVisible(false);
		}
	}

	public void removeNotify() {
		super.removeNotify();
		editModelView.setVisible(false);
		preMiningFiltersView.setVisible(false);
		traceView.setVisible(false);
		highlightingFiltersView.setVisible(false);
		graphPanel.pause();
	}

	public void makeNodeSelectable(final LocalDotNode dotNode, boolean select) {
		dotNode.addSelectionListener(new DotElementSelectionListener() {
			public void selected(DotElement element, SVGDiagram image) {
				InductiveVisualMinerSelectionColourer.colourSelectedNode(image, dotNode, true);
			}

			public void deselected(DotElement element, SVGDiagram image) {
				InductiveVisualMinerSelectionColourer.colourSelectedNode(image, dotNode, false);
			}
		});
		if (select) {
			graphPanel.select(dotNode);
		}
	}

	public void makeEdgeSelectable(final LocalDotEdge dotEdge, boolean select) {
		dotEdge.addSelectionListener(new DotElementSelectionListener() {
			public void selected(DotElement element, SVGDiagram image) {
				InductiveVisualMinerSelectionColourer.colourSelectedEdge(graphPanel.getSVG(), dotEdge, true);
				graphPanel.repaint();
			}

			public void deselected(DotElement element, SVGDiagram image) {
				InductiveVisualMinerSelectionColourer.colourSelectedEdge(graphPanel.getSVG(), dotEdge, false);
				graphPanel.repaint();
			}
		});
		if (select) {
			graphPanel.select(dotEdge);
		}
	}

	public InductiveVisualMinerAnimationPanel getGraph() {
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

	public ClassifierChooser getClassifiers() {
		return classifiersCombobox;
	}

	public JComboBox<?> getColourSelection() {
		return colourSelection;
	}

	public JButton getPreMiningFiltersButton() {
		return preMiningFiltersButton;
	}

	public PreMiningFiltersView getPreMiningFiltersView() {
		return preMiningFiltersView;
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

	public void setOnSelectionChanged(InputFunction<Selection> onSelectionChanged) {
		this.onSelectionChanged = onSelectionChanged;
	}

	public void setOnGraphDirectionChanged(Runnable onGraphDirectionChanged) {
		this.onGraphDirectionChanged = onGraphDirectionChanged;
	}

	public TraceView getTraceView() {
		return traceView;
	}

	public JButton getTraceViewButton() {
		return traceViewButton;
	}

	public EditModelView getEditModelView() {
		return editModelView;
	}

	public JButton getEditModelButton() {
		return editModelButton;
	}

	public HighlightingFiltersView getColouringFiltersView() {
		return highlightingFiltersView;
	}

	public JButton getColouringFiltersViewButton() {
		return highlightingFiltersViewButton;
	}

	public JLabel getAnimationTimeLabel() {
		return animationTimeLabel;
	}
}
