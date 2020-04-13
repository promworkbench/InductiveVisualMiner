package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapPropertyDuration;

public class ExporterTraceData extends Exporter {

	private final InductiveVisualMinerState state;

	public ExporterTraceData(InductiveVisualMinerState state) {
		this.state = state;
	}

	@Override
	public String getDescription() {
		return "csv (trace data)";
	}

	protected String getExtension() {
		return "csv";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		assert (state.getIvMLogFiltered() != null && state.isAlignmentReady());
		final IvMLogFilteredImpl log = state.getIvMLogFiltered();
		final AttributesInfo attributes = state.getAttributesInfo();

		PrintWriter w = new PrintWriter(file, "UTF-8");
		char fieldSeparator = ',';

		//header
		for (Attribute attribute : attributes.getTraceAttributes()) {
			w.print(escape(attribute.getName()));
			w.print(fieldSeparator);
		}
		w.print("fitness");
		w.println("");

		//body
		for (IvMTrace trace : log) {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				XAttribute value = trace.getAttributes().get(attribute.getName());
				if (value != null) {
					w.print(escape(value.toString()));
				} else {
					String v;
					if (attribute.isTraceDuration()) {
						v = TraceColourMapPropertyDuration.getTraceDuration(trace) + "";
					} else if (attribute.isTraceNumberofEvents()) {
						v = trace.getNumberOfEvents() + "";
					}
				}
				w.print(fieldSeparator);
			}
			w.print(Fitness.compute(trace));
			w.println("");
		}

		w.close();
	}

	public static String escape(String s) {
		return StringEscapeUtils.escapeCsv(s);
	}

}
