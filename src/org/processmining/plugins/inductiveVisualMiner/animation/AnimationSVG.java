package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotNode;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;

public class AnimationSVG {
	public static SVGTokens animateTokens(Tokens tokens, InductiveVisualMinerPanel panel) {
		
		SVGTokens svgTokens = new SVGTokens();
		for (Token token : tokens) {
			animateToken(token, svgTokens, panel);
		}
		
		return svgTokens;
	}

	public static void animateToken(Token token, SVGTokens svgTokens, InductiveVisualMinerPanel panel) {
		StringBuilder result = new StringBuilder();

		double fadeDuration;
		if (token.getFade()) {
			fadeDuration = 0.5;
		} else {
			fadeDuration = 0.005;
		}

		//fade in
		result.append("<animate ");
		result.append("attributeName='opacity' ");
		result.append("attributeType='XML' ");
		result.append("from='0' ");
		result.append("to='1' ");
		result.append("begin='");
		result.append(token.getStartTime() - (fadeDuration / 2));
		result.append("s' ");
		result.append("dur='");
		result.append(fadeDuration);
		result.append("s' ");
		result.append("fill='freeze'/>");

		for (int i = 0; i < token.getPoints().size(); i++) {
			animatePoint(token, i, result, panel);
		}

		//fade out
		result.append("<animate ");
		result.append("attributeName='opacity' ");
		result.append("attributeType='XML' ");
		result.append("from='1' ");
		result.append("to='0' ");
		result.append("begin='");
		result.append(token.getPoints().get(token.getPoints().size() - 1).getRight() - (fadeDuration / 2));
		result.append("s' ");
		result.append("dur='");
		result.append(fadeDuration);
		result.append("s' ");
		result.append("fill='freeze'/>");

		svgTokens.addTrace(result.toString(), token.getPoints().get(token.getPoints().size() - 1).getRight() + (fadeDuration / 2));
	}

	public static void animatePoint(Token token, int index, StringBuilder result, InductiveVisualMinerPanel panel) {
		Pair<LocalDotEdge, Double> point = token.getPoints().get(index);
		LocalDotEdge edge = point.getLeft();
		double endTime = point.getRight();

		//get the start time and compute the duration
		double startTime;
		if (index == 0) {
			startTime = token.getStartTime();
		} else {
			startTime = token.getPoints().get(index - 1).getRight();
		}
		double duration = endTime - startTime;

		//get the svg-line with the edge
		SVGElement SVGline = panel.getGraph().getSVGElementOf(edge).getChild(1);

		//get the start node
		LocalDotNode startNode = edge.getSource();

		//compute the path
		String path = "M" + getCenter(startNode, panel);
		if (edge.isDirectionForward()) {
			path += "L" + panel.getGraph().getAttributeOf(SVGline, "d").substring(1);
		} else {
			path += reversePath(panel.getGraph().getAttributeOf(SVGline, "d"));
		}
		path += "L" + getCenter(edge.getTarget(), panel);

		//put it all together
		result.append("<animateMotion ");
		result.append("path='");
		result.append(path);
		result.append("' begin='");
		result.append(startTime);
		result.append("s' dur='");
		result.append(duration);
		result.append("s' ");
		if (index == token.getPoints().size() - 1) {
			result.append("fill='freeze'");
		}
		result.append("/>");
	}

	private static String getCenter(LocalDotNode node, InductiveVisualMinerPanel panel) {
		Group nodeGroup = panel.getGraph().getSVGElementOf(node);
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

	/*
	 * Reverses a path, assuming it consists of one move/line followed by one or
	 * more cubic bezier curves.
	 */
	private static Pattern pattern = Pattern.compile("-?(\\d*\\.)?\\d+,-?(\\d*\\.)?\\d+");

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
