package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class ExporterTraceData extends Exporter {

	private final InductiveVisualMinerState state;

	public ExporterTraceData(InductiveVisualMinerState state) {
		this.state = state;
	}

	@Override
	public String getDescription() {
		return "csv (trace attributes)";
	}

	protected String getExtension() {
		return "csv";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		assert state.getIvMLogFiltered() != null && state.isAlignmentReady();
		final IvMLogFilteredImpl log = state.getIvMLogFiltered();
		final IvMAttributesInfo attributes = state.getIvMAttributesInfo();
		assert attributes != null;

		PrintWriter w = new PrintWriter(file, "UTF-8");
		char fieldSeparator = ',';

		//header
		for (Attribute attribute : attributes.getTraceAttributes()) {
			w.print(escape(attribute.getName()));
			w.print(fieldSeparator);
		}
		w.println("");

		//body
		for (IvMTrace trace : log) {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				String value = valueString(attribute, trace);
				if (value != null) {
					w.print(escape(value));
				}
				w.print(fieldSeparator);
			}
			w.println("");
		}

		w.close();
	}

	private String valueString(Attribute attribute, IvMTrace trace) {
		if (attribute.isDuration()) {
			double value = attribute.getDuration(trace);
			if (value != -Double.MAX_VALUE) {
				return value + "";
			}
		} else if (attribute.isNumeric()) {
			double value = attribute.getNumeric(trace);
			if (value != -Double.MAX_VALUE) {
				return value + "";
			}
		} else if (attribute.isTime()) {
			long value = attribute.getTime(trace);
			if (value != Long.MIN_VALUE) {
				return ResourceTimeUtils.timeToString(value);
			}
		} else if (attribute.isLiteral()) {
			return attribute.getLiteral(trace);
		}
		return "";
	}

	public static String escape(String s) {
		return StringEscapeUtils.escapeCsv(s);
	}

}
