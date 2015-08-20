package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.Classifiers.ClassifierWrapper;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.visualisation.export.ExportDialog;
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.inductiveVisualMiner.TraceView.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl01MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl02FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl03Mine;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04LayoutModel;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl05Align;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl06LayoutWithAlignment;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl07Animate;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl08FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl09Performance;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilterPluginFinder;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFiltersView;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterAvi;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class InductiveVisualMinerController {

	final InductiveVisualMinerPanel panel;
	final InductiveVisualMinerState state;

	private final Chain chain;
	private final PluginContext context;

	public InductiveVisualMinerController(PluginContext context, final InductiveVisualMinerPanel panel,
			final InductiveVisualMinerState state) {
		this.panel = panel;
		this.state = state;
		this.context = context;

		//initialise gui handlers
		initGui();

		//set up the chain
		chain = new Chain(context.getExecutor(), state);

		//make log
		final Function<Exception, Object> onException;
		{
			Cl01MakeLog m = new Cl01MakeLog();
			m.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getSaveImageButton().setText("image");
					state.resetAlignment();
					state.resetPerformance();
					setStatus("Making log..");
					setAnimationStatus(" ", false);
				}
			});
			m.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getLog());
				}
			});
			onException = new Function<Exception, Object>() {
				public Object call(Exception input) throws Exception {
					setStatus("- error - aborted -");
					return null;
				}
			};
			m.setOnException(onException);
			chain.add(m);
		}

		//filter on activities
		{
			Cl02FilterLogOnActivities f = new Cl02FilterLogOnActivities();
			f.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getSaveImageButton().setText("image");
					state.resetAlignment();
					state.resetPerformance();
					setStatus("Filtering activities..");
					setAnimationStatus(" ", false);
				}
			});
			f.setOnException(onException);
			chain.add(f);
		}

		//mine a model
		{
			Cl03Mine m = new Cl03Mine();
			m.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveImageButton().setText("image");
					panel.getTraceView().set(state.getLog());
					state.resetAlignment();
					state.resetPerformance();
					setStatus("Mining..");
					setAnimationStatus(" ", false);
				}
			});
			m.setOnComplete(new Runnable() {
				public void run() {
					panel.getSaveModelButton().setEnabled(true);
					panel.getSaveImageButton().setEnabled(true);
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
					panel.getSaveImageButton().setText("image");

					//if the view does not show deviations, do not select any log moves
					if (!state.getMode().isShowDeviations()) {
						state.setSelectedLogMoves(new HashSet<LogMovePosition>());
					}
				}
			};
			layoutComplete = new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);
					panel.getTraceView().setColourMap(state.getTraceViewColourMap());

					makeNodesSelectable(state.getVisualisationInfo(), panel, state.getSelectedNodes(),
							state.getSelectedLogMoves());

				}
			};
			Cl04LayoutModel chainLinkLayout = new Cl04LayoutModel();
			chainLinkLayout.setOnStart(layoutStart);
			chainLinkLayout.setOnComplete(layoutComplete);
			chainLinkLayout.setOnException(onException);
			chain.add(chainLinkLayout);
		}

		//align
		{
			Cl05Align a = new Cl05Align();
			a.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					setAnimationStatus(" ", false);
					panel.getSaveImageButton().setText("image");
					state.resetAlignment();
					state.resetPerformance();
					setStatus("Aligning log and model..");
				}
			});
			a.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getIvMLog());
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//layout
		{
			Cl06LayoutWithAlignment l = new Cl06LayoutWithAlignment();
			l.setOnStart(layoutStart);
			l.setOnComplete(layoutComplete);
			l.setOnException(onException);
			chain.add(l);
		}

		//animate
		{
			Cl07Animate a = new Cl07Animate(state, panel);
			a.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					panel.getSaveImageButton().setText("image");
					state.resetPerformance();
					setAnimationStatus("Creating animation.. ", false);
				}
			});
			a.setOnComplete(new Runnable() {
				public void run() {
					//this is located in the chainlink
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//filter node selection
		{
			Cl08FilterNodeSelection f = new Cl08FilterNodeSelection();
			f.setOnStart(new Runnable() {
				public void run() {
					state.resetPerformance();
					setStatus("Highlighting selection..");
				}
			});
			f.setOnComplete(new Runnable() {
				public void run() {

					state.resetPerformance();

					ColouringFiltersView.updateSelectionDescription(panel, state.getSelectedNodes(),
							state.getSelectedLogMoves(), state.getColouringFilters());

					//tell trace view the colour map and the selection
					panel.getTraceView().set(state.getIvMLogFiltered());

					updateHighlighting();
					updatePopup();

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
			Cl09Performance q = new Cl09Performance();
			q.setOnStart(new Runnable() {
				public void run() {
					state.resetPerformance();
					setStatus("Measuring performance..");
				}
			});
			q.setOnComplete(new Runnable() {
				public void run() {
					setStatus(" ");
					updateHighlighting();
					updatePopup();
					panel.getGraph().repaint();
				}
			});
			q.setOnException(onException);
			chain.add(q);
		}

		//set up plug-ins
		List<ColouringFilter> colouringFilters = ColouringFilterPluginFinder.findFilteringPlugins(context, panel,
				state.getXLog());
		state.setColouringFilters(colouringFilters);
		panel.getColouringFiltersView().initialise(colouringFilters);
		initialiseColourFilters(state.getXLog(), context.getExecutor());

		//start the chain
		chain.execute(Cl01MakeLog.class);
	}

	private void initGui() {

		//noise filter
		panel.getPathsSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getPathsSlider().getSlider().getValueIsAdjusting()) {
					state.setPaths(panel.getPathsSlider().getValue());
					chain.execute(Cl03Mine.class);
				}

				//give the focus back to the graph panel
				panel.getGraph().requestFocus(true);
			}
		});

		//classifier
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setClassifier(((ClassifierWrapper) panel.getClassifiers().getSelectedItem()).classifier);
				chain.execute(Cl01MakeLog.class);
			}
		});

		//miner
		panel.getMinerSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setMiner(((VisualMinerWrapper) panel.getMinerSelection().getSelectedItem()));
				chain.execute(Cl01MakeLog.class);
			}
		});

		//activities filter
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					state.setActivitiesThreshold(panel.getActivitiesSlider().getValue());
					chain.execute(Cl02FilterLogOnActivities.class);
				}

				//give the focus back to the graph panel
				panel.getGraph().requestFocus(true);
			}
		});

		//display mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setMode((Mode) panel.getColourModeSelection().getSelectedItem());
				chain.execute(Cl06LayoutWithAlignment.class);
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Pair<Set<UnfoldedNode>, Set<LogMovePosition>>>() {
			public void call(Pair<Set<UnfoldedNode>, Set<LogMovePosition>> input) throws Exception {
				state.setSelectedNodes(input.getA());
				state.setSelectedLogMoves(input.getB());
				chain.execute(Cl08FilterNodeSelection.class);
			}
		});

		//graph direction changed
		panel.setOnGraphDirectionChanged(new InputFunction<Dot.GraphDirection>() {
			public void call(GraphDirection input) throws Exception {
				state.setGraphDirection(input);
				chain.execute(Cl06LayoutWithAlignment.class);
			}
		});

		//set model export button
		panel.getSaveModelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//store the resulting Process tree or Petri net
				String name = XConceptExtension.instance().extractName(state.getXLog());
				ProcessTree tree = state.getTree();

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
			panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveAs"); // - key
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

		//set trace view button
		panel.getTraceViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getTraceView().swapVisibility();
			}
		});

		//set colouring filters button
		panel.getColouringFiltersViewButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel.getColouringFiltersView().swapVisibility();
			}
		});

		//set mouse-in-out node updater
		panel.getGraph().addMouseInElementsChangedListener(new MouseInElementsChangedListener<DotElement>() {
			public void mouseInElementsChanged(Set<DotElement> mouseInElements) {
				panel.getGraph().setShowPopup(!mouseInElements.isEmpty());
				updatePopup();
				panel.repaint();
			}
		});

		//set animation time updater
		panel.getGraph().setAnimationTimeChangedListener(new AnimationTimeChangedListener() {
			public void timeStepTaken(double userTime) {
				if (panel.getGraph().isAnimationEnabled()) {
					long logTime = Math.round(state.getAnimationScaler().userTime2LogTime(userTime));
					if (state.getAnimationScaler().isCorrectTime()) {
						setAnimationStatus(TimestampsAdder.toString(logTime), true);
					} else {
						setAnimationStatus("animation random", true);
					}

					//draw queues
					if (state.getMode().isUpdateWithTimeStep(state)) {
						state.getVisualisationData().setTime(logTime);
						updateHighlighting();
						panel.getTraceView().repaint();
					}
				}
			}
		});
	}

	private static void makeNodesSelectable(ProcessTreeVisualisationInfo info, InductiveVisualMinerPanel panel,
			Set<UnfoldedNode> selectedNodes, Set<LogMovePosition> selectedLogMoves) {
		for (LocalDotNode dotNode : info.getAllActivityNodes()) {
			panel.makeNodeSelectable(dotNode, selectedNodes.contains(dotNode.getUnode()));
		}
		for (LocalDotEdge logMoveEdge : info.getAllLogMoveEdges()) {
			panel.makeEdgeSelectable(logMoveEdge, selectedLogMoves.contains(LogMovePosition.of(logMoveEdge)));
		}
	}

	public synchronized void setStatus(String s) {
		panel.getStatusLabel().setText(s);
	}

	public synchronized void setAnimationStatus(String s, boolean isTime) {
		if (isTime) {
			panel.getAnimationTimeLabel().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			panel.getAnimationTimeLabel().setText(s);
		} else {
			panel.getAnimationTimeLabel().setFont(panel.getStatusLabel().getFont());
			panel.getAnimationTimeLabel().setText(s);
		}
	}

	/**
	 * Call all colouring filters to initialise their guis.
	 * 
	 * @param xLog
	 * @param executor
	 */
	private void initialiseColourFilters(final XLog xLog, Executor executor) {
		final Runnable onUpdate = new Runnable() {
			public void run() {
				chain.execute(Cl08FilterNodeSelection.class);
			}
		};
		for (final ColouringFilter colouringFilter : state.getColouringFilters()) {
			executor.execute(new Runnable() {
				public void run() {
					colouringFilter.initialiseFilter(xLog, onUpdate);
					panel.getColouringFiltersView().setPanel(colouringFilter, onUpdate);
				}
			});
		}
	}

	/**
	 * update the highlighting
	 */
	public void updateHighlighting() {
		TraceViewColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(panel.getGraph()
				.getSVG(), state.getVisualisationInfo(), state.getTree(), state.getVisualisationData(), state.getMode()
				.getVisualisationParameters(state));
		colourMap.setSelectedNodes(state.getSelectedNodes(), state.getSelectedLogMoves());
		panel.getTraceView().setColourMap(colourMap);
	}

	private void updatePopup() {
		if (panel.getGraph().getMouseInElements().isEmpty()) {
			panel.getGraph().setShowPopup(false);
		} else {
			//output statistics about the node
			DotElement element = panel.getGraph().getMouseInElements().iterator().next();
			if (element instanceof LocalDotNode) {
				UnfoldedNode unode = ((LocalDotNode) element).getUnode();
				if (state.isAlignmentReady() && unode.getNode() instanceof Manual) {
					List<String> popup = new ArrayList<>();

					//frequencies
					popup.add("frequency             "
							+ IvMLogMetrics.getNumberOfTracesRepresented(unode, false, state.getIvMLogInfoFiltered()));

					//waiting time
					if (state.isPerformanceReady()) {
						if (state.getPerformance().getWaitingTime(unode) > -0.1) {
							popup.add("average waiting time  "
									+ Performance.timeToString((long) state.getPerformance().getWaitingTime(unode)));
						} else {
							popup.add("average waiting time  -");
						}
					} else {
						popup.add(" ");
					}

					//queueing time
					if (state.isPerformanceReady()) {
						if (state.getPerformance().getWaitingTime(unode) > -0.1) {
							popup.add("average queueing time "
									+ Performance.timeToString((long) state.getPerformance().getQueueingTime(unode)));
						} else {
							popup.add("average queueing time -");
						}
					} else {
						popup.add(" ");
					}

					//service time
					if (state.isPerformanceReady()) {
						if (state.getPerformance().getServiceTime(unode) > -0.1) {
							popup.add("average service time  "
									+ Performance.timeToString((long) state.getPerformance().getServiceTime(unode)));
						} else {
							popup.add("average service time  -");
						}
					} else {
						popup.add(" ");
					}

					//sojourn time
					if (state.isPerformanceReady()) {
						if (state.getPerformance().getSojournTime(unode) > -0.1) {
							popup.add("average sojourn time  "
									+ Performance.timeToString((long) state.getPerformance().getSojournTime(unode)));
						} else {
							popup.add("average sojourn time  -");
						}
					} else {
						popup.add(" ");
					}

					panel.getGraph().setPopup(popup);
					panel.getGraph().setShowPopup(true);
				} else {
					panel.getGraph().setShowPopup(false);
				}
			} else {
				panel.getGraph().setShowPopup(false);
			}
		}
	}

	public static void debug(Object s) {
		System.out.println(s);
	}
}
