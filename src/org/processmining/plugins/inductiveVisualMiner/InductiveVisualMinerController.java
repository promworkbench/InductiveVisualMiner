package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl00GatherAttributes;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl01SortEvents;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl02MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl03FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04Mine;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl05LayoutModel;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl06Align;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl07LayoutWithAlignment;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl08AnimationScaler;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl09Animate;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl10TraceColouring;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl11FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl12Performance;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl13Histogram;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterAvi;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupPopulator;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.processtree.ProcessTree;

public class InductiveVisualMinerController {

	final InductiveVisualMinerPanel panel;
	final InductiveVisualMinerState state;

	private final Chain chain;
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

		//set up the chain
		chain = new Chain(context.getExecutor(), state, canceller);

		final Function<Exception, Object> onException = new Function<Exception, Object>() {
			public Object call(Exception input) throws Exception {
				setStatus("- error - aborted -");
				return null;
			}
		};

		//gather attributes
		{
			Cl00GatherAttributes m = new Cl00GatherAttributes();
			m.setOnStart(new Runnable() {
				public void run() {
					state.setAttributesInfo(null, null, null);
					panel.getClassifiers().setEnabled(false);
					setStatus("Gathering attributes..");
				}
			});
			m.setOnComplete(new Runnable() {
				public void run() {
					panel.getClassifiers().setEnabled(true);

					//update the classifier combobox
					panel.getClassifiers().replaceClassifiers(state.getClassifiers(), state.getInitialClassifier());

					//initialise the filters
					{
						Runnable onUpdatePreMining = new Runnable() {
							public void run() {
								chain.execute(Cl03FilterLogOnActivities.class);
							}
						};
						Runnable onUpdateHighlighting = new Runnable() {
							public void run() {
								chain.execute(Cl11FilterNodeSelection.class);
							}
						};
						Function<TraceColourMapSettings, Object> onUpdateTraceColourMap = new Function<TraceColourMapSettings, Object>() {
							public Object call(TraceColourMapSettings input) throws Exception {
								state.setTraceColourMapSettings(input);
								chain.execute(Cl10TraceColouring.class);
								return null;
							}
						};
						state.setFiltersController(new IvMFiltersController(context, panel, state, onUpdatePreMining,
								onUpdateHighlighting));
						panel.getTraceColourMapView().initialise(state.getAttributesInfo(), onUpdateTraceColourMap);
					}
				}
			});
			m.setOnException(onException);
			chain.add(m);
		}

