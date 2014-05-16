package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.Rectangle2D;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.LocalDotNode;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;

public class AnimationSVG {
	public static String animateTokens(Tokens tokens, InductiveVisualMinerPanel panel) {
		StringBuilder result = new StringBuilder();
		for (Token token : tokens) {
			animateToken(token, result, panel);
		}
		return result.toString();
	}

	public static void animateToken(Token token, StringBuilder result, InductiveVisualMinerPanel panel) {
		result.append("<ellipse fill='yellow' stroke='black' cx='0' cy='0' rx='4' ry='4'>");

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
		result.append(token.getPoints().get(token.getPoints().size() - 1).getRight());
		result.append("s' ");
		if (token.getFade()) {
			result.append("dur='0.5s' ");
		} else {
			result.append("dur='0.005s' ");
		}
		result.append("fill='freeze'/>");

		result.append("</ellipse>\n");
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
		path += "L" + panel.getGraph().getAttributeOf(SVGline, "d").substring(1);
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
}
