package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;

import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

/**
 * Interface to support trace colouring.
 * 
 * @author sander
 *
 */
public interface TraceColourMap {

	/**
	 * Warning: this method is called in the animation loop. Do -not- create new
	 * objects or variables, as the garbage collector -will- kick in and ruin
	 * the smoothness of the animation. DO NOT ACCESS TRACE ATTRIBUTES FROM THIS
	 * METHOD!
	 * 
	 * Rather, prepare an array that contains a (link to) a colour for each
	 * trace index, in the constructor.
	 * 
	 * @param traceIndex
	 * @return the colour that this bezier should have.
	 */
	public Color getColour(int traceIndex);

	/**
	 * 
	 * @param trace
	 * @return The colour of a trace.
	 */
	public Color getColour(IMTrace trace);

	/**
	 * 
	 * @param trace
	 * @return The colour of a trace.
	 */
	public Color getColour(IvMTrace trace);

	/**
	 * This method is less critical than getColour() and is not executed in the
	 * animation thread.
	 * 
	 * @param trace
	 * @return The value on which the colour is to be computed.
	 */
	public String getValue(IvMTrace trace);

	/**
	 * This method is less critical than getColour() and is not executed in the
	 * animation thread.
	 * 
	 * @param trace
	 * @return The value on which the colour is to be computed.
	 */
	public String getValue(IMTrace trace);

}
