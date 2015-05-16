package org.processmining.plugins.inductiveVisualMiner.animation;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState.ColourMode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMMove.Scaler;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.Trace2DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.animation.svgToken.DotTokens2SVGtokens;
import org.processmining.plugins.inductiveVisualMiner.animation.svgToken.SVGTokens;
import org.processmining.plugins.inductiveVisualMiner.export.ExportAnimation;

import com.google.common.collect.FluentIterable;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class ComputeAnimation {

	public static double animationDuration = 20;
	public static double beginEndEdgeDuration = 1;
	private static Random random = new Random(123);

	public static Pair<SVGDiagram, Scaler> computeAnimation(final IvMLog timedLog, final ColourMode colourMode,
			final AlignedLogVisualisationInfo info, final int maxTraces, final Dot dot, final SVGDiagram svg,
			final Canceller canceller) {
		
		//filter the log to show only the first traces in the animation
		final Iterable<IvMTrace> filteredTimedLog = FluentIterable.from(timedLog).limit(maxTraces);
		return computeAnimation(filteredTimedLog, colourMode, info, dot, svg, canceller);
	}

	public static Pair<SVGDiagram, Scaler> computeAnimation(final Iterable<IvMTrace> timedLog, final ColourMode colourMode,
			final AlignedLogVisualisationInfo info, final Dot dot, final SVGDiagram svg, final Canceller canceller) {

		Pair<SVGTokens, Scaler> p = computeSVGTokens(timedLog, info, colourMode, svg, canceller);
		if (p == null) {
			return null;
		}
		final SVGTokens animatedTokens = p.getA();
		if (canceller.isCancelled()) {
			return null;
		}

		//make an svg and read it in directly
		try {
			PipedInputStream svgStream = ExportAnimation.copy(new Function<PipedOutputStream, Object>() {
				public Object call(PipedOutputStream input) throws Exception {
					ExportAnimation.exportSVG(animatedTokens, input, dot);
					return null;
				}
			});

			SVGUniverse universe = new SVGUniverse();
			return Pair.of(universe.getDiagram(universe.loadSVG(svgStream, "anim")), p.getB());
		} catch (IOException e) {
			return null;
		}
	}

	public static Pair<SVGTokens, Scaler> computeSVGTokens(final Iterable<IvMTrace> timedLog,
			final AlignedLogVisualisationInfo info, final ColourMode colourMode, final SVGDiagram svg,
			final Canceller canceller) {
		//make a shortest path graph
		final ShortestPathGraph graph = new ShortestPathGraph(info.getNodes(), info.getEdges());

		//scale timestamps
		final Scaler scaler = getScaler(timedLog, canceller);
		if (canceller.isCancelled()) {
			return null;
		}

		//compute tokens
		final List<DotToken> tokens = computeTokens(timedLog, info, colourMode, scaler, graph, canceller);
		if (canceller.isCancelled()) {
			return null;
		}

		return Pair.of(DotTokens2SVGtokens.animateTokens(tokens, svg), scaler);
	}

	public static List<DotToken> computeTokens(Iterable<IvMTrace> timedLog,
			final AlignedLogVisualisationInfo info, final ColourMode colourMode, Scaler scaler,
			ShortestPathGraph graph, final Canceller canceller) {
		boolean showDeviations = colourMode != ColourMode.paths;
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

	public static Scaler getScaler(Iterable<IvMTrace> timedLog, final Canceller canceller) {
		long logMin = Long.MAX_VALUE;
		long logMax = Long.MIN_VALUE;
		for (IvMTrace trace : timedLog) {
			for (IvMMove move : trace) {
				if (move.getLogTimestamp() != null) {
					logMin = Math.min(logMin, move.getLogTimestamp());
					logMax = Math.max(logMax, move.getLogTimestamp());
				}
			}

			if (canceller.isCancelled()) {
				return null;
			}
		}
		if (logMin == Double.MAX_VALUE) {
			return null;
		}
		return new Scaler(animationDuration, logMin, logMax);
	}

	public static double guessStartTime(List<IvMMove> trace, ShortestPathGraph shortestGraph,
			AlignedLogVisualisationInfo info, Scaler scaler) {
		//find the first timed move
		IvMMove firstTimedMove = null;
		int firstTimedMoveIndex;
		for (firstTimedMoveIndex = 0; firstTimedMoveIndex < trace.size(); firstTimedMoveIndex++) {
			if (trace.get(firstTimedMoveIndex).getLogTimestamp() != null) {
				firstTimedMove = trace.get(firstTimedMoveIndex);
				break;
			}
		}

		if (firstTimedMove == null) {
			return randomStartTime();
		}

		//find the edges the trace is going through before the first timed move
		List<IvMMove> partialTrace;
		try {
			partialTrace = trace.subList(0, firstTimedMoveIndex + 1);
		} catch (Exception e) {
			throw e;
		}

		//the trace ends with 2 seconds per edge
		return firstTimedMove.getScaledTimestamp(scaler)
				- Animation.getEdgesOnMovePath(partialTrace, shortestGraph, info, true, false).size()
				* beginEndEdgeDuration;
	}

	public static double guessEndTime(List<IvMMove> trace, double startTime, ShortestPathGraph shortestGraph,
			AlignedLogVisualisationInfo info, Scaler scaler) {
		//find the last timed move
		IvMMove lastTimedMove = null;
		int lastTimedMoveIndex;
		for (lastTimedMoveIndex = trace.size() - 1; lastTimedMoveIndex >= 0; lastTimedMoveIndex--) {
			if (trace.get(lastTimedMoveIndex).getScaledTimestamp(scaler) != null) {
				lastTimedMove = trace.get(lastTimedMoveIndex);
				break;
			}
		}

		if (lastTimedMove == null) {
			return randomDuration(startTime);
		}

		//find the edges the trace is going through after the last timed move
		List<IvMMove> partialTrace = trace.subList(lastTimedMoveIndex, trace.size());

		//the trace ends with 2 seconds per edge
		return lastTimedMove.getScaledTimestamp(scaler)
				+ Animation.getEdgesOnMovePath(partialTrace, shortestGraph, info, false, true).size()
				* beginEndEdgeDuration;
	}

	private static double randomStartTime() {
		double startTime;
		startTime = random.nextInt((int) (animationDuration - 10));
		return startTime;
	}

	private static double randomDuration(double startTime) {
		double traceDuration = 10 + random.nextInt((int) (animationDuration - (startTime + 10)));
		return traceDuration;
	}
}
