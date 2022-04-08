package org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.ConfigurationWithDecorator;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.CorrelationDensityPlot;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.DataRowBlockTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.statisticaltests.helperclasses.Correlation;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

public class DataRowBlockAssociations<C extends ConfigurationWithDecorator, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "assoc-att";
	}

	public String getStatusBusyMessage() {
		return "Computing associations of trace attributes..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info_merged, IvMObject.aligned_log_filtered,
				IvMObject.data_analyses_delay, IvMObject.selected_associations_enabled };
	}

	public List<DataRow<Object>> compute(ConfigurationWithDecorator configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		if (!inputs.get(IvMObject.selected_associations_enabled)) {
			return new ArrayList<>();
		}

		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info_merged);
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();

			for (Attribute attributeA : attributes.getTraceAttributes()) {
				for (Attribute attributeB : attributes.getTraceAttributes()) {
					if (attributeA.compareTo(attributeB) < 0) {
						result.addAll(DataRowBlockTrace.merge(
								createAttributeData(logFiltered, attributeA, attributeB, canceller,
										configuration.getDecorator()),
								createAttributeData(negativeLog, attributeA, attributeB, canceller,
										configuration.getDecorator()),
								canceller));
					}
				}
			}
		} else {
			for (Attribute attributeA : attributes.getTraceAttributes()) {
				for (Attribute attributeB : attributes.getTraceAttributes()) {
					if (attributeA.compareTo(attributeB) < 0) {
						result.addAll(createAttributeData(logFiltered, attributeA, attributeB, canceller,
								configuration.getDecorator()));
					}
				}
			}
		}

		return result;
	}

	public static List<DataRow<Object>> createAttributeData(IvMLogFiltered logFiltered, Attribute attributeA,
			Attribute attributeB, IvMCanceller canceller, IvMDecoratorI decorator) {
		if (attributeA.isNumeric() && attributeB.isNumeric()) {
			return createAttributeDataNumericNumeric(logFiltered, attributeA, attributeB, canceller, decorator);
		}
		return new ArrayList<>();
	}

	private static List<DataRow<Object>> createAttributeDataNumericNumeric(IvMLogFiltered log, Attribute attributeA,
			Attribute attributeB, IvMCanceller canceller, IvMDecoratorI decorator) {
		List<DataRow<Object>> result = new ArrayList<>();

		//gather values
		TDoubleList valuesA = new TDoubleArrayList();
		TDoubleList valuesB = new TDoubleArrayList();
		{
			for (IteratorWithPosition<IvMTrace> traceIt = log.iterator(); traceIt.hasNext();) {
				IvMTrace trace = traceIt.next();

				double valueA = attributeA.getNumeric(trace);
				double valueB = attributeB.getNumeric(trace);
				if (valueA != -Double.MAX_VALUE && valueB != -Double.MAX_VALUE) {
					valuesA.add(valueA);
					valuesB.add(valueB);
				}

				if (canceller.isCancelled()) {
					return result;
				}
			}
		}

		double[] vA = valuesA.toArray();
		double[] vB = valuesB.toArray();
		double minA = StatUtils.min(vA);
		double maxA = StatUtils.max(vA);
		double minB = StatUtils.min(vB);
		double maxB = StatUtils.max(vB);

		//correlation value
		{
			BigDecimal meanB = Correlation.mean(vB);
			double standardDeviationB = Correlation.standardDeviation(vB, meanB);
			double association = Correlation.correlation(vA, vB, meanB, standardDeviationB).doubleValue();

			if (association != -Double.MAX_VALUE) {
				result.add(new DataRow<Object>(DisplayType.numeric(association), attributeA.getName(),
						attributeB.getName(), AssociationType.association.toString()));
				result.add(new DataRow<Object>(DisplayType.numeric(association), attributeB.getName(),
						attributeA.getName(), AssociationType.association.toString()));
			} else {
				result.add(new DataRow<Object>(DisplayType.NA(), attributeA.getName(), attributeB.getName(),
						AssociationType.association.toString()));
				result.add(new DataRow<Object>(DisplayType.NA(), attributeB.getName(), attributeA.getName(),
						AssociationType.association.toString()));
			}
		}

		//correlation measure name
		{
			result.add(new DataRow<Object>(DisplayType.literal("Pearson correlation"), attributeA.getName(),
					attributeB.getName(), AssociationType.associationMeasure.toString()));
			result.add(new DataRow<Object>(DisplayType.literal("Pearson correlation"), attributeB.getName(),
					attributeA.getName(), AssociationType.associationMeasure.toString()));
		}

		//correlation plot
		{
			//CorrelationPlot plot = new CorrelationPlot();
			BufferedImage imageA = CorrelationDensityPlot.create(attributeA.getName(), vA, minA, maxA,
					attributeB.getName(), vB, minB, maxB, decorator);
			result.add(new DataRow<Object>(DisplayType.image(imageA), attributeA.getName(), attributeB.getName(),
					AssociationType.associationPlot.toString()));

			BufferedImage imageB = CorrelationDensityPlot.create(attributeB.getName(), vB, minB, maxB,
					attributeA.getName(), vA, minA, maxA, decorator);
			result.add(new DataRow<Object>(DisplayType.image(imageB), attributeB.getName(), attributeA.getName(),
					AssociationType.associationPlot.toString()));
		}

		return result;
	}
}
