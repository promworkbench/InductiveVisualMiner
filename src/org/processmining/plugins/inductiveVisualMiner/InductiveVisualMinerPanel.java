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

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.BoundsPopupMenuListener;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.SelectionChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisView;
import org.processmining.plugins.inductiveVisualMiner.editModel.EditModelView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ControllerView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMClassifierChooser;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFiltersView;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsDeviations;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsQueueLengths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsService;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsSojourn;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsWaiting;
import org.processmining.plugins.inductiveVisualMiner.mode.ModeRelativePaths;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapView;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceView;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerPanel extends IvMPanel {

	private static final long serialVersionUID = -1078786029763735572L;

	private static final int sidePanelWidth = 240;
	private static final int lineHeight = 20;

	private static final Insets margins = new Insets(2, 0, 0, 0);

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
	private IvMClassifierChooser classifiersCombobox;
	//private final JLabel classifierSecondLabel;
	//private IvMClassifierChooser classifiersSecondCombobox;
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
	private final DataAnalysisView dataAnalysisView;
	private final JButton traceColourMapViewButton;
	private final TraceColourMapView traceColourMapView;
	private final JButton highlightingFiltersViewButton;
	private final HighlightingFiltersView highlightingFiltersView;
	private final ControllerView controllerView;

	private InputFunction<Selection> onSelectionChanged = null;
	private Runnable onGraphDirectionChanged = null;
	private AnimationEnabledChangedListener onAnimationEnabledChanged = null;

	@Deprecated
	public static InductiveVisualMinerPanel panel(final PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, boolean miningEnabled, ProMCanceller canceller) {
		return panel(context, state, miners, canceller);
	}

	@Deprecated
	public static InductiveVisualMinerPanel panel(final PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, ProMCanceller canceller) {
		Mode[] modes = new Mode[] { new ModePaths(), new ModePathsDeviations(), new ModePathsQueueLengths(),
				new ModePathsSojourn(), new ModePathsWaiting(), new ModePathsService(), new ModeRelativePaths() };
		return new InductiveVisualMinerPanel(context, state, miners, modes, canceller);
	}

	public static InductiveVisualMinerPanel panel(final PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, Mode[] modes, ProMCanceller canceller) {
		return new InductiveVisualMinerPanel(context, state, miners, modes, canceller);
	}

	private static final Image logo = Toolkit.getDefaultToolkit().getImage(InductiveVisualMinerPanel.class
			.getResource("/org/processmining/plugins/inductiveVisualMiner/inductive miner logo.png"));

	public static final String title = "visual Miner";

	private InductiveVisualMinerPanel(final PluginContext context, InductiveVisualMinerState state,
			VisualMinerWrapper[] miners, Mode[] modes, ProMCanceller canceller) {
		int gridy = 0;

		setLayout(new BorderLayout());

		setOpaque(false);

		//controls the margin on the left side of the settings panel
		Border leftBorder = new EmptyBorder(0, 5, 0, 0);

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
				pathsSlider = SlickerFactory.instance().createNiceDoubleSlider("paths", 0, 1.0, state.getPaths(),
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
				IvMDecorator.decorate(classifierLabel);
				classifierLabel.setBorder(leftBorder);
				GridBagConstraints cClassifierLabel = new GridBagConstraints();
				cClassifierLabel.gridx = 0;
				cClassifierLabel.gridy = gridy;
				cClassifierLabel.gridwidth = 1;
				cClassifierLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(classifierLabel, cClassifierLabel);

				classifiersCombobox = new IvMClassifierChooser(null, null, false);
				IvMDecorator.decorate(classifiersCombobox.getMultiComboBox());
				classifiersCombobox.setEnabled(false);
				GridBagConstraints cClassifiers = new GridBagConstraints();
				cClassifiers.gridx = 1;
				cClassifiers.gridy = gridy++;
				cClassifiers.gridwidth = 1;
				cClassifiers.insets = margins;
				cClassifiers.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(classifiersCombobox, cClassifiers);
			}

			//secondary classifiers
			/*
			 * { classifierSecondLabel = new JLabel("2nd Classifier");
			 * IvMDecorator.decorate(classifierSecondLabel);
			 * classifierSecondLabel.setBorder(leftBorder); GridBagConstraints
			 * cClassifierLabel = new GridBagConstraints();
			 * cClassifierLabel.gridx = 0; cClassifierLabel.gridy = gridy;
			 * cClassifierLabel.gridwidth = 1; cClassifierLabel.anchor =
			 * GridBagConstraints.WEST;
			 * otherSettingsPanel.add(classifierSecondLabel, cClassifierLabel);
			 * 
			 * classifiersSecondCombobox = new IvMClassifierChooser(null, null,
			 * false);
			 * IvMDecorator.decorate(classifiersSecondCombobox.getMultiComboBox(
			 * )); classifiersSecondCombobox.setEnabled(false);
			 * GridBagConstraints cClassifiers = new GridBagConstraints();
			 * cClassifiers.gridx = 1; cClassifiers.gridy = gridy++;
			 * cClassifiers.gridwidth = 1; cClassifiers.insets = margins;
			 * cClassifiers.fill = GridBagConstraints.HORIZONTAL;
			 * otherSettingsPanel.add(classifiersSecondCombobox, cClassifiers);
			 * }
			 */

			//pre-mining filters
			{
				preMiningFiltersView = new PreMiningFiltersView(this);
				preMiningFiltersButton = new JButton("pre-mining filters");
				IvMDecorator.decorate(preMiningFiltersButton);
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
				IvMDecorator.decorate(minerLabel);
				minerLabel.setBorder(leftBorder);
				GridBagConstraints cMinerLabel = new GridBagConstraints();
				cMinerLabel.gridx = 0;
				cMinerLabel.gridy = gridy;
				cMinerLabel.gridwidth = 1;
				cMinerLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(minerLabel, cMinerLabel);

				minerCombobox = new JComboBox<>(miners);
				IvMDecorator.decorate(minerCombobox);
				minerCombobox.addPopupMenuListener(new BoundsPopupMenuListener(true, false));
				minerCombobox.setFocusable(false);
				GridBagConstraints cMiners = new GridBagConstraints();
				cMiners.gridx = 1;
				cMiners.gridy = gridy++;
				cMiners.gridwidth = 1;
				cMiners.insets = margins;
				cMiners.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(minerCombobox, cMiners);
				minerCombobox.setSelectedItem(state.getMiner());
			}

			//edit model view
			{
				editModelView = new EditModelView(this);
				editModelButton = new JButton("edit model");
				IvMDecorator.decorate(editModelButton);
				GridBagConstraints cEditModelButton = new GridBagConstraints();
				cEditModelButton.gridx = 1;
				cEditModelButton.gridy = gridy++;
				cEditModelButton.gridwidth = 1;
				cEditModelButton.insets = margins;
				cEditModelButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(editModelButton, cEditModelButton);
			}

			{
				colourLabel = new JLabel("Show");
				IvMDecorator.decorate(colourLabel);
				colourLabel.setBorder(leftBorder);
				GridBagConstraints cColourLabel = new GridBagConstraints();
				cColourLabel.gridx = 0;
				cColourLabel.gridy = gridy;
				cColourLabel.gridwidth = 1;
				cColourLabel.anchor = GridBagConstraints.WEST;
				otherSettingsPanel.add(colourLabel, cColourLabel);

				colourSelection = new JComboBox<>(modes);
				IvMDecorator.decorate(colourSelection);
				colourSelection.addPopupMenuListener(new BoundsPopupMenuListener(true, false));
				colourSelection.setFocusable(false);
				GridBagConstraints ccolourSelection = new GridBagConstraints();
				ccolourSelection.gridx = 1;
				ccolourSelection.gridy = gridy++;
				ccolourSelection.gridwidth = 1;
				ccolourSelection.insets = margins;
				ccolourSelection.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(colourSelection, ccolourSelection);
			}

			//trace colouring view
			{
				traceColourMapView = new TraceColourMapView(this);
				traceColourMapViewButton = new JButton("trace colouring");
				IvMDecorator.decorate(traceColourMapViewButton);
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
				highlightingFiltersView = new HighlightingFiltersView(this);
				highlightingFiltersViewButton = new JButton("highlighting filters");
				IvMDecorator.decorate(highlightingFiltersViewButton);
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
				traceView = new TraceView(this);
				traceViewButton = new JButton("traces");
				IvMDecorator.decorate(traceViewButton);
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
				dataAnalysisView = new DataAnalysisView(this);
				dataAnalysisViewButton = new JButton("data analysis");
				IvMDecorator.decorate(dataAnalysisViewButton);
				GridBagConstraints cDataAnalysisViewButton = new GridBagConstraints();
				cDataAnalysisViewButton.gridx = 1;
				cDataAnalysisViewButton.gridy = gridy++;
				cDataAnalysisViewButton.gridwidth = 1;
				cDataAnalysisViewButton.insets = margins;
				cDataAnalysisViewButton.fill = GridBagConstraints.HORIZONTAL;
				otherSettingsPanel.add(dataAnalysisViewButton, cDataAnalysisViewButton);
			}

			//controller view
			{
				controllerView = new ControllerView(this);
			}

			//save log button
			{
				saveLogButton = new JButton("export log");
				IvMDecorator.decorate(saveLogButton);
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
				IvMDecorator.decorate(saveModelButton);
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
				IvMDecorator.decorate(saveImageButton);
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
			graphPanel = new InductiveVisualMinerAnimationPanel(canceller, state.isAnimationGlobalEnabled());
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

		//handle pre-mined tree case
		if (state.getPreMinedModel() != null) {
			activitiesSlider.setVisible(false);
			pathsSlider.setVisible(false);
			preMiningFiltersButton.setVisible(false);
			minerLabel.setVisible(false);
			minerCombobox.setVisible(false);
		}

		//handle pre-mined classifier case
		if (state.getPreMinedPerformanceClassifier() != null) {
			editModelButton.setVisible(false);
			classifierLabel.setVisible(false);
			classifiersCombobox.setVisible(false);
			//classifiersSecondCombobox.setVisible(false);
		}
	}

	//	@Override
	//	protected void paintComponent(Graphics grphcs) {
	//		Graphics2D g2d = (Graphics2D) grphcs;
	//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	//
	//		//GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, getHeight(), new Color(171,186,171));
	//		//GradientPaint gp = new GradientPaint(0, 0, new Color(109, 213, 237), 0, getHeight(), new Color(33, 147, 176));
	//		//GradientPaint gp = new GradientPaint(0, 0, new Color(75, 108, 183), 0, getHeight(), new Color(24, 40, 72));
	//		GradientPaint gp = new GradientPaint(0, 0, new Color(161, 207, 243), 0, getHeight(), new Color(84, 141, 184));
	//
	//		//GradientPaint gp = new GradientPaint(0, 0, getBackground(), 0, getHeight(), getBackground().darker());
	//
	//		g2d.setPaint(gp);
	//		g2d.fillRect(0, 0, getWidth(), getHeight());
	//
	//		super.paintComponent(grphcs);
	//	}

	public void removeNotify() {
		super.removeNotify();
		editModelView.setVisible(false);
		preMiningFiltersView.setVisible(false);
		traceView.setVisible(false);
		dataAnalysisView.setVisible(false);
		highlightingFiltersView.setVisible(false);
		traceColourMapView.setVisible(false);
		controllerView.setVisible(false);
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

	public IvMClassifierChooser getClassifiers() {
		return classifiersCombobox;
	}

	//	public IvMClassifierChooser getSecondClassifiers() {
	//		return classifiersSecondCombobox;
	//	}

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

	public DataAnalysisView getDataAnalysisView() {
		return dataAnalysisView;
	}

	public JButton getDataAnalysisViewButton() {
		return dataAnalysisViewButton;
	}

	public ControllerView getControllerView() {
		return controllerView;
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
}
