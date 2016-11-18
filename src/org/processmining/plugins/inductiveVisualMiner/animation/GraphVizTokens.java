package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;

import com.kitfox.svg.SVGElement;
import com.kitfox.svg.animation.Bezier;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

/**
 * Keeps a collection of tokens. If performance lacks, the implementation could
 * be replaced by an interval tree.
 * 
 * @author sleemans
 *
 */
public class GraphVizTokens implements GraphVizTokensIterator {

	private final TDoubleArrayList startTimes;
	private final TDoubleArrayList endTimes;
	private final TIntArrayList tracePointers;

	private final TIntArrayList bezierPointers;
	private final BezierList beziers;

	private final Map<String, Pair<List<Bezier>, Double>> pathCache;

	public GraphVizTokens() {
		startTimes = new TDoubleArrayList();
		endTimes = new TDoubleArrayList();
		tracePointers = new TIntArrayList();

		bezierPointers = new TIntArrayList();
		beziers = new BezierList();

		pathCache = new THashMap<>();
	}

	public void add(double startTime, double endTime, String path, boolean fadeIn, boolean fadeOut,
			AffineTransform transform, int traceIndex) throws NoninvertibleTransformException {

		double startOpacity;
		double endOpacity;
		if (fadeIn && fadeOut) {
			startOpacity = 0;
			endOpacity = 0;
		} else if (fadeIn) {
			startOpacity = 0;
			endOpacity = 1;
		} else if (fadeOut) {
			startOpacity = 1;
			endOpacity = 0;
		} else {
			startOpacity = 1;
			endOpacity = 1;
		}

		//split the path into bezier curves (or get from cache)
		Pair<List<Bezier>, Double> p;
		if (pathCache.containsKey(path)) {
			p = pathCache.get(path);
		} else {
			p = processPath(path);
			pathCache.put(path, p);
		}

		double lastTime = startTime;
		double lastOpacity = startOpacity;
		double lastLength = 0;
		for (Bezier bezier : p.getA()) {

			//move t
			double length = bezier.getLength();

			double t = (lastLength + length) / p.getB(); //the [0..1] how far we are on the path

			double newEndTime = startTime + t * (endTime - startTime);
			double newEndOpacity = startOpacity + t * (endOpacity - startOpacity);

			add(lastTime, newEndTime, bezier, lastOpacity, newEndOpacity, transform, traceIndex);

			//move for the next round
			lastTime = newEndTime;
			lastOpacity = newEndOpacity;
			lastLength = lastLength + length;
		}
	}

	private void add(double beginTime, double endTime, Bezier bezier, double startOpacity, double endOpacity,
			AffineTransform transform, int traceIndex) throws NoninvertibleTransformException {
		startTimes.add(beginTime);
		endTimes.add(endTime);
		tracePointers.add(traceIndex);
		bezierPointers.add(beziers.add(bezier, transform, transform.createInverse(), startOpacity, endOpacity));
	}

	public void cleanUp() {
		beziers.cleanUp();
		pathCache.clear();
	}

	/**
	 * 
	 * @param time
	 * @return Walks over the tokens that span the time
	 */
	public IteratorWithPosition<Integer> getTokensAtTime(final double time) {
		return new IteratorWithPosition<Integer>() {

			private int next = getNext(0);
			private int now = next - 1;

			private int getNext(int i) {
				while (i < startTimes.size() && (startTimes.get(i) > time || time > endTimes.get(i))) {
					i++;
				}
				return i;
			}

			public void remove() {
				throw new RuntimeException();
			}

			public Integer next() {
				now = next;
				next = getNext(next + 1);
				return now;
			}

			public boolean hasNext() {
				return next < startTimes.size();
			}

			public int getPosition() {
				return next;
			}
		};
	}

	public AffineTransform getTransform(int tokenIndex) {
		return beziers.getTransform(bezierPointers.get(tokenIndex));
	}

	public AffineTransform getTransformInverse(int tokenIndex) {
		return beziers.getTransformInverse(bezierPointers.get(tokenIndex));
	}

	public double getStart(int tokenIndex) {
		return startTimes.get(tokenIndex);
	}

	public double getEnd(int tokenIndex) {
		return endTimes.get(tokenIndex);
	}

