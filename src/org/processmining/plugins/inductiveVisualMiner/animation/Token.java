package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode.NodeType;

public class Token {
	private final LocalDotNode startPosition;
	private Double startTime;
	private final boolean fade;
	private List<Pair<LocalDotEdge, Double>> points;
	private List<Set<Token>> subTokens;

	public Token(LocalDotNode startPosition, Double startTime, boolean fade) {
		//		System.out.println("create new token @" + startTime);
		this.startPosition = startPosition;
		this.startTime = startTime;
		this.fade = fade;
		points = new ArrayList<Pair<LocalDotEdge, Double>>();
		subTokens = new ArrayList<>();
	}

	/**
	 * Returns the last known time stamp in this token; Can return null.
	 * 
	 * @return
	 */
	public Double getLastTime() {
		for (int i = points.size() - 1; i >= 0; i--) {
			if (points.get(i).getRight() != null) {
				return points.get(i).getRight();
			}
		}
		return startTime;
	}

	/**
	 * Returns the last known dot node in this token.
	 * 
	 * @return
	 */
	public LocalDotNode getLastPosition() {
		if (points.isEmpty()) {
			return startPosition;
		}
		return points.get(points.size() - 1).getLeft().getTarget();
	}

	/**
	 * Gets the target dot node after the edge of the given index. -1 gives the
	 * start position
	 * 
	 * @param index
	 * @return
	 */
	public LocalDotNode getTarget(int index) {
		if (index == -1) {
			return getStartPosition();
		} else {
			return points.get(index).getLeft().getTarget();
		}
	}

	/**
	 * Gets the time stamp after the edge of the given index. -1 gives the start
	 * time
	 * 
	 * @param index
	 * @return
	 */
	public Double getTimestamp(int index) {
		if (index == -1) {
			return getStartTime();
		} else {
			return points.get(index).getRight();
		}
	}

	public void setTimestampOfPoint(int index, Double timestamp) {
		//set this token
		points.set(index, Pair.of(points.get(index).getLeft(), timestamp));

		//set all subtokens
		for (Token subToken : subTokens.get(index)) {
			subToken.setStartTime(timestamp);
		}

		performSanityCheck(null);

	}

	/**
	 * Returns a set containing this token and all its subtokens.
	 * 
	 * @return
	 */
	public Set<Token> getAllTokensRecursively() {
		Set<Token> result = new HashSet<>();
		result.add(this);
		for (int i = 0; i < points.size(); i++) {
			for (Token subToken : getSubTokensAtPoint(i)) {
				result.addAll(subToken.getAllTokensRecursively());
			}
		}
		return result;
	}

	private double performSanityCheck(Double last2) {
		//perform sanity check
		double last;
		if (last2 != null) {
			last = last2;
		} else if (getStartTime() != null) {
			last = getStartTime();
		} else {
			last = Double.NEGATIVE_INFINITY;
		}

		if (last2 != null && last < last2) {
			System.out.println("===========");
			System.out.println(this);
			throw new RuntimeException("token cannot move back in time");
		}

		for (int i = 0; i < points.size(); i++) {
			Pair<LocalDotEdge, Double> p = points.get(i);

			if (p.getRight() != null) {
				if (p.getRight() < last) {
					System.out.println("===========");
					System.out.println(this);
					throw new RuntimeException("token cannot move back in time");
				}
				last = p.getRight();
			}

			//check sub tokens
			for (Token subToken : getSubTokensAtPoint(i)) {
				subToken.performSanityCheck(last);
			}
		}

		return last;
	}

	public void addPoint(LocalDotEdge edge, Double time) {
		points.add(Pair.of(edge, time));
		subTokens.add(new HashSet<Token>());

		performSanityCheck(null);

		//				System.out.println("  add point to token " + points.get(points.size() - 1) + fade);
		//		
		//				System.out.println("   to " + edge.getTarget().toString().replaceAll("\\n", " "));
	}

	public void addSubToken(Token token) {

		//check whether this sub token is added to a parallel split
		if (getLastPosition().getType() != NodeType.parallelSplit) {
			throw new RuntimeException("A sub token can only be added to a parallel split node.");
		}

		subTokens.get(subTokens.size() - 1).add(token);
		//		System.out.println("  add subtoken at " + (subTokens.size() - 1));
	}

	/**
	 * Returns a list of all edges and timestamps in this token. Note: does not
	 * return the start time.
	 * 
	 * @return
	 */
	public List<Pair<LocalDotEdge, Double>> getPoints() {
		return Collections.unmodifiableList(points);
	}

	/**
	 * Returns a set of tokens that start after the given index. Subtokens
	 * cannot start at the start of the token, so -1 is not a valid input.
	 * 
	 * @param index
	 * @return
	 */
	public Set<Token> getSubTokensAtPoint(int index) {
		return subTokens.get(index);
	}

	/**
	 * Returns the set of subtokens that start at the last known position in
	 * this token.
	 * 
	 * @return
	 */
	public Set<Token> getLastSubTokens() {
		return subTokens.get(subTokens.size() - 1);
	}

	/**
	 * Returns the start dot node of this token.
	 * 
	 * @return
	 */
	public LocalDotNode getStartPosition() {
		return startPosition;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;

		performSanityCheck(null);
	}

	/**
	 * Returns the start time of this token.
	 * 
	 * @return
	 */
	public Double getStartTime() {
		return startTime;
	}

	/**
	 * Returns whether this token is supposed to be faded in and out at start
	 * and end.
	 * 
	 * @return
	 */
	public boolean isFade() {
		return fade;
	}

	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		String sIndent = new String(new char[indent]).replace("\0", "  ");

		StringBuilder result = new StringBuilder();
		result.append(sIndent);
		result.append("Token starts at " + startPosition.getId() + " @" + startTime);
		result.append("\n");
		for (int i = 0; i < points.size(); i++) {
			Pair<LocalDotEdge, Double> p = points.get(i);
			result.append(sIndent);
			result.append(p.getLeft().getTarget().toString().replaceAll("\\n", " "));
			result.append(" @");
			result.append(p.getRight());
			result.append("\n");

			//subtokens
			Set<Token> sub = subTokens.get(i);
			for (Token token : sub) {
				result.append(token.toString(indent + 1));
			}
		}
		return result.toString();
	}

	/**
	 * Returns whether there are subtokens after the given index. A token cannot
	 * have subtokens at its start position, so -1 is not a valid input.
	 * 
	 * @param index
	 * @return
	 */
	public boolean hasSubTokensAt(int index) {
		return !subTokens.get(index).isEmpty();
	}

	/**
	 * Given an index, returns the index of the parallel join where index is the
	 * parallel split of.
	 * 
	 * @param index
	 * @return
	 */
	public int getParallelDestination(int index) {
		Token subToken = getSubTokensAtPoint(index).iterator().next();
		LocalDotNode parallelJoin = subToken.getLastPosition();

		//search for the parallel join in the token, starting from offset
		//that is, the last parallel join of the first list of matching parallel joins
		for (int i = index + 1; i < points.size(); i++) {
			if (getTarget(i).equals(parallelJoin)) {
				if (index == points.size() - 1 || !getTarget(i + 1).equals(parallelJoin)) {
					return i;
				}
			}
		}

		return -1;
	}

	/**
	 * Returns whether the position after index is a parallel join, i.e. has
	 * ending sub tokens.
	 * 
	 * @param index
	 * @return
	 */
	public boolean isParallelJoin(int index) {
		//check whether this node is a parallel join
		if (getTarget(index).getType() != NodeType.parallelJoin) {
			return false;
		}

		//check whether the next node is a parallel join
		//in that case, this join is caused by a log move
		return index == points.size() - 1 || getTarget(index + 1).getType() != NodeType.parallelJoin;
	}

}
