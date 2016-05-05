package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

public class QueueLengthsImplCPHEnqueueStartComplete extends QueueLengths {

	private class Cluster implements Comparable<Cluster> {
		public double center;
		public double lambda;

		public int compareTo(Cluster arg0) {
			return Double.compare(center, arg0.center);
		}
	}

	private final TIntObjectMap<Cluster[]> clusters;

	public QueueLengthsImplCPHEnqueueStartComplete(TIntObjectMap<QueueActivityLog> queueActivityLogs, int k) {
		clusters = new TIntObjectHashMap<>(10, 0.5f, -1);
		for (int unode : queueActivityLogs.keySet().toArray()) {
			QueueActivityLog l = queueActivityLogs.get(unode);

			//create intervals
			List<DoublePoint> intervals = new ArrayList<DoublePoint>(l.size());
			for (int index = 0; index < l.size(); index++) {
				double[] d = { (double) l.getStart(index) - l.getEnqueue(index) };
				intervals.add(new DoublePoint(d));
			}

			KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(k);
			List<CentroidCluster<DoublePoint>> cs = clusterer.cluster(intervals);

			Cluster[] css = new Cluster[k];
			{
				int i = 0;
				for (CentroidCluster<DoublePoint> cluster : cs) {

					Cluster c = new Cluster();
					css[i] = c;

					//denote the center point of the cluster
					c.center = cluster.getCenter().getPoint()[0];

					i++;
				}
			}
			Arrays.sort(css);
			clusters.put(unode, css);

			long[] sums = new long[k];
			long[] counts = new long[k];
			for (int i = 0; i < l.size(); i++) {
				if (l.getStart(i) > 0 && l.getEnqueue(i) > 0) {
					int cluster = getClusterNumber(css, l.getStart(i) - l.getEnqueue(i));
					sums[cluster] += l.getStart(i) - l.getEnqueue(i);
					counts[cluster]++;
				}
			}
			for (int cluster = 0; cluster < k; cluster++) {
				css[cluster].lambda = 1 / (sums[cluster] / (counts[cluster] * 1.0));
			}
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
		if (l.getEnqueue(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getEnqueue(traceIndex) <= time
				&& time <= l.getStart(traceIndex)) {
			Cluster[] cs = clusters.get(unode);
			long xI = time - l.getEnqueue(traceIndex);
			int c = getClusterNumber(cs, l.getStart(traceIndex) - l.getEnqueue(traceIndex));

			return cs[c].lambda * Math.exp(-cs[c].lambda * xI);
		}
		return 0;
	}

	public String getName() {
		return "CPH enqueue start complete";
	}
}
