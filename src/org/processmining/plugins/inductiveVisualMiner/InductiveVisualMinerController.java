package org.processmining.plugins.inductiveVisualMiner;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl13FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkComputation;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGui;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainLinkGuiAbstract;
import org.processmining.plugins.inductiveVisualMiner.chain.FutureImpl;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.chain.OnException;
import org.processmining.plugins.inductiveVisualMiner.chain.OnStatus;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfigurationDefault;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisController;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis2HighlightingFilterHandler;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.HighlightingFilter2CohortAnalysisHandler;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAlignment;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAlignment.Type;
import org.processmining.plugins.inductiveVisualMiner.export.ExportController;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
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
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.popup.LogPopupListener;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupController;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
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
	private final InductiveVisualMinerConfiguration configuration;
	private final DataChain chain;
	private final PluginContext context;
	private final UserStatus userStatus;

	private PopupController popupController;

	//the following fields are time critical and should not go via the state
	private Scaler animationScaler = null;
	private Mode animationMode = null;
	private boolean animationEnabled;
	private AlignedLogVisualisationData animationVisualisationData = null;

	//preferences
	private static final Preferences preferences = Preferences.userRoot()
			.node("org.processmining.inductivevisualminer");
	public static final String playAnimationOnStartupKey = "playanimationonstartup";

	public InductiveVisualMinerController(final PluginContext context,
			final InductiveVisualMinerConfiguration configuration, final XLog log, final ProMCanceller canceller) {
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
		chain.setFixedObject(IvMObject.input_log, log);
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
		this(context, new InductiveVisualMinerConfigurationDefault(canceller, context.getExecutor()), state.getXLog(),
				canceller);
	}

	protected void initGui(final ProMCanceller canceller, InductiveVisualMinerConfiguration configuration) {

		initGuiPopups();

		initGuiClassifiers();

		initGuiMiner();

		initGuiAlignment();

		initGuiAnimation();

		initGuiHistogram();

		ExportController.initialise(chain, configuration, panel);

		//model editor
		panel.getEditModelView().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof IvMModel) {
					chain.setObject(IvMObject.model, (IvMModel) e.getSource());
				}
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
		setObject(IvMObject.selected_graph_user_settings, panel.getGraph().getUserSettings());

		//animation enabled/disabled
		animationEnabled = preferences.getBoolean(playAnimationOnStartupKey, true);
		setObject(IvMObject.selected_animation_enabled, animationEnabled);
		panel.getGraph().setAnimationGlobalEnabled(animationEnabled);
		panel.setOnAnimationEnabledChanged(new AnimationEnabledChangedListener() {
			public boolean animationEnabledChanged() {
				animationEnabled = !animationEnabled;
				chain.setObject(IvMObject.selected_animation_enabled, animationEnabled);
				preferences.putBoolean(playAnimationOnStartupKey, animationEnabled);
				if (!animationEnabled) {
					//animation gets disabled
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus(panel, "animation disabled", true);
					panel.repaint();
				}
				return animationEnabled;
			}
		});

		//set model export button
		panel.getSaveModelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					FutureImpl f = chain.getObjectValues(IvMObject.input_log, IvMObject.model);
					IvMObjectValues inputs = f.get();

					if (f.isAllObjectsPresent()) {
						//store the resulting Process tree or Petri net
						String name = XConceptExtension.instance().extractName(inputs.get(IvMObject.sorted_log));
						IvMModel model = inputs.get(IvMObject.model);

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
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);

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
				} catch (HeadlessException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		panel.getSaveModelButton().setEnabled(false);

		//set image/animation export button
		panel.getSaveImageButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getGraph().exportView();
			}
		});

		//set alignment export button
		panel.getSaveLogButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				IvMObjectValues inputs;
				try {
					inputs = chain.getObjectValues(IvMObject.input_log, IvMObject.aligned_log_filtered, IvMObject.model,
							IvMObject.selected_classifier).get();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					return;
				}

				String name = XConceptExtension.instance().extractName(inputs.get(IvMObject.input_log));
				IvMLog log = inputs.get(IvMObject.aligned_log_filtered);
				IvMModel model = inputs.get(IvMObject.model);
				XEventClassifier classifier = AttributeClassifiers
						.constructClassifier(inputs.get(IvMObject.selected_classifier));

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

		//set graph handlers
		initGuiGraph();

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
			setObject(IvMObject.selected_cohort_analysis_enabled, false);
			panel.getDataAnalysesView().addSwitcherListener(CohortAnalysisTableFactory.name, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean selected = ((AbstractButton) e.getSource()).getModel().isSelected();
					if (selected) {
						//start the computation
						chain.setObject(IvMObject.selected_cohort_analysis_enabled, true);
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

		initGuiTraceColouring();

		//set highlighting filters button
		initGuiHighlightingFilters();

	}

	protected void initGuiTraceColouring() {
		//set trace colouring button to open the trace colouring window
		panel.getTraceColourMapViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getTraceColourMapView().enableAndShow();
			}
		});

		//
		//		TraceColourMap traceColourMap = new TraceColourMapFixed(RendererFactory.defaultTokenFillColour);
		//		state.putObject(IvMObject.trace_colour_map, traceColourMap);
		//panel.getGraph().setTraceColourMap(traceColourMap);
		panel.getTraceColourMapView().setOnUpdate(new Function<TraceColourMapSettings, Object>() {
			public Object call(TraceColourMapSettings input) throws Exception {
				chain.setObject(IvMObject.trace_colour_map_settings, input);
				return null;
			}
		});
	}

	protected void initGuiMiner() {
		//miner
		setObject(IvMObject.selected_miner, new Miner());
		panel.getMinerSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(IvMObject.selected_miner,
						(VisualMinerWrapper) panel.getMinerSelection().getSelectedItem());
			}
		});

		//noise threshold
		setObject(IvMObject.selected_noise_threshold, 0.8);
		panel.getPathsSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getPathsSlider().getSlider().getValueIsAdjusting()) {
					chain.setObject(IvMObject.selected_noise_threshold, panel.getPathsSlider().getValue());
				}
			}
		});

		//model-related buttons
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "enable model-related buttons";
			}

			public IvMObject<?>[] createInputObjects() {
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
	}

	protected void initGuiGraph() {
		//update layout
		chain.register(new DataChainLinkGuiAbstract() {
			public String getName() {
				return "model dot";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.graph_dot, IvMObject.graph_svg };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				Dot dot = inputs.get(IvMObject.graph_dot);
				SVGDiagram svg = inputs.get(IvMObject.graph_svg);

				panel.getGraph().changeDot(dot, svg, true);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//here, we could put the graph on blank, but that is annoying
				//				Dot dot = new Dot();
				//				DotNode dotNode = dot.addNode("...");
				//				dotNode.setOption("shape", "plaintext");
				//				panel.getGraph().changeDot(dot, true);
			}
		});

		//mode switch
		setObject(IvMObject.selected_visualisation_mode, new ModePaths());
		panel.getVisualisationModeSelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(IvMObject.selected_visualisation_mode,
						(Mode) panel.getVisualisationModeSelector().getSelectedItem());
			}
		});

		//register the requirements of the modes
		registerModeRequests();

		//trace view event colour map & model node selection
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "trace view event colour map";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.trace_view_event_colour_map, IvMObject.graph_visualisation_info };
			}

			public IvMObject<?>[] createNonTriggerObjects() {
				return new IvMObject<?>[] { IvMObject.selected_model_selection };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				TraceViewEventColourMap traceViewEventColourMap = inputs.get(IvMObject.trace_view_event_colour_map);

				panel.getTraceView().setEventColourMap(traceViewEventColourMap);

				/**
				 * We don't want to be triggered by a change in selection, so we
				 * get it as a non-trigger. This is a bit risky, as we have to
				 * assume it is always available.
				 */
				assert inputs.has(IvMObject.selected_model_selection);
				Selection selection = inputs.get(IvMObject.selected_model_selection);
				ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info);
				makeElementsSelectable(visualisationInfo, panel, selection);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				// TODO no action taken?
			}
		});
	}

	protected void initGuiTraceView() {
		panel.getTraceViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getTraceView().enableAndShow();
			}
		});

		//trace view (IM log)
		DataChainLinkGui traceViewIMLog = new DataChainLinkGuiAbstract() {

			public String getName() {
				return "trace view (IMLog)";
			}

			public IvMObject<?>[] createInputObjects() {
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
		};
		chain.register(traceViewIMLog);

		//chain.register (aligned log)
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "trace view (IvMLog)";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered,
						IvMObject.selected_model_selection, IvMObject.trace_colour_map };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IvMModel model = inputs.get(IvMObject.model);
				IvMLogFilteredImpl aLog = inputs.get(IvMObject.aligned_log_filtered);
				Selection selection = inputs.get(IvMObject.selected_model_selection);
				TraceColourMap traceColourMap = inputs.get(IvMObject.trace_colour_map);

				panel.getTraceView().set(model, aLog, selection, traceColourMap);
				panel.getTraceView().repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//do nothing to prevent the IM log to be overruled
			}
		});

		chain.register(new DataChainLinkGuiAbstract() {
			public String getName() {
				return "trace colour map";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.trace_colour_map };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				TraceColourMap traceColourMap = inputs.get(IvMObject.trace_colour_map);
				panel.getGraph().setTraceColourMap(traceColourMap);
				panel.repaint();

				panel.getTraceView().setTraceColourMap(traceColourMap);
				panel.getTraceView().repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {

			}
		});
	}

	protected void initGuiDataAnalyses() {
		panel.getDataAnalysisViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getDataAnalysesView().enableAndShow();
			}
		});

		DataAnalysisController.initialise(configuration, chain);
	}

	protected void initGuiClassifiers() {
		//get the selected classifier to the gui
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "classifier to gui";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.classifier_for_gui };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				AttributeClassifier[] value = inputs.get(IvMObject.classifier_for_gui);
				panel.getClassifiers().getMultiComboBox().setSelectedItems(value);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//no action necessary (combobox will be disabled until new classifiers are computed)
			}
		});

		//update data on classifiers
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chain.setObject(IvMObject.selected_classifier, panel.getClassifiers().getSelectedClassifier());
			}
		});

		//update classifiers on data
		chain.register(new DataChainLinkGuiAbstract() {
			public String getName() {
				return "set classifiers";
			}

			public IvMObject<?>[] createInputObjects() {
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

	protected void initGuiPopups() {
		popupController = new PopupController(chain, configuration);

		//set mouse-in-out node updater
		panel.getGraph().addMouseInElementsChangedListener(new MouseInElementsChangedListener<DotElement>() {
			public void mouseInElementsChanged(Set<DotElement> mouseInElements) {
				popupController.showPopup(panel, chain);
				panel.getGraph().repaint();
			}
		});

		//set log popup handler
		if (!configuration.getPopupItemsLog().isEmpty()) {
			panel.getGraph().addLogPopupListener(new LogPopupListener() {
				public void isMouseInButton(boolean isIn) {
					popupController.showPopup(panel, chain);
					panel.getGraph().repaint();
				}
			});
		}
	}

	protected void initGuiPreMiningFilters() {
		setObject(IvMObject.selected_activities_threshold, 1.0);
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					chain.setObject(IvMObject.selected_activities_threshold, panel.getActivitiesSlider().getValue());
				}
			}
		});

		setObject(IvMObject.controller_premining_filters, new IvMPreMiningFiltersController(
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
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "initialise pre-mining filters";
			}

			public IvMObject<?>[] createInputObjects() {
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

	protected void initGuiAlignment() {

		//save log button
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "save aligned log";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.aligned_log_filtered };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				panel.getSaveLogButton().setEnabled(true);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getSaveLogButton().setEnabled(false);
			}
		});
	}

	protected void initGuiHighlightingFilters() {

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
		setObject(IvMObject.controller_highlighting_filters, new IvMHighlightingFiltersController(
				configuration.getHighlightingFilters(), panel.getHighlightingFiltersView()));

		//initialise filters
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "initialise highlighting filters";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.ivm_attributes_info, IvMObject.controller_highlighting_filters };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				IvMAttributesInfo attributesInfo = inputs.get(IvMObject.ivm_attributes_info);
				IvMHighlightingFiltersController controller = inputs.get(IvMObject.controller_highlighting_filters);

				controller.setAttributesInfo(attributesInfo);

				panel.getTraceColourMapView().setAttributes(attributesInfo);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getTraceColourMapView().invalidateAttributes();
			}
		});

		//filtering description
		chain.register(new DataChainLinkGuiAbstract() {
			public String getName() {
				return "selection description";
			}

			public IvMObject<?>[] createInputObjects() {
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
	}

	protected void initGuiAnimation() {
		//enable animation
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "animation enabled";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.selected_animation_enabled };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				boolean enabled = inputs.get(IvMObject.selected_animation_enabled);
				if (!enabled) {
					System.out.println("animation disabled");
					InductiveVisualMinerController.setAnimationStatus(panel, "animation disabled", true);
					panel.getGraph().setAnimationEnabled(false);
				} else {
					//this is taken care of by the animation handler
				}
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//no action necessary
			}
		});

		//animation to panel
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "update animation";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.animation, IvMObject.animation_scaler };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				GraphVizTokens animation = inputs.get(IvMObject.animation);
				Scaler scaler = inputs.get(IvMObject.animation_scaler);

				panel.getGraph().setTokens(animation);
				panel.getGraph().setAnimationExtremeTimes(scaler.getMinInUserTime(), scaler.getMaxInUserTime());
				panel.getGraph().setAnimationEnabled(true);

				panel.repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getGraph().setAnimationEnabled(false);
				InductiveVisualMinerController.setAnimationStatus(panel, " ", false);
			}
		});

		//filtered log to animation
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "filtered log to animation";
			}

			public IvMObject<?>[] createInputObjects() {
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

		//
		/**
		 * Set animation time updater. Naturally, this does not go via the
		 * chain, and we cache the scaler
		 */
		chain.register(new DataChainLinkGuiAbstract() {
			public String getName() {
				return "catch animation objects";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.animation_scaler, IvMObject.selected_visualisation_mode,
						IvMObject.visualisation_data };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				animationScaler = inputs.get(IvMObject.animation_scaler);
				animationMode = inputs.get(IvMObject.selected_visualisation_mode);
				animationVisualisationData = inputs.get(IvMObject.visualisation_data);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				animationScaler = null;
			}
		});

		panel.getGraph().setAnimationTimeChangedListener(new AnimationTimeChangedListener() {
			public void timeStepTaken(double userTime) {
				if (panel.getGraph().isAnimationEnabled()) {
					Scaler scaler = animationScaler;
					if (scaler != null) {
						long logTime = Math.round(scaler.userTime2LogTime(userTime));
						if (scaler.isCorrectTime()) {
							setAnimationStatus(panel, ResourceTimeUtils.timeToString(logTime), true);
						} else {
							setAnimationStatus(panel, "random", true);
						}

						//draw modes that require an update with each time step
						if (animationMode != null && animationVisualisationData != null) {
							if (animationMode.isUpdateWithTimeStep()) {
								animationVisualisationData.setTime(logTime);
								try {
									//TODO: re-enable
									//updateHighlighting(panel, state);
								} catch (UnknownTreeNodeException e) {
									e.printStackTrace();
								}
								panel.getTraceView().repaint();
							}
						}
					}
				}
			}
		});

		//show histogram
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "show histogram";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.histogram_data };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				HistogramData histogramData = inputs.get(IvMObject.histogram_data);

				panel.getGraph().setHistogramData(histogramData);
				panel.getGraph().repaint();
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				panel.getGraph().setHistogramData(null);
			}
		});
	}

	protected void initGuiHistogram() {
		//resize handler
		panel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//on resize, we have to resize the histogram as well
				chain.setObject(IvMObject.histogram_width, (int) panel.getGraph().getControlsProgressLine().getWidth());
			}
		});

		//Update the width once the dot is ready. We cannot initialise the width as long as the window has not been drawn yet. Once the dot is computed, this should be fine. 
		chain.register(new DataChainLinkGuiAbstract() {

			public String getName() {
				return "histogram width";
			}

			public IvMObject<?>[] createInputObjects() {
				return new IvMObject<?>[] { IvMObject.graph_dot };
			}

			public void updateGui(InductiveVisualMinerPanel panel, IvMObjectValues inputs) throws Exception {
				int width = (int) panel.getGraph().getControlsProgressLine().getWidth();
				chain.setObject(IvMObject.histogram_width, width);
			}

			public void invalidate(InductiveVisualMinerPanel panel) {
				//no action necessary				
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

	public void registerModeRequests() {
		//TODO: now if there is one mode with a certain trigger, then all modes will update with that trigger.
	}

	private <C> void updateObjectInGui(final IvMObject<C> object, final C value, final boolean fixed) {
		if (object.equals(IvMObject.selected_miner)) {
			panel.getMinerSelection().setSelectedItem(value);
		} else if (object.equals(IvMObject.model) && fixed) {
			panel.getActivitiesSlider().setVisible(false);
			panel.getPathsSlider().setVisible(false);
			panel.getPreMiningFiltersButton().setVisible(false);
			panel.getMinerLabel().setVisible(false);
			panel.getMinerSelection().setVisible(false);
		} else if (object.equals(IvMObject.selected_classifier) && fixed) {
			panel.getEditModelButton().setVisible(false);
			panel.getClassifierLabel().setVisible(false);
			panel.getClassifiers().setVisible(false);
		} else if (object.equals(IvMObject.selected_classifier) || object.equals(IvMObject.classifier_for_gui)) {
			panel.getClassifiers().getMultiComboBox().setSelectedItems((AttributeClassifier[]) value);
		} else if (object.equals(IvMObject.selected_noise_threshold)) {
			panel.getPathsSlider().setValue((double) value);
		} else if (object.equals(IvMObject.selected_activities_threshold)) {
			panel.getActivitiesSlider().setValue((double) value);
		} else if (object.equals(IvMObject.selected_visualisation_mode)) {
			panel.getVisualisationModeSelector().setSelectedItem(value);
		} else if (object.equals(IvMObject.selected_animation_enabled)) {
			panel.getGraph().setAnimationEnabled((boolean) value);
		}
	}

	public static void debug(Object s) {
		System.out.println(s);
	}

	public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	public <C> void setObject(IvMObject<C> object, C value) {
		updateObjectInGui(object, value, false);
		chain.setObject(object, value);
	}

	public <C> void setFixedObject(IvMObject<C> object, C value) {
		updateObjectInGui(object, value, true);
		chain.setFixedObject(object, value);
	}
}
