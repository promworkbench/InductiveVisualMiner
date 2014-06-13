package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;

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

	public Double getLastTime() {
		for (int i = points.size() - 1; i >= 0; i--) {
			if (points.get(i).getRight() != null) {
				return points.get(i).getRight();
			}
		}
		return startTime;
	}

	public LocalDotNode getLastPosition() {
		if (points.isEmpty()) {
			return startPosition;
		}
		return points.get(points.size() - 1).getLeft().getTarget();
	}

	public LocalDotNode getTarget(int index) {
		if (index == -1) {
			return getStartPosition();
		} else {
			return points.get(index).getLeft().getTarget();
		}
	}

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

		performSanityCheck();

	}

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

	private void performSanityCheck() {
		//perform sanity check
		double last;
		if (getStartTime() != null) {
			last = getStartTime();
		} else {
			last = Double.NEGATIVE_INFINITY;
		}
		for (Pair<LocalDotEdge, Double> p : points) {
			if (p.getRight() != null) {
				if (p.getRight() < last) {
					System.out.println("===========");
					System.out.println(this);
					throw new RuntimeException("token cannot move back in time");
				}
				last = p.getRight();
			}
		}
	}

	public void addPoint(LocalDotEdge edge, Double time) {
		points.add(Pair.of(edge, time));
		subTokens.add(new HashSet<Token>());

		performSanityCheck();

		//				System.out.println("  add point to token " + points.get(points.size() - 1) + fade);
		//		
		//				System.out.println("   to " + edge.getTarget().toString().replaceAll("\\n", " "));
	}

	public void addSubToken(Token token) {
		subTokens.get(subTokens.size() - 1).add(token);
//		System.out.println("  add subtoken at " + (subTokens.size() - 1));
	}

	public List<Pair<LocalDotEdge, Double>> getPoints() {
		return Collections.unmodifiableList(points);
	}

	public Set<Token> getSubTokensAtPoint(int index) {
		return subTokens.get(index);
	}

	public Set<Token> getLastSubTokens() {
		return subTokens.get(subTokens.size() - 1);
	}

	public LocalDotNode getStartPosition() {
		return startPosition;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;

		performSanityCheck();
	}

	public Double getStartTime() {
		return startTime;
	}

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
}
