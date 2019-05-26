package org.processmining.plugins.inductiveVisualMiner.export;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class ExportAlignment {

	public static enum Type {
		modelView, logView, both
	}

	public static InductiveVisualMinerAlignment exportAlignment(IvMLog log, IvMModel model,
			XEventClassifier classifier) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog result = factory.createLog((XAttributeMap) log.getAttributes().clone());
		XModelAlignmentExtension alignmentExtension = XModelAlignmentExtension.instance();

		/**
		 * Store the model and the classifier in the log
		 */
		if (model.isTree()) {
			alignmentExtension.assignModel(result, XTreeExtension.KEY_TREE,
					XTreeExtension.fromEfficientTree(model.getTree()));
		} else {
			alignmentExtension.assignModel(result, XDFMExtension.KEY_DFM, XDFMExtension.fromDfg(model.getDfg()));
		}

		alignmentExtension.assignClassifier(result, classifier.getDefiningAttributeKeys());

		/**
		 * Export the log
		 */
		for (IvMTrace trace : log) {
			XTrace newTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());

			for (IvMMove move : trace) {
				XEvent event = factory.createEvent();

				if (move.isSyncMove() || move.isLogMove()) {
					//log part
					if (!move.isIgnoredModelMove() && !(move.isSyncMove() && model.isTau(move.getTreeNode()))) {
						event.setAttributes((XAttributeMap) move.getAttributes().clone());
					}
				}

				if (move.isSyncMove() || move.isModelMove()) {
					//model part
					alignmentExtension.assignMoveSourceNode(event, move.getSourceNode());
					alignmentExtension.assignMoveModelNode(event, move.getTreeNode());
				}

				XLifecycleExtension.instance().assignTransition(event, move.getLifeCycleTransition().name());

				alignmentExtension.assignMoveType(event, move.getType().name());

				newTrace.add(event);
			}

			result.add(newTrace);
		}

		return new InductiveVisualMinerAlignment(result);
	}

	public static void exportAlignment(PluginContext context, IvMLog log, IvMModel model, String name, Type type) {

		XFactory factory = new XFactoryNaiveImpl();

		List<XTrace> traces = new ArrayList<>();

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			List<XEvent> xTrace = new ArrayList<>();

			for (IvMMove event : trace) {

				boolean include = !event.isTauStart() && !(event.isSyncMove() && model.isTau(event.getTreeNode()));
				switch (type) {
					case both :
						break;
					case logView :
						include &= event.isLogMove() || event.isSyncMove();
						break;
					case modelView :
						include &= event.isModelSync();
						break;
				}

				if (include) {
					XEvent xEvent;
					if (event.getAttributes() != null) {
						xEvent = factory.createEvent((XAttributeMap) event.getAttributes().clone());
					} else {
						xEvent = factory.createEvent();
					}

					if (!event.isLogMove()) {
						xEvent.getAttributes().put("concept:name", factory.createAttributeLiteral("concept:name",
								model.getActivityName(event.getTreeNode()), null));
						xEvent.getAttributes().put("lifecycle:transition", factory.createAttributeLiteral(
								"lifecycle:transition", event.getLifeCycleTransition().name(), null));
					}

					xEvent.getAttributes().put("IvM move type",
							factory.createAttributeLiteral("IvM move type", event.getType().name(), null));

					xTrace.add(xEvent);
				}
			}

			XTrace xxTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
			xxTrace.addAll(xTrace);
			traces.add(xxTrace);
		}

		XLog xLog = factory.createLog();
		xLog.addAll(traces);

		context.getProvidedObjectManager().createProvidedObject("Model view of " + name, xLog, XLog.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(xLog)
					.setFavorite(true);
		}
	}

}
