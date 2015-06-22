package org.processmining.plugins.inductiveVisualMiner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.Classifiers.ClassifierWrapper;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.graphviz.visualisation.AnimatableSVGPanel.Callback;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState.ColourMode;
import org.processmining.plugins.inductiveVisualMiner.TraceView.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMMove.Scaler;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl01MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl02FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl03Mine;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04Layout;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl05Align;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl06Layout;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl07FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl08ApplyHighlighting;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl09MakeIvMLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl10Animate;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl11Queues;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilterPluginFinder;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFiltersView;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAnimation;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
import org.processmining.plugins.inductiveVisualMiner.export.SaveAsDialog;
import org.processmining.plugins.inductiveVisualMiner.export.SaveAsDialog.FileType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsVisualiser;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class InductiveVisualMinerController {

	final InductiveVisualMinerPanel panel;
	final InductiveVisualMinerState state;
	public static final int maxAnimatedTraces = 50;

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
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getSaveImageButton().setText("image");
					setStatus("Making log..");
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
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveModelButton().setEnabled(false);
					panel.getSaveImageButton().setEnabled(false);
					panel.getSaveImageButton().setText("image");
					setStatus("Filtering activities..");
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
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveImageButton().setText("image");
					panel.getTraceView().set(state.getLog());
					setStatus("Mining..");
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

					panel.getGraph().setEnableAnimation(false);
					panel.getSaveImageButton().setText("image");

					//if the view does not show deviations, do not select any log moves
					if (state.getColourMode() == ColourMode.paths) {
						state.setSelectedLogMoves(new HashSet<LogMovePosition>());
					}
				}
			};
			layoutComplete = new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);

					makeNodesSelectable(state.getVisualisationInfo(), panel, state.getSelectedNodes(),
							state.getSelectedLogMoves());

				}
			};
			Cl04Layout chainLinkLayout = new Cl04Layout();
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
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveImageButton().setText("image");
					setStatus("Aligning log and model..");
				}
			});
			a.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getAlignedLog());
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//layout
		{
			Cl06Layout l = new Cl06Layout();
			l.setOnStart(layoutStart);
			l.setOnComplete(layoutComplete);
			l.setOnException(onException);
			chain.add(l);
		}

		//filter node selection
		{
			Cl07FilterNodeSelection f = new Cl07FilterNodeSelection();
			f.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveImageButton().setText("image");
					setStatus("Highlighting selection..");
				}
			});
			f.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getAlignedFilteredLog());
				}
			});
			f.setOnException(onException);
			chain.add(f);
		}

		//apply highlighting
		{
			Cl08ApplyHighlighting a = new Cl08ApplyHighlighting();
			a.setOnComplete(new Runnable() {
				public void run() {
					AlignedLogInfo result = state.getAlignedFilteredLogInfo();

					TraceViewColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(panel
							.getGraph().getSVG(), state.getVisualisationInfo(), state.getTree(), result,
							InductiveVisualMinerPanel.getViewParameters(state));
					ColouringFiltersView.updateSelectionDescription(panel, state.getSelectedNodes(), state
							.getSelectedLogMoves(), state.getColouringFilters(), state.getAlignedFilteredLog().size(),
							maxAnimatedTraces);

					//tell trace view the colour map and the selection
					panel.getTraceView().setColourMap(colourMap);
					colourMap.setSelectedNodes(state.getSelectedNodes(), state.getSelectedLogMoves());

					panel.repaint();
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//IvM log
		{
			Cl09MakeIvMLog m = new Cl09MakeIvMLog();
			m.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveImageButton().setText("image");
					setStatus("Creating IvM log..");
				}
			});
			m.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getIvMLog());
				}
			});
			m.setOnException(onException);
			chain.add(m);
		}

		//animate
		{
			Cl10Animate a = new Cl10Animate();
			a.setOnStart(new Runnable() {
				public void run() {
					panel.getGraph().setEnableAnimation(false);
					panel.getSaveImageButton().setText("image");
					setStatus("Creating animation..");
				}
			});
			a.setOnComplete(new Runnable() {
				public void run() {
					//re-colour the selected nodes (i.e. the dashed red border)
					InductiveVisualMinerSelectionColourer.colourSelection(state.getAnimatedSVGDiagram(),
							state.getSelectedNodes(), state.getSelectedLogMoves(), state.getVisualisationInfo());

					//re-highlight the model
					TraceViewColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(
							state.getAnimatedSVGDiagram(), state.getVisualisationInfo(), state.getTree(),
							state.getAlignedFilteredLogInfo(), InductiveVisualMinerPanel.getViewParameters(state));

					//tell trace view the colour map and the selection
					panel.getTraceView().setColourMap(colourMap);
					colourMap.setSelectedNodes(state.getSelectedNodes(), state.getSelectedLogMoves());

					//update selection description
					ColouringFiltersView.updateSelectionDescription(panel, state.getSelectedNodes(), state
							.getSelectedLogMoves(), state.getColouringFilters(), state.getAlignedFilteredLog().size(),
							maxAnimatedTraces);

					panel.getSaveImageButton().setText("animation");
					panel.getGraph().setImage(state.getAnimatedSVGDiagram(), false);
					panel.getGraph().setEnableAnimation(true);
					
					setStatus(" ");
				}
			});
			a.setOnException(onException);
			chain.add(a);
		}

		//mine queues
		{
			Cl11Queues q = new Cl11Queues();
			q.setOnStart(new Runnable() {
				public void run() {
					setStatus("Mining queues..");
				}
			});
			q.setOnComplete(new Runnable() {
				public void run() {
					setStatus(" ");
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

		//colour mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setColourMode((ColourMode) panel.getColourModeSelection().getSelectedItem());
				chain.execute(Cl06Layout.class);
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Pair<Set<UnfoldedNode>, Set<LogMovePosition>>>() {
			public void call(Pair<Set<UnfoldedNode>, Set<LogMovePosition>> input) throws Exception {
				state.setSelectedNodes(input.getA());
				state.setSelectedLogMoves(input.getB());
				chain.execute(Cl07FilterNodeSelection.class);
			}
		});

		//graph direction changed
		panel.setOnGraphDirectionChanged(new InputFunction<Dot.GraphDirection>() {
			public void call(GraphDirection input) throws Exception {
				state.setGraphDirection(input);
				chain.execute(Cl06Layout.class);
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
				SaveAsDialog dialog = new SaveAsDialog(panel.getGraph().isEnableAnimation());
				final Pair<File, FileType> p = dialog.askUser(panel);
				if (p == null) {
					return;
				}
				switch (p.getRight()) {
					case pdfImage :
						//save the file asynchronously
						new Thread(new Runnable() {
							public void run() {
								Dot2Image.dot2image(panel.getGraph().getDot(), p.getLeft(), Type.pdf);
							}
						}).start();
						break;
					case pngImage :
						//save the file asynchronously
						new Thread(new Runnable() {
							public void run() {
								Dot2Image.dot2image(panel.getGraph().getDot(), p.getLeft(), Type.png);
							}
						}).start();
						break;
					case svgImage :
						//save the file asynchronously
						new Thread(new Runnable() {
							public void run() {
								Dot2Image.dot2image(panel.getGraph().getDot(), p.getLeft(), Type.svg);
							}
						}).start();
						break;
					case aviMovie :
					//save avi asynchronously
					{
						final SVGDiagram svg = panel.getGraph().getSVG();
						final ColourMode colourMode = state.getColourMode();
						final Dot dot = panel.getGraph().getDot();
						final IvMLog timedLog = state.getIvMLog();
						final AlignedLogVisualisationInfo info = state.getVisualisationInfo();
						new Thread(new Runnable() {
							public void run() {
								try {
									if (!ExportAnimation.saveAVItoFile(timedLog, info, colourMode, svg, dot, p.getA(),
											panel)) {
										System.out.println("deleted");
										p.getA().delete();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
						break;
					case svgMovie :
					//save svg asynchronously
					{
						final SVGDiagram svg = panel.getGraph().getSVG();
						final ColourMode colourMode = state.getColourMode();
						final Dot dot = panel.getGraph().getDot();
						final IvMLog timedLog = state.getIvMLog();
						final AlignedLogVisualisationInfo info = state.getVisualisationInfo();
						new Thread(new Runnable() {
							public void run() {
								try {
									Canceller canceller = new Canceller() {
										public boolean isCancelled() {
											return false;
										}
									};
									ExportAnimation.saveSVGtoFile(timedLog, info, colourMode, svg, canceller, dot,
											p.getA());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
						break;
				}
			}
		});

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

		//set animation time updater
		panel.getGraph().setTimeStepCallback(new Callback<Double, Object>() {
			public Object call(Double arg) {
				Scaler scaler = state.getAnimationTimeScaler();
				if (scaler != null && scaler.getMin() != Long.MAX_VALUE && scaler.getMax() != Long.MIN_VALUE
						&& panel.getGraph().isEnableAnimation()) {
					Long time = scaler.scaleBack(arg);
					panel.getAnimationTimeLabel().setText(TimestampsAdder.toString(time));

					//draw queues
					if (state.getQueueLengths() != null) {
						try {
							QueueLengthsVisualiser.drawQueues(panel.getGraph().getSVG(), state.getTree(),
									state.getVisualisationInfo(), state.getQueueLengths(), time);
						} catch (SVGException e) {
							e.printStackTrace();
						}
					}
				} else {
					panel.getAnimationTimeLabel().setText(" ");

					//undo drawing queues
					QueueLengthsVisualiser.resetQueues(panel);
				}
				return null;
			}
		});
	}

	private static void makeNodesSelectable(AlignedLogVisualisationInfo info, InductiveVisualMinerPanel panel,
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

	/**
	 * Call all colouring filters to initialise their guis.
	 * 
	 * @param xLog
	 * @param executor
	 */
	private void initialiseColourFilters(final XLog xLog, Executor executor) {
		final Runnable onUpdate = new Runnable() {
			public void run() {
				chain.execute(Cl07FilterNodeSelection.class);
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

	public static void debug(Object s) {
		System.out.println(s);
	}
}
