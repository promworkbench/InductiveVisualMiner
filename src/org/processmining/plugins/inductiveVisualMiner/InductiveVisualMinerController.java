package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.export.ExportDialog;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.chain2.Chain2;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl01GatherAttributes;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl02SortEvents;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl03MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl04FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl05Mine;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl06LayoutModel;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl07Align;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl08LayoutAlignment;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl09AnimationScaler;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl10Animate;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl11TraceColouring;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl12FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl13Performance;
import org.processmining.plugins.inductiveVisualMiner.chain2.Cl14Histogram;
import org.processmining.plugins.inductiveVisualMiner.chain2.OnException;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterAvi;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterStatistics;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupPopulator;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.processtree.ProcessTree;

public class InductiveVisualMinerController {

	final InductiveVisualMinerPanel panel;
	final InductiveVisualMinerState state;

	private final Chain2 chain;
	private final PluginContext context;

	public InductiveVisualMinerController(final PluginContext context, final InductiveVisualMinerPanel panel,
			final InductiveVisualMinerState state, ProMCanceller canceller) {
		this.panel = panel;
		this.state = state;
		this.context = context;
		state.setGraphUserSettings(panel.getGraph().getUserSettings());
		state.getGraphUserSettings().setDirection(GraphDirection.leftRight);

		//initialise gui handlers
		initGui();

		//set up exception handling
		final OnException onException2 = new OnException() {
			public void onException(Exception e) {
				setStatus("- error - aborted -");
			}
		};

		//set up the chain
		chain = new Chain2(state, canceller, context.getExecutor(), onException2);

		//gather attributes
		Cl01GatherAttributes gatherAttributes = new Cl01GatherAttributes();
		{
			gatherAttributes.setOnStart(new Runnable() {
				public void run() {
					panel.getClassifiers().setEnabled(false);
					setStatus("Gathering attributes..");
				}
			});
			gatherAttributes.setOnComplete(new Runnable() {
				public void run() {
					panel.getClassifiers().setEnabled(true);

					//update the classifier combobox
					panel.getClassifiers().replaceClassifiers(state.getClassifiers(), state.getInitialClassifier());

					//initialise the filters
					{
						Runnable onUpdatePreMining = new Runnable() {
							public void run() {
								chain.execute(Cl04FilterLogOnActivities.class);
							}
						};
						Runnable onUpdateHighlighting = new Runnable() {
							public void run() {
								chain.execute(Cl12FilterNodeSelection.class);
							}
						};
						Function<TraceColourMapSettings, Object> onUpdateTraceColourMap = new Function<TraceColourMapSettings, Object>() {
							public Object call(TraceColourMapSettings input) throws Exception {
								state.setTraceColourMapSettings(input);
								chain.execute(Cl11TraceColouring.class);
								return null;
							}
						};
						state.setFiltersController(new IvMFiltersController(context, panel, state, onUpdatePreMining,
								onUpdateHighlighting));
						panel.getTraceColourMapView().initialise(state.getAttributesInfo(), onUpdateTraceColourMap);
					}
				}
			});
			gatherAttributes.setOnException(onException2);
		}

		//reorder events
		Cl02SortEvents sortEvents = new Cl02SortEvents();
		{
			sortEvents.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					setStatus("Checking time stamps..");
					setAnimationStatus(" ", false);
				}
			});
			sortEvents.setOnComplete(new Runnable() {
				public void run() {

				}
			});
			sortEvents.setOnIllogicalTimeStamps(new Function<Object, Boolean>() {
				public Boolean call(Object input) throws Exception {
					setStatus("Illogical time stamps; aborted.");
					String[] options = new String[] { "Continue with neither animation nor performance",
							"Reorder events" };
					int n = JOptionPane.showOptionDialog(panel,
							"The event log contains illogical time stamps,\n i.e. some time stamps contradict the order of events.\n\nInductive visual Miner can reorder the events and discover a new model.\nWould you like to do that?", //message
							"Illogical Time Stamps", //title
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, //do not use a custom Icon
							options, //the titles of buttons
							options[0]); //default button title
					if (n == 1) {
						//the user requested to reorder the events
						return true;
					}
					return false;
				}
			});
			sortEvents.setOnException(onException2);
		}

		chain.addConnection(gatherAttributes, sortEvents);

		//make log
		Cl03MakeLog makeLog = new Cl03MakeLog();
		{
			makeLog.setOnStart(new Runnable() {
				public void run() {
					setStatus("Making log..");
				}
			});
			makeLog.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getLog(), state.getTraceColourMap());

					state.getFiltersController().updateFiltersWithIMLog(panel, state.getLog(), state.getSortedXLog(),
							context.getExecutor());
				}
			});
			makeLog.setOnException(onException2);
		}

		chain.addConnection(sortEvents, makeLog);

		//filter on activities
		Cl04FilterLogOnActivities filterLogOnActivities = new Cl04FilterLogOnActivities();
		{
			filterLogOnActivities.setOnStart(new Runnable() {
				public void run() {
					setStatus("Filtering activities..");
				}
			});
			filterLogOnActivities.setOnException(onException2);
		}
		
		chain.addConnection(makeLog, filterLogOnActivities);

		//mine a model
		Cl05Mine mine = new Cl05Mine();
		{
			mine.setOnStart(new Runnable() {
				public void run() {
					setStatus("Mining..");
					setAnimationStatus(" ", false);
				}
			});
			mine.setOnComplete(new Runnable() {
				public void run() {
					panel.getSaveModelButton().setEnabled(true);
					panel.getEditModelView().setTree(state.getTree());
				}
			});
			mine.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getSaveModelButton().setEnabled(false);
					panel.getEditModelView().setTree(null);
					state.setSelection(new Selection());
				}
			});
			mine.setOnException(onException2);
		}
		
		chain.addConnection(filterLogOnActivities, mine);

		//layout
		Cl06LayoutModel layoutModel = new Cl06LayoutModel();
		{
			layoutModel.setOnStart(new Runnable() {
				public void run() {
					setStatus("Layouting model..");
				}
			});
			layoutModel.setOnComplete(new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);
					panel.getSaveImageButton().setEnabled(true);
				}
			});
			layoutModel.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getSaveImageButton().setEnabled(false);
				}
			});
			layoutModel.setOnException(onException2);
		}

		chain.addConnection(mine, layoutModel);

		//align
		Cl07Align align = new Cl07Align();
		{
			align.setOnStart(new Runnable() {
				public void run() {
					setStatus("Aligning log and model..");
				}
			});
			align.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getTree(), state.getIvMLog(), state.getSelection(),
							state.getTraceColourMap());

					state.getFiltersController().updateFiltersWithIvMLog(panel, state.getIvMLog(),
							context.getExecutor());
				}
			});
			align.setOnException(onException2);
		}

		chain.addConnection(mine, align);

		//layout with alignment
		Cl08LayoutAlignment layoutAlignment = new Cl08LayoutAlignment();
		{
			layoutAlignment.setOnStart(new Runnable() {
				public void run() {
					setStatus("Layouting aligned model..");
					
					//if the view does not show deviations, do not select any log moves
					if (!state.getMode().isShowDeviations()) {
						state.removeModelAndLogMovesSelection();
					}
				}
			});
			layoutAlignment.setOnComplete(new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);
					
					makeElementsSelectable(state.getVisualisationInfo(), panel, state.getSelection());
					
					//tell the trace view the colours of activities
					panel.getTraceView().setEventColourMap(state.getTraceViewColourMap());
				}
			});
			layoutAlignment.setOnException(onException2);
		}

		chain.addConnection(layoutModel, layoutAlignment);
		chain.addConnection(align, layoutAlignment);

		//animation scaler
		Cl09AnimationScaler animationScaler = new Cl09AnimationScaler();
		{
			animationScaler.setOnStart(new Runnable() {
				public void run() {
					setStatus("Scaling animation..");
				}
			});
			animationScaler.setOnException(onException2);
		}

		chain.addConnection(align, animationScaler);

		//animate
		Cl10Animate animate = new Cl10Animate();
		{
			animate.setOnStart(new Runnable() {
				public void run() {
					setAnimationStatus("Creating animation.. ", false);
				}
			});

			animate.setOnComplete(new Runnable() {
				public void run() {
					if (state.getAnimationGraphVizTokens() != null) {
						//animation enabled; store the result
						panel.getGraph().setTokens(state.getAnimationGraphVizTokens());
						panel.getGraph().setAnimationExtremeTimes(state.getAnimationScaler().getMinInUserTime(),
								state.getAnimationScaler().getMaxInUserTime());
						panel.getGraph().setFilteredLog(state.getIvMLogFiltered());
						panel.getGraph().setAnimationEnabled(true);
					} else {
						//animation disabled
						System.out.println("animation disabled");
						setAnimationStatus("animation disabled", true);
						panel.getGraph().setAnimationEnabled(false);
					}
					panel.repaint();
					
					//record the width of the panel (necessary for histogram later)
					state.setHistogramWidth((int) panel.getGraph().getControlsProgressLine().getWidth());
				}
			});
			
			animate.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus(" ", false);
				}
			});
			
			animate.setOnException(onException2);
		}
		
		chain.addConnection(animationScaler, animate);
		chain.addConnection(layoutAlignment, animate);

		//colour traces
		Cl11TraceColouring traceColouring = new Cl11TraceColouring();
		{
			traceColouring.setOnStart(new Runnable() {
				public void run() {
					setStatus("Colouring traces..");
				}
			});
			traceColouring.setOnComplete(new Runnable() {
				public void run() {
					//tell the animation and the trace view the trace colour map
					panel.getGraph().setTraceColourMap(state.getTraceColourMap());
					panel.getTraceView().setTraceColourMap(state.getTraceColourMap());
					panel.getTraceView().repaint();

					panel.repaint();
				}
			});
			traceColouring.setOnException(onException2);
		}

		chain.addConnection(align, traceColouring);

		//filter node selection
		Cl12FilterNodeSelection filterNodeSelection = new Cl12FilterNodeSelection();
		{
			filterNodeSelection.setOnStart(new Runnable() {
				public void run() {
					setStatus("Highlighting selection..");
				}
			});
			filterNodeSelection.setOnComplete(new Runnable() {
				public void run() {

					HighlightingFiltersView.updateSelectionDescription(panel, state.getSelection(),
							state.getFiltersController(), state.getTree());

					//tell trace view the colour map and the selection
					panel.getTraceView().set(state.getTree(), state.getIvMLogFiltered(), state.getSelection(),
							state.getTraceColourMap());

					try {
						updateHighlighting();
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}

					//tell the animation the filtered log
					panel.getGraph().setFilteredLog(state.getIvMLogFiltered());

					panel.repaint();
				}
			});
			filterNodeSelection.setOnInvalidate(new Runnable() {
				public void run() {
					//tell the animation the filtered log
					panel.getGraph().setFilteredLog(null);
					
					try {
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});
			filterNodeSelection.setOnException(onException2);
		}

		chain.addConnection(layoutAlignment, filterNodeSelection);

		//mine performance
		Cl13Performance performance = new Cl13Performance();
		{
			performance.setOnStart(new Runnable() {
				public void run() {
					setStatus("Measuring performance..");
				}
			});
			performance.setOnComplete(new Runnable() {
				public void run() {
					try {
						updateHighlighting();
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});
			performance.setOnInvalidate(new Runnable() {
				public void run() {
					try {
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});
			performance.setOnException(onException2);
		}

		chain.addConnection(filterNodeSelection, performance);

		//compute histogram
		Cl14Histogram histogram = new Cl14Histogram();
		{
			histogram.setOnStart(new Runnable() {
				public void run() {
					setStatus("Computing histogram..");
				}
			});
			histogram.setOnComplete(new Runnable() {
				public void run() {
					setStatus(" ");
					//pass the histogram data to the panel
					panel.getGraph().setHistogramData(state.getHistogramData());
					panel.getGraph().repaint();
				}
			});
			histogram.setOnException(onException2);
		}
		
		chain.addConnection(filterNodeSelection, histogram);

		//start the chain
		chain.execute(Cl01GatherAttributes.class);
	}

	private void initGui() {

		//resize handler
		panel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//on resize, we have to resize the histogram as well
				state.setHistogramWidth((int) panel.getGraph().getControlsProgressLine().getWidth());
				chain.execute(Cl14Histogram.class);
			}
		});

		//noise filter
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

		//classifier
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setClassifier(panel.getClassifiers().getSelectedClassifier());
				chain.execute(Cl03MakeLog.class);
			}
		});

		//miner
		panel.getMinerSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setMiner(((VisualMinerWrapper) panel.getMinerSelection().getSelectedItem()));
				chain.execute(Cl03MakeLog.class);
			}
		});

		//model editor
		panel.getEditModelView().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof IvMEfficientTree) {
					IvMEfficientTree tree = (IvMEfficientTree) e.getSource();
					state.setTree(tree);
					chain.execute(Cl06LayoutModel.class);
				}
			}
		});

		//activities filter
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					state.setActivitiesThreshold(panel.getActivitiesSlider().getValue());
					chain.execute(Cl04FilterLogOnActivities.class);
				}

				//give the focus back to the graph panel
				panel.getGraph().requestFocus(true);
			}
		});

		//display mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setMode((Mode) panel.getColourModeSelection().getSelectedItem());
				chain.execute(Cl08LayoutAlignment.class);
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Selection>() {
			public void call(Selection input) throws Exception {
				state.setSelection(input);
				chain.execute(Cl12FilterNodeSelection.class);
			}
		});

		//graph direction changed
		panel.setOnGraphDirectionChanged(new Runnable() {
			public void run() {
				chain.execute(Cl08LayoutAlignment.class);
			}
		});

		panel.setOnAnimationEnabledChanged(new AnimationEnabledChangedListener() {
			public boolean animationEnabledChanged() {
				if (state.isAnimationGlobalEnabled()) {
					//animation gets disabled
					state.setAnimationGlobalEnabled(false);
					state.setAnimation(null);
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus("animation disabled", true);
					return false;
				} else {
					//animation gets enabled
					state.setAnimationGlobalEnabled(true);
					chain.execute(Cl10Animate.class);
					return true;
				}
			}
		});

		//set model export button
		panel.getSaveModelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//store the resulting Process tree or Petri net
				String name = XConceptExtension.instance().extractName(state.getSortedXLog());
				ProcessTree tree = state.getTree().getDTree();

				Object[] options = { "Petri net", "Process tree" };
				int n = JOptionPane.showOptionDialog(panel,
						"As what would you like to save the model?\nIt will become available in ProM.", "Save",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				if (n == 0) {
					//store as Petri net
					ExportModel.exportPetrinet(context, tree, name);
				} else if (n == 1) {
					//store as Process tree
					ExportModel.exportProcessTree(context, tree, name);
				}
			}
		});

		//set image/animation export button
		panel.getSaveImageButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveView();
			}
		});

		//listen to ctrl s to save image/animation (should override keyboard shortcut of GraphViz)
		{
			panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK), "saveAs"); // - key
			panel.getActionMap().put("saveAs", new AbstractAction() {
				private static final long serialVersionUID = -4780600363000017631L;

				public void actionPerformed(ActionEvent arg0) {
					saveView();
				}

			});
		}

		//set pre-mining filters button
		panel.getPreMiningFiltersButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.getPreMiningFiltersView().enableAndShow();
			}
		});

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

		//set trace colouring button
		panel.getTraceColourMapViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getTraceColourMapView().enableAndShow();
			}
		});

		//set highlighting filters button
		panel.getHighlightingFiltersViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getColouringFiltersView().enableAndShow();
			}
		});

		//set mouse-in-out node updater
		panel.getGraph().addMouseInElementsChangedListener(new MouseInElementsChangedListener<DotElement>() {
			public void mouseInElementsChanged(Set<DotElement> mouseInElements) {
				panel.getGraph().setShowPopup(!mouseInElements.isEmpty());
				try {
					PopupPopulator.updatePopup(panel, state);
				} catch (UnknownTreeNodeException e) {
					e.printStackTrace();
				}
				panel.repaint();
			}
		});

		//set animation time updater
		panel.getGraph().setAnimationTimeChangedListener(new AnimationTimeChangedListener() {
			public void timeStepTaken(double userTime) {
				if (panel.getGraph().isAnimationEnabled()) {
					long logTime = Math.round(state.getAnimationScaler().userTime2LogTime(userTime));
					if (state.getAnimationScaler().isCorrectTime()) {
						setAnimationStatus(ResourceTimeUtils.timeToString(logTime), true);
					} else {
						setAnimationStatus("random", true);
					}

					//draw queues
					if (state.getMode().isUpdateWithTimeStep(state)) {
						state.getVisualisationData().setTime(logTime);
						try {
							updateHighlighting();
						} catch (UnknownTreeNodeException e) {
							e.printStackTrace();
						}
						panel.getTraceView().repaint();
					}
				}
			}
		});
	}

	private static void makeElementsSelectable(ProcessTreeVisualisationInfo info, InductiveVisualMinerPanel panel,
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

	public synchronized void setStatus(String s) {
		panel.getStatusLabel().setText(s);
	}

	public synchronized void setAnimationStatus(String s, boolean isTime) {
		if (isTime) {
			panel.getAnimationTimeLabel().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
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
	public void updateHighlighting() throws UnknownTreeNodeException {
		TraceViewEventColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(
				panel.getGraph().getSVG(), state.getVisualisationInfo(), state.getTree(), state.getVisualisationData(),
				state.getMode().getVisualisationParameters(state));
		colourMap.setSelectedNodes(state.getSelection());
		panel.getTraceView().setEventColourMap(colourMap);
	}

	//save the view
	private void saveView() {
		List<Exporter> exporters = new ArrayList<>();

		if (panel.getGraph().isAnimationEnabled()) {
			exporters.add(new ExporterAvi(state));
		}
		if (state.isPerformanceReady()) {
			exporters.add(new ExporterStatistics(state));
		}

		Exporter[] exporters2 = new Exporter[exporters.size()];
		exporters2 = exporters.toArray(exporters2);
		new ExportDialog(panel.getGraph(), exporters2);
	}

	public static void debug(Object s) {
		System.out.println(s);
	}
}
