package org.processmining.plugins.inductiveVisualMiner.animation;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode.NodeType;

public class DotToken implements Iterable<DotTokenStep> {
	private final LocalDotNode startPosition;
	private Double startTime;
	private final boolean fade;
	private final List<DotTokenStep> steps;
	private final List<Set<DotToken>> subTokens;

	public DotToken(LocalDotNode startPosition, Double startTime, boolean fade) {
		//		System.out.println("create new token @" + startTime);
		this.startPosition = startPosition;
		this.startTime = startTime;
		this.fade = fade;
		steps = new ArrayList<>();
		subTokens = new ArrayList<>();
	}

	/**
	 * 
	 * @return the last known time stamp in this token; Can return null.
	 */
	public Double getLastTime() {
		for (int i = steps.size() - 1; i >= 0; i--) {
			if (steps.get(i).hasArrivalTime()) {
				return steps.get(i).getArrivalTime();
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
		if (steps.isEmpty()) {
			return startPosition;
		}
		return steps.get(steps.size() - 1).getDestinationNode();
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
			return steps.get(index).getDestinationNode();
		}
	}

	/**
	 * Gets
	 * 
	 * @param index
	 * @return the arrival time stamp of the step at the given index. -1 gives
	 *         the start time.
	 */
	public Double getTimestamp(int index) {
		if (index == -1) {
			return getStartTime();
		} else if (steps.get(index).hasArrivalTime()) {
			return steps.get(index).getArrivalTime();
		} else {
			return null;
		}
	}

	public void setTimestampOfPoint(int index, Double timestamp) {
		if (timestamp == null) {
			return;
		}
		//set this token
		steps.get(index).setArrivalTime(timestamp);

		//set all subtokens
		for (DotToken subToken : subTokens.get(index)) {
			subToken.setStartTime(timestamp);
		}

		performSanityCheck(null);

	}

	/**
	 * Returns a set containing this token and all its subtokens.
	 * 
	 * @return
	 */
	public Set<DotToken> getAllTokensRecursively() {
		Set<DotToken> result = new HashSet<>();
		result.add(this);
		for (int i = 0; i < steps.size(); i++) {
			for (DotToken subToken : getSubTokensAtPoint(i)) {
				result.addAll(subToken.getAllTokensRecursively());
			}
		}
		return result;
	}

	private double performSanityCheck(Double last2) {
		//perform sanity check

		//check that the token does not walk over a node twice
		boolean lastNode = false;
		for (DotTokenStep step : steps) {
			if (step.isOverEdge()) {
				lastNode = false;
			} else if (lastNode) {
				System.out.println("===========");
				System.out.println(this);
				throw new RuntimeException("token takes two steps over a node");
			} else {
				lastNode = true;
			}
		}

		//check that the token does not move back in time
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

		for (int i = 0; i < steps.size(); i++) {
			DotTokenStep p = steps.get(i);

			if (p.hasArrivalTime()) {
				if (p.getArrivalTime() < last) {
					System.out.println("===========");
					//System.out.println(this);
					throw new RuntimeException("token cannot move back in time");
				}
				last = p.getArrivalTime();
			}

			//check sub tokens
			for (DotToken subToken : getSubTokensAtPoint(i)) {
				subToken.performSanityCheck(last);
			}
		}

		return last;
	}

	public void addStepInNode(LocalDotNode node, Double arrivalTime) {
		steps.add(DotTokenStep.node(node, arrivalTime));
		subTokens.add(new THashSet<DotToken>());
		performSanityCheck(null);
	}

	public void addStepOverEdge(LocalDotEdge edge, Double arrivalTime) {
		steps.add(DotTokenStep.edge(edge, arrivalTime));
		subTokens.add(new THashSet<DotToken>());
		performSanityCheck(null);
	}

	public void addSubToken(DotToken token) {

		//check whether this sub token is added to a concurrent/interleaved/or split
		assert (getLastPosition().getType() == NodeType.concurrentSplit
				|| getLastPosition().getType() == NodeType.interleavedSplit || getLastPosition().getType() == NodeType.orSplit);

		subTokens.get(subTokens.size() - 1).add(token);
		//		System.out.println("  add subtoken at " + (subTokens.size() - 1));
	}

	public DotTokenStep getLastStep() {
		return steps.get(steps.size() - 1);
	}

	/**
	 * Returns a set of tokens that start after the given index. Subtokens
	 * cannot start at the start of the token, so -1 is not a valid input.
	 * 
	 * @param index
	 * @return
	 */
	public Set<DotToken> getSubTokensAtPoint(int index) {
		return subTokens.get(index);
	}

	/**
	 * Returns the set of subtokens that start at the last known position in
	 * this token.
	 * 
	 * @return
	 */
	public Set<DotToken> getLastSubTokens() {
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
		result.append("Token starts at " + startPosition.getType() + " @" + startTime);
		result.append("\n");
		for (int i = 0; i < steps.size(); i++) {
			DotTokenStep p = steps.get(i);
			result.append(sIndent);
			result.append(p.toString());
			result.append("\n");

			//subtokens
			Set<DotToken> sub = subTokens.get(i);
			for (DotToken token : sub) {
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
	public int getTokenDestination(int index) {
		DotToken subToken = getSubTokensAtPoint(index).iterator().next();
		LocalDotNode tokenJoin = subToken.getLastPosition();

		//search for the parallel join in the token, starting from offset
		//that is, the last parallel join of the first list of matching parallel joins
		for (int i = index + 1; i < steps.size(); i++) {
			if (getTarget(i).equals(tokenJoin)) {
				if (index == steps.size() - 1 || !getTarget(i + 1).equals(tokenJoin)) {
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
		if (getTarget(index).getType() != NodeType.concurrentJoin && getTarget(index).getType() != NodeType.orJoin
				&& getTarget(index).getType() != NodeType.interleavedJoin) {
			return false;
		}

		//check whether the next node is not the same
		//in that case, this join is caused by a log move
		if (getTarget(index) == getTarget(index + 1)) {
			return false;
		}
		return true;

	}

	/**
	 * Returns whether all timestamps have been set
	 * 
	 * @return
	 */
	public boolean isAllTimestampsSet() {
		if (getStartTime() == null) {
			return false;
		}
		for (int i = 0; i < steps.size() - 1; i++) {
			if (!steps.get(i).hasArrivalTime()) {
				return false;
			}

			for (DotToken subToken : getSubTokensAtPoint(i)) {
				if (!subToken.isAllTimestampsSet()) {
					return false;
				}
			}
		}

		return true;
	}

	//collection-like functions

	public Iterator<DotTokenStep> iterator() {
		return Collections.unmodifiableList(steps).iterator();
	}

	public int size() {
		return steps.size();
	}

	public DotTokenStep get(int i) {
		return steps.get(i);
	}
}
