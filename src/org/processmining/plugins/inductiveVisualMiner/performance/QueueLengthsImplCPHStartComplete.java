package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class QueueLengthsImplCPHStartComplete extends QueueLengths {

	private class Cluster implements Comparable<Cluster> {
		public double center;
		public double lambda1;
		public double lambda2;

		public int compareTo(Cluster arg0) {
			return Double.compare(center, arg0.center);
		}
	}

	private final TIntObjectMap<Cluster[]> clusters;

	public QueueLengthsImplCPHStartComplete(TIntObjectMap<QueueActivityLog> queueActivityLogs, int k) {
		clusters = new TIntObjectHashMap<>(10, 0.5f, -1);
		for (int unode : queueActivityLogs.keySet().toArray()) {
			QueueActivityLog l = queueActivityLogs.get(unode);

			//create intervals
			List<DoublePoint> intervals = new ArrayList<DoublePoint>(l.size());
			for (int index = 0; index < l.size(); index++) {
				double[] d = { (double) l.getStart(index) - l.getInitiate(index) };
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

			//L1
			//			css[0].lambda1 = 0.00003208256;
			//			css[0].lambda2 = 0.00003208085;
			//
			//			css[1].lambda1 = 0.00001378153;
			//			css[1].lambda2 = 0.00001376297;
			//
			//			css[2].lambda1 = 0.000007196115;
			//			css[2].lambda2 = 0.000007188530;
			//
			//			css[3].lambda1 = 0.000002222535;
			//			css[3].lambda2 = 0.000002198928;

			//L2
			//			css[0].lambda1 = 0.00006698487;
			//			css[0].lambda2 = 0.00027087680;
			//
			//			css[1].lambda1 = 0.00002102458;
			//			css[1].lambda2 = 0.00002112377;
			//
			//			css[2].lambda1 = 0.000009177158;
			//			css[2].lambda2 = 0.000009199203;
			//
			//			css[3].lambda1 = 0.000002331028;
			//			css[3].lambda2 = 0.000002336724;

			//L3
			css[0].lambda1 = 0.000001513577;
			css[0].lambda2 = 0.000001503805;

			css[1].lambda1 = 0.0000005987237;
			css[1].lambda2 = 0.0000006014447;

			css[2].lambda1 = 0.0000003380778;
			css[2].lambda2 = 0.0000003412313;

			css[3].lambda1 = 0.0000001558959;
			css[3].lambda2 = 0.0000001538938;
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
			long xI = time - l.getInitiate(traceIndex);
			int c = getClusterNumber(cs, l.getStart(traceIndex) - l.getInitiate(traceIndex));

			DoubleMatrix m = DoubleMatrix.zeros(2, 2);
			m.put(0, 0, (-cs[c].lambda1) * xI);
			m.put(0, 1, cs[c].lambda1 * xI);
			m.put(1, 1, (-cs[c].lambda2) * xI);
			DoubleMatrix m2 = MatrixFunctions.expm(m);

			return m2.get(0, 1);
		}
		return 0;
	}

	public String getName() {
		return "CPH start complete";
	}
}
