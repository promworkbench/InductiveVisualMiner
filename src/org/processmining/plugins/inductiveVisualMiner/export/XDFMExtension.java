package org.processmining.plugins.inductiveVisualMiner.export;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeLiteral;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModelImplQuadratic;

import gnu.trove.iterator.TIntIterator;

public class XDFMExtension {

	public static final String KEY_DFM = "dfm";
	public static final String KEY_DFM_EDGES = "dfm:edges";
	public static final String KEY_DFM_EDGE = "dfm:edge";
	public static final String KEY_DFM_ACTIVITIES = "dfm:activities";
	public static final String KEY_DFM_ACTIVITY = "dfm:activity";
	public static final String KEY_DFM_START_ACTIVITIES = "dfm:startactivities";
	public static final String KEY_DFM_START_ACTIVITY = "dfm:startactivity";
	public static final String KEY_DFM_END_ACTIVITIES = "dfm:endactivities";
	public static final String KEY_DFM_END_ACTIVITY = "dfm:endactivity";
	public static final String KEY_DFM_EMPTY_TRACES = "dfm:emptytraces";

	public static List<XAttribute> fromDfg(DirectlyFollowsModel dfm) {
		ArrayList<XAttribute> result = new ArrayList<>();
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		/**
		 * First: list of activities
		 */
		{
			XAttributeList activitiesList = factory.createAttributeList(KEY_DFM_ACTIVITIES,
					XModelAlignmentExtension.instance());
			XAttributeLiteral string = factory.createAttributeLiteral(KEY_DFM_ACTIVITY, "__invalid__",
					XModelAlignmentExtension.instance());
			for (String activity : dfm.getAllNodeNames()) {
				XAttributeLiteral att = (XAttributeLiteral) string.clone();
				att.setValue(activity);
				activitiesList.addToCollection(att);
			}
			result.add(activitiesList);
		}

		/**
		 * Second: list of edges
		 */
		{
			XAttributeList edgeList = factory.createAttributeList(KEY_DFM_EDGES, XModelAlignmentExtension.instance());
			XAttributeDiscrete number = factory.createAttributeDiscrete(KEY_DFM_EDGE, -1,
					XModelAlignmentExtension.instance());
			for (long edgeIndex : dfm.getEdges()) {
				XAttributeDiscrete att = (XAttributeDiscrete) number.clone();
				int source = dfm.getEdgeSource(edgeIndex);
				int target = dfm.getEdgeTarget(edgeIndex);
				att.setValue((((long) target) << 32) | (source & 0xffffffffL));
				edgeList.addToCollection(att);
			}
			result.add(edgeList);
		}

		/**
		 * Third: start activities
		 */
		{
			XAttributeList startActivitiesList = factory.createAttributeList(KEY_DFM_START_ACTIVITIES,
					XModelAlignmentExtension.instance());
			XAttributeDiscrete number = factory.createAttributeDiscrete(KEY_DFM_START_ACTIVITY, -1,
					XModelAlignmentExtension.instance());
			for (TIntIterator it = dfm.getStartNodes().iterator(); it.hasNext();) {
				XAttributeDiscrete att = (XAttributeDiscrete) number.clone();
				att.setValue(it.next());
				startActivitiesList.addToCollection(att);
			}
			result.add(startActivitiesList);
		}

		/**
		 * Fourth: end activities
		 */
		{
			XAttributeList endActivitiesList = factory.createAttributeList(KEY_DFM_END_ACTIVITIES,
					XModelAlignmentExtension.instance());
			XAttributeDiscrete number = factory.createAttributeDiscrete(KEY_DFM_END_ACTIVITY, -1,
					XModelAlignmentExtension.instance());
			for (TIntIterator it = dfm.getEndNodes().iterator(); it.hasNext();) {
				XAttributeDiscrete att = (XAttributeDiscrete) number.clone();
				att.setValue(it.next());
				endActivitiesList.addToCollection(att);
			}
			result.add(endActivitiesList);
		}

		/**
		 * Fifht: empty traces
		 */
		{
			XAttributeBoolean att = factory.createAttributeBoolean(KEY_DFM_EMPTY_TRACES, false,
					XModelAlignmentExtension.instance());
			att.setValue(dfm.isEmptyTraces());
			result.add(att);
		}

		return result;
	}

	public static DirectlyFollowsModel toDFM(List<XAttribute> attributes) {
		DirectlyFollowsModelImplQuadratic result = new DirectlyFollowsModelImplQuadratic();

		if (attributes.size() < 5) {
			return null;
		}

		/**
		 * First: list of activities
		 */
		{
			XAttribute attActivities = attributes.get(0);
			if (attActivities == null || !(attActivities instanceof XAttributeList)) {
				return null;
			}
			XAttributeList attActivitiesL = (XAttributeList) attActivities;
			String[] activities = new String[attActivitiesL.getCollection().size()];
			int i = 0;
			for (XAttribute attActivity : attActivitiesL.getCollection()) {
				if (attActivity == null || !(attActivity instanceof XAttributeLiteral)) {
					return null;
				}
				XAttributeLiteral attActivityD = (XAttributeLiteral) attActivity;
				activities[i] = attActivityD.getValue();
				i++;
			}

			result.addNodes(activities);
		}

		/**
		 * Second: list of edges
		 */
		{
			XAttribute attEdges = attributes.get(1);
			if (attEdges == null || !(attEdges instanceof XAttributeList)) {
				return null;
			}
			XAttributeList attNodesL = (XAttributeList) attEdges;
			for (XAttribute attNode : attNodesL.getCollection()) {
				if (attNode == null || !(attNode instanceof XAttributeDiscrete)) {
					return null;
				}
				XAttributeDiscrete attNodeD = (XAttributeDiscrete) attNode;
				long edge = attNodeD.getValue();
				int source = (int) edge;
				int target = (int) (edge >> 32);

				result.addEdge(source, target);
			}
		}

		/**
		 * Third: list of start activities
		 */
		{
			XAttribute attEdges = attributes.get(2);
			if (attEdges == null || !(attEdges instanceof XAttributeList)) {
				return null;
			}
			XAttributeList attNodesL = (XAttributeList) attEdges;
			for (XAttribute attNode : attNodesL.getCollection()) {
				if (attNode == null || !(attNode instanceof XAttributeDiscrete)) {
					return null;
				}
				XAttributeDiscrete attNodeD = (XAttributeDiscrete) attNode;
				int startActivity = (int) attNodeD.getValue();
				result.addStartNode(startActivity);
			}
		}

		/**
		 * Fourth: list of end activities
		 */
		{
			XAttribute attEdges = attributes.get(3);
			if (attEdges == null || !(attEdges instanceof XAttributeList)) {
				return null;
			}
			XAttributeList attNodesL = (XAttributeList) attEdges;
			for (XAttribute attNode : attNodesL.getCollection()) {
				if (attNode == null || !(attNode instanceof XAttributeDiscrete)) {
					return null;
				}
				XAttributeDiscrete attNodeD = (XAttributeDiscrete) attNode;
				int startActivity = (int) attNodeD.getValue();
				result.addEndNode(startActivity);
			}
		}

		/**
		 * Fifth: empty traces
		 */
		{
			XAttribute attEmptyTraces = attributes.get(4);
			if (attEmptyTraces == null || !(attEmptyTraces instanceof XAttributeBoolean)) {
				return null;
			}
			XAttributeBoolean attEmptyTracesB = (XAttributeBoolean) attEmptyTraces;
			result.setEmptyTraces(attEmptyTracesB.getValue());
		}

		return result;
	}
}
