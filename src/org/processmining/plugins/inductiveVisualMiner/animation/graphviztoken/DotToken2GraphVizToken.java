package org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotTokenStep;
import org.processmining.plugins.inductiveVisualMiner.animation.svgToken.DotTokens2SVGtokens;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;

public class DotToken2GraphVizToken {

	public static GraphVizTokens convert(Iterable<DotToken> tokens, SVGDiagram svg) {
		//convert each token
		GraphVizTokens result = new GraphVizTokens();
		for (DotToken token : tokens) {
			boolean first = true;
			for (DotToken subToken : token.getAllTokensRecursively()) {
				animateToken(subToken, first, first, result, svg);
				first = false;
			}
		}

		return result;
	}

	public static void animateToken(DotToken token, boolean fadeIn, boolean fadeOut, GraphVizTokens result,
			SVGDiagram svg) {
		assert (token.isAllTimestampsSet());

		for (int i = 0; i < token.size(); i++) {
			animateDotTokenStep(token, i, fadeIn && i == 0, fadeOut && i == token.size() - 1, result, svg);
		}
	}

	/**
	 * Record the animation of one dot token step
	 * 
	 * @param dotToken
	 * @param stepIndex
	 * @param result
	 * @param svg
	 */
	public static void animateDotTokenStep(DotToken dotToken, int stepIndex, boolean fadeIn, boolean fadeOut,
			GraphVizTokens result, SVGDiagram svg) {
		DotTokenStep step = dotToken.get(stepIndex);
		if (step.isOverEdge()) {
			animateDotTokenStepEdge(dotToken, stepIndex, fadeIn, fadeOut, result, svg);
		} else {
			animateDotTokenStepNode(dotToken, stepIndex, fadeIn, fadeOut, result, svg);
		}
	}

	/**
	 * Animate a token over one edge
	 * 
	 * @param dotToken
	 * @param stepIndex
	 * @param fadeIn
	 * @param fadeOut
	 * @param result
	 * @param image
	 */
	public static void animateDotTokenStepEdge(DotToken dotToken, int stepIndex, boolean fadeIn, boolean fadeOut,
			GraphVizTokens result, SVGDiagram image) {
		DotTokenStep step = dotToken.get(stepIndex);

		LocalDotEdge edge = step.getEdge();
		double endTime = step.getArrivalTime();

		//get the start time and compute the duration
		double startTime;
		if (stepIndex == 0) {
			startTime = dotToken.getStartTime();
		} else {
			startTime = dotToken.get(stepIndex - 1).getArrivalTime();
		}

		//get the svg-line with the edge
		SVGElement SVGline = DotPanel.getSVGElementOf(image, edge).getChild(1);

		//compute the path
		String path;

		//start the token in its last position
		if (stepIndex == 0 || dotToken.get(stepIndex - 1).isOverEdge()) {
			//If there was no activity before, start at the center of the source node.
			path = "M" + DotTokens2SVGtokens.getCenter(edge.getSource(), image);
		} else {
			//If there was an activity before, then the token was gracefully put on the source already.
			path = "M" + DotTokens2SVGtokens.getSourceLocation(edge, image);
		}

		//move over the edge
		if (edge.isDirectionForward()) {
			path += "L" + DotPanel.getAttributeOf(SVGline, "d").substring(1);
		} else {
			path += DotTokens2SVGtokens.reversePath(DotPanel.getAttributeOf(SVGline, "d"));
		}

		//Leave the token in a nice place.
		if (stepIndex == dotToken.size() - 1 || dotToken.get(stepIndex + 1).isOverEdge()) {
			//If there's no activity afterwards, leave it on the center of the target node.
			path += "L" + DotTokens2SVGtokens.getCenter(edge.getTarget(), image);
		} else {
			//If there is an activity afterwards, move over the arrowhead.
			path += "L" + DotTokens2SVGtokens.getArrowHeadPoint(edge, image);
		}

		//add to the result
		result.add(startTime, endTime, path, fadeIn, fadeOut);
	}

	public static void animateDotTokenStepNode(DotToken dotToken, int stepIndex, boolean fadeIn, boolean fadeOut,
			GraphVizTokens result, SVGDiagram image) {
		DotTokenStep step = dotToken.get(stepIndex);

		//get the start time and compute the duration
		double endTime = step.getArrivalTime();
		double startTime;
		if (stepIndex == 0) {
			startTime = dotToken.getStartTime();
		} else {
			startTime = dotToken.get(stepIndex - 1).getArrivalTime();
		}

		//move to last point on the preceding edge
		String path = "M" + DotTokens2SVGtokens.getArrowHeadPoint(dotToken.get(stepIndex - 1).getEdge(), image);

		//line to the center of the node
		path += "L" + DotTokens2SVGtokens.getCenter(step.getNode(), image);

		//line to the first point on the edge after this 
		path += "L" + DotTokens2SVGtokens.getSourceLocation(dotToken.get(stepIndex + 1).getEdge(), image);

		//put it all together
		result.add(startTime, endTime, path, fadeIn, fadeOut);
	}
}
