package org.processmining.plugins.inductiveVisualMiner.export;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XModelAlignmentExtension extends XExtension {

	private static final long serialVersionUID = 2611151899412488309L;

	/**
	 * Unique URI of this extension.
	 */
	public static final URI EXTENSION_URI = URI.create("http://www.xes-standard.org/treealignment.xesext");

	/**
	 * Keys for the attributes.
	 */

	public static final String KEY_MODEL_TYPE = "treealignment:modeltype";
	public static final String KEY_MODEL = "treealignment:model";
	public static final String KEY_CLASSIFIER = "treealignment:classifier";
	public static final String KEY_CLASSIFIER_FIELD = "treealignment:classifier:field";

	public static final String KEY_MOVE_TYPE = "treealignment:movetype";
	public static final String KEY_MOVE_SOURCE_NODE = "treealignment:movesourcenode";
	public static final String KEY_MOVE_MODEL_NODE = "treealignment:movemodelnode";

	public static XAttributeLiteral ATTR_MODEL_TYPE;
	public static XAttributeList ATTR_MODEL;
	public static XAttributeList ATTR_CLASSIFIER;
	public static XAttributeLiteral ATTR_CLASSIFIER_FIELD;

	public static XAttributeLiteral ATTR_MOVE_TYPE;
	public static XAttributeDiscrete ATTR_MOVE_SOURCE_NODE;
	public static XAttributeDiscrete ATTR_MOVE_MODEL_NODE;

	/**
	 * Singleton instance of this extension.
	 */
	private final static XModelAlignmentExtension SINGLETON;

	private XFactory factory = XFactoryRegistry.instance().currentDefault();

	static {
		SINGLETON = new XModelAlignmentExtension();
		XExtensionManager.instance().register(SINGLETON);
	}

	/**
	 * @return the singleton {@link XAlignmentExtension}.
	 */
	public static XModelAlignmentExtension instance() {
		return SINGLETON;
	}

	private XModelAlignmentExtension() {
		super("TreeAlignment", "treealignment", EXTENSION_URI);

		ATTR_MODEL_TYPE = factory.createAttributeLiteral(KEY_MODEL_TYPE, "__invalid__", this);
		ATTR_MODEL = factory.createAttributeList(KEY_MODEL, this);
		ATTR_CLASSIFIER = factory.createAttributeList(KEY_CLASSIFIER, this);
		ATTR_CLASSIFIER_FIELD = factory.createAttributeLiteral(KEY_CLASSIFIER_FIELD, "__invalid__", this);

		ATTR_MOVE_TYPE = factory.createAttributeLiteral(KEY_MOVE_TYPE, "__invalid__", this);
		ATTR_MOVE_SOURCE_NODE = factory.createAttributeDiscrete(KEY_MOVE_SOURCE_NODE, -1, this);
		ATTR_MOVE_MODEL_NODE = factory.createAttributeDiscrete(KEY_MOVE_MODEL_NODE, -1, this);

		this.logAttributes.add((XAttribute) ATTR_MODEL_TYPE.clone());
		this.logAttributes.add((XAttribute) ATTR_MODEL.clone());
		this.logAttributes.add((XAttribute) ATTR_CLASSIFIER.clone());

		this.eventAttributes.add((XAttribute) ATTR_MOVE_TYPE.clone());
		this.eventAttributes.add((XAttribute) ATTR_MOVE_SOURCE_NODE.clone());
		this.eventAttributes.add((XAttribute) ATTR_MOVE_MODEL_NODE.clone());
	}

	public void assignModel(XLog log, String modelType, List<XAttribute> model) {
		XAttributeLiteral attrModelType = (XAttributeLiteral) ATTR_MODEL_TYPE.clone();
		XAttributeList attrModel = (XAttributeList) ATTR_MODEL.clone();
		attrModelType.setValue(modelType);
		for (XAttribute modelT : model) {
			attrModel.addToCollection(modelT);
		}
		log.getAttributes().put(attrModelType.getKey(), attrModelType);
		log.getAttributes().put(attrModel.getKey(), attrModel);
	}

	/**
	 * 
	 * @param log
	 * @return the model type, or NULL if it is not an attribute of the log.
	 */
	public String extractModelType(XLog log) {
		XAttribute fitness = log.getAttributes().get(KEY_MODEL_TYPE);
		if (fitness != null && fitness instanceof XAttributeLiteral) {
			return ((XAttributeLiteral) fitness).getValue();
		}
		return null;
	}

	/**
	 * 
	 * @param log
	 * @return the model, or NULL if it is not an attribute of the log.
	 */
	public List<XAttribute> extractModel(XLog log) {
		XAttribute fitness = log.getAttributes().get(KEY_MODEL);
		if (fitness != null && fitness instanceof XAttributeList) {
			return new ArrayList<>(((XAttributeList) fitness).getCollection());
		}
		return null;
	}

	public void assignClassifier(XLog log, String... attributes) {
		XAttributeList attrList = (XAttributeList) ATTR_CLASSIFIER.clone();
		for (String attribute : attributes) {
			XAttributeLiteral field = (XAttributeLiteral) ATTR_CLASSIFIER_FIELD.clone();
			field.setValue(attribute);
			attrList.addToCollection(field);
		}
		log.getAttributes().put(attrList.getKey(), attrList);
	}

	/**
	 * 
	 * @param log
	 * @return The classifier, or NULL if it is not an attribute of the log.
	 */
	public String[] extractClassifier(XLog log) {
		XAttribute fitness = log.getAttributes().get(KEY_CLASSIFIER);
		if (fitness != null && fitness instanceof XAttributeList) {
			Collection<XAttribute> list = ((XAttributeList) fitness).getCollection();
			ArrayList<String> result = new ArrayList<>();
			for (XAttribute attribute : list) {
				if (attribute != null && attribute instanceof XAttributeLiteral) {
					result.add(((XAttributeLiteral) attribute).getValue());
				}
			}
			String[] r = new String[result.size()];
			return result.toArray(r);
		}
		return null;
	}

	public void assignMoveType(XEvent event, String moveType) {
		XAttributeLiteral attrMoveType = (XAttributeLiteral) ATTR_MOVE_TYPE.clone();
		attrMoveType.setValue(moveType);
		event.getAttributes().put(attrMoveType.getKey(), attrMoveType);
	}

	public String extractMoveType(XEvent event) {
		XAttribute fitness = event.getAttributes().get(KEY_MOVE_TYPE);
		if (fitness != null && fitness instanceof XAttributeLiteral) {
			return ((XAttributeLiteral) fitness).getValue();
		}
		return null;
	}

	public void assignMoveSourceNode(XEvent event, int moveSourceNode) {
		XAttributeDiscrete attrModelMoveNode = (XAttributeDiscrete) ATTR_MOVE_SOURCE_NODE.clone();
		attrModelMoveNode.setValue(moveSourceNode);
		event.getAttributes().put(attrModelMoveNode.getKey(), attrModelMoveNode);
	}

	public void assignMoveModelNode(XEvent event, int moveModelNode) {
		XAttributeDiscrete attrModelMoveNode = (XAttributeDiscrete) ATTR_MOVE_MODEL_NODE.clone();
		attrModelMoveNode.setValue(moveModelNode);
		event.getAttributes().put(attrModelMoveNode.getKey(), attrModelMoveNode);
	}

	/**
	 * 
	 * @param event
	 * @return The model node this move is mapped to, or Integer.MIN_VALUE if
	 *         the node is not mapped.
	 */
	public int extractMoveSourceNode(XEvent event) {
		XAttribute fitness = event.getAttributes().get(KEY_MOVE_SOURCE_NODE);
		if (fitness != null && fitness instanceof XAttributeDiscrete) {
			return (int) ((XAttributeDiscrete) fitness).getValue();
		}
		return Integer.MIN_VALUE;
	}

	/**
	 * 
	 * @param event
	 * @return The model node this move is mapped to, or Integer.MIN_VALUE if
	 *         the node is not mapped.
	 */
	public int extractMoveModelNode(XEvent event) {
		XAttribute fitness = event.getAttributes().get(KEY_MOVE_MODEL_NODE);
		if (fitness != null && fitness instanceof XAttributeDiscrete) {
			return (int) ((XAttributeDiscrete) fitness).getValue();
		}
		return Integer.MIN_VALUE;
	}

	/**
	 * Removes the attributes that this extension defines from the log level
	 * (does not recurse onto traces or events).
	 * 
	 * @param log
	 */
	public void removeAttributes(XLog log) {
		log.getAttributes().remove(KEY_CLASSIFIER);
		log.getAttributes().remove(KEY_MODEL);
		log.getAttributes().remove(KEY_MODEL_TYPE);
	}

	/**
	 * Removes the attributes that this extension defines from the trace level
	 * (does not recurse onto events).
	 * 
	 * @param trace
	 */
	public void removeAttributes(XTrace trace) {

	}

	public void removeAttributes(XEvent event) {
		event.getAttributes().remove(KEY_MOVE_MODEL_NODE);
		event.getAttributes().remove(KEY_MOVE_TYPE);
	}
}
