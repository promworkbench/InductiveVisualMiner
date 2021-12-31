package org.processmining.plugins.inductiveVisualMiner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.BoundsPopupMenuListener;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.SelectionChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.editModel.EditModelView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ControllerView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMClassifierChooser;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMPanel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFiltersView;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view.IvMFilterTreeView;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapView;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceView;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerPanel extends IvMPanel {

	private static final long serialVersionUID = -1078786029763735572L;

	private static final int sidePanelWidth = 260;
	private static final int lineHeight = 20;

	private static final Insets margins = new Insets(2, 0, 0, 0);

	//gui elements
	private final InductiveVisualMinerAnimationPanel graphPanel;
	private final JComboBox<Mode> modeSelection;
	private final JLabel modeLabel;
	private final JLabel statusLabel;
	private final JLabel animationTimeLabel;
	private final JTextArea selectionLabel;
	private final NiceDoubleSlider activitiesSlider;
	private final NiceDoubleSlider pathsSlider;
	private final JLabel classifierLabel;
	private IvMClassifierChooser classifiersCombobox;
	private final JButton preMiningFiltersButton;
	private final PreMiningFiltersView preMiningFiltersView;
	private final JButton editModelButton;
	private final EditModelView editModelView;
	private final JLabel minerLabel;
	private JComboBox<?> minerCombobox;
	private final JButton saveLogButton;
	private final JButton saveModelButton;
	private final JButton saveImageButton;
	private final JButton traceViewButton;
	private final TraceView traceView;
	private final JButton dataAnalysisViewButton;
	private final DataAnalysesView<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> dataAnalysisView;
	private final JButton traceColourMapViewButton;
	private final TraceColourMapView traceColourMapView;
	private final JButton highlightingFiltersViewButton;
	private final HighlightingFiltersView highlightingFiltersView;
	private final IvMFilterTreeView<IvMTrace> highlightingFilterTreeView;
	private final ControllerView<DataState> controllerView;

	private InputFunction<Selection> onSelectionChanged = null;
	private Runnable onGraphDirectionChanged = null;
	private AnimationEnabledChangedListener onAnimationEnabledChanged = null;

	private static final Image logo = Toolkit.getDefaultToolkit().getImage(InductiveVisualMinerPanel.class
			.getResource("/org/processmining/plugins/inductiveVisualMiner/inductive miner logo.png"));

	public static final String title = "visual Miner";

	public InductiveVisualMinerPanel(InductiveVisualMinerConfiguration configuration, ProMCanceller canceller) {
		super(configuration.getDecorator());
		IvMDecoratorI decorator = configuration.getDecorator();

		int gridy = 0;

		setLayout(new BorderLayout());

		setOpaque(false);

		//controls the margin on the left side of the settings panel
		Border leftBorder = new EmptyBorder(0, 2, 0, 0);

		JPanel sidePanel = new JPanel();
		sidePanel.setOpaque(false);
		sidePanel.setLayout(new BorderLayout());
		sidePanel.setMaximumSize(new Dimension(sidePanelWidth, 10000));
		sidePanel.setMinimumSize(new Dimension(sidePanelWidth, 100));
		sidePanel.setPreferredSize(new Dimension(sidePanelWidth, 10000));
		add(sidePanel, BorderLayout.LINE_END);

		//sliders panel
		{
			JPanel slidersPanel = new JPanel();
			sidePanel.add(slidersPanel, BorderLayout.CENTER);
			slidersPanel.setOpaque(false);
			slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.LINE_AXIS));

			//activities slider
			{
				activitiesSlider = SlickerFactory.instance().createNiceDoubleSlider("activities", 0, 1.0, 1.0,
						Orientation.VERTICAL);
				slidersPanel.add(activitiesSlider);
			}

			//paths slider
			{
				pathsSlider = SlickerFactory.instance().createNiceDoubleSlider("paths", 0, 1.0, 1,
						Orientation.VERTICAL);
				slidersPanel.add(pathsSlider);
			}
		}

		//other settings
		{
			JPanel otherSettingsPanel = new JPanel();
			sidePanel.add(otherSettingsPanel, BorderLayout.PAGE_END);
			otherSettingsPanel.setOpaque(false);
			otherSettingsPanel.setLayout(new GridBagLayout());
			{
				classifierLabel = new JLabel("Classifier");
				decorator.decorate(getClassifierLabel());
				classifierLabel.setBorder(leftBorder);
				GridBagConstraints cClassifierLabel = new GridBagConstraints();
				cClassifierLabel.gridx = 0;
				cClassifierLabel.gridy = gridy;
				cClassifierLabel.gridwidth = 1;
				cClassifierLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(classifierLabel, cClassifierLabel);

				classifiersCombobox = new IvMClassifierChooser(null, null, false);
				decorator.decorate(classifiersCombobox.getMultiComboBox());
				classifiersCombobox.setEnabled(false);
				GridBagConstraints cClassifiers = new GridBagConstraints();
				cClassifiers.gridx = 1;
				cClassifiers.gridy = gridy++;
				cClassifiers.gridwidth = 1;
				cClassifiers.insets = margins;
				cClassifiers.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(classifiersCombobox, cClassifiers);
			}

			//pre-mining filters
			{
				preMiningFiltersView = new PreMiningFiltersView(this, configuration.getPreMiningFilters(),
						configuration.getDecorator());
				preMiningFiltersButton = new JButton("pre-mining filters");
				decorator.decorate(preMiningFiltersButton);
				GridBagConstraints cTraceViewButton = new GridBagConstraints();
				cTraceViewButton.gridx = 1;
				cTraceViewButton.gridy = gridy++;
				cTraceViewButton.gridwidth = 1;
				cTraceViewButton.insets = margins;
				cTraceViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(preMiningFiltersButton, cTraceViewButton);
			}

			//miner
			{
				minerLabel = new JLabel("Miner");
				decorator.decorate(getMinerLabel());
				getMinerLabel().setBorder(leftBorder);
				GridBagConstraints cMinerLabel = new GridBagConstraints();
				cMinerLabel.gridx = 0;
				cMinerLabel.gridy = gridy;
				cMinerLabel.gridwidth = 1;
				cMinerLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(getMinerLabel(), cMinerLabel);

				minerCombobox = new JComboBox<>(configuration.getDiscoveryTechniquesArray());
				decorator.decorate(minerCombobox);
				minerCombobox.addPopupMenuListener(new BoundsPopupMenuListener(true, false));
				minerCombobox.setFocusable(false);
				GridBagConstraints cMiners = new GridBagConstraints();
				cMiners.gridx = 1;
				cMiners.gridy = gridy++;
				cMiners.gridwidth = 1;
				cMiners.insets = margins;
				cMiners.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(minerCombobox, cMiners);
			}

			//edit model view
			{
				editModelView = new EditModelView(this, decorator);
				editModelButton = new JButton("edit model");
				decorator.decorate(editModelButton);
				GridBagConstraints cEditModelButton = new GridBagConstraints();
				cEditModelButton.gridx = 1;
				cEditModelButton.gridy = gridy++;
				cEditModelButton.gridwidth = 1;
				cEditModelButton.insets = margins;
				cEditModelButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(editModelButton, cEditModelButton);
			}

			{
				modeLabel = new JLabel("Show");
				decorator.decorate(modeLabel);
				modeLabel.setBorder(leftBorder);
				GridBagConstraints cColourLabel = new GridBagConstraints();
				cColourLabel.gridx = 0;
				cColourLabel.gridy = gridy;
				cColourLabel.gridwidth = 1;
				cColourLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(modeLabel, cColourLabel);

				modeSelection = new JComboBox<>(configuration.getModesArray());
				decorator.decorate(modeSelection);
				modeSelection.addPopupMenuListener(new BoundsPopupMenuListener(true, false));
				modeSelection.setFocusable(false);
				GridBagConstraints ccolourSelection = new GridBagConstraints();
				ccolourSelection.gridx = 1;
				ccolourSelection.gridy = gridy++;
				ccolourSelection.gridwidth = 1;
				ccolourSelection.insets = margins;
				ccolourSelection.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(modeSelection, ccolourSelection);
			}

			//trace colouring view
			{
				traceColourMapView = new TraceColourMapView(configuration.getDecorator(), this);
				traceColourMapViewButton = new JButton("trace colouring");
				decorator.decorate(traceColourMapViewButton);
				GridBagConstraints cTraceColourMapViewButton = new GridBagConstraints();
				cTraceColourMapViewButton.gridx = 1;
				cTraceColourMapViewButton.gridy = gridy++;
				cTraceColourMapViewButton.gridwidth = 1;
				cTraceColourMapViewButton.insets = margins;
				cTraceColourMapViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(traceColourMapViewButton, cTraceColourMapViewButton);
			}

			//highlighting filters view
			{
				highlightingFiltersView = new HighlightingFiltersView(this, configuration.getHighlightingFilters(),
						configuration.getDecorator());
				highlightingFilterTreeView = new IvMFilterTreeView<IvMTrace>(this, "Highlighting filters", decorator);

				highlightingFiltersViewButton = new JButton("highlighting filters");
				decorator.decorate(highlightingFiltersViewButton);
				GridBagConstraints cColouringFiltersViewButton = new GridBagConstraints();
				cColouringFiltersViewButton.gridx = 1;
				cColouringFiltersViewButton.gridy = gridy++;
				cColouringFiltersViewButton.gridwidth = 1;
				cColouringFiltersViewButton.insets = margins;
				cColouringFiltersViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(highlightingFiltersViewButton, cColouringFiltersViewButton);
			}

			//trace view
			{
				traceView = new TraceView(decorator, this);
				traceViewButton = new JButton("traces");
				decorator.decorate(traceViewButton);
				GridBagConstraints cTraceViewButton = new GridBagConstraints();
				cTraceViewButton.gridx = 1;
				cTraceViewButton.gridy = gridy++;
				cTraceViewButton.gridwidth = 1;
				cTraceViewButton.insets = margins;
				cTraceViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(traceViewButton, cTraceViewButton);
			}

			//data analysis view
			{
				dataAnalysisView = new DataAnalysesView<>(this, configuration.getDataAnalysisTables(),
						configuration.getDecorator());
				dataAnalysisViewButton = new JButton("data analysis");
				decorator.decorate(dataAnalysisViewButton);
				GridBagConstraints cDataAnalysisViewButton = new GridBagConstraints();
				cDataAnalysisViewButton.gridx = 1;
				cDataAnalysisViewButton.gridy = gridy++;
				cDataAnalysisViewButton.gridwidth = 1;
				cDataAnalysisViewButton.insets = margins;
				cDataAnalysisViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(dataAnalysisViewButton, cDataAnalysisViewButton);
			}

			//save log button
			{
				saveLogButton = new JButton("export log");
				decorator.decorate(saveLogButton);
				GridBagConstraints cExitButton = new GridBagConstraints();
				cExitButton.gridx = 1;
				cExitButton.gridy = gridy++;
				cExitButton.gridwidth = 1;
				cExitButton.insets = margins;
				cExitButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(saveLogButton, cExitButton);
			}

			//save model button
			{
				saveModelButton = new JButton("export model");
				decorator.decorate(saveModelButton);
				GridBagConstraints cExitButton = new GridBagConstraints();
				cExitButton.gridx = 1;
				cExitButton.gridy = gridy++;
				cExitButton.gridwidth = 1;
				cExitButton.insets = margins;
				cExitButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(saveModelButton, cExitButton);
			}

			//save image button 
			{
				saveImageButton = new JButton("export ..");
				decorator.decorate(saveImageButton);
				GridBagConstraints cExitButton = new GridBagConstraints();
				cExitButton.gridx = 1;
				cExitButton.gridy = gridy++;
				cExitButton.gridwidth = 1;
				cExitButton.insets = margins;
				cExitButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(saveImageButton, cExitButton);
			}

			//status area
			{
				JPanel statusArea = new JPanel() {
					private static final long serialVersionUID = -3004738781055843682L;

					//paint a background with our icon
					@Override
					protected void paintComponent(Graphics g) {
						super.paintComponent(g);
						if (logo != null) {
							g.drawImage(logo, getWidth() / 2 - logo.getWidth(null) / 2, 5, null);
						}
					}
				};
				//statusArea.setMinimumSize(new Dimension(sidePanelWidth, 70));
				//statusArea.setPreferredSize(new Dimension(sidePanelWidth, 70));
				statusArea.setMaximumSize(new Dimension(sidePanelWidth, 10000));
				statusArea.setOpaque(false);
				statusArea.setLayout(new BorderLayout());
				GridBagConstraints cStatusArea = new GridBagConstraints();
				cStatusArea.gridx = 0;
				cStatusArea.gridy = gridy++;
				cStatusArea.gridwidth = 2;
				cStatusArea.anchor = GridBagConstraints.NORTH;
				//cStatusArea.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(statusArea, cStatusArea);

				{
					selectionLabel = new JTextArea(" ");
					selectionLabel.setWrapStyleWord(true);
					selectionLabel.setLineWrap(true);
					selectionLabel.setEditable(false);
					selectionLabel.setOpaque(false);
					selectionLabel.setBorder(leftBorder);
					statusArea.add(selectionLabel, BorderLayout.CENTER);
				}

				{
					animationTimeLabel = SlickerFactory.instance().createLabel(" ");
					animationTimeLabel.setMinimumSize(new Dimension(sidePanelWidth - 5, lineHeight));
					animationTimeLabel.setPreferredSize(new Dimension(sidePanelWidth - 5, lineHeight));
					animationTimeLabel.setBorder(leftBorder);
					statusArea.add(animationTimeLabel, BorderLayout.PAGE_START);
				}

				{
					statusLabel = SlickerFactory.instance().createLabel(" ");
					statusLabel.setMinimumSize(new Dimension(10, lineHeight));
					statusLabel.setPreferredSize(new Dimension(sidePanelWidth - 5, (int) (lineHeight * 1.5)));
					statusLabel.setVerticalAlignment(JLabel.BOTTOM);
					statusLabel.setBorder(leftBorder);
					statusArea.add(statusLabel, BorderLayout.PAGE_END);
				}
			}
		}

		//graph panel
		{
			graphPanel = new InductiveVisualMinerAnimationPanel(canceller, decorator);
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

			//set the animation enabled changed listener
			graphPanel.addAnimationEnabledChangedListener(new AnimationEnabledChangedListener() {
				public boolean animationEnabledChanged() {
					if (onAnimationEnabledChanged != null) {
						return onAnimationEnabledChanged.animationEnabledChanged();
					}
					return true;
				}
			});

			add(graphPanel, BorderLayout.CENTER);
		}

		//controller view
		{
			controllerView = new ControllerView<>(this);
			graphPanel.getHelperControlsShortcuts().add("ctrl o");
			graphPanel.getHelperControlsExplanations().add("show controller");
		}

		//cost model
		{
			graphPanel.getHelperControlsShortcuts().add("ctrl c");
			graphPanel.getHelperControlsExplanations().add("change cost model");
		}
	}

	public void removeNotify() {
		super.removeNotify();
		for (SideWindow sideWindow : getSideWindows()) {
			sideWindow.setVisible(false);
		}
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

	public SideWindow[] getSideWindows() {
		return new SideWindow[] { editModelView, preMiningFiltersView, traceView, dataAnalysisView,
				highlightingFiltersView, highlightingFilterTreeView, traceColourMapView, controllerView };
	}

	public InductiveVisualMinerAnimationPanel getGraph() {
		return graphPanel;
	}

	public JComboBox<?> getMinerSelection() {
		return minerCombobox;
	}

	public JComboBox<Mode> getVisualisationModeSelector() {
		return modeSelection;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public IvMClassifierChooser getClassifiers() {
		return classifiersCombobox;
	}

	public JComboBox<?> getColourSelection() {
		return modeSelection;
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

	public JButton getSaveLogButton() {
		return saveLogButton;
	}

	public void setOnSelectionChanged(InputFunction<Selection> onSelectionChanged) {
		this.onSelectionChanged = onSelectionChanged;
	}

	public void setOnGraphDirectionChanged(Runnable onGraphDirectionChanged) {
		this.onGraphDirectionChanged = onGraphDirectionChanged;
	}

	public void setOnAnimationEnabledChanged(AnimationEnabledChangedListener onAnimationEnabledChanged) {
		this.onAnimationEnabledChanged = onAnimationEnabledChanged;
	}

	public TraceView getTraceView() {
		return traceView;
	}

	public JButton getTraceViewButton() {
		return traceViewButton;
	}

	public DataAnalysesView<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> getDataAnalysesView() {
		return dataAnalysisView;
	}

	public JButton getDataAnalysisViewButton() {
		return dataAnalysisViewButton;
	}

	public ControllerView<DataState> getControllerView() {
		return controllerView;
	}

	public EditModelView getEditModelView() {
		return editModelView;
	}

	public JButton getEditModelButton() {
		return editModelButton;
	}

	public HighlightingFiltersView getHighlightingFiltersView() {
		return highlightingFiltersView;
	}

	public IvMFilterTreeView<IvMTrace> getHighlightingFilterTreeView() {
		return highlightingFilterTreeView;
	}

	public JButton getHighlightingFiltersViewButton() {
		return highlightingFiltersViewButton;
	}

	public TraceColourMapView getTraceColourMapView() {
		return traceColourMapView;
	}

	public JButton getTraceColourMapViewButton() {
		return traceColourMapViewButton;
	}

	public JLabel getAnimationTimeLabel() {
		return animationTimeLabel;
	}

	public JLabel getMinerLabel() {
		return minerLabel;
	}

	public JLabel getClassifierLabel() {
		return classifierLabel;
	}

}