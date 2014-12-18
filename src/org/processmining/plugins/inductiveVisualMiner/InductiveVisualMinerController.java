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

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.InductiveMiner.Classifiers.ClassifierWrapper;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState.ColourMode;
import org.processmining.plugins.inductiveVisualMiner.TraceView.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.ComputeAlignment;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.ComputeTimedLog;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedLog;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilterPluginFinder;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFiltersView;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ComputeColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAnimation;
import org.processmining.plugins.inductiveVisualMiner.export.ExportModel;
import org.processmining.plugins.inductiveVisualMiner.export.SaveAsDialog;
import org.processmining.plugins.inductiveVisualMiner.export.SaveAsDialog.FileType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.Chain;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ChainLink;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerController {

	private final InductiveVisualMinerPanel panel;
	private final InductiveVisualMinerState state;
	private static final int maxAnimatedTraces = 50;

	public class ResettableCanceller implements Canceller {

		private boolean cancelled = false;

		public void cancel() {
			this.cancelled = true;
		}

		public void reset() {
			this.cancelled = false;
		}

		public boolean isCancelled() {
			return cancelled;
		}

	}
	
	private class IMLog2IMLogInfo {
		
	}

	//make an IMlog out of an XLog
	private class MakeLog extends
			ChainLink<Triple<XLog, XEventClassifier, IMLog2IMLogInfo>, Triple<XLogInfo, IMLog, IMLogInfo>> {

		protected Triple<XLog, XEventClassifier, IMLog2IMLogInfo> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveModelButton().setEnabled(false);
			panel.getSaveImageButton().setEnabled(false);
			panel.getSaveImageButton().setText("image");
//			return Triple.of(state.getXLog(), state.getMiningParameters().getClassifier(), state.getMiningParameters().getLog2LogInfo());
			return Triple.of(state.getXLog(), state.getMiningParameters().getClassifier(), new IMLog2IMLogInfo());
		}

		protected Triple<XLogInfo, IMLog, IMLogInfo> executeLink(Triple<XLog, XEventClassifier, IMLog2IMLogInfo> input) {
			setStatus("Making log..");

			IMLog imLog = new IMLog(input.getA(), input.getB());
//			IMLogInfo imLogInfo = input.getC().createLogInfo(imLog);
			IMLogInfo imLogInfo = null;
			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(input.getA(), input.getB());

			return Triple.of(xLogInfo, imLog, imLogInfo);
		}

		protected void processResult(Triple<XLogInfo, IMLog, IMLogInfo> result) {
			state.setLog(result.getA(), result.getB(), result.getC());
		}

		public void cancel() {

		}
	}

	//filter the log using activities threshold
	private class FilterLog
			extends
			ChainLink<Quadruple<IMLog, IMLogInfo, Double, MiningParameters>, Triple<IMLog, IMLogInfo, Set<XEventClass>>> {

		protected Quadruple<IMLog, IMLogInfo, Double, MiningParameters> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveModelButton().setEnabled(false);
			panel.getSaveImageButton().setEnabled(false);
			panel.getSaveImageButton().setText("image");
			return new Quadruple<IMLog, IMLogInfo, Double, MiningParameters>(state.getLog(), state.getLogInfo(),
					state.getActivitiesThreshold(), state.getMiningParameters());
		}

		protected Triple<IMLog, IMLogInfo, Set<XEventClass>> executeLink(
				Quadruple<IMLog, IMLogInfo, Double, MiningParameters> input) {
			if (input.getC() < 1.0) {
				return FilterLeastOccurringActivities.filter(input.getA(), input.getB(), input.getC(), input.getD());
			} else {
				return new Triple<IMLog, IMLogInfo, Set<XEventClass>>(input.getA(), input.getB(),
						new HashSet<XEventClass>());
			}
		}

		protected void processResult(Triple<IMLog, IMLogInfo, Set<XEventClass>> result) {
			state.setActivityFilteredIMLog(result.getA(), result.getB(), result.getC());

			panel.getTraceView().set(state.getLog());
		}

		public void cancel() {

		}
	}

	//mine a model
	private class Mine extends ChainLink<Triple<ProcessTree, IMLog, MiningParameters>, ProcessTree> {

		protected Triple<ProcessTree, IMLog, MiningParameters> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveImageButton().setText("image");
			panel.getTraceView().set(state.getLog());
			return Triple.of(state.getPreMinedTree(), state.getActivityFilteredIMLog(), state.getMiningParameters());
		}

		protected ProcessTree executeLink(Triple<ProcessTree, IMLog, MiningParameters> input) {
			setStatus("Mining..");
			if (input.getA() == null) {
				//mine a new tree
				ProcessTree tree = IMProcessTree.mineProcessTree(input.getB(), input.getC());
				System.out.println("Mining done.");
				return tree;
			} else {
				//use the existing tree
				return input.getA();
			}
		}

		protected void processResult(ProcessTree result) {
			state.setTree(result);
			state.setSelectedNodes(new HashSet<UnfoldedNode>());
			state.setSelectedLogMoves(new HashSet<LogMovePosition>());
			state.resetAlignment();

			panel.getSaveModelButton().setEnabled(true);
			panel.getSaveImageButton().setEnabled(true);
			setStatus("Layouting model..");

			//deviation from chain: already show the model, without alignment
			//this is to not have the user wait for the alignment without visual feedback
			panel.updateModel(state);
		}

		public void cancel() {

		}
	}

	//compute alignment
	private class Align extends ChainLink<Quadruple<ProcessTree, XEventClassifier, XLog, IMLogInfo>, AlignmentResult> {

		private ResettableCanceller canceller = new ResettableCanceller();

		protected Quadruple<ProcessTree, XEventClassifier, XLog, IMLogInfo> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveImageButton().setText("image");
			return new Quadruple<ProcessTree, XEventClassifier, XLog, IMLogInfo>(state.getTree(), state
					.getMiningParameters().getClassifier(), state.getXLog(), state.getLogInfo());
		}

		protected AlignmentResult executeLink(Quadruple<ProcessTree, XEventClassifier, XLog, IMLogInfo> input) {
			setStatus("Computing alignment..");
			canceller.reset();
			return ComputeAlignment.computeAlignment(input.getA(), input.getB(), input.getC(), input.getD(), canceller);
		}

		protected void processResult(AlignmentResult result) {
			state.setAlignment(result);
		}

		public void cancel() {
			canceller.cancel();
		}

	}

	//perform layout
	private class Layout extends ChainLink<Object, Object> {

		protected Object generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveImageButton().setText("image");
			//if the view does not show log moves anymore, do not select any nodes
			if (state.getColourMode() == ColourMode.paths) {
				state.setSelectedLogMoves(new HashSet<LogMovePosition>());
			}
			return null;
		}

		protected SVGDiagram executeLink(Object input) {
			setStatus("Layouting graph..");
			return null;
		}

		protected void processResult(Object result) {
			Pair<Dot, AlignedLogVisualisationInfo> p = panel.updateModel(state);
			state.setLayout(p.getLeft(), p.getRight());
			makeNodesSelectable(state.getVisualisationInfo(), panel, state.getSelectedNodes(),
					state.getSelectedLogMoves());
		}

		public void cancel() {

		}
	}

	//filter log for node selection
	private class FilterNodeSelection
			extends
			ChainLink<Septuple<AlignedLog, Set<UnfoldedNode>, Set<LogMovePosition>, AlignedLogInfo, XLog, XLogInfo, List<ColouringFilter>>, Triple<AlignedLog, AlignedLogInfo, XLog>> {

		private ResettableCanceller canceller = new ResettableCanceller();

		protected Septuple<AlignedLog, Set<UnfoldedNode>, Set<LogMovePosition>, AlignedLogInfo, XLog, XLogInfo, List<ColouringFilter>> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveImageButton().setText("image");
			return Septuple.of(state.getAlignedLog(), state.getSelectedNodes(), state.getSelectedLogMoves(),
					state.getAlignedLogInfo(), state.getXLog(), state.getXLogInfo(), state.getColouringFilters());
		}

		protected Triple<AlignedLog, AlignedLogInfo, XLog> executeLink(
				Septuple<AlignedLog, Set<UnfoldedNode>, Set<LogMovePosition>, AlignedLogInfo, XLog, XLogInfo, List<ColouringFilter>> input) {
			setStatus("Highlighting selection..");

			canceller.reset();

			//apply colouring filters
			Triple<AlignedLog, AlignedLogInfo, XLog> colouringFilteredAlignment = ComputeColouringFilter
					.applyColouringFilter(input.getA(), input.getD(), input.getE(), input.getF(), input.getG(),
							canceller);

			//apply node/edge selection filters
			if (!input.getB().isEmpty() || !input.getC().isEmpty()) {
				return filterOnSelection(colouringFilteredAlignment.getA(), input.getB(), input.getC(),
						colouringFilteredAlignment.getC());
			} else {
				return colouringFilteredAlignment;
			}

		}

		protected void processResult(Triple<AlignedLog, AlignedLogInfo, XLog> result) {
			state.setAlignedFilteredLog(result.getA(), result.getB(), result.getC());

			panel.getTraceView().set(state.getAlignedFilteredLog());
		}

		public void cancel() {
			canceller.cancel();
		}
	}

	private class TimeLog extends ChainLink<Triple<AlignedLog, XLog, XLogInfo>, TimedLog> {

		private ResettableCanceller canceller = new ResettableCanceller();

		protected Triple<AlignedLog, XLog, XLogInfo> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveImageButton().setText("image");
			return Triple.of(state.getAlignedFilteredLog(), state.getAlignedFilteredXLog(), state.getXLogInfo());
		}

		protected TimedLog executeLink(Triple<AlignedLog, XLog, XLogInfo> input) {
			setStatus("Creating timed log..");
			canceller.reset();
			return ComputeTimedLog.computeTimedLog(input.getA(), input.getB(), input.getC(), canceller);
		}

		protected void processResult(TimedLog result) {
			state.setTimedLog(result);

			//update the trace view
			panel.getTraceView().set(result);
		}

		public void cancel() {
			canceller.cancel();
		}

	}

	//prepare animation
	private class Animate extends
			ChainLink<Quintuple<TimedLog, ColourMode, AlignedLogVisualisationInfo, Dot, SVGDiagram>, SVGDiagram> {

		private ResettableCanceller canceller = new ResettableCanceller();

		protected Quintuple<TimedLog, ColourMode, AlignedLogVisualisationInfo, Dot, SVGDiagram> generateInput() {
			panel.getGraph().setEnableAnimation(false);
			panel.getSaveImageButton().setText("image");
			return Quintuple.of(state.getTimedLog(), state.getColourMode(), state.getVisualisationInfo(), panel
					.getGraph().getDot(), panel.getGraph().getSVG());
		}

		protected SVGDiagram executeLink(
				Quintuple<TimedLog, ColourMode, AlignedLogVisualisationInfo, Dot, SVGDiagram> input) {
			setStatus("Creating animation..");
			canceller.reset();

			return ComputeAnimation.computeAnimation(input.getA(), input.getB(), input.getC(), maxAnimatedTraces,
					input.getD(), input.getE(), canceller);
		}

		protected void processResult(SVGDiagram result) {
			if (result == null) {
				return;
			}

			panel.getGraph().setImage(result, false);
			panel.getGraph().setEnableAnimation(true);
			panel.getSaveImageButton().setText("animation");

			//re-colour the selected nodes (i.e. the dashed red border)
			for (UnfoldedNode unode : state.getSelectedNodes()) {
				LocalDotNode dotNode = Animation.getDotNodeFromActivity(unode, state.getVisualisationInfo());
				InductiveVisualMinerSelectionColourer.colourSelectedNode(panel.getGraph().getSVG(), dotNode, true);
			}
			//re-colour the selected log moves
			for (LogMovePosition logMove : state.getSelectedLogMoves()) {
				LocalDotEdge dotEdge = Animation.getDotEdgeFromLogMove(logMove, state.getVisualisationInfo());
				InductiveVisualMinerSelectionColourer.colourSelectedEdge(panel.getGraph().getSVG(), dotEdge, true);
			}
		}

		public void cancel() {
			canceller.cancel();
		}

	}

	//colour the nodes
	private class ApplyNodeSelectionColouring extends ChainLink<AlignedLogInfo, AlignedLogInfo> {

		protected AlignedLogInfo generateInput() {
			return state.getAlignedFilteredLogInfo();
		}

		protected AlignedLogInfo executeLink(AlignedLogInfo input) {
			return input;
		}

		protected void processResult(AlignedLogInfo result) {
			TraceViewColourMap colourMap = InductiveVisualMinerSelectionColourer.colour(panel.getGraph().getSVG(),
					state.getVisualisationInfo(), state.getTree(), result,
					InductiveVisualMinerPanel.getViewParameters(state));
			ColouringFiltersView.updateSelectionDescription(panel, state.getSelectedNodes(),
					state.getSelectedLogMoves(), state.getColouringFilters(), state.getAlignedFilteredLog().size(),
					maxAnimatedTraces);

			//make a colour map for the trace view
			panel.getTraceView().setColourMap(colourMap);
			colourMap.setSelectedNodes(state.getSelectedNodes(), state.getSelectedLogMoves());

			setStatus(" ");
			panel.repaint();
		}

		public void cancel() {

		}

	}

	private final Chain chain;
	private final UIPluginContext context;

	public InductiveVisualMinerController(UIPluginContext context, InductiveVisualMinerPanel panel,
			InductiveVisualMinerState state) {
		this.panel = panel;
		this.state = state;
		this.context = context;

		//initialise gui handlers
		initGui();

		chain = new Chain(context.getExecutor());
		chain.add(new MakeLog());
		chain.add(new FilterLog());
		chain.add(new Mine());
		chain.add(new Align());
		chain.add(new Layout());
		chain.add(new FilterNodeSelection());
		chain.add(new ApplyNodeSelectionColouring());
		chain.add(new TimeLog());
		chain.add(new Animate());
		chain.add(new ApplyNodeSelectionColouring());

		//set up plug-ins
		List<ColouringFilter> colouringFilters = ColouringFilterPluginFinder.findFilteringPlugins(context, panel,
				state.getXLog());
		state.setColouringFilters(colouringFilters);
		panel.getColouringFiltersView().initialise(colouringFilters);
		initialiseColourFilters(state.getXLog(), context.getExecutor());

		//start the chain
		chain.execute(MakeLog.class);
	}

	private void initGui() {

		//noise filter
		panel.getNoiseSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getNoiseSlider().getSlider().getValueIsAdjusting()) {
					state.getMiningParameters().setNoiseThreshold((float) (1 - panel.getNoiseSlider().getValue()));
					chain.execute(Mine.class);
				}
			}
		});

		//classifier
		panel.getClassifiers().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.getMiningParameters().setClassifier(
						((ClassifierWrapper) panel.getClassifiers().getSelectedItem()).classifier);
				chain.execute(MakeLog.class);
			}
		});

		//activities filter
		panel.getActivitiesSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!panel.getActivitiesSlider().getSlider().getValueIsAdjusting()) {
					state.setActivitiesThreshold(panel.getActivitiesSlider().getValue());
					chain.execute(FilterLog.class);
				}
			}
		});

		//colour mode
		panel.getColourModeSelection().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.setColourMode((ColourMode) panel.getColourModeSelection().getSelectedItem());
				chain.execute(Layout.class);
			}
		});

		//node selection changed
		panel.setOnSelectionChanged(new InputFunction<Pair<Set<UnfoldedNode>, Set<LogMovePosition>>>() {
			public void call(Pair<Set<UnfoldedNode>, Set<LogMovePosition>> input) throws Exception {
				state.setSelectedNodes(input.getA());
				state.setSelectedLogMoves(input.getB());
				chain.execute(FilterNodeSelection.class);
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
								Dot2Image.dot2image(state.getDot(), p.getLeft(), Type.pdf);
							}
						}).start();
						break;
					case pngImage :
						//save the file asynchronously
						new Thread(new Runnable() {
							public void run() {
								Dot2Image.dot2image(state.getDot(), p.getLeft(), Type.png);
							}
						}).start();
						break;
					case svgImage :
						//save the file asynchronously
						new Thread(new Runnable() {
							public void run() {
								Dot2Image.dot2image(state.getDot(), p.getLeft(), Type.svg);
							}
						}).start();
						break;
					case aviMovie :
					//save avi asynchronously
					{
						final SVGDiagram svg = panel.getGraph().getSVG();
						final ColourMode colourMode = state.getColourMode();
						final Dot dot = state.getDot();
						final TimedLog timedLog = state.getTimedLog();
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
						final Dot dot = state.getDot();
						final TimedLog timedLog = state.getTimedLog();
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

	private static Triple<AlignedLog, AlignedLogInfo, XLog> filterOnSelection(AlignedLog alignedLog,
			Set<UnfoldedNode> selectedNodes, Set<LogMovePosition> selectedLogMoves, XLog xLog) {

		AlignedLog fl = new AlignedLog();
		boolean useNodes = !selectedNodes.isEmpty();
		boolean useLogMoves = !selectedLogMoves.isEmpty();
		for (AlignedTrace trace : alignedLog) {
			for (Move move : trace) {
				if (useNodes && move.isModelSync() && selectedNodes.contains(move.getUnode())) {
					fl.add(trace, alignedLog.getCardinalityOf(trace));
					break;
				}
				if (useLogMoves && move.isLogMove() && selectedLogMoves.contains(LogMovePosition.of(move))) {
					fl.add(trace, alignedLog.getCardinalityOf(trace));
					break;
				}
			}

		}

		AlignedLogInfo fli = new AlignedLogInfo(fl);
		return Triple.of(fl, fli, xLog);
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
				chain.execute(FilterNodeSelection.class);
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
