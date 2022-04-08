package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.ConfigurationWithDecorator;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.Histogram;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;

public class DataRowBlockEventDataHistogram<C extends ConfigurationWithDecorator, P>
		extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "event-att-hist";
	}

	public String getStatusBusyMessage() {
		return "Computing event attribute histograms..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.attributes_info, IvMObject.aligned_log_filtered,
				IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(ConfigurationWithDecorator configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		AttributesInfo attributes = inputs.get(IvMObject.attributes_info);
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();

			for (Attribute attribute : attributes.getEventAttributes()) {
				result.addAll(TraceDataRowBlock.merge(
						createAttributeData(logFiltered, attribute, canceller, configuration.getDecorator()),
						createAttributeData(negativeLog, attribute, canceller, configuration.getDecorator()),
						canceller));
			}
		} else {
			for (Attribute attribute : attributes.getEventAttributes()) {
				result.addAll(createAttributeData(logFiltered, attribute, canceller, configuration.getDecorator()));
			}
		}

		return result;
	}

	public static List<DataRow<Object>> createAttributeData(IvMLogFiltered logFiltered, Attribute attribute,
			IvMCanceller canceller, IvMDecoratorI decorator) {
		if (attribute.isNumeric()) {
			return createAttributeDataNumeric(logFiltered, attribute, canceller, decorator);
		} else if (attribute.isBoolean()) {
			//return createAttributeDataBoolean(logFiltered, attribute, canceller);
		} else if (attribute.isTime()) {
			return createAttributeDataTime(logFiltered, attribute, canceller, decorator);
		} else if (attribute.isLiteral()) {
			//return createAttributeDataLiteral(logFiltered, attribute, canceller);
		} else if (attribute.isDuration()) {
			//return createAttributeDataDuration(logFiltered, attribute, canceller);
		}
		return new ArrayList<>();
	}

	public static List<DataRow<Object>> createAttributeDataNumeric(IvMLogFiltered log, Attribute attribute,
			IvMCanceller canceller, IvMDecoratorI decorator) {
		List<DataRow<Object>> result = new ArrayList<>();

		//gather values
		TDoubleList values = new TDoubleArrayList();
		{
			for (IteratorWithPosition<IvMTrace> traceIt = log.iterator(); traceIt.hasNext();) {
				IvMTrace trace = traceIt.next();

				for (IvMMove event : trace) {
					double value = attribute.getNumeric(event);
					if (value != -Double.MAX_VALUE) {
						values.add(value);
					}
				}

				if (canceller.isCancelled()) {
					return result;
				}
			}
		}

		//create histogram
		BufferedImage image = Histogram.create(values.toArray(), DisplayType.fromAttribute(attribute), null, false,
				decorator);
		result.add(new DataRow<Object>(DisplayType.image(image), attribute.getName(), "histogram"));

		return result;
	}

	public static List<DataRow<Object>> createAttributeDataTime(IvMLogFiltered log, Attribute attribute,
			IvMCanceller canceller, IvMDecoratorI decorator) {
		List<DataRow<Object>> result = new ArrayList<>();

		//gather values
		TLongList values = new TLongArrayList();
		{
			for (IteratorWithPosition<IvMTrace> traceIt = log.iterator(); traceIt.hasNext();) {
				IvMTrace trace = traceIt.next();

				for (IvMMove event : trace) {
					long value = attribute.getTime(event);
					if (value != Long.MIN_VALUE) {
						values.add(value);
					}
				}

				if (canceller.isCancelled()) {
					return result;
				}
			}
		}

		//create histogram
		BufferedImage image = Histogram.create(values.toArray(), DisplayType.fromAttribute(attribute), null, false,
				decorator);
		result.add(new DataRow<Object>(DisplayType.image(image), attribute.getName(), "histogram"));

		return result;
	}
}