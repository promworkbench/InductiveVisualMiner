package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

public class QueueLengthsImplCLIStartComplete extends QueueLengths {

	private class Cluster implements Comparable<Cluster> {
		public int size;
		public double center;

		public int compareTo(Cluster arg0) {
			return Double.compare(center, arg0.center);
		}
	}

	private final TIntObjectMap<Cluster[]> clusters;
	private final TIntDoubleMap priors;
	private final static int maxIterations = 100;

	public QueueLengthsImplCLIStartComplete(TIntObjectMap<QueueActivityLog> queueActivityLogs, int k) {
		clusters = new TIntObjectHashMap<>(10, 0.5f, -1);
		priors = new TIntDoubleHashMap(10, 0.5f, -1, 0);
		for (TIntIterator it = queueActivityLogs.keySet().iterator(); it.hasNext();) {
			int unode = it.next();
			QueueActivityLog l = queueActivityLogs.get(unode);

			//create intervals
			List<DoublePoint> intervals = new ArrayList<DoublePoint>(l.size());
			for (int index = 0; index < l.size(); index++) {
				double[] d = { (double) l.getStart(index) - l.getInitiate(index) };
				intervals.add(new DoublePoint(d));
			}

			if (intervals.size() < k) {
				continue;
			}

			KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(k, maxIterations);
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

	public int getClusterNumber(Cluster[] cs, long duration) {
		for (int i = 0; i < cs.length - 1; i++) {
			if (duration < (cs[i].center + cs[i + 1].center) / 2.0) {
				return i;
			}
		}
		return cs.length - 1;
	}

	public double getQueueProbability(int unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getInitiate(traceIndex) <= time
				&& time <= l.getStart(traceIndex)) {
			Cluster[] cs = clusters.get(unode);
			double priorA = priors.get(unode);

			long xI = time - l.getInitiate(traceIndex);

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
			return priorA * posteriorI / likelihoodI;
		}
		return 0;
	}

	public String getName() {
		return "CLI start complete";
	}
}
