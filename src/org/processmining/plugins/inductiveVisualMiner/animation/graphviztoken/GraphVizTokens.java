package org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken;

import gnu.trove.list.array.TDoubleArrayList;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;

import com.kitfox.svg.SVGElement;
import com.kitfox.svg.animation.Bezier;

/**
 * Keeps a collection of tokens. If performance lacks, the implementation could
 * be replaced by an interval tree.
 * 
 * @author sleemans
 *
 */
public class GraphVizTokens {

	private final TDoubleArrayList startTimes;
	private final TDoubleArrayList endTimes;
	private final BitSet fadeIn;
	private final BitSet fadeOut;

	private final List<List<Bezier>> segments;
	private final TDoubleArrayList lengths;

	public GraphVizTokens() {
		startTimes = new TDoubleArrayList();
		endTimes = new TDoubleArrayList();
		fadeIn = new BitSet();
		fadeOut = new BitSet();

		segments = new ArrayList<>();
		lengths = new TDoubleArrayList();
	}

	public void add(double beginTime, double endTime, String path, boolean fadeIn, boolean fadeOut) {
		startTimes.add(beginTime);
		endTimes.add(endTime);
		this.fadeIn.set(this.startTimes.size() - 1, fadeIn);
		this.fadeIn.set(this.startTimes.size() - 1, fadeOut);

		//do magic with the path
		Pair<List<Bezier>, Double> p = processPath(path);
		segments.add(p.getA());
		lengths.add(p.getB());
	}

	/**
	 * 
	 * @param time
	 * @return Walks over the tokens that span the time
	 */
	public Iterable<Integer> getTokensAtTime(final double time) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {

					int next = getNext(0);
					int now = next - 1;

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
				};
			}
		};
	}

	public double getStart(int tokenIndex) {
		return startTimes.get(tokenIndex);
	}

	public double getEnd(int tokenIndex) {
		return endTimes.get(tokenIndex);
	}

	public boolean isFadeIn(int tokenIndex) {
		return fadeIn.get(tokenIndex);
	}

	public boolean isFadeOut(int tokenIndex) {
		return fadeOut.get(tokenIndex);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int tokenIndex = 0; tokenIndex < startTimes.size(); tokenIndex++) {
			result.append(fadeIn.get(tokenIndex) ? "fade " : "");
			result.append("@");
			result.append(startTimes.get(tokenIndex));
			result.append(" - @");
			result.append(endTimes.get(tokenIndex));
			result.append(fadeOut.get(tokenIndex) ? " fade" : "");
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
	 * @param t
	 *            [0..1]
	 * @return the point at which the curve is at time t
	 */
	public Point2D.Double eval(int tokenIndex, double t) {
		double curLength = lengths.get(tokenIndex) * t;
		Point2D.Double point = new Point2D.Double();
		for (Iterator<Bezier> it = segments.get(tokenIndex).iterator(); it.hasNext();) {
			Bezier bez = it.next();

			double bezLength = bez.getLength();
			if (curLength < bezLength) {
				double param = curLength / bezLength;
				bez.eval(param, point);
				break;
			}

			curLength -= bezLength;
		}
		return point;
	}
}
