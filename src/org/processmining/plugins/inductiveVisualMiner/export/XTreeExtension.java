package org.processmining.plugins.inductiveVisualMiner.export;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeLiteral;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeImpl;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class XTreeExtension {

	public static final String KEY_TREE = "tree";
	public static final String KEY_TREE_NODES = "tree:nodes";
	public static final String KEY_TREE_NODE = "tree:node";
	public static final String KEY_TREE_ACTIVITIES = "tree:activities";
	public static final String KEY_TREE_ACTIVITY = "tree:activity";

	public static List<XAttribute> fromEfficientTree(EfficientTree tree) {
		ArrayList<XAttribute> result = new ArrayList<>();
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		/**
		 * First: list of activities
		 */
		{
			XAttributeList activitiesList = factory.createAttributeList(KEY_TREE_ACTIVITIES,
					XModelAlignmentExtension.instance());
			XAttributeLiteral string = factory.createAttributeLiteral(KEY_TREE_ACTIVITY, "__invalid__",
					XModelAlignmentExtension.instance());
			for (String activity : tree.getInt2activity()) {
				XAttributeLiteral att = (XAttributeLiteral) string.clone();
				att.setValue(activity);
				activitiesList.addToCollection(att);
			}
			result.add(activitiesList);
		}

		/**
		 * Second: list of nodes
		 */
		{
			XAttributeList treeList = factory.createAttributeList(KEY_TREE_NODES, XModelAlignmentExtension.instance());
			XAttributeDiscrete number = factory.createAttributeDiscrete(KEY_TREE_NODE, -1,
					XModelAlignmentExtension.instance());
			for (int node = 0; node < tree.getMaxNumberOfNodes(); node++) {
				XAttributeDiscrete att = (XAttributeDiscrete) number.clone();
				att.setValue(tree.getActivity(node));
				treeList.addToCollection(att);
			}
			result.add(treeList);
		}

		return result;
	}

	public static EfficientTree toEfficientTree(List<XAttribute> attributes) {
		if (attributes.size() < 2) {
			return null;
		}

		/**
		 * First: list of activities
		 */
		String[] int2activity;
		{
			XAttribute attActivities = attributes.get(0);
			if (attActivities == null || !(attActivities instanceof XAttributeList)) {
				return null;
			}
			XAttributeList attActivitiesL = (XAttributeList) attActivities;
			int2activity = new String[attActivitiesL.getCollection().size()];
			int i = 0;
			for (XAttribute attActivity : attActivitiesL.getCollection()) {
				if (attActivity == null || !(attActivity instanceof XAttributeLiteral)) {
					return null;
				}
				XAttributeLiteral attActivityD = (XAttributeLiteral) attActivity;
				int2activity[i] = attActivityD.getValue();
				i++;
			}
		}

		/**
		 * Second: list of nodes
		 */
		int[] nodeList;
		{
			XAttribute attNodes = attributes.get(1);
			if (attNodes == null || !(attNodes instanceof XAttributeList)) {
				return null;
			}
			XAttributeList attNodesL = (XAttributeList) attNodes;
			nodeList = new int[attNodesL.getCollection().size()];
			int i = 0;
			for (XAttribute attNode : attNodesL.getCollection()) {
				if (attNode == null || !(attNode instanceof XAttributeDiscrete)) {
					return null;
				}
				XAttributeDiscrete attNodeD = (XAttributeDiscrete) attNode;
				nodeList[i] = (int) attNodeD.getValue();
				i++;
			}
		}

		//make the inverse of the map
		TObjectIntMap<String> activity2int = new TObjectIntHashMap<>(10, 0.5f, -1);
		for (int i = 0; i < int2activity.length; i++) {
			activity2int.put(int2activity[i], i);
		}

		return new EfficientTreeImpl(nodeList, activity2int, int2activity);
	}
}
