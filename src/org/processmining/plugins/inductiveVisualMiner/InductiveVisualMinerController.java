package org.processmining.plugins.inductiveVisualMiner;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState.ColourMode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogSplitter;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentETM;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.Animation;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationSVG;
import org.processmining.plugins.inductiveVisualMiner.animation.ExportAnimation;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;
import org.processmining.plugins.inductiveVisualMiner.animation.Tokens;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.Chain;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ChainLink;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.logFiltering.FilterLeastOccurringActivities;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerController {

	private final InductiveVisualMinerPanel panel;
	private final InductiveVisualMinerState state;

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

	//make an IMlog out of an XLog
	private class MakeLog extends ChainLink<Pair<XLog, XEventClassifier>, Triple<XLogInfo, IMLog, IMLogInfo>> {

		protected Pair<XLog, XEventClassifier> generateInput() {
			return new Pair<XLog, XEventClassifier>(state.getXLog(), state.getMiningParameters().getClassifier());
		}

		protected Triple<XLogInfo, IMLog, IMLogInfo> executeLink(Pair<XLog, XEventClassifier> input) {
			setStatus("Making log..");

			IMLog imLog = new IMLog(input.getLeft(), input.getRight());
			IMLogInfo imLogInfo = new IMLogInfo(imLog);
			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(input.getLeft(), input.getRight());

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
		}

		public void cancel() {

		}
	}

	//mine a model
	private class Mine extends ChainLink<Pair<IMLog, MiningParameters>, ProcessTree> {

		protected Pair<IMLog, MiningParameters> generateInput() {
			return new Pair<IMLog, MiningParameters>(state.getActivityFilteredIMLog(), state.getMiningParameters());
		}

		protected ProcessTree executeLink(Pair<IMLog, MiningParameters> input) {
			setStatus("Mining..");
			return IMProcessTree.mineProcessTree(input.getLeft(), input.getRight());
		}

		protected void processResult(ProcessTree result) {
			state.setTree(result);
			state.setSelectedNodes(new HashSet<UnfoldedNode>());
			state.resetAlignment();

			//deviation from chain: already show the model, without alignment
			//this is to not have the user wait for the alignment without visual feedback
			try {
				panel.updateModel(state);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {

		}
	}

	//compute alignment
	private class Align
			extends
			ChainLink<Quintuple<ProcessTree, XEventClassifier, XLog, Set<XEventClass>, IMLogInfo>, Pair<AlignmentResult, Map<UnfoldedNode, AlignedLogInfo>>> {

		private ResettableCanceller canceller = new ResettableCanceller();

		protected Quintuple<ProcessTree, XEventClassifier, XLog, Set<XEventClass>, IMLogInfo> generateInput() {
			return new Quintuple<ProcessTree, XEventClassifier, XLog, Set<XEventClass>, IMLogInfo>(state.getTree(),
					state.getMiningParameters().getClassifier(), state.getXLog(), state.getFilteredActivities(),
					state.getLogInfo());
		}

		protected Pair<AlignmentResult, Map<UnfoldedNode, AlignedLogInfo>> executeLink(
				Quintuple<ProcessTree, XEventClassifier, XLog, Set<XEventClass>, IMLogInfo> input) {
			setStatus("Computing alignment..");
			canceller.reset();
			return computeAlignment(input.getA(), input.getB(), input.getC(), input.getD(), input.getE(), canceller);
		}

		protected void processResult(Pair<AlignmentResult, Map<UnfoldedNode, AlignedLogInfo>> result) {
			state.setAlignment(result.getLeft(), result.getRight());
		}

		public void cancel() {
			canceller.cancel();
		}

	}

	//perform layout
	private class Layout extends ChainLink<Object, SVGDiagram> {

		protected Object generateInput() {
			return null;
		}

		protected SVGDiagram executeLink(Object input) {
			setStatus("Layouting graph..");
			return null;
		}

		protected void processResult(SVGDiagram result) {
			try {
				panel.updateModel(state);
				makeNodesSelectable();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {

		}
	}

	//colour node selection
	private class SelectionColouring
			extends
			ChainLink<Septuple<AlignedLog, Set<UnfoldedNode>, Set<UnfoldedNode>, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>, ProcessTree, AlignedLogVisualisationParameters>, Quintuple<AlignedLog, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>, ProcessTree, AlignedLogVisualisationParameters>> {

		protected Septuple<AlignedLog, Set<UnfoldedNode>, Set<UnfoldedNode>, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>, ProcessTree, AlignedLogVisualisationParameters> generateInput() {
			return Septuple.of(state.getAlignedLog(), state.getSelectedNodes(),
					AlignedLogMetrics.getAllDfgNodes(new UnfoldedNode(state.getTree().getRoot())),
					state.getAlignedLogInfo(), state.getDfgLogInfos(), state.getTree(),
					InductiveVisualMinerPanel.getViewParameters(state));
		}

		protected Quintuple<AlignedLog, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>, ProcessTree, AlignedLogVisualisationParameters> executeLink(
				Septuple<AlignedLog, Set<UnfoldedNode>, Set<UnfoldedNode>, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>, ProcessTree, AlignedLogVisualisationParameters> input) {
			setStatus("Colouring selection..");
			if (input.getB().size() > 0) {
				Triple<AlignedLog, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>> c = filterOnSelection(
						input.getA(), input.getB(), input.getC());
				return Quintuple.of(c.getA(), c.getB(), c.getC(), input.getF(), input.getG());
			} else {
				return Quintuple.of(input.getA(), input.getD(), input.getE(), input.getF(), input.getG());
			}

		}

		protected void processResult(
				Quintuple<AlignedLog, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>, ProcessTree, AlignedLogVisualisationParameters> result) {
			state.setAlignedFilteredLog(result.getA(), result.getB(), result.getC());
			InductiveVisualMinerSelectionColourer.colour(panel, result.getD(), result.getB(), result.getC(),
					result.getE());
			updateSelectionDescription(panel, state.getSelectedNodes());
		}

		public void cancel() {

		}

	}

	//prepare animation
	private class Animate extends ChainLink<Quintuple<XLog, AlignedLog, ProcessTree, XLogInfo, ColourMode>, Tokens> {

		protected Quintuple<XLog, AlignedLog, ProcessTree, XLogInfo, ColourMode> generateInput() {
			return Quintuple.of(state.getXLog(), state.getAlignedLog(), state.getTree(), state.getXLogInfo(),
					state.getColourMode());
		}

		protected Tokens executeLink(Quintuple<XLog, AlignedLog, ProcessTree, XLogInfo, ColourMode> input) {
			setStatus("Creating animation..");

			XLog xLog = input.getA();
			AlignedLog aLog = input.getB();
			ProcessTree tree = input.getC();
			XLogInfo xLogInfo = input.getD();
			ColourMode colourMode = input.getE();

			long maxTraces = 1000;

			//gather timestamp information
			Pair<Double, Double> extremeTimstamps = TimestampsAdder.getExtremeTimestamps(xLog, maxTraces);

			//make a log-projection-hashmap
			HashMap<List<XEventClass>, IMTraceG<Move>> map = TimestampsAdder.getIMTrace2AlignedTrace(aLog);

			//make a shortest path graph
			ShortestPathGraph graph = new ShortestPathGraph(panel.getNodes(), panel.getEdges());

			boolean showDeviations = colourMode != ColourMode.paths;
			showDeviations = false;

			//compute the animation
			Tokens tokens = new Tokens();
			{
				long indexTrace = 0;
				for (XTrace trace : xLog) {

					if (indexTrace == maxTraces) {
						break;
					}

					TimedTrace timedTrace = TimestampsAdder.timeTrace(map, trace, xLogInfo, extremeTimstamps,
							indexTrace, showDeviations, graph, panel);
					if (timedTrace != null) {
						Animation.positionTrace(timedTrace, new UnfoldedNode(tree.getRoot()), tokens, graph, panel);
					}

					indexTrace++;
				}
			}

			return tokens;
		}

		protected void processResult(Tokens result) {
			//			System.out.println(result);
			//System.out.println(AnimationSVG.animateTokens(result, panel));
			try {
				OutputStream streamOut = new FileOutputStream("D:\\animationTest.svg");
				ExportAnimation.export(AnimationSVG.animateTokens(result, panel), streamOut, panel);
			} catch (IOException e) {

			}
			setStatus(" ");
		}

		public void cancel() {

		}

	}

	private final Chain chain;

	public InductiveVisualMinerController(PluginContext context, InductiveVisualMinerPanel panel,
			InductiveVisualMinerState state) {
		this.panel = panel;
		this.state = state;

		//initialise gui handlers
		initGui();

		chain = new Chain(context.getExecutor());
		chain.add(new MakeLog());
		chain.add(new FilterLog());
		chain.add(new Mine());
		chain.add(new Align());
		chain.add(new Layout());
		chain.add(new SelectionColouring());
		chain.add(new Animate());

		chain.execute(MakeLog.class);
	}

	private static Pair<AlignmentResult, Map<UnfoldedNode, AlignedLogInfo>> computeAlignment(ProcessTree tree,
			XEventClassifier classifier, XLog xLog, Set<XEventClass> filteredActivities, IMLogInfo logInfo,
			Canceller canceller) {

		//ETM
		AlignmentResult alignment = AlignmentETM.alignTree(tree, classifier, xLog, filteredActivities, canceller);

		//		if (alignment.log.size() == 0) {
		//			//Felix
		//			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(xLog, classifier);
		//			alignment = AlignmentFelix.alignTree(tree, classifier, logInfo, xLog, xLogInfo, filteredActivities);
		//		}

		//Arya
		//		AlignmentResult alignment = AlignmentArya.alignTree(tree, classifier, logInfo, null, xLog, filteredActivities);

		Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos = computeDfgAlignment(alignment.log, tree);

		return Pair.of(alignment, dfgLogInfos);
	}

	public static Map<UnfoldedNode, AlignedLogInfo> computeDfgAlignment(AlignedLog log, ProcessTree tree) {
		Map<UnfoldedNode, AlignedLogInfo> result = new HashMap<ProcessTree2Petrinet.UnfoldedNode, AlignedLogInfo>();

		for (UnfoldedNode unode : AlignedLogMetrics.getAllDfgNodes(new UnfoldedNode(tree.getRoot()))) {
			result.put(unode, new AlignedLogInfo(AlignedLogSplitter.getLog(unode, log)));
		}

		return result;
	}

	private void initGui() {

		//set the default classifier
		for (int i = 0; i < panel.getClassifiers().getItemCount(); i++) {
			if (((XEventClassifier) panel.getClassifiers().getItemAt(i)).name().equals("Event Name")) {
				panel.getClassifiers().setSelectedIndex(i);
				break;
			}
		}

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
				state.getMiningParameters().setClassifier((XEventClassifier) panel.getClassifiers().getSelectedItem());
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
		panel.setOnSelectionChanged(new InputFunction<Set<UnfoldedNode>>() {
			public void call(Set<UnfoldedNode> input) throws Exception {
				state.setSelectedNodes(input);
				chain.execute(SelectionColouring.class);
			}
		});
	}

	private void makeNodesSelectable() {
		for (UnfoldedNode unode : panel.getActivity2dotNode().keySet()) {
			LocalDotNode dotNode = panel.getActivity2dotNode().get(unode);
			panel.makeNodeSelectable(dotNode, state.getSelectedNodes().contains(unode));
		}
	}

	public synchronized void setStatus(String s) {
		panel.getStatusLabel().setText(s);
	}

	private static Triple<AlignedLog, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>> filterOnSelection(
			AlignedLog alignedLog, Set<UnfoldedNode> selected, Set<UnfoldedNode> dfgNodes) {

		AlignedLog fl = new AlignedLog();
		for (IMTraceG<Move> trace : alignedLog) {
			for (Move move : trace) {
				if (selected.contains(move.getUnode())) {
					fl.add(trace, alignedLog.getCardinalityOf(trace));
					break;
				}
			}

		}

		AlignedLogInfo fli = new AlignedLogInfo(fl);
		Map<UnfoldedNode, AlignedLogInfo> fldfg = computeDfgAlignment(fl, dfgNodes);
		return new Triple<AlignedLog, AlignedLogInfo, Map<UnfoldedNode, AlignedLogInfo>>(fl, fli, fldfg);
	}

	private static Map<UnfoldedNode, AlignedLogInfo> computeDfgAlignment(AlignedLog log, Set<UnfoldedNode> dfgNodes) {
		Map<UnfoldedNode, AlignedLogInfo> result = new HashMap<ProcessTree2Petrinet.UnfoldedNode, AlignedLogInfo>();

		for (UnfoldedNode unode : dfgNodes) {
			result.put(unode, new AlignedLogInfo(AlignedLogSplitter.getLog(unode, log)));
		}

		return result;
	}

	private static void updateSelectionDescription(InductiveVisualMinerPanel panel, Set<UnfoldedNode> selected) {
		//show the user which traces are shown
		if (selected.size() == 0) {
			panel.getSelectionLabel().setText("Showing all traces\n");
		} else {
			String s = "Showing traces that (should) pass through ";
			Iterator<UnfoldedNode> it = selected.iterator();
			{
				s += it.next().getNode();
			}
			while (it.hasNext()) {
				String p = it.next().getNode().toString();
				if (it.hasNext()) {
					s += ", " + p;
				} else {
					s += " or " + p;
				}
			}
			panel.getSelectionLabel().setText(s);
		}
	}
}
