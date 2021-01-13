package org.processmining.plugins.inductiveVisualMiner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl03MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl05Mine;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl13FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl15Histogram;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl18DataAnalysisCohort;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkComputation;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.chain.OnException;
import org.processmining.plugins.inductiveVisualMiner.chain.OnStatus;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfigurationDefault;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis2HighlightingFilterHandler;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.HighlightingFilter2CohortAnalysisHandler;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAlignment;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAlignment.Type;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterAvi;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterDataAnalyses;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterModelStatistics;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterTraceData;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.UserStatus;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMHighlightingFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMPreMiningFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.popup.LogPopupListener;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupPopulator;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class InductiveVisualMinerController {

	private final InductiveVisualMinerPanel panel;
	private final DataState state;

	private final DataChain chain;
	private final PluginContext context;
	private final UserStatus userStatus;

	//preferences
	private static final Preferences preferences = Preferences.userRoot()
			.node("org.processmining.inductivevisualminer");
	public static final String playAnimationOnStartupKey = "playanimationonstartup";

	public InductiveVisualMinerController(final PluginContext context,
			final InductiveVisualMinerConfiguration configuration, final ProMCanceller canceller) {
		this.state = configuration.getState();
		state.setConfiguration(configuration);
		this.panel = configuration.getPanel();
		this.userStatus = new UserStatus();
		this.context = context;
		chain = configuration.getChain();

		//initialise gui handlers
		initGui(canceller, configuration);

		//set up the controller view
		chain.setOnChange(new Runnable() {
			public void run() {
				panel.getControllerView().pushCompleteChainLinks(chain);
			}
		});

		//set up exception handling
		chain.setOnException(new OnException() {
			public void onException(Exception e) {
				setStatus("- error - aborted -", 0);
			}
		});

		//set up status handling
		chain.setOnStatus(new OnStatus() {
			public void startComputation(DataChainLinkComputation chainLink) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setStatus(chainLink.getStatusBusyMessage(), chainLink.hashCode());
					}
				});
			}

			public void endComputation(DataChainLinkComputation chainLink) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setStatus(null, chainLink.hashCode());
					}
				});
			}
		});

		//start the chain
		chain.setObject(DataState.input_log, state.getXLog());
	}

	/**
	 * Given panel and state are ignored.
	 * 
	 * @param context
	 * @param panel
	 * @param state
	 * @param canceller
	 */
	@Deprecated
	public InductiveVisualMinerController(final PluginContext context, final InductiveVisualMinerPanel panel,
			final InductiveVisualMinerState state, ProMCanceller canceller) {
		this(context, new InductiveVisualMinerConfigurationDefault(state.getXLog(), canceller, context.getExecutor()),
				canceller);
	}

	private void initGui(final ProMCanceller canceller, InductiveVisualMinerConfiguration configuration) {

		//resize handler
		panel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//on resize, we have to resize the histogram as well
				state.setHistogramWidth((int) panel.getGraph().getControlsProgressLine().getWidth());
				chain.execute(Cl15Histogram.class);
			}
		});

		//noise filter slider
		panel.getPathsSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getPathsSlider().getSlider().getValueIsAdjusting()) {
					state.setPaths(panel.getPathsSlider().getValue());
					chain.execute(Cl05Mine.class);
				}

				//give the focus back to the graph panel
				panel.getGraph().requestFocus(true);
			}
		});

		//classifier slider
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setClassifier(panel.getClassifiers().getSelectedClassifier());

				chain.execute(Cl03MakeLog.class);
			}
		});

		//miner
		panel.getMinerSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(DataState.selected_miner, panel.getMinerSelection().getSelectedItem());
			}
		});

		//model editor
		panel.getEditModelView().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof IvMModel) {
					chain.setObject(DataState.model, e.getSource());
				}
			}
		});

		//activities filter
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					chain.setObject(DataState.selected_activities_threshold, panel.getActivitiesSlider().getValue());
				}
			}
		});

		//display mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(DataState.selected_visualisation_mode,
						panel.getColourModeSelection().getSelectedItem());
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Selection>() {
			public void call(Selection input) throws Exception {
				chain.setObject(DataState.selected_model_selection, input);
			}
		});

		//graph direction changed
		panel.setOnGraphDirectionChanged(new Runnable() {
			public void run() {
				chain.setObject(DataState.selected_graph_user_settings, panel.getGraph().getUserSettings());
			}
		});
		panel.getGraph().getUserSettings().setDirection(GraphDirection.leftRight);
		chain.setObject(DataState.selected_graph_user_settings, panel.getGraph().getUserSettings());

		//animation enabled/disabled
		panel.setOnAnimationEnabledChanged(new AnimationEnabledChangedListener() {
			public boolean animationEnabledChanged() {
				boolean enable = !state.hasObject(DataState.selected_animation_enabled);
				state.putObject(DataState.selected_animation_enabled, enable);
				preferences.putBoolean(playAnimationOnStartupKey, enable);

				if (!enable) {
					//animation gets disabled
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus(panel, "animation disabled", true);
					panel.repaint();
					state.removeObject(DataState.selected_animation_enabled);
					return false;
				} else {
					//animation gets enabled
					state.putObject(DataState.selected_animation_enabled, true);
					return true;
				}
			}
		});
		if (preferences.getBoolean(playAnimationOnStartupKey, true)) {
			state.putObject(DataState.selected_animation_enabled, true);
		}

		//set model export button
		panel.getSaveModelButton().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				//store the resulting Process tree or Petri net
				String name = XConceptExtension.instance().extractName((XLog) state.getObject(DataState.sorted_log));
				IvMModel model = (IvMModel) state.getObject(DataState.model);

				Object[] options;
				if (model.isTree()) {
					options = new Object[5];
					options[0] = "Petri net";
					options[1] = "Accepting Petri net";
					options[2] = "Expanded accepting Petri net";
					options[3] = "Process Tree";
					options[4] = "Efficient tree";
				} else {
					options = new Object[4];
					options[0] = "Petri net";
					options[1] = "Accepting Petri net";
					options[2] = "Expanded accepting Petri net";
					options[3] = "Directly follows model";
				}

				int n = JOptionPane.showOptionDialog(panel,
						"As what would you like to save the model?\nIt will become available in ProM.", "Save",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				switch (n) {
					case 0 :
						//store as Petri net
						ExportModel.exportPetrinet(context, model, name, canceller);
						break;
					case 1 :
						ExportModel.exportAcceptingPetriNet(context, model, name, canceller);
						break;
					case 2 :
						ExportModel.exportExpandedAcceptingPetriNet(context, model, name, canceller);
						break;
					case 3 :
						if (model.isTree()) {
							ExportModel.exportProcessTree(context, model.getTree().getDTree(), name);
						} else {
							ExportModel.exportDirectlyFollowsModel(context, model, name);
						}
						break;
					case 4 :
						ExportModel.exportEfficientTree(context, model.getTree(), name);
						break;
				}
			}
		});
		panel.getSaveModelButton().setEnabled(false);

		//add animation and statistics to export
		panel.getGraph().setGetExporters(new GetExporters() {
			public List<Exporter> getExporters(List<Exporter> exporters) {
				exporters.add(new ExporterDataAnalyses(state));
				if (state.getIvMLogFiltered() != null && state.isAlignmentReady()
						&& state.getIvMAttributesInfo() != null) {
					exporters.add(new ExporterTraceData(state));
				}
				if (state.isPerformanceReady()) {
					exporters.add(new ExporterModelStatistics(state.getConfiguration()));
				}
				if (panel.getGraph().isAnimationEnabled()) {
					exporters.add(new ExporterAvi(state));
				}
				return exporters;
			}
		});

		//set image/animation export button
		panel.getSaveImageButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getGraph().exportView();
			}
		});

		//set alignment export button
		panel.getSaveLogButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String name = XConceptExtension.instance().extractName(state.getSortedXLog());
				IvMLog log = state.getIvMLogFiltered();
				IvMModel model = state.getModel();
				XEventClassifier classifier = state.getActivityClassifier();

				Object[] options = { "Just the log (log & sync moves)", "Aligned log (all moves)",
						"Model view (model & sync moves)" };
				int n = JOptionPane.showOptionDialog(panel,
						"Which filtered view of the log would you like to export?\nIt will become available as an event log in ProM.",
						"Export Log", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);

				switch (n) {
					case 0 :
						ExportAlignment.exportAlignment(context, log, model, name, Type.logView);
						break;
					case 1 :
						//ExportAlignment.exportAlignment(context, log, model, name, Type.both);
						InductiveVisualMinerAlignment alignment = ExportAlignment.exportAlignment(log, model,
								classifier);
						context.getProvidedObjectManager().createProvidedObject(name + " (alignment)", alignment,
								InductiveVisualMinerAlignment.class, context);
						if (context instanceof UIPluginContext) {
							((UIPluginContext) context).getGlobalContext().getResourceManager()
									.getResourceForInstance(alignment).setFavorite(true);
						}
						break;
					case 2 :
						ExportAlignment.exportAlignment(context, log, model, name, Type.modelView);
						break;
				}
			}
		});
		panel.getSaveLogButton().setEnabled(false);

		//listen to ctrl c to show the controller view
		{
			panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "showControllerView"); // - key
			panel.getActionMap().put("showControllerView", new AbstractAction() {
				private static final long serialVersionUID = 1727407514105090094L;

				public void actionPerformed(ActionEvent arg0) {
					panel.getControllerView().setVisible(true);
				}

			});
		}

		//set pre-mining filters button
		panel.getPreMiningFiltersButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getPreMiningFiltersView().enableAndShow();
			}
		});
		panel.getPreMiningFiltersView().setOnUpdate(new Runnable() {
			public void run() {
				chain.execute(Cl04FilterLogOnActivities.class);
			}
		});
		state.setPreMiningFiltersController(new IvMPreMiningFiltersController(configuration.getPreMiningFilters(),
				panel.getPreMiningFiltersView()));

		//set edit model button
		panel.getEditModelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getEditModelView().enableAndShow();
			}
		});

		//set trace view button
		panel.getTraceViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getTraceView().enableAndShow();
			}
		});

		//set data analysis button
		{
			panel.getDataAnalysisViewButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					panel.getDataAnalysesView().enableAndShow();
				}
			});

			//link cohort data analysis view and cohort highlighting filter
			panel.getHighlightingFiltersView()
					.setHighlightingFilter2CohortAnalysisHandler(new HighlightingFilter2CohortAnalysisHandler() {
						public void showCohortAnalysis() {
							panel.getDataAnalysesView().enableAndShow();
							panel.getDataAnalysesView().showAnalysis(CohortAnalysisTableFactory.name);
						}

						public void setEnabled(boolean enabled) {
							//do nothing if the user disables the cohort
						}
					});
			panel.getDataAnalysesView()
					.setCohortAnalysis2HighlightingFilterHandler(new CohortAnalysis2HighlightingFilterHandler() {
						public void setSelectedCohort(Cohort cohort, boolean highlightInCohort) {
							panel.getHighlightingFiltersView().setHighlightingFilterSelectedCohort(cohort,
									highlightInCohort);
						}
					});

			//link cohort data analysis view switch and cohort computations
			panel.getDataAnalysesView().setSwitcherEnabled(CohortAnalysisTableFactory.name,
					state.isCohortAnalysisEnabled());
			panel.getDataAnalysesView().addSwitcherListener(CohortAnalysisTableFactory.name, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();
					if (selected) {
						//start the computation
						state.setCohortAnalysisEnabled(true);
						chain.execute(Cl18DataAnalysisCohort.class);
						panel.getDataAnalysesView().setSwitcherMessage(CohortAnalysisTableFactory.name,
								"Compute " + CohortAnalysisTableFactory.name + " [computing..]");
					} else {
						//stop the computation
						/*
						 * It seems counter-intuitive, but we already have means
						 * in place to stop running computations. That is, if we
						 * start a new one [which will not compute anything due
						 * the flag set], the old one will be stopped.
						 * automatically.
						 */
						state.setCohortAnalysisEnabled(false);
						chain.execute(Cl18DataAnalysisCohort.class);
						panel.getDataAnalysesView().setSwitcherMessage(CohortAnalysisTableFactory.name,
								"Compute " + CohortAnalysisTableFactory.name);
					}
				}
			});
		}

		//set trace colouring button
		{
			panel.getTraceColourMapViewButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					panel.getTraceColourMapView().enableAndShow();
				}
			});
			TraceColourMap traceColourMap = new TraceColourMapFixed(RendererFactory.defaultTokenFillColour);
			state.putObject(DataState.trace_colour_map, traceColourMap);
			panel.getGraph().setTraceColourMap(traceColourMap);
			panel.getTraceColourMapView().setOnUpdate(new Function<TraceColourMapSettings, Object>() {
				public Object call(TraceColourMapSettings input) throws Exception {
					chain.setObject(DataState.trace_colour_map_settings, input);
					return null;
				}
			});
		}

		//set highlighting filters button
		{
			panel.getHighlightingFiltersViewButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					panel.getHighlightingFiltersView().enableAndShow();
				}
			});
			panel.getHighlightingFiltersView().setOnUpdate(new Runnable() {
				public void run() {
					chain.execute(Cl13FilterNodeSelection.class);
				}
			});
			state.putObject(DataState.highlighting_filters_controller, new IvMHighlightingFiltersController(
					configuration.getHighlightingFilters(), panel.getHighlightingFiltersView()));
		}

		//set mouse-in-out node updater
		panel.getGraph().addMouseInElementsChangedListener(new MouseInElementsChangedListener<DotElement>() {
			public void mouseInElementsChanged(Set<DotElement> mouseInElements) {
				try {
					PopupPopulator.updatePopup(panel, state);
				} catch (UnknownTreeNodeException e) {
					e.printStackTrace();
					panel.getGraph().setShowPopup(false, 10);
				}
				panel.repaint();
			}
		});

		//set log popup handler
		if (!state.getConfiguration().getPopupItemsLog().isEmpty()) {
			panel.getGraph().addLogPopupListener(new LogPopupListener() {
				public void isMouseInButton(boolean isIn) {
					PopupPopulator.updatePopup(panel, state);
					panel.repaint();
				}
			});
		}

		//set animation time updater
		panel.getGraph().setAnimationTimeChangedListener(new AnimationTimeChangedListener() {
			public void timeStepTaken(double userTime) {
				if (panel.getGraph().isAnimationEnabled()) {
					long logTime = Math.round(state.getAnimationScaler().userTime2LogTime(userTime));
					if (state.getAnimationScaler().isCorrectTime()) {
						setAnimationStatus(panel, ResourceTimeUtils.timeToString(logTime), true);
					} else {
						setAnimationStatus(panel, "random", true);
					}

					//draw queues
					if (state.getMode().isUpdateWithTimeStep(state)) {
						state.getVisualisationData().setTime(logTime);
						try {
							updateHighlighting(panel, state);
						} catch (UnknownTreeNodeException e) {
							e.printStackTrace();
						}
						panel.getTraceView().repaint();
					}
				}
			}
		});
	}

	public static void makeElementsSelectable(ProcessTreeVisualisationInfo info, InductiveVisualMinerPanel panel,
			Selection selection) {
		for (LocalDotNode dotNode : info.getAllActivityNodes()) {
			panel.makeNodeSelectable(dotNode, selection.isSelected(dotNode));
		}
		for (LocalDotEdge logMoveEdge : info.getAllLogMoveEdges()) {
			panel.makeEdgeSelectable(logMoveEdge, selection.isSelected(logMoveEdge));
		}
		for (LocalDotEdge modelMoveEdge : info.getAllModelMoveEdges()) {
			panel.makeEdgeSelectable(modelMoveEdge, selection.isSelected(modelMoveEdge));
		}
		for (LocalDotEdge edge : info.getAllModelEdges()) {
			panel.makeEdgeSelectable(edge, selection.isSelected(edge));
		}
	}

	/**
	 * Sets the status message of number. The status message stays in view until
	 * it is reset using NULL for that number.
	 * 
	 * @param message
	 * @param number
	 */
	public void setStatus(String message, int number) {
		userStatus.setStatus(message, number);
		panel.getStatusLabel().setText(userStatus.getText());
		panel.getStatusLabel().repaint();
	}

	public static void setAnimationStatus(InductiveVisualMinerPanel panel, String s, boolean isTime) {
		if (isTime) {
			panel.getAnimationTimeLabel().setFont(IvMDecorator.fontMonoSpace);
			panel.getAnimationTimeLabel().setText("time: " + s);
		} else {
			panel.getAnimationTimeLabel().setFont(panel.getStatusLabel().getFont());
			panel.getAnimationTimeLabel().setText(s);
		}
	}

	/**
	 * update the highlighting
	 * 
	 * @throws UnknownTreeNodeException
	 */
	public static void updateHighlighting(InductiveVisualMinerPanel panel, InductiveVisualMinerState state)
			throws UnknownTreeNodeException {
		TraceViewEventColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(
				panel.getGraph().getSVG(), state.getVisualisationInfo(), state.getModel(), state.getVisualisationData(),
				state.getMode().getVisualisationParameters(state));
		colourMap.setSelectedNodes(state.getSelection());
		panel.getTraceView().setEventColourMap(colourMap);
	}

	public static void debug(Object s) {
		System.out.println(s);
	}

	public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	public InductiveVisualMinerState getState() {
		return state;
	}
}