		//reorder events
		{
			Cl01SortEvents se = new Cl01SortEvents();
			se.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getEditModelView().setTree(null);
					state.resetAlignment();
					state.resetTraceColourMap();
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Checking time stamps..");
					setAnimationStatus(" ", false);
				}
			});
			se.setOnComplete(new Runnable() {
				public void run() {

				}
			});
			se.setOnIllogicalTimeStamps(new Function<Object, Boolean>() {
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
			se.setOnException(onException);
			chain.add(se);
		}

		//make log
		{
			Cl02MakeLog m = new Cl02MakeLog();
			m.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getEditModelView().setTree(null);
					state.resetAlignment();
					state.resetTraceColourMap();
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Making log..");
					setAnimationStatus(" ", false);
				}
			});
			m.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getLog(), state.getTraceColourMap());

					state.getFiltersController().updateFiltersWithIMLog(panel, state.getLog(), state.getXLog(),
							context.getExecutor());
				}
			});
			m.setOnException(onException);
			chain.add(m);
		}

		//filter on activities
		{
			Cl03FilterLogOnActivities f = new Cl03FilterLogOnActivities();
			f.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getEditModelView().setTree(null);
					state.resetAnimation();
					state.resetTraceColourMap();
					state.resetScaler();
					state.resetAlignment();
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Filtering activities..");
					setAnimationStatus(" ", false);
				}
			});
			f.setOnException(onException);
			chain.add(f);
		}

		//mine a model
		{
			Cl04Mine m = new Cl04Mine();
			m.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getTraceView().set(state.getLog(), state.getTraceColourMap());
					panel.getEditModelView().setTree(null);
					state.resetAnimation();
					state.resetTraceColourMap();
					state.resetScaler();
					state.resetAlignment();
					state.resetPerformance();
					state.resetHistogramData();
					state.setSelection(new Selection());
					setStatus("Mining..");
					setAnimationStatus(" ", false);
				}
			});
			m.setOnComplete(new Runnable() {
				public void run() {
					panel.getSaveModelButton().setEnabled(true);
					panel.getSaveImageButton().setEnabled(true);
					panel.getEditModelView().setTree(state.getTree());
				}
			});
			m.setOnException(onException);
			chain.add(m);
		}

		//layout
		final Runnable layoutStart;
		final Runnable layoutComplete;
		{
			layoutStart = new Runnable() {
				public void run() {
					setStatus("Layouting graph..");
					setAnimationStatus(" ", false);

					panel.getGraph().setAnimationEnabled(false);

					//if the view does not show deviations, do not select any log moves
					if (!state.getMode().isShowDeviations()) {
						state.removeModelAndLogMovesSelection();
					}
				}
			};
			layoutComplete = new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);
					panel.getTraceView().setEventColourMap(state.getTraceViewColourMap());

					makeElementsSelectable(state.getVisualisationInfo(), panel, state.getSelection());
				}
			};
			Cl05LayoutModel chainLinkLayout = new Cl05LayoutModel();
			chainLinkLayout.setOnStart(new Runnable() {

				public void run() {
					layoutStart.run();
					panel.getGraph().setAnimationEnabled(false);
					panel.getTraceView().set(state.getLog(), state.getTraceColourMap());
					state.resetAnimation();
					state.resetTraceColourMap();
					state.resetScaler();
					state.resetAlignment();
					state.resetPerformance();
					state.resetHistogramData();
					state.setSelection(new Selection());
				}
			});
			chainLinkLayout.setOnComplete(layoutComplete);
			chainLinkLayout.setOnException(onException);
			chain.add(chainLinkLayout);
		}

		//align
		{
			Cl06Align a = new Cl06Align();
			a.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus(" ", false);
					state.resetAnimation();
					state.resetScaler();
					state.resetTraceColourMap();
					state.resetAlignment();
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Aligning log and model..");
				}
			});
			a.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getTree(), state.getIvMLog(), state.getSelection(),
							state.getTraceColourMap());

					state.getFiltersController().updateFiltersWithIvMLog(panel, state.getIvMLog(),
							context.getExecutor());
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//layout
		{
			Cl07LayoutWithAlignment l = new Cl07LayoutWithAlignment();
			l.setOnStart(layoutStart);
			l.setOnComplete(layoutComplete);
			l.setOnException(onException);
			chain.add(l);
		}

		//animation scaler
		{
			Cl08AnimationScaler f = new Cl08AnimationScaler();
			f.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					state.resetAnimation();
					state.resetScaler();
					state.resetTraceColourMap();
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Scaling animation..");
				}
			});
			f.setOnComplete(new Runnable() {
				public void run() {
					setStatus(" ");
				}
			});
			f.setOnException(onException);
			chain.add(f);
		}

		//animate
		{
			Cl09Animate a = new Cl09Animate(context.getExecutor(), state, panel);
			a.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					state.resetPerformance();
					state.resetHistogramData();
					state.resetTraceColourMap();
					state.resetAnimation();
					setAnimationStatus("Creating animation.. ", false);
				}
			});

			/*
			 * Animation is performed asynchronously, so we must do the complete
			 * in the animation chain link. Therefore, it's an inputfunction
			 * instead of a runnable.
			 */
			a.setOnComplete(new InputFunction<GraphVizTokens>() {
				public void call(GraphVizTokens result) {
					if (result != null) {
						//animation enabled; store the result
						state.setAnimation(result);
						panel.getGraph().setTokens(state.getAnimationGraphVizTokens());
						panel.getGraph().setAnimationExtremeTimes(state.getAnimationScaler().getMinInUserTime(),
								state.getAnimationScaler().getMaxInUserTime());
						panel.getGraph().setAnimationEnabled(true);
					} else {
						//animation disabled
						System.out.println("animation disabled");
						state.resetAnimation();
						setAnimationStatus("animation disabled", true);
						panel.getGraph().setAnimationEnabled(false);
					}
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//colour traces
		{
			Cl10TraceColouring f = new Cl10TraceColouring();
			f.setOnStart(new Runnable() {
				public void run() {
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Colouring traces..");
				}
			});
			f.setOnComplete(new Runnable() {
				public void run() {
					state.resetPerformance();

					//tell the animation and the trace view the trace colour map
					panel.getGraph().setTraceColourMap(state.getTraceColourMap());
					panel.getTraceView().setTraceColourMap(state.getTraceColourMap());

					panel.repaint();
				}
			});
			f.setOnException(onException);
			chain.add(f);
		}

		//filter node selection
		{
			Cl11FilterNodeSelection f = new Cl11FilterNodeSelection();
			f.setOnStart(new Runnable() {
				public void run() {
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Highlighting selection..");
				}
			});
			f.setOnComplete(new Runnable() {
				public void run() {

					state.resetPerformance();

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
			f.setOnException(onException);
			chain.add(f);
		}

		//mine performance
		{
			Cl12Performance q = new Cl12Performance();
			q.setOnStart(new Runnable() {
				public void run() {
					state.resetPerformance();
					state.resetHistogramData();
					setStatus("Measuring performance..");
				}
			});
			q.setOnComplete(new Runnable() {
				public void run() {
					try {
						updateHighlighting();
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
					state.setHistogramWidth((int) panel.getGraph().getControlsProgressLine().getWidth());
				}
			});
			q.setOnException(onException);
			chain.add(q);
		}

		//compute histogram
		{
			Cl13Histogram f = new Cl13Histogram();
			f.setOnStart(new Runnable() {
				public void run() {
					state.resetHistogramData();
					setStatus("Computing histogram..");
				}
			});
			f.setOnComplete(new Runnable() {
				public void run() {
					setStatus(" ");
					//pass the histogram data to the panel
					panel.getGraph().setHistogramData(state.getHistogramData());
					panel.getGraph().repaint();
				}
			});
			f.setOnException(onException);
			chain.add(f);
		}

		//start the chain
		chain.execute(Cl00GatherAttributes.class);
	}

	private void initGui() {

		//resize handler
		panel.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//on resize, we have to resize the histogram as well
				state.setHistogramWidth((int) panel.getGraph().getControlsProgressLine().getWidth());
				chain.execute(Cl13Histogram.class);
			}
		});

		//noise filter
		panel.getPathsSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getPathsSlider().getSlider().getValueIsAdjusting()) {
					state.setPaths(panel.getPathsSlider().getValue());
					chain.execute(Cl04Mine.class);
				}

				//give the focus back to the graph panel
				panel.getGraph().requestFocus(true);
			}
		});

		//classifier
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setClassifier(panel.getClassifiers().getSelectedClassifier());
				chain.execute(Cl02MakeLog.class);
			}
		});

		//miner
		panel.getMinerSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setMiner(((VisualMinerWrapper) panel.getMinerSelection().getSelectedItem()));
				chain.execute(Cl02MakeLog.class);
			}
		});

		//model editor
		panel.getEditModelView().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof IvMEfficientTree) {
					IvMEfficientTree tree = (IvMEfficientTree) e.getSource();
					state.setTree(tree);
					chain.execute(Cl05LayoutModel.class);
				}
			}
		});

		//activities filter
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					state.setActivitiesThreshold(panel.getActivitiesSlider().getValue());
					chain.execute(Cl03FilterLogOnActivities.class);
				}

				//give the focus back to the graph panel
				panel.getGraph().requestFocus(true);
			}
		});

		//display mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setMode((Mode) panel.getColourModeSelection().getSelectedItem());
				chain.execute(Cl07LayoutWithAlignment.class);
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Selection>() {
			public void call(Selection input) throws Exception {
				state.setSelection(input);
				chain.execute(Cl11FilterNodeSelection.class);
			}
		});

		//graph direction changed
		panel.setOnGraphDirectionChanged(new Runnable() {
			public void run() {
				chain.execute(Cl07LayoutWithAlignment.class);
			}
		});

		panel.setOnAnimationEnabledChanged(new AnimationEnabledChangedListener() {
			public boolean animationEnabledChanged() {
				if (state.isAnimationGlobalEnabled()) {
					//animation gets disabled
					state.setAnimationGlobalEnabled(false);
					state.resetAnimation();
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus("animation disabled", true);
					return false;
				} else {
					//animation gets enabled
					state.setAnimationGlobalEnabled(true);
					chain.execute(Cl09Animate.class);
					return true;
				}
			}
		});

		//set model export button
		panel.getSaveModelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//store the resulting Process tree or Petri net
				String name = XConceptExtension.instance().extractName(state.getXLog());
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
				if (panel.getGraph().isAnimationEnabled()) {
					new ExportDialog(panel.getGraph(), new ExporterAvi(state));
				} else {
					new ExportDialog(panel.getGraph());
				}
			}
		});

		//listen to ctrl s to save image/animation (should override keyboard shortcut of GraphViz)
		{
			panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveAs"); // - key
			panel.getActionMap().put("saveAs", new AbstractAction() {
				private static final long serialVersionUID = -4780600363000017631L;

				public void actionPerformed(ActionEvent arg0) {
					if (panel.getGraph().isAnimationEnabled()) {
						new ExportDialog(panel.getGraph(), new ExporterAvi(state));
					} else {
						new ExportDialog(panel.getGraph());
					}
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

	public static void debug(Object s) {
		System.out.println(s);
	}
}
