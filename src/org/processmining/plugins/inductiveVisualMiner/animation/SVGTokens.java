package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;

public class SVGTokens {

	private List<SVGToken> tokens = new ArrayList<SVGToken>();

	public void addTrace(String trace, double endTime, boolean fade) {
		SVGToken token = new SVGToken(fade);
		token.addTrace(trace, endTime);
		tokens.add(token);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (SVGToken token : tokens) {
			result.append(token.toString());
		}
		return result.toString();
	}
}
