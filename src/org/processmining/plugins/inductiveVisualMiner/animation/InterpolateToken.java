package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.InductiveMiner.Pair;

public class InterpolateToken {
	
	/*
	 * interpolate the missing timestamps in a token
	 * and adjust the token to them
	 */
	public static void interpolateToken(Token token) {
		double lastSeenTimestamp = token.getStartTime();

		for (int i = 0; i < token.getPoints().size(); i++) {
			if (token.getPoints().get(i).getRight() == null) {
				Pair<Integer, Double> p = getNextTimestamp(token, i);
				double newTimestamp = lastSeenTimestamp + (p.getRight() - lastSeenTimestamp) / (p.getLeft() + 1.0);
				token.getPoints().set(i, Pair.of(token.getPoints().get(i).getLeft(), newTimestamp));
			}
			lastSeenTimestamp = token.getPoints().get(i).getRight();
		}
	}

	/*
	 * get the first timestamp != null in a trace, from position i
	 */
	private static Pair<Integer, Double> getNextTimestamp(Token token, int i) {
		for (int j = i + 1; j < token.getPoints().size(); j++) {
			if (token.getPoints().get(j).getRight() != null) {
				return Pair.of(j - i, token.getPoints().get(j).getRight());
			}
		}
		//should never arrive here
		throw new RuntimeException("there should be a known last position in each token");
	}
}
