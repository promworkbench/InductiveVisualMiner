package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.processmining.plugins.InductiveMiner.Pair;
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

	private static final AtomicLong idPool = new AtomicLong(0);
	private final long id;

	private final TDoubleArrayList startTimes;
	private final TDoubleArrayList endTimes;
	private final TIntArrayList tracePointers;

	private final TIntArrayList bezierPointers;
	private final BezierList beziers;

	private final Map<String, Pair<List<Bezier>, Double>> pathCache;

	public GraphVizTokens() {
		id = idPool.incrementAndGet();
		startTimes = new TDoubleArrayList();
		endTimes = new TDoubleArrayList();
		tracePointers = new TIntArrayList();

		bezierPointers = new TIntArrayList();
		beziers = new BezierList();

		pathCache = new THashMap<>();
	}

	public void add(double startTime, double endTime, String path, boolean fadeIn, boolean fadeOut,
			AffineTransform transform, int traceIndex) {

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
			p = processPath(path, transform);
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

			add(lastTime, newEndTime, bezier, lastOpacity, newEndOpacity, traceIndex);

			//move for the next round
			lastTime = newEndTime;
			lastOpacity = newEndOpacity;
			lastLength = lastLength + length;
		}
	}

	private void add(double beginTime, double endTime, Bezier bezier, double startOpacity, double endOpacity,
			int traceIndex) {
		startTimes.add(beginTime);
		endTimes.add(endTime);
		tracePointers.add(traceIndex);
		bezierPointers.add(beziers.add(bezier, startOpacity, endOpacity));
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

	public double getStartTime(int tokenIndex) {
		return startTimes.get(tokenIndex);
	}

	public double getEndTime(int tokenIndex) {
		return endTimes.get(tokenIndex);
	}

	public int getTraceIndex(int tokenIndex) {
		return tracePointers.get(tokenIndex);
	}

	public int getBezierIndex(int tokenIndex) {
		return bezierPointers.get(tokenIndex);
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
	 * @param transform
	 * @return a list of bezier segments and the total length of the curve. Each
	 *         point of every bezier will be transformed using transform.
	 */
	public static Pair<List<Bezier>, Double> processPath(String path, AffineTransform transform) {
		GeneralPath generalPath = SVGElement.buildPath(path, GeneralPath.WIND_NON_ZERO);
		final List<Bezier> bezierSegs = new ArrayList<>();
		double curveLength = 0;

		double[] coords = new double[6];

		Point2D.Double s = new Point2D.Double(0, 0);

		for (PathIterator pathIt = generalPath.getPathIterator(new AffineTransform()); !pathIt.isDone(); pathIt
				.next()) {
			Bezier bezier = null;

			int segType = pathIt.currentSegment(coords);

			/**
			 * Transform the coordinates from bezier-space to image-space. Doing
			 * it now means that is doesn't have to happen in the animation
			 * loop.
			 */
			coords[0] = coords[0] * transform.getScaleX() + transform.getTranslateX();
			coords[1] = coords[1] * transform.getScaleY() + transform.getTranslateY();
			coords[2] = coords[2] * transform.getScaleX() + transform.getTranslateX();
			coords[3] = coords[3] * transform.getScaleY() + transform.getTranslateY();
			coords[4] = coords[4] * transform.getScaleX() + transform.getTranslateX();
			coords[5] = coords[5] * transform.getScaleY() + transform.getTranslateY();

			switch (segType) {
				case PathIterator.SEG_LINETO : {
					bezier = new Bezier(s.x, s.y, coords, 1);
					s.x = coords[0];
					s.y = coords[1];
					break;
				}
				case PathIterator.SEG_QUADTO : {
					bezier = new Bezier(s.x, s.y, coords, 2);
					s.x = coords[2];
					s.y = coords[3];
					break;
				}
				case PathIterator.SEG_CUBICTO : {
					bezier = new Bezier(s.x, s.y, coords, 3);
					s.x = coords[4];
					s.y = coords[5];
					break;
				}
				case PathIterator.SEG_MOVETO : {
					s.x = coords[0];
					s.y = coords[1];
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

	public static class EvalScratch {
		double x;
		double y;
		double opacity;

		private double t;
		private double startOpacity;
		private double endOpacity;
		private Point2D.Double point = new Point2D.Double();
		private int bezier;
	}

	/**
	 * 
	 * @param tokenIndex
	 * @param time
	 * @return the point at which the curve is at time t and its opacity (in
	 *         EvalScratch result, which prevents us from allocating anything in
	 *         this method. This might seem useless optimisation, but once GC
	 *         kicks in, animation stutters.)
	 */
	public void eval(int tokenIndex, double time, EvalScratch result) {

		//normalise how far we are on the bezier to [0..1]
		if (endTimes.get(tokenIndex) == startTimes.get(tokenIndex)) {
			//single-time token point
			result.t = 0;
		} else {
			result.t = (time - startTimes.get(tokenIndex)) / (endTimes.get(tokenIndex) - startTimes.get(tokenIndex));
		}

		result.bezier = bezierPointers.get(tokenIndex);

		//compute the position
		beziers.getBezier(result.bezier).eval(result.t, result.point);

		//compute opacity
		result.startOpacity = beziers.getStartOpacity(result.bezier);
		result.endOpacity = beziers.getEndOpacity(result.bezier);
		if (result.startOpacity == 1 && result.endOpacity == 1) {
			result.opacity = 1;
		} else if (result.startOpacity == 0 && result.endOpacity == 0) {
			result.opacity = Math.abs(result.t - 0.5) * 2;
		} else {
			result.opacity = (1 - result.t) * result.startOpacity + result.t * result.endOpacity;
		}

		result.x = result.point.x;
		result.y = result.point.y;
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

	public int itNext() {
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
			itOpacity = (1 - itT) * beziers.getStartOpacity(itBezierPointer)
					+ itT * beziers.getEndOpacity(itBezierPointer);
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

	public BezierList getBeziers() {
		return beziers;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphVizTokens other = (GraphVizTokens) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
