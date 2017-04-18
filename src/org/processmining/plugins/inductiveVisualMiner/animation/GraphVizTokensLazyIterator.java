package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.AffineTransform;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens.EvalScratch;

/**
 * Provides an independent iterator over graphviztokens.
 * 
 * @author sleemans
 *
 */
public class GraphVizTokensLazyIterator implements GraphVizTokensIterator {

	private final GraphVizTokens tokens;

	public GraphVizTokensLazyIterator(GraphVizTokens tokens) {
		this.tokens = tokens;
	}

	public GraphVizTokensLazyIterator(GraphVizTokensLazyIterator oldIterator) {
		this.tokens = oldIterator.tokens;
	}

	//built-in no-object creating iterator
	private double itTime;
	private int itNext;
	private int itNow;

	private EvalScratch itEval = new EvalScratch(); //this object holds the most recently evaluated x, y and opacity.

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

	public int itNext() {
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
		tokens.eval(itNow, itTime, itEval);
	}

	/**
	 * 
	 * @return the opacity of the last bezier itEval() was called on.
	 */
	public double itGetOpacity() {
		return itEval.opacity;
	}

	/**
	 * 
	 * @return the x of the last bezier itEval() was called on.
	 */
	public double itGetX() {
		return itEval.x;
	}

	/**
	 * 
	 * @return the y of the last bezier itEval() was called on.
	 */
	public double itGetY() {
		return itEval.y;
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
