package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.animation.shortestPath.ShortestPathGraph;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.LogSplitter;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class Trace2Token {

	public static Token trace2token(TimedTrace trace, boolean showDeviations, ShortestPathGraph shortestPath,
			AlignedLogVisualisationInfo info) {

		debug("", 0);

		//copy the trace
		List<TimedMove> copyTrace = new ArrayList<TimedMove>(trace);

		Token token = trace2token(copyTrace, Pair.of(info.getSource(), trace.getStartTime()),
				Pair.of(info.getSink(), trace.getEndTime()), new ArrayList<UnfoldedNode>(), showDeviations,
				shortestPath, info, 0);

		//interpolate the missing timestamps from the token
		InterpolateToken.interpolateToken(token);

		debug(token, 0);

		return token;
	}

	public static Token trace2token(List<TimedMove> trace, Pair<LocalDotNode, Double> startPosition,
			Pair<LocalDotNode, Double> endPosition, List<UnfoldedNode> inParallelUnodes, boolean showDeviations,
			ShortestPathGraph shortestPath, AlignedLogVisualisationInfo info, int indent) {

		debug("translate trace " + trace, indent);
		
		Token token = new Token(startPosition.getLeft(), startPosition.getRight(), inParallelUnodes.isEmpty());
		List<UnfoldedNode> localInParallelUnodes = new ArrayList<>(inParallelUnodes);

		//walk through the trace
		for (int i = 0; i < trace.size(); i++) {
			TimedMove move = trace.get(i);

			if (move.isLogMove()) {
				debug(" consider move " + move + " (" + move.getLogMoveUnode() + " before " + move.getLogMoveBeforeChild() + ")", indent);
			} else {
				debug(" consider move " + move, indent);
			}

			//see if we are leaving a parallel subtrace
			for (int j = localInParallelUnodes.size() - 1; j >= 0; j--) {
				UnfoldedNode parallel = localInParallelUnodes.get(j);
				if (!isInNode(move, parallel)) {
					//we are not in this parallel unode anymore, remove from the list

					localInParallelUnodes.remove(j);

					exitParallel(parallel, token, shortestPath, info, indent);
				} else {
					break;
				}
			}

			if (move.isModelSync() && entersParallel(move, localInParallelUnodes) != null) {
				//we are entering a parallel subtree

				//find out the parallel unode we are entering
				UnfoldedNode parallel = entersParallel(move, localInParallelUnodes);

				//find out where we are exiting it again
				int exitsAt = findParallelExit(trace, parallel, i);

				debug("  entering parallel " + parallel + " with move " + move, indent);
				debug("  will exit at " + exitsAt, indent);

				//extract parallel subtrace we're entering
				List<TimedMove> parallelSubtrace = trace.subList(i, exitsAt + 1);
				debug("  parallel subtrace " + parallelSubtrace, indent);

				//spit the subtrace into sublogs
				List<List<TimedMove>> subsubTraces = splitTrace(parallel, parallelSubtrace);
				debug("  subsub traces " + subsubTraces, indent);

				//move the token up to the parallel split
				LocalDotNode parallelSplit = Animation.getParallelSplit(parallel, info);
				{
					List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), parallelSplit);
					for (LocalDotEdge edge : path) {
						token.addPoint(edge, null);
					}
				}

				//alter the trace:
				//remove the parallel subtrace, and replace with an arbitrary subsubtrace
				{
					for (int j = i; j < exitsAt + 1; j++) {
						trace.remove(i);
					}
					List<TimedMove> subsubTrace = subsubTraces.get(0);
					for (int j = 0; j < subsubTrace.size(); j++) {
						trace.add(i + j, subsubTrace.get(j));
					}
				}

				debug(" trace after parallel reduction " + trace, indent);

				//denote that we have entered this parallel unode (prevents infinite loops)
				localInParallelUnodes.add(parallel);

				//recurse on other subsubTraces
				LocalDotNode parallelJoin = Animation.getParallelJoin(parallel, info);
				for (int j = 1; j < subsubTraces.size(); j++) {
					List<TimedMove> subsubTrace = subsubTraces.get(j);
					List<UnfoldedNode> childInParallelUnodes = new ArrayList<>(localInParallelUnodes);
					Token childToken = trace2token(subsubTrace, Pair.of(parallelSplit, (Double) null),
							Pair.of(parallelJoin, (Double) null), childInParallelUnodes, showDeviations, shortestPath,
							info, indent + 1);

					token.addSubToken(childToken);
				}

				//sanity check
				if (token.getLastSubTokens().size() == 0) {
					debug(token, 0);
					debug("no subtokens", 0);
					throw new RuntimeException("no subtokens created");
				}

				//continue, and in the next iteration re-process the same move
				i--;
				continue;
			} else if (move.getUnode() != null && move.getUnode().getNode() instanceof Automatic) {
				//tau, by definition synchronous

				//move from the last known position to the start of the tau edge,
				//then take the move tau itself
				LocalDotEdge tauEdge = Animation.getTauEdge(move, info);

				List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), tauEdge.getSource());
				for (LocalDotEdge edge : path) {
					token.addPoint(edge, null);
				}

				token.addPoint(tauEdge, null);

			} else if (move.getType() == Type.synchronous || (move.getType() == Type.model && !showDeviations)) {
				//synchronous move or model move without deviations showing
				LocalDotNode nextDestination = Animation.getDotNodeFromActivity(move, info);

				//move from the last known position to the new position
				//the last edge gets a timestamp
				List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), nextDestination);
				if (!path.isEmpty()) {
					for (int j = 0; j < path.size() - 1; j++) {
						token.addPoint(path.get(j), null);
					}
					token.addPoint(path.get(path.size() - 1), move.getTimestamp());
				}

			} else if (move.getType() == Type.model) {
				//model move, showing deviations

				//move from the last known position to the start of the move edge,
				//then take the move edge itself
				LocalDotEdge moveEdge = Animation.getModelMoveEdge(move, info);

				List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), moveEdge.getSource());
				for (LocalDotEdge edge : path) {
					token.addPoint(edge, null);
				}

				token.addPoint(moveEdge, null);
			} else if (move.getType() == Type.log) {
				//log move (should be filtered out if not showing deviations)
				LocalDotEdge moveEdge = Animation.getLogMoveEdge(move.getLogMoveUnode(), move.getLogMoveBeforeChild(),
						info);

				//move from the last known position to the start of the move edge,
				//then take the move edge itself
				List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), moveEdge.getSource());
				for (LocalDotEdge edge : path) {
					token.addPoint(edge, null);
				}

				//add the move edge
				token.addPoint(moveEdge, move.getTimestamp());
			}
		}

		//exit remaining parallel unodes
		for (UnfoldedNode parallel : localInParallelUnodes) {
			if (!inParallelUnodes.contains(parallel)) {
				exitParallel(parallel, token, shortestPath, info, indent);
			}
		}

		//add path to final destination
		List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), endPosition.getLeft());
		for (int j = 0; j < path.size() - 1; j++) {
			token.addPoint(path.get(j), null);
		}
		if (path.size() != 0) {
			token.addPoint(path.get(path.size() - 1), endPosition.getRight());
		} else {
			//the trace has already ended, fill in the end time
			token.setTimestampOfPoint(token.getPoints().size() - 1, endPosition.getRight());
		}

		return token;
	}

	/*
	 * leave a parallel unode
	 */
	private static void exitParallel(UnfoldedNode parallel, Token token, ShortestPathGraph shortestPath,
			AlignedLogVisualisationInfo info, int indent) {
		//move the token to the parallel join
		LocalDotNode parallelJoin = Animation.getParallelJoin(parallel, info);
		List<LocalDotEdge> path = shortestPath.getShortestPath(token.getLastPosition(), parallelJoin);
		for (LocalDotEdge edge : path) {
			token.addPoint(edge, null);
		}

		debug("  leaving parallel " + parallel, indent);
	}

	/*
	 * returns the parallel unode that is being entered to move, if any
	 * inParallelUnodes parallel nodes are not reported
	 */
	private static UnfoldedNode entersParallel(TimedMove move, List<UnfoldedNode> inParallelUnodes) {

		if (move == null) {
			//there's nothing being entered here
			return null;
		}

		UnfoldedNode unode = new UnfoldedNode(move.getUnode().getPath().get(0));

		if (move.getUnode().getPath().get(0) instanceof And && !inParallelUnodes.contains(unode)) {
			return unode;
		}

		for (int i = 1; i < move.getUnode().getPath().size(); i++) {
			unode = unode.unfoldChild(move.getUnode().getPath().get(i));
			if (unode.getNode() instanceof And && !inParallelUnodes.contains(unode)) {
				return unode;
			}
		}
		return null;
	}

	/*
	 * finds the position of the last move in trace (from offset) that is still
	 * in unode
	 */
	private static int findParallelExit(List<TimedMove> trace, UnfoldedNode unode, int offset) {
		for (int i = offset + 1; i < trace.size(); i++) {
			Move move = trace.get(i);
			if (!isInNode(move, unode)) {
				return i - 1;
			}
		}
		return trace.size() - 1;
	}

	/*
	 * return whether the move happened in unode
	 */
	private static boolean isInNode(Move move, UnfoldedNode unode) {
		List<Node> path1 = new ArrayList<>(move.getPositionUnode().getPath());
		List<Node> path2 = unode.getPath();

		Iterator<Node> it1 = path1.iterator();

		//the path of 2 must be in 1
		for (Node node : path2) {
			if (!it1.hasNext()) {
				return false;
			}

			if (!node.equals(it1.next())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * split a trace according to a node
	 */
	public static List<List<TimedMove>> splitTrace(UnfoldedNode unode, List<TimedMove> trace) {

		LogSplitter.SigmaMaps<TimedMove> maps = LogSplitter.makeSigmaMaps(unode);

		//split the trace
		for (TimedMove move : trace) {
			Set<UnfoldedNode> sigma = maps.mapUnode2sigma.get(move.getPositionUnode());
			if (sigma != null) {
				maps.mapSigma2subtrace.get(sigma).add(move);
			} else {
				//this is a log move that was mapped on unode itself and not on one of its children
				//put it in the mapped sigma
				sigma = maps.mapUnode2sigma.get(move.getLogMoveParallelBranchMappedTo());
				if (sigma != null) {
					maps.mapSigma2subtrace.get(sigma).add(move);
				} else {
					//put it on the first branch
					maps.sublogs.get(0).add(move);
				}
			}
		}

		return maps.sublogs;
	}

	private static void debug(Object s, int indent) {
		//		String sIndent = new String(new char[indent]).replace("\0", "   ");
		//		System.out.print(sIndent);
		//				System.out.println(s);
		//				System.out.println(s.toString().replaceAll("\\n", " "));
	}
}
