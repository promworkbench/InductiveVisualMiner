package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class DotTokenInterpolate {

	/*
	 * interpolate the missing timestamps in a token and adjust the token to
	 * them
	 */
	public static void interpolateToken(DotToken token) {
		double lastSeenTimestamp = token.getStartTime();

		//walk over the edges to assign timestamps
		for (int i = 0; i < token.size(); i++) {

			if (token.getTimestamp(i) == null) {
				//this time stamp needs to be filled in

				//compute the time stamp for this point

				Pair<Integer, Double> splitForwardTimestamp = getTimestampForward(token, i, Integer.MAX_VALUE,
						lastSeenTimestamp, 1);
				double splitTimestamp = getLocalArrivalTime(1, splitForwardTimestamp.getLeft(), lastSeenTimestamp,
						splitForwardTimestamp.getRight());
				token.setTimestampOfPoint(i, splitTimestamp);
			}

			if (isTokenSplit(token, i) && token.hasSubTokensAt(i)) {
				//if this node is a parallel split, we need to set the corresponding join now
				int indexOfJoin = token.getTokenDestination(i);

				//get the latest time stamp from the parallel branches
				Pair<Integer, Double> joinBackwardsTimestamp = getTimestampBackward(token, indexOfJoin, 0);
				debug(" backward timestamp " + joinBackwardsTimestamp);

				//now compute the time stamp of the join
				Pair<Integer, Double> joinForwardTimestamp = getTimestampForward(token, indexOfJoin, Integer.MAX_VALUE,
						joinBackwardsTimestamp.getRight(), joinBackwardsTimestamp.getLeft());
				debug(" forward timestamp " + joinForwardTimestamp);

				double joinTimestamp = getLocalArrivalTime(joinBackwardsTimestamp.getLeft(),
						joinForwardTimestamp.getLeft(), joinBackwardsTimestamp.getRight(),
						joinForwardTimestamp.getRight());
				debug(" join timestamp " + joinTimestamp);
				token.setTimestampOfPoint(indexOfJoin, joinTimestamp);

				//process the subtokens
				for (DotToken subToken : token.getSubTokensAtPoint(i)) {
					//set the end time of the subtoken
					subToken.setTimestampOfPoint(subToken.size() - 1, joinTimestamp);

					//interpolate the subtoken
					interpolateToken(subToken);
				}

			}
			debug(token);

			lastSeenTimestamp = token.getTimestamp(i);
		}
	}

	/*
	 * returns the number of edges till the first timestamp and that timestamp,
	 * limited to to; notice: offset means "after the #offset edge in the token"
	 */
	private static Pair<Integer, Double> getTimestampForward(DotToken token, int offset, int to,
			double previousTimestamp, int edgesFromPreviousTimestamp) {

		debug(" get timestamp: offset " + offset + ", to " + to + ", edges from previous timestamp "
				+ edgesFromPreviousTimestamp);

		//if we have reached the end of the token, return the token's end time
		if (offset == token.size()) {
			return Pair.of(edgesFromPreviousTimestamp, token.getLastTime());
		}

		//if we are at to, return nothing
		if (offset == to) {
			return Pair.of(edgesFromPreviousTimestamp, null);
		}

		DotTokenStep thisPoint = token.get(offset);

		//if this point has a timestamp, return that
		if (thisPoint.hasArrivalTime()) {
			return Pair.of(edgesFromPreviousTimestamp, thisPoint.getArrivalTime());
		}

		//if this is a parallel split node, we have to find out at what time each branch needs to get a token
		if (token.hasSubTokensAt(offset)) {
			//this is a parallel split node and has sub tokens
			//we could pass a parallel split on our way to a log move, so not every parallel split in the path is used as such

			//see if somewhere in this parallel sub trace, a timestamp is present
			//if multiple, pick the one that needs to arrive first
			int parallelPieceTill = token.getTokenDestination(offset);

			//recurse on the parallel sub trace that is within this token
			//triple(edges, timestamp, arrivalTime)
			int leastEdges;
			Triple<Integer, Double, Double> earliestTimestamp;
			{
				Pair<Integer, Double> subTracePair = getTimestampForward(token, offset + 1, parallelPieceTill,
						previousTimestamp, edgesFromPreviousTimestamp + 1);
				earliestTimestamp = Triple.of(
						subTracePair.getLeft(),
						subTracePair.getRight(),
						getLocalArrivalTime(edgesFromPreviousTimestamp, subTracePair.getLeft(), previousTimestamp,
								subTracePair.getRight()));
				leastEdges = subTracePair.getLeft();
			}

			//recurse on all parallel sub tokens
			Set<DotToken> subTokens = token.getSubTokensAtPoint(offset);
			for (DotToken subToken : subTokens) {

				Pair<Integer, Double> subPair = getTimestampForward(subToken, 0, Integer.MAX_VALUE, previousTimestamp,
						edgesFromPreviousTimestamp + 1);

				leastEdges = Math.min(leastEdges, subPair.getLeft());
				Double localArrival = getLocalArrivalTime(edgesFromPreviousTimestamp, subPair.getLeft(),
						previousTimestamp, subPair.getRight());
				if (localArrival != null
						&& (earliestTimestamp.getC() == null || earliestTimestamp.getC() > localArrival)) {
					earliestTimestamp = Triple.of(subPair.getLeft(), subPair.getRight(), localArrival);
				}
			}

			//see if one of them has a solution and if so, return the best solution
			if (earliestTimestamp.getC() != null) {
				return Pair.of(earliestTimestamp.getA(), earliestTimestamp.getB());
			}

			//if not, move to after the parallel join
			return getTimestampForward(token, parallelPieceTill, to, previousTimestamp, leastEdges);
		} else {
			//if this node is not a parallel split, we move to the next point
			return getTimestampForward(token, offset + 1, to, previousTimestamp, edgesFromPreviousTimestamp + 1);
		}
	}

	/**
	 * Returns the number of edges from the last timestamp and that timestamp;
	 * notice: offset means "after the #offset edge in the token"
	 * 
	 * @param token
	 * @param offset
	 * @param edgesTillNow
	 * @return
	 */
	private static Pair<Integer, Double> getTimestampBackward(DotToken token, int offset, int edgesTillNow) {

		//if this node has a timestamp, we have found one
		//if we hit the beginning of a trace, return the start time (known or not)
		if (token.getTimestamp(offset) != null || offset == -1) {
			return Pair.of(edgesTillNow, token.getTimestamp(offset));
		}

		//if this is the last node of a token,
		//then this is a subtoken. recurse directly
		if (offset == token.size() - 1) {
			return getTimestampBackward(token, offset - 1, edgesTillNow + 1);
		}

		if (token.isParallelJoin(offset)) {
			//this is a parallel join

			//recurse on the parallel sub trace that is within this token
			Pair<Integer, Double> subTracePair = getTimestampBackward(token, offset - 1, edgesTillNow + 1);
			Pair<Integer, Double> bestPair = subTracePair;

			//recurse on all sub tokens that end here, keep track of maximum timestamp
			Set<DotToken> subTokens = getSubTokensOfParallelJoin(token, offset);

			for (DotToken subToken : subTokens) {
				Pair<Integer, Double> subPair = getTimestampBackward(subToken, subToken.size() - 1, edgesTillNow);
				if (bestPair.getRight() == subPair.getRight() && subPair.getLeft() > bestPair.getLeft()) {
					bestPair = subPair;
				} else if (subPair.getRight() != null
						&& (bestPair.getRight() == null || subPair.getRight() > bestPair.getRight())) {
					bestPair = subPair;
				}
			}
			return bestPair;
		} else {
			//if this node is not a parallel join, we move to the next point
			return getTimestampBackward(token, offset - 1, edgesTillNow + 1);
		}
	}

	private static Double getLocalArrivalTime(int edgesFromDeparture, int totalEdges, double departureTime,
			Double arrivalTime) {
		//				debug("  get local arrival, edgesFromDeparture " + edgesFromDeparture + ", totalEdges "
		//						+ totalEdges + ", departure @" + departureTime + ", arrival @" + arrivalTime);
		if (arrivalTime == null) {
			return null;
		}

		//total duration of this part
		double duration = arrivalTime - departureTime;

		//ratio of part that is already travelled
		if (totalEdges == 0) {
			//if there are no edges to be travelled, we are already at the destination
			return departureTime;
		}
		double p = edgesFromDeparture / (1.0 * totalEdges);

		//compute the time with this ratio
		return departureTime + duration * p;
	}

	private static Set<DotToken> getSubTokensOfParallelJoin(DotToken token, int offset) {
		LocalDotNode join = token.getTarget(offset);
		for (int i = offset - 1; i >= 0; i--) {
			//get the sub tokens that start at this point
			Set<DotToken> subTokens = token.getSubTokensAtPoint(i);
			if (!subTokens.isEmpty()) {
				DotToken subToken = subTokens.iterator().next();
				if (subToken.getLastPosition().equals(join)) {
					return subTokens;
				}
			}
		}
		return null;
	}

	public static boolean isTokenSplit(DotToken token, int i) {
		return token.getTarget(i).getType() == NodeType.parallelSplit
				|| token.getTarget(i).getType() == NodeType.interleavedSplit;
	}

	private static void debug(Object s) {
//				System.out.println(s);
		//		System.out.println(s.toString().replaceAll("\\n", " "));
	}
}
