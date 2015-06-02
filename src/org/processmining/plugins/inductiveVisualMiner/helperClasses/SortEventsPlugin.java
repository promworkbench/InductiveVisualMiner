package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.Collections;
import java.util.Comparator;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class SortEventsPlugin {
	@Plugin(name = "Sort events in traces, based on time stamp (in place)", returnLabels = { "Log" }, returnTypes = { XLog.class }, parameterLabels = { "Log with sorted events" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public XLog sort(PluginContext context, XLog log) {
		for (XTrace trace : log) {
			Collections.sort(trace, new EventsComparator());
		}
		return log;
	}
	
	
	public static class EventsComparator implements Comparator<XEvent>{
		public int compare(XEvent o1, XEvent o2) {
			return XTimeExtension.instance().extractTimestamp(o1).compareTo(XTimeExtension.instance().extractTimestamp(o2));
		}
	}
}
