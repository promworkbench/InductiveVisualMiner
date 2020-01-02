package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.Iterator;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class IvMLog2XLog {

	/**
	 * This one doesn't clone the attribute maps, so any change will propagate.
	 * 
	 * @param logFiltered
	 * @return
	 */
	public static XLog convert(IvMLogFiltered logFiltered, IvMModel model) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog result = factory.createLog();

		for (Iterator<IvMTrace> it = logFiltered.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			XTrace newTrace = factory.createTrace();

			for (IvMMove move : trace) {
				if (move.isSyncMove() || move.isLogMove()) {
					//log part
					if (!move.isIgnoredModelMove() && !(move.isSyncMove() && model.isTau(move.getTreeNode()))) {
						XEvent event = factory.createEvent(move.getAttributes());
						newTrace.add(event);
					}
				}
			}

			result.add(newTrace);
		}
		return result;
	}
}
