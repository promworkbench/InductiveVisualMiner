package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplNPEMStartComplete implements QueueLengths {

	private class Cluster implements Comparable<Cluster> {
		public int size;
		public double center;

		public int compareTo(Cluster arg0) {
			return Double.compare(center, arg0.center);
		}
	}

	private final Map<UnfoldedNode, QueueActivityLog> queueActivityLogs;
	private final Map<UnfoldedNode, Cluster[]> clusters;
	private final TObjectDoubleMap<UnfoldedNode> priors;

	private final static int k = 4;

	public QueueLengthsImplNPEMStartComplete(IvMLog iLog) {
		queueActivityLogs = QueueMineActivityLog.mine(iLog, true, false, true, false);
		clusters = new THashMap<>();
		priors = new TObjectDoubleHashMap<>();
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			QueueActivityLog l = queueActivityLogs.get(unode);

			//create intervals
			List<DoublePoint> intervals = new ArrayList<DoublePoint>(l.size());
			for (int index = 0; index < l.size(); index++) {
				double[] d = { (double) l.getStart(index) - l.getInitiate(index) };
				intervals.add(new DoublePoint(d));
			}

			FuzzyKMeansClusterer<DoublePoint> clusterer = new FuzzyKMeansClusterer<>(k, 1.01);
			List<CentroidCluster<DoublePoint>> cs = clusterer.cluster(intervals);

			Cluster[] css = new Cluster[k];
			{
				int i = 0;
				for (CentroidCluster<DoublePoint> cluster : cs) {

					Cluster c = new Cluster();
					css[i] = c;

					//denote the center point of the cluster
					c.center = cluster.getCenter().getPoint()[0];

					//keep track of the number of ... in the cluster
					c.size = cluster.getPoints().size();

					i++;
				}
			}
			Arrays.sort(css);
			clusters.put(unode, css);
			
			//determine the prior
			priors.put(unode, (l.size() - css[0].size) / (l.size() * 1.0));
		}
	}

	public double getQueueLength(UnfoldedNode unode, long time) {
		QueueActivityLog l = queueActivityLogs.get(unode);
		Cluster[] cs = clusters.get(unode);
		if (l == null) {
			return -1;
		}

		double queueLength = 0;
		double priorA = priors.get(unode);
		for (int index = 0; index < l.size(); index++) {
			if (l.getInitiate(index) <= time && time <= l.getStart(index)) {

				long xI = time - l.getInitiate(index);

				int likelihoodCount = 0;
				int posteriorCount = 0;
				for (int index2 = 0; index2 < l.size(); index2++) {
					//count for likelihood if longer than durationI
					long durationJ = l.getStart(index2) - l.getInitiate(index2);
					if (durationJ > xI) {
						likelihoodCount++;

						//count for posterior if in cluster and longer than duration
						int clusterJ = getClusterNumber(cs, durationJ);
						if (clusterJ != 0) {
							posteriorCount++;
						}
					}
				}
				double likelihoodI = likelihoodCount / (l.size() * 1.0);
				double posteriorI = posteriorCount / (l.size() - cs[0].size * 1.0);
				double p = priorA * posteriorI / likelihoodI;
//				System.out.println("l  " + likelihoodI);
//				System.out.println("po " + posteriorI);
//				System.out.println("pr " + priorA);
				queueLength += p;
			}
		}

		return queueLength;
	}

	public int getClusterNumber(Cluster[] cs, long duration) {
		if (duration < (cs[0].center + cs[1].center) / 2.0) {
			return 0;
		} else if (duration < (cs[1].center + cs[2].center) / 2.0) {
			return 1;
		} else if (duration < (cs[2].center + cs[3].center) / 2.0) {
			return 2;
		} else {
			return 3;
		}
	}

}
