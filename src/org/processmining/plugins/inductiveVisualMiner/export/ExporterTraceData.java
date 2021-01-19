package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class ExporterTraceData extends IvMExporter {

	@Override
	public String getDescription() {
		return "csv (trace attributes)";
	}

	@Override
	protected String getExtension() {
		return "csv";
	}

	@Override
	protected IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log_filtered, IvMObject.ivm_attributes_info };
	}

	@Override
	protected IvMObject<?>[] createNonTriggerObjects() {
		return new IvMObject<?>[] {};
	}

	@Override
	public void export(IvMObjectValues inputs, InductiveVisualMinerAnimationPanel panel, File file) throws Exception {
		final IvMLogFilteredImpl log = inputs.get(IvMObject.aligned_log_filtered);
		final IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

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