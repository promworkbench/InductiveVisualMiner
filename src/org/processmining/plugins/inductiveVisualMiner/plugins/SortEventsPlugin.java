package org.processmining.plugins.inductiveVisualMiner.plugins;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

public class SortEventsPlugin {
	@Plugin(name = "Sort events in traces, based on time stamp (in place)", returnLabels = { "Log" }, returnTypes = {
			XLog.class }, parameterLabels = { "Log with sorted events" }, userAccessible = true, categories = {
					PluginCategory.Filtering }, help = "Sort events in traces, based on time stamp (in place).")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public XLog sort(PluginContext context, XLog log) {
		for (XTrace trace : log) {
			Collections.sort(trace, new EventsComparator());
		}
		return log;
	}

	public static class EventsComparator implements Comparator<XEvent> {
		public int compare(XEvent o1, XEvent o2) {
			Date time1 = XTimeExtension.instance().extractTimestamp(o1);
			Date time2 = XTimeExtension.instance().extractTimestamp(o2);
			if (time1 == null && time2 == null) {
				return 0;
			}
			if (time1 == null) {
				return 1;
			}
			if (time2 == null) {
				return -1;
			}
			return time1.compareTo(time2);
		}
	}
}
