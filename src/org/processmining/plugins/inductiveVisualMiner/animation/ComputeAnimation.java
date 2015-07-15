package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.Trace2DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken.DotToken2GraphVizToken;
import org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.colouringmode.ColouringMode;

import com.kitfox.svg.SVGDiagram;

public class ComputeAnimation {

	public static final double initDuration = 1;
	public static final double animationDuration = 180;
	private static Random random = new Random(123);

	public static Pair<Scaler, GraphVizTokens> computeAnimation(final IvMLog ivmLog, final ColouringMode colourMode,
			final AlignedLogVisualisationInfo info, final SVGDiagram svg,
			final Canceller canceller) {

		//scale timestamps
		final Scaler scaler = Scaler.fromLog(ivmLog, initDuration, animationDuration, canceller);

		if (canceller.isCancelled()) {
			return null;
		}

		//make a shortest path graph
		final ShortestPathGraph graph = new ShortestPathGraph(info.getNodes(), info.getEdges());

		if (canceller.isCancelled()) {
			return null;
		}

		//make dot tokens
		final List<DotToken> dotTokens = computeDotTokens(ivmLog, info, colourMode, scaler, graph, canceller);

		if (canceller.isCancelled()) {
			return null;
		}

		//make graphviz tokens
		GraphVizTokens graphVizTokens = DotToken2GraphVizToken.convert(dotTokens, svg);

		if (canceller.isCancelled()) {
			return null;
		}

		return Pair.of(scaler, graphVizTokens);
	}

	public static List<DotToken> computeDotTokens(Iterable<IvMTrace> timedLog, final AlignedLogVisualisationInfo info,
			final ColouringMode colourMode, Scaler scaler, ShortestPathGraph graph, final Canceller canceller) {
		boolean showDeviations = colourMode.isShowDeviations();
		final List<DotToken> tokens = new ArrayList<>();
		for (IvMTrace timedTrace : timedLog) {
			try {
				//guess start and end time of the trace
				timedTrace.setStartTime(guessStartTime(timedTrace, graph, info, scaler));
				timedTrace.setEndTime(guessEndTime(timedTrace, timedTrace.getStartTime(), graph, info, scaler));

				//compute the tokens of this trace
				tokens.add(Trace2DotToken.trace2token(timedTrace, showDeviations, graph, info, scaler));
			} catch (Exception e) {
				//for the demo, just ignore this case
				InductiveVisualMinerController.debug(timedTrace);
			}

			if (canceller.isCancelled()) {
				return null;
			}
		}
		return tokens;
	}

	public static double guessStartTime(List<IvMMove> trace, ShortestPathGraph shortestGraph,
			AlignedLogVisualisationInfo info, Scaler scaler) {
		//find the first timed move
		for (int i = 0; i < trace.size(); i++) {
			IvMMove firstTimedMove = trace.get(i);
			if (firstTimedMove.getLogTimestamp() != null) {
				//the trace ends with a fixed initialisation time
				return scaler.logTime2UserTime(firstTimedMove.getLogTimestamp() - scaler.getInitialisationInLogTime());
			}
		}

		return randomStartTime();
	}

	public static double guessEndTime(List<IvMMove> trace, double startTime, ShortestPathGraph shortestGraph,
			AlignedLogVisualisationInfo info, Scaler scaler) {
		//find the last timed move
		for (int i = trace.size() - 1; i >= 0; i--) {
			IvMMove lastTimedMove = trace.get(i);
			if (lastTimedMove.getLogTimestamp() != null) {
				//the trace ends with a fixed initialisation time
				return scaler.logTime2UserTime(lastTimedMove.getLogTimestamp() + scaler.getInitialisationInLogTime());
			}
		}

		return randomDuration(startTime);
	}

	private static double randomStartTime() {
		double startTime;
		startTime = random.nextInt((int) (animationDuration - 10));
		return startTime;
	}

	private static double randomDuration(double startTime) {
		return startTime + random.nextInt((int) ((animationDuration - startTime) - 10)) + 10;
	}
}
