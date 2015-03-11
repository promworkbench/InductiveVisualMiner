package org.processmining.plugins.inductiveVisualMiner.animation.svgToken;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotTokenStep;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;

public class DotTokens2SVGtokens {

	public static SVGTokens animateTokens(List<DotToken> tokens, SVGDiagram svg) {
		SVGTokens svgTokens = new SVGTokens();
		for (DotToken token : tokens) {
			for (DotToken subToken : token.getAllTokensRecursively()) {
				animateToken(subToken, svgTokens, svg);
			}
		}
		return svgTokens;
	}

	public static void animateToken(DotToken token, SVGTokens svgTokens, SVGDiagram svg) {
		StringBuilder result = new StringBuilder();

		if (!token.isAllTimestampsSet()) {
			assert (token.isAllTimestampsSet());
		}

		//fade in
		fade(token.isFade(), true, token.getStartTime(), null, result);

		for (int i = 0; i < token.size(); i++) {
			animateDotTokenStep(token, i, result, svg);
		}

		//fade out
		fade(token.isFade(), false, null, token.getLastTime(), result);

		svgTokens.addTrace(result.toString(), token.getLastTime(), token.isFade());
	}

	public static double fadeDuration = 0.5;

	public static void fade(boolean fade, boolean trueInFalseOut, Double beginTime, Double endTime, StringBuilder result) {
		//we would use set if svgsalamander would support
		//it does not, so we have to use an animate tag
		double fadeDuration2;
		if (!fade) {
			fadeDuration2 = 0.05;
			fade = true;
		} else {
			fadeDuration2 = fadeDuration;
		}

		if (fade) {
			result.append("\t<animate ");
			result.append("attributeName='opacity' ");
			result.append("attributeType='XML' ");

			result.append("from='");
			if (trueInFalseOut) {
				result.append("0");
			} else {
				result.append("1");
			}
			result.append("' ");

			result.append("to='");
			if (trueInFalseOut) {
				result.append("1");
			} else {
				result.append("0");
			}
			result.append("' ");

			result.append("begin='");
			if (beginTime != null) {
				result.append(beginTime);
			} else {
				result.append(endTime - fadeDuration2);
			}
			result.append("s' ");

			result.append("dur='");
			result.append(fadeDuration2);
			result.append("s' ");

			result.append("fill='freeze'/>\n");
		} else {
			if (trueInFalseOut) {
				//fade in
				result.append("\t<set attributeName='visibility' attributeType='XML' to='visible' begin='");
				if (beginTime != null) {
					result.append(beginTime);
				} else {
					result.append(endTime);
				}
				result.append("s' fill='freeze'/>\n");
			} else {
				//fade out
				result.append("\t<set attributeName='visibility' attributeType='XML' to='hidden' begin='");
				if (beginTime != null) {
					result.append(beginTime);
				} else {
					result.append(endTime);
				}
				result.append("s' fill='freeze'/>\n");
			}
		}
	}

	/**
	 * Animate a token over one dotTokenStep.
	 * @param dotToken
	 * @param stepIndex
	 * @param result
	 * @param svg
	 */
	public static void animateDotTokenStep(DotToken dotToken, int stepIndex, StringBuilder result, SVGDiagram svg) {
		DotTokenStep step = dotToken.get(stepIndex);
		if (step.isOverEdge()) {
			animateDotTokenStepEdge(dotToken, stepIndex, result, svg);
		} else {
			animateDotTokenStepNode(dotToken, stepIndex, result, svg);
		}
	}

	/**
	 * Animate a token over one edge.
	 * @param dotToken
	 * @param stepIndex
	 * @param result
	 * @param svg
	 */
	public static void animateDotTokenStepEdge(DotToken dotToken, int stepIndex, StringBuilder result, SVGDiagram svg) {
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
		double duration = endTime - startTime;

		//get the svg-line with the edge
		SVGElement SVGline = DotPanel.getSVGElementOf(svg, edge).getChild(1);

		//get the start node
		LocalDotNode startNode = edge.getSource();
		
		//compute the path
//		String path = "M" + getCenter(startNode, svg);
		
		String path = "M" + getSourceLocation(edge, svg);
		if (edge.isDirectionForward()) {
			path += "L" + DotPanel.getAttributeOf(SVGline, "d").substring(1);
		} else {
			path += reversePath(DotPanel.getAttributeOf(SVGline, "d"));
		}
		path += "L" + getCenter(edge.getTarget(), svg);

		//put it all together
		result.append("\t<animateMotion ");
		result.append("path='");
		result.append(path);
		result.append("' begin='");
		result.append(startTime);
		result.append("s' dur='");
		result.append(duration);
		result.append("s' ");
		if (stepIndex == dotToken.size() - 1) {
			result.append("fill='freeze'");
		}
		result.append("/>\n");
	}
	
	public static void animateDotTokenStepNode(DotToken dotToken, int stepIndex, StringBuilder result, SVGDiagram svg) {
		DotTokenStep step = dotToken.get(stepIndex);
		
		
	}
	
	private static String getSourceLocation(LocalDotEdge edge, SVGDiagram image) {
		SVGElement SVGline = DotPanel.getSVGElementOf(image, edge).getChild(1);
		String path = DotPanel.getAttributeOf(SVGline, "d");
		Matcher matcher = pattern.matcher(path);
		matcher.find();
		String point = matcher.group();
		return point;
	}

	private static String getCenter(LocalDotNode node, SVGDiagram image) {
		Group nodeGroup = DotPanel.getSVGElementOf(image, node);
		Rectangle2D bb = null;
		try {
			bb = nodeGroup.getBoundingBox();
		} catch (SVGException e) {
			e.printStackTrace();
		}
		double centerX = bb.getCenterX();
		double centerY = bb.getCenterY();
		return centerX + "," + centerY;
	}

	private static final Pattern pattern = Pattern.compile("-?(\\d*\\.)?\\d+,-?(\\d*\\.)?\\d+");

	/**
	 * 
	 * @param path
	 * @return The reversed path, assuming the original path consists of one move/line followed by one or
	 * more cubic bezier curves.
	 */
	public static String reversePath(String path) {

		//get the points from the path
		Matcher matcher = pattern.matcher(path);

		List<String> points = new ArrayList<String>();
		while (matcher.find()) {
			points.add(matcher.group());
		}

		//reverse the list of points
		Collections.reverse(points);

		//output as a new path
		StringBuilder result = new StringBuilder();
		Iterator<String> it = points.iterator();

		result.append("L");
		result.append(it.next());

		try {
			while (it.hasNext()) {
				result.append("C");
				result.append(it.next());
				result.append(" ");
				result.append(it.next());
				result.append(" ");
				result.append(it.next());
			}
		} catch (NoSuchElementException e) {
			return path;
		}

		return result.toString();
	}
}
