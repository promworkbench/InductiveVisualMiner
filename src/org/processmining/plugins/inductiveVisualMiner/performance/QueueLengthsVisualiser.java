package org.processmining.plugins.inductiveVisualMiner.performance;


public class QueueLengthsVisualiser {

//	/**
//	 * Draw the queues in the visualisation.
//	 * 
//	 * @param panel
//	 * @param lengths
//	 * @param visualisationParameters
//	 * @return
//	 * @throws SVGException
//	 */
//	public static TraceViewColourMap drawQueues(SVGDiagram svg, ProcessTree tree, AlignedLogVisualisationInfo info,
//			QueueLengthsWrapper lengths, long time, AlignedLogVisualisationParameters visualisationParameters)
//			throws SVGException {
//
//		//compute queue lengths
//		TObjectDoubleMap<UnfoldedNode> queueLengths = new TObjectDoubleHashMap<ProcessTree2Petrinet.UnfoldedNode>(10,
//				0.5f, -1);
//		long min = Long.MAX_VALUE;
//		long max = Long.MIN_VALUE;
//		{
//			for (UnfoldedNode unode : TreeUtils.unfoldAllNodes(new UnfoldedNode(tree.getRoot()))) {
//				if (unode.getNode() instanceof Manual) {
//					double l = lengths.getQueueLength(unode, time);
//					queueLengths.put(unode, l);
//					if (l > max) {
//						max = (long) (l + 0.5);
//					}
//					if (l < min && l > -0.1) {
//						min = (long) (l + 0.5);
//					}
//				}
//			}
//		}
//
//		//style nodes
//		TraceViewColourMap colourMap = new TraceViewColourMap();
//		{
//			for (UnfoldedNode unode : queueLengths.keySet()) {
//				long length = Math.round(queueLengths.get(unode));
//
//				Pair<Color, Color> colour = InductiveVisualMinerSelectionColourer.styleUnfoldedNode(unode, svg, info,
//						length, min, max, "queue length: ", "", visualisationParameters);
//				if (unode.getNode() instanceof Manual) {
//					colourMap.set(unode, colour.getA(), colour.getB());
//				}
//			}
//		}
//		return colourMap;
//	}
}
