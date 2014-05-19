package org.processmining.plugins.inductiveVisualMiner.animation;

import java.util.ArrayList;
import java.util.List;

public class SVGTokens {

	private List<SVGToken> tokens = new ArrayList<SVGToken>();

	public void addTrace(String trace, double endTime) {
//		for (SVGToken token : tokens) {
//			if (token.getEndTime() < endTime) {
//				token.addTrace(trace, endTime);
//				return;
//			}
//		}
		SVGToken token = new SVGToken();
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
