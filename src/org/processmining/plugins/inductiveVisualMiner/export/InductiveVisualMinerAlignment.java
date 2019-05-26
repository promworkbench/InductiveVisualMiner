package org.processmining.plugins.inductiveVisualMiner.export;

import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class InductiveVisualMinerAlignment {
	private XLog alignedLog;
	private XLog xLog = null;
	private IvMModel model = null;

	public InductiveVisualMinerAlignment(XLog alignedLog) {
		this.alignedLog = alignedLog;
	}

	public XLog getAlignedLog() {
		return alignedLog;
	}

	public void setAlignedLog(XLog alignedLog) {
		this.alignedLog = alignedLog;
	}

	/**
	 * Extract the model from this aligned log.
	 * 
	 * @return
	 */
	public IvMModel getModel() {
		if (model != null) {
			return model;
		}
		String modelType = XModelAlignmentExtension.instance().extractModelType(alignedLog);
		List<XAttribute> model = XModelAlignmentExtension.instance().extractModel(alignedLog);
		if (modelType == null || model == null) {
			return null;
		}
		if (modelType.equals(XTreeExtension.KEY_TREE)) {
			EfficientTree tree = XTreeExtension.toEfficientTree(model);
			if (tree == null) {
				return null;
			}

			this.model = new IvMModel(tree);
			return this.model;
		} else if (modelType.equals(XDFMExtension.KEY_DFM)) {
			DirectlyFollowsModel dfm = XDFMExtension.toDFM(model);
			if (dfm == null) {
				return null;
			}

			this.model = new IvMModel(dfm);
			return this.model;
		}
		return null;
	}

	public XEventClassifier getClassifier() {
		String[] fields = XModelAlignmentExtension.instance().extractClassifier(alignedLog);

		AttributeClassifier[] x = new AttributeClassifier[fields.length];
		int i = 0;
		for (String field : fields) {
			x[i] = new AttributeClassifier(field);
			i++;
		}

		return AttributeClassifiers.constructClassifier(x);
	}

	/**
	 * In order for all filters to work properly, we need to revert the
	 * alignment back to a log.
	 * 
	 * @return
	 */
	public XLog getXLog() {
		if (xLog != null) {
			return xLog;
		}
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		xLog = factory.createLog((XAttributeMap) alignedLog.getAttributes().clone());
		XModelAlignmentExtension alignmentExtension = XModelAlignmentExtension.instance();
		IvMModel model = getModel();

		alignmentExtension.instance().removeAttributes(xLog);
		for (XTrace trace : alignedLog) {
			XTrace newTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
			alignmentExtension.removeAttributes(newTrace);

			for (XEvent event : trace) {
				boolean keep = false;
				switch (alignmentExtension.extractMoveType(event)) {
					case "modelMove" :
						break;
					case "logMove" :
						keep = true;
						break;
					case "synchronousMove" :
						int node = alignmentExtension.extractMoveModelNode(event);
						if (node == Integer.MIN_VALUE) {
							return null;
						}
						if (!model.isTau(node)) {
							keep = true;
						}
						break;
					case "ignoredLogMove" :
						keep = true;
						break;
					case "ignoredModelMove" :
						break;
				}

				if (keep) {
					XEvent newEvent = factory.createEvent((XAttributeMap) event.getAttributes().clone());
					alignmentExtension.removeAttributes(newEvent);
					newTrace.add(newEvent);
				}

			}

			xLog.add(newTrace);
		}

		return xLog;
	}
}
