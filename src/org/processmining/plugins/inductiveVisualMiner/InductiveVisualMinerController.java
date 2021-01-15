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
import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl13FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkComputation;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGui;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.chain.OnException;
import org.processmining.plugins.inductiveVisualMiner.chain.OnStatus;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfigurationDefault;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis2HighlightingFilterHandler;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.HighlightingFilter2CohortAnalysisHandler;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.LogAttributeAnalysisTableFactory;
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
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.LogPopupListener;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupPopulator;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.Miner;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerController {

	private final InductiveVisualMinerPanel panel;
	private final DataState state;
	private final InductiveVisualMinerConfiguration configuration;
	private final DataChain chain;
	private final PluginContext context;
	private final UserStatus userStatus;

	private DataChainLinkGui updatePopups;

	//preferences
	private static final Preferences preferences = Preferences.userRoot()
			.node("org.processmining.inductivevisualminer");
	public static final String playAnimationOnStartupKey = "playanimationonstartup";

	public InductiveVisualMinerController(final PluginContext context,
			final InductiveVisualMinerConfiguration configuration, final ProMCanceller canceller) {
		this.state = configuration.getState();
		state.setConfiguration(configuration);
		this.configuration = configuration;
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
		chain.setObject(IvMObject.input_log, state.getXLog());
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

		initGuiPopups();

		//resize handler
		panel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//on resize, we have to resize the histogram as well
				chain.setObject(IvMObject.histogram_width, (int) panel.getGraph().getControlsProgressLine().getWidth());
			}
		});
		chain.setObject(IvMObject.histogram_width, (int) panel.getGraph().getControlsProgressLine().getWidth());

		//classifier chooser
		initGuiClassifiers();

		initGuiMiner();

		//model editor
		panel.getEditModelView().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof IvMModel) {
					chain.setObject(IvMObject.model, (IvMModel) e.getSource());
				}
			}
		});

		//display mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(IvMObject.selected_visualisation_mode,
						(Mode) panel.getColourModeSelection().getSelectedItem());
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Selection>() {
			public void call(Selection input) throws Exception {
				chain.setObject(IvMObject.selected_model_selection, input);
			}
		});

		//graph direction changed
		panel.setOnGraphDirectionChanged(new Runnable() {
			public void run() {
				chain.setObject(IvMObject.selected_graph_user_settings, panel.getGraph().getUserSettings());
			}
		});
		panel.getGraph().getUserSettings().setDirection(GraphDirection.leftRight);
		chain.setObject(IvMObject.selected_graph_user_settings, panel.getGraph().getUserSettings());

		//animation enabled/disabled
		panel.setOnAnimationEnabledChanged(new AnimationEnabledChangedListener() {
			public boolean animationEnabledChanged() {
				boolean enable = !state.hasObject(IvMObject.selected_animation_enabled);
				chain.setObject(IvMObject.selected_animation_enabled, enable);
				preferences.putBoolean(playAnimationOnStartupKey, enable);

				if (!enable) {
					//animation gets disabled
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus(panel, "animation disabled", true);
					panel.repaint();
					return false;
				} else {
					//animation gets enabled
					return true;
				}
			}
		});
		if (preferences.getBoolean(playAnimationOnStartupKey, true)) {
			state.putObject(IvMObject.selected_animation_enabled, true);
		}

		//set model export button
		panel.getSaveModelButton().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				//store the resulting Process tree or Petri net
				String name = XConceptExtension.instance().extractName(state.getObject(IvMObject.sorted_log));
				IvMModel model = state.getObject(IvMObject.model);

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
					chain.getOnChange().run();
				}

			});
		}

		//set pre-mining filters button
		initGuiPreMiningFilters();

		//set edit model button
		panel.getEditModelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getEditModelView().enableAndShow();
			}
		});

		//set trace view button
		initGuiTraceView();

		//set data analyses
		{
			initGuiDataAnalyses();

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
			chain.setObject(IvMObject.selected_cohort_analysis_enabled, false);
			panel.getDataAnalysesView().addSwitcherListener(CohortAnalysisTableFactory.name, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();
					if (selected) {
						//start the computation
						chain.setObject(IvMObject.selected_cohort_analysis_enabled, false);
						panel.getDataAnalysesView().setSwitcherMessage(CohortAnalysisTableFactory.name,
								"Compute " + CohortAnalysisTableFactory.name + " [computing..]");
					} else {
						//stop the computation
						/*
						 * It seems counter-intuitive, but we already have means
						 * in place to stop running computations. That is, if we
						 * start a new one [which will not compute anything due
						 * the flag set], the old one will be stopped
						 * automatically.
						 */
						chain.setObject(IvMObject.selected_cohort_analysis_enabled, false);
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
			state.putObject(IvMObject.trace_colour_map, traceColourMap);
			panel.getGraph().setTraceColourMap(traceColourMap);
			panel.getTraceColourMapView().setOnUpdate(new Function<TraceColourMapSettings, Object>() {
				public Object call(TraceColourMapSettings input) throws Exception {
					chain.setObject(IvMObject.trace_colour_map_settings, input);
					return null;
				}
			});
		}

		//set highlighting filters button
		initGuiHighlightingFilters();

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

	private void initGuiMiner() {
		//miner
		chain.setObject(IvMObject.selected_miner, new Miner());
		panel.getMinerSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(IvMObject.selected_miner,
						(VisualMinerWrapper) panel.getMinerSelection().getSelectedItem());
			}
		});

		//noise threshold
		chain.setObject(IvMObject.selected_noise_threshold, 0.8);
		panel.getPathsSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getPathsSlider().getSlider().getValueIsAdjusting()) {
					chain.setObject(IvMObject.selected_noise_threshold, panel.getPathsSlider().getValue());
				}
			}
		});

		//model-related buttons
		chain.register(new DataChainLinkGui() {

			public String getName() {
				return "enable model-related buttons";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.model };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IvMModel model = inputs.get(IvMObject.model);

				panel.getSaveModelButton().setEnabled(true);
				panel.getEditModelView().setModel(model);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getSaveModelButton().setEnabled(false);
				panel.getEditModelView().setMessage("Mining tree...");
			}
		});

		//layout
		chain.register(new DataChainLinkGui() {
			public String getName() {
				return "model dot";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.graph_dot, IvMObject.graph_svg };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				Dot dot = inputs.get(IvMObject.graph_dot);
				SVGDiagram svg = inputs.get(IvMObject.graph_svg);

				panel.getGraph().changeDot(dot, svg, true);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				Dot dot = InductiveVisualMinerAnimationPanel.getSplashScreen();
				panel.getGraph().changeDot(dot, true);
			}
		});
	}

	public void initGuiTraceView() {
		panel.getTraceViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getTraceView().enableAndShow();
			}
		});

		chain.register(new DataChainLinkGui() {

			public String getName() {
				return "trace view (IMLog)";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.imlog, IvMObject.trace_colour_map };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IMLog imLog = inputs.get(IvMObject.imlog);
				TraceColourMap traceColourMap = inputs.get(IvMObject.trace_colour_map);

				panel.getTraceView().set(imLog, traceColourMap);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getTraceView().set(null, null);
			}
		});
	}

	public void initGuiDataAnalyses() {
		panel.getDataAnalysisViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getDataAnalysesView().enableAndShow();
			}
		});

		chain.register(new DataChainLinkGui() {
			public String getName() {
				return "update log data analysis";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.data_analysis_log };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				panel.getDataAnalysesView().setData(LogAttributeAnalysisTableFactory.name, state);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getDataAnalysesView().invalidate(LogAttributeAnalysisTableFactory.name);
			}
		});
	}

	private void initGuiClassifiers() {
		//update data on classifiers
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(IvMObject.selected_classifier, panel.getClassifiers().getSelectedClassifier());
			}
		});

		//update classifiers on data
		chain.register(new DataChainLinkGui() {
			public String getName() {
				return "set classifiers";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.classifiers };
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getClassifiers().setEnabled(false);
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				AttributeClassifier[] classifiers = inputs.get(IvMObject.classifiers);
				panel.getClassifiers().setEnabled(true);
				panel.getClassifiers().replaceClassifiers(classifiers);
			}
		});
	}

	public void initGuiPopups() {
		updatePopups = new DataChainLinkGui() {

			public String getName() {
				return "update popup (basic)";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.model, IvMObject.graph_visualisation_info,
						IvMObject.aligned_log_info_filtered };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IvMModel model = inputs.get(IvMObject.model);
				ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info);
				IvMLogInfo ivmLogInfoFiltered = inputs.get(IvMObject.aligned_log_info_filtered);

				PopupPopulator.updatePopup(state, configuration, panel, model, visualisationInfo, ivmLogInfoFiltered);
				panel.repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				PopupPopulator.updatePopup(state, configuration, panel, null, null, null);
				panel.repaint();
			}
		};
		chain.register(updatePopups);

		//set mouse-in-out node updater
		panel.getGraph().addMouseInElementsChangedListener(new MouseInElementsChangedListener<DotElement>() {
			public void mouseInElementsChanged(Set<DotElement> mouseInElements) {
				chain.executeLink(updatePopups);
			}
		});

		//set log popup handler
		if (!configuration.getPopupItemsLog().isEmpty()) {
			panel.getGraph().addLogPopupListener(new LogPopupListener() {
				public void isMouseInButton(boolean isIn) {
					chain.executeLink(updatePopups);
				}
			});
		}
	}

	public void initGuiPreMiningFilters() {
		chain.setObject(IvMObject.selected_activities_threshold, 1.0);
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					chain.setObject(IvMObject.selected_activities_threshold, panel.getActivitiesSlider().getValue());
				}
			}
		});

		chain.setObject(IvMObject.controller_premining_filters, new IvMPreMiningFiltersController(
				configuration.getPreMiningFilters(), panel.getPreMiningFiltersView()));

		panel.getPreMiningFiltersButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getPreMiningFiltersView().enableAndShow();
			}
		});

		panel.getPreMiningFiltersView().setOnUpdate(new Runnable() {
			public void run() {
				chain.executeLink(Cl04FilterLogOnActivities.class);
			}
		});

		//initialise filters
		chain.register(new DataChainLinkGui() {

			public String getName() {
				return "initialise pre-mining filters";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.attributes_info, IvMObject.controller_premining_filters };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				AttributesInfo attributesInfo = inputs.get(IvMObject.attributes_info);
				IvMPreMiningFiltersController controller = inputs.get(IvMObject.controller_premining_filters);

				controller.setAttributesInfo(attributesInfo);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//TODO: no action taken?
			}
		});
	}

	public void initGuiHighlightingFilters() {

		panel.getHighlightingFiltersViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getHighlightingFiltersView().enableAndShow();
			}
		});
		panel.getHighlightingFiltersView().setOnUpdate(new Runnable() {
			public void run() {
				chain.executeLink(Cl13FilterNodeSelection.class);
			}
		});
		state.putObject(IvMObject.controller_highlighting_filters, new IvMHighlightingFiltersController(
				configuration.getHighlightingFilters(), panel.getHighlightingFiltersView()));

		//filtering description
		chain.register(new DataChainLinkGui() {
			public String getName() {
				return "selection description";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.selected_model_selection,
						IvMObject.controller_highlighting_filters, IvMObject.model };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				Selection selection = inputs.get(IvMObject.selected_model_selection);
				IvMHighlightingFiltersController controller = inputs.get(IvMObject.controller_highlighting_filters);
				IvMModel model = inputs.get(IvMObject.model);

				HighlightingFiltersView.updateSelectionDescription(panel, selection, controller, model);
				panel.repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				HighlightingFiltersView.updateSelectionDescription(panel, null, null, null);
				panel.repaint();
			}
		});

		//tell trace view the colour map and the selection
		chain.register(new DataChainLinkGui() {

			public String getName() {
				return "trace colour map";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered,
						IvMObject.selected_model_selection };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IvMModel model = inputs.get(IvMObject.model);
				IvMLogFilteredImpl ivmLogFiltered = inputs.get(IvMObject.aligned_log_filtered);
				Selection selection = inputs.get(IvMObject.selected_model_selection);
				TraceColourMap traceColourMap = inputs.get(IvMObject.trace_colour_map);

				panel.getTraceView().set(model, ivmLogFiltered, selection, traceColourMap);
				panel.getTraceView().repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//TODO: no action necessary?
			}
		});

		//filtered log to animation
		chain.register(new DataChainLinkGui() {

			public String getName() {
				return "filtered log to animation";
			}

			public IvMObject<?>[] getInputNames() {
				return new IvMObject<?>[] { IvMObject.aligned_log_filtered };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IvMLogFilteredImpl ivmLog = inputs.get(IvMObject.aligned_log_filtered);

				panel.getGraph().setFilteredLog(ivmLog);

				panel.getGraph().repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getGraph().setFilteredLog(null);
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
