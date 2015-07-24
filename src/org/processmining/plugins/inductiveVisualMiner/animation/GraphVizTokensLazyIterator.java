package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.AffineTransform;

import org.processmining.plugins.InductiveMiner.Triple;

/**
 * Provides an independent iterator over graphviztokens.
 * @author sleemans
 *
 */
public class GraphVizTokensLazyIterator implements GraphVizTokensIterator {

	private final GraphVizTokens tokens;
	
	public GraphVizTokensLazyIterator(GraphVizTokens tokens) {
		this.tokens = tokens;
	}
	
	//build-in no-object creating iterator
	private double itTime;
	private int itNext;
	private int itNow;
	
	private double itOpacity;
	private double itX;
	private double itY;

	public void itInit(double time) {
		itTime = time;
		itNext = itGetNext(0);
		itNow = itNext - 1;
	}

	private int itGetNext(int i) {
		while (i < tokens.size() && (tokens.getStart(i) > itTime || itTime > tokens.getEnd(i))) {
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
		return itNext < tokens.size();
	}

	public int itGetPosition() {
		return itNext;
	}

	public void itEval() {
		Triple<Double, Double, Double> t = tokens.eval(itNow, itTime);
		itX = t.getA();
		itY = t.getB();
		itOpacity = t.getC();
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
		return tokens.getTraceIndex(itNow);
	}
	
	public AffineTransform itGetTransform() {
		return tokens.getTransform(itNow);
	}
	
	public AffineTransform itGetTransformInverse() {
		return tokens.getTransformInverse(itNow);
	}
}