	public int getTraceIndex(int tokenIndex) {
		return tracePointers.get(tokenIndex);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("size ");
		result.append(startTimes.size());
		result.append("\n");
		for (int tokenIndex = 0; tokenIndex < startTimes.size(); tokenIndex++) {
			result.append("@");
			result.append(startTimes.get(tokenIndex));
			result.append(" - @");
			result.append(endTimes.get(tokenIndex));
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * 
	 * @param path
	 * @return a list of bezier segments and the total length of the curve
	 */
	public static Pair<List<Bezier>, Double> processPath(String path) {
		GeneralPath generalPath = SVGElement.buildPath(path, GeneralPath.WIND_NON_ZERO);
		final List<Bezier> bezierSegs = new ArrayList<>();
		double curveLength = 0;

		double[] coords = new double[6];
		double sx = 0, sy = 0;

		for (PathIterator pathIt = generalPath.getPathIterator(new AffineTransform()); !pathIt.isDone(); pathIt.next()) {
			Bezier bezier = null;

			int segType = pathIt.currentSegment(coords);

			switch (segType) {
				case PathIterator.SEG_LINETO : {
					bezier = new Bezier(sx, sy, coords, 1);
					sx = coords[0];
					sy = coords[1];
					break;
				}
				case PathIterator.SEG_QUADTO : {
					bezier = new Bezier(sx, sy, coords, 2);
					sx = coords[2];
					sy = coords[3];
					break;
				}
				case PathIterator.SEG_CUBICTO : {
					bezier = new Bezier(sx, sy, coords, 3);
					sx = coords[4];
					sy = coords[5];
					break;
				}
				case PathIterator.SEG_MOVETO : {
					sx = coords[0];
					sy = coords[1];
					break;
				}
				case PathIterator.SEG_CLOSE :
					//Do nothing
					break;

			}

			if (bezier != null) {
				bezierSegs.add(bezier);
				curveLength += bezier.getLength();
			}
		}
		return Pair.of(bezierSegs, curveLength);
	}

	/**
	 * 
	 * @param tokenIndex
	 * @param time
	 * @return the point at which the curve is at time t and its opacity
	 */
	public Triple<Double, Double, Double> eval(int tokenIndex, double time) {
		
		//normalise how far we are on the bezier to [0..1]
		double t;
		if (endTimes.get(tokenIndex) == startTimes.get(tokenIndex)) {
			//single-time token point
			t = 0;
		} else {
			t = (time - startTimes.get(tokenIndex)) / (endTimes.get(tokenIndex) - startTimes.get(tokenIndex));
		}
		
		int bezier = bezierPointers.get(tokenIndex);

		//compute the position
		Point2D.Double point = new Point2D.Double();
		beziers.getBezier(bezier).eval(t, point);

		//compute opacity
		double opacity = 1;
		double startOpacity = beziers.getStartOpacity(bezier);
		double endOpacity = beziers.getEndOpacity(bezier);
		if (startOpacity == 1 && endOpacity == 1) {

		} else if (startOpacity == 0 && endOpacity == 0) {
			opacity = Math.abs(t - 0.5) * 2;
		} else {
			opacity = (1 - t) * startOpacity + t * endOpacity;
		}

		return Triple.of(point.x, point.y, opacity);
	}

	public int size() {
		return startTimes.size();
	}

	//build-in no-object creating iterator
	private double itTime;
	private int itNext;
	private int itNow;
	private int itBezierPointer;
	private double itOpacity;
	private double itX;
	private double itY;

	public void itInit(double time) {
		itTime = time;
		itNext = itGetNext(0);
		itNow = itNext - 1;
	}

	private int itGetNext(int i) {
		while (i < startTimes.size() && (startTimes.get(i) > itTime || itTime > endTimes.get(i))) {
			i++;
		}
		return i;
	}

	public Integer itNext() {
		itNow = itNext;
		itNext = itGetNext(itNext + 1);
		return itNow;
	}

	public boolean itHasNext() {
		return itNext < startTimes.size();
	}

	public int itGetPosition() {
		return itNext;
	}

	private double itT;

	public void itEval() {
		//normalise how far we are on the current bezier to [0..1]
		itT = (itTime - startTimes.get(itNow)) / (endTimes.get(itNow) - startTimes.get(itNow));

		itBezierPointer = bezierPointers.get(itNow);

		//compute the position
		Point2D.Double point = new Point2D.Double();
		beziers.getBezier(itBezierPointer).eval(itT, point);
		itX = point.getX();
		itY = point.getY();

		//compute opacity
		if (beziers.getStartOpacity(itBezierPointer) == 1 && beziers.getEndOpacity(itBezierPointer) == 1) {
			itOpacity = 1;
		} else if (beziers.getStartOpacity(itBezierPointer) == 0 && beziers.getEndOpacity(itBezierPointer) == 0) {
			itOpacity = Math.abs(itT - 0.5) * 2;
		} else {
			itOpacity = (1 - itT) * beziers.getStartOpacity(itBezierPointer) + itT
					* beziers.getEndOpacity(itBezierPointer);
		}
	}

	/**
	 * 
	 * @return the opacity of the last bezier itEval() was called on.
	 */
	public double itGetOpacity() {
		return itOpacity;
	}

	/**
	 * 
	 * @return the x of the last bezier itEval() was called on.
	 */
	public double itGetX() {
		return itX;
	}

	/**
	 * 
	 * @return the y of the last bezier itEval() was called on.
	 */
	public double itGetY() {
		return itY;
	}

	/**
	 * 
	 * @return the current trace index.
	 */
	public int itGetTraceIndex() {
		return tracePointers.get(itNow);
	}

	public AffineTransform itGetTransform() {
		return getTransform(itNow);
	}

	public AffineTransform itGetTransformInverse() {
		return getTransformInverse(itNow);
	}
}
