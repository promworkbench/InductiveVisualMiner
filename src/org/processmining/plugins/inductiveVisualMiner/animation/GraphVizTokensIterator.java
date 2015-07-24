package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.geom.AffineTransform;


/**
 * Keeps a collection of tokens and allows fast iteration.
 * 
 * @author sleemans
 *
 */
public interface GraphVizTokensIterator {

	/**
	 * Initialises the iterator on a particular time.
	 * @param time
	 */
	public void itInit(double time);

	/**
	 * Default iterator.next()
	 * @return
	 */
	public Integer itNext();

	/**
	 * Default iterator.hasNext()
	 * @return
	 */
	public boolean itHasNext();

	/**
	 * 
	 * @return the index of the bezier last returned by itNext()
	 */
	public int itGetPosition();

	/**
	 * Evaluate the bezier last returned by itNext()
	 */
	public void itEval();
	
	/**
	 * 
	 * @return the opacity of the last bezier itEval() was called on.
	 */
	public double itGetOpacity();
	
	/**
	 * 
	 * @return the x of the last bezier itEval() was called on.
	 */
	public double itGetX();
	
	/**
	 * 
	 * @return the y of the last bezier itEval() was called on.
	 */
	public double itGetY();
	
	/**
	 * 
	 * @return the current trace index.
	 */
	public int itGetTraceIndex();
	
	/**
	 * 
	 * @return the current transformation
	 */
	public AffineTransform itGetTransform();
	
	/**
	 * 
	 * @return the inverse of the current transformation
	 */
	public AffineTransform itGetTransformInverse();
}
