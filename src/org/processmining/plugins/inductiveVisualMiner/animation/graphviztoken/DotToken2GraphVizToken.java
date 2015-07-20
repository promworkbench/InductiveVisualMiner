package org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotEdge;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotToken;
import org.processmining.plugins.inductiveVisualMiner.animation.dotToken.DotTokenStep;

import com.kitfox.svg.Group;
import com.kitfox.svg.Path;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.TransformableElement;

public class DotToken2GraphVizToken {

	public static void convertTokens(Iterable<DotToken> tokens, GraphVizTokens result, SVGDiagram svg)
			throws NoninvertibleTransformException {
		for (DotToken token : tokens) {
			for (DotToken subToken : token.getAllTokensRecursively()) {
				convertToken(subToken, result, svg);
			}
		}

	}

	public static void convertToken(DotToken token, GraphVizTokens result,
			SVGDiagram svg) throws NoninvertibleTransformException {
		assert (token.isAllTimestampsSet());

		for (int i = 0; i < token.size(); i++) {
			animateDotTokenStep(token, i, token.isFade() && i == 0, token.isFade() && i == token.size() - 1, result, svg);
		}
	}

	/**
	 * Record the animation of one dot token step
	 * 
	 * @param dotToken
	 * @param stepIndex
	 * @param result
	 * @param svg
	 * @throws NoninvertibleTransformException
	 */
	public static void animateDotTokenStep(DotToken dotToken, int stepIndex, boolean fadeIn, boolean fadeOut,
			GraphVizTokens result, SVGDiagram svg) throws NoninvertibleTransformException {
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
	 * @throws NoninvertibleTransformException
	 */
	public static void animateDotTokenStepEdge(DotToken dotToken, int stepIndex, boolean fadeIn, boolean fadeOut,
			GraphVizTokens result, SVGDiagram image) throws NoninvertibleTransformException {
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

		//compute the path
		String path;

		//start the token in its last position
		if (stepIndex == 0 || dotToken.get(stepIndex - 1).isOverEdge()) {
			//If there was no activity before, start at the center of the source node.
			path = "M" + getCenter(edge.getSource(), image);
		} else {
			//If there was an activity before, then the token was gracefully put on the source already.
			path = "M" + getSourceLocation(edge, image);
		}

		//move over the edge
		Path line = (Path) DotPanel.getSVGElementOf(image, edge).getChild(1);
		if (edge.isDirectionForward()) {
			path += "L" + DotPanel.getAttributeOf(line, "d").substring(1);
		} else {
			path += reversePath(DotPanel.getAttributeOf(line, "d"));
		}

		//Leave the token in a nice place.
		if (stepIndex == dotToken.size() - 1 || dotToken.get(stepIndex + 1).isOverEdge()) {
			//If there's no activity afterwards, leave it on the center of the target node.
			path += "L" + getCenter(edge.getTarget(), image);
		} else {
			//If there is an activity afterwards, move over the arrowhead.
			path += "L" + getArrowHeadPoint(edge, image);
		}

		//add to the result
		result.add(startTime, endTime, path, fadeIn, fadeOut, getTotalTransform(image, edge));
	}

	public static void animateDotTokenStepNode(DotToken dotToken, int stepIndex, boolean fadeIn, boolean fadeOut,
			GraphVizTokens result, SVGDiagram image) throws NoninvertibleTransformException {
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
		String path = "M" + getArrowHeadPoint(dotToken.get(stepIndex - 1).getEdge(), image);

		//line to the center of the node
		path += "L" + getCenter(step.getNode(), image);

		//line to the first point on the edge after this 
		path += "L" + getSourceLocation(dotToken.get(stepIndex + 1).getEdge(), image);

		//get the transformation
		AffineTransform transform = getTotalTransform(image, dotToken.get(stepIndex - 1).getEdge());

		//put it all together
		result.add(startTime, endTime, path, fadeIn, fadeOut, transform);
	}

	public static AffineTransform getTotalTransform(SVGDiagram image, DotEdge edge) {
		//get the svg-line with the edge
		Path line = (Path) DotPanel.getSVGElementOf(image, edge).getChild(1);

		//get the viewbox transformation
		SVGRoot root = line.getRoot();
		AffineTransform transform = root.getViewXform();

		//walk through the path downwards
		for (Object parent : line.getPath(null)) {
			if (parent instanceof TransformableElement) {
				transform.concatenate(((TransformableElement) parent).getTranform());
			}
		}

		return transform;
	}
	
	public static String getSourceLocation(LocalDotEdge edge, SVGDiagram image) {
		SVGElement SVGline = DotPanel.getSVGElementOf(image, edge).getChild(1);
		String path = DotPanel.getAttributeOf(SVGline, "d");

		if (edge.isDirectionForward()) {
			return getFirstLocation(path);
		} else {
			return getLastLocation(path);
		}
	}
	
	public static String getFirstLocation(String path) {
		Matcher matcher = pattern.matcher(path);
		matcher.find();
		String point = matcher.group();
		return point;
	}

	public static String getLastLocation(String path) {
		Matcher matcher = pattern.matcher(path);
		String location = null;
		while (matcher.find()) {
			location = matcher.group();
		}
		return location;
	}

	public static String getCenter(LocalDotNode node, SVGDiagram image) {
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

	public static final Pattern pattern = Pattern.compile("-?(\\d*\\.)?\\d+,-?(\\d*\\.)?\\d+");

	/**
	 * 
	 * @param path
	 * @return The reversed path, assuming the original path consists of one
	 *         move/line followed by one or more cubic bezier curves.
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

	public static String getArrowHeadPoint(LocalDotEdge edge, SVGDiagram image) {
		//the arrowhead polygon is the second child of the edge object
		SVGElement svgArrowHead = DotPanel.getSVGElementOf(image, edge).getChild(2);
		String path = DotPanel.getAttributeOf(svgArrowHead, "points");

		//the point we're looking for is the second point
		Matcher matcher = pattern.matcher(path);
		matcher.find();
		matcher.find();
		return matcher.group();
	}
}
