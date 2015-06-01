package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplCPHComplete extends QueueLengths {

	private class Cluster implements Comparable<Cluster> {
		public double center;
		public double lambda1;
		public double lambda2;
		public double lambda3;

		public int compareTo(Cluster arg0) {
			return Double.compare(center, arg0.center);
		}
	}

	private final Map<UnfoldedNode, Cluster[]> clusters;

	public QueueLengthsImplCPHComplete(Map<UnfoldedNode, QueueActivityLog> queueActivityLogs, int k) {
		clusters = new THashMap<>();
		for (UnfoldedNode unode : queueActivityLogs.keySet()) {
			QueueActivityLog l = queueActivityLogs.get(unode);

			//create intervals
			List<DoublePoint> intervals = new ArrayList<DoublePoint>(l.size());
			for (int index = 0; index < l.size(); index++) {
				double[] d = { (double) l.getComplete(index) - l.getInitiate(index) };
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

			//L2
			//			css[0].lambda1 = 0.00001721049;
			//			css[0].lambda2 = 0.00001720265;
			//			css[0].lambda3 = 0.00001724251;
			//			
			//			css[1].lambda1 = 0.000007458071;
			//			css[1].lambda2 = 0.000007449284;
			//			css[1].lambda3 = 0.000007454065;
			//			
			//			css[2].lambda1 = 0.000003741333;
			//			css[2].lambda2 = 0.000003743186;
			//			css[2].lambda3 = 0.000003743619;
			//			
			//			css[3].lambda1 = 0.000001794651;
			//			css[3].lambda2 = 0.000001805033;
			//			css[3].lambda3 = 0.000001791622;

			//L3
			css[0].lambda1 = 0.00002417718;
			css[0].lambda2 = 0.00002422288;
			css[0].lambda3 = 0.00002421426;

			css[1].lambda1 = 0.000008849225;
			css[1].lambda2 = 0.000008844158;
			css[1].lambda3 = 0.000008870430;

			css[2].lambda1 = 0.000004172628;
			css[2].lambda2 = 0.000004153202;
			css[2].lambda3 = 0.000004147342;

			css[3].lambda1 = 0.000001926176;
			css[3].lambda2 = 0.000001940269;
			css[3].lambda3 = 0.000001916556;
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

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getComplete(traceIndex) > 0 && l.getInitiate(traceIndex) <= time
				&& time <= l.getComplete(traceIndex)) {
			Cluster[] cs = clusters.get(unode);
			long xI = time - l.getInitiate(traceIndex);
			int c = getClusterNumber(cs, l.getComplete(traceIndex) - l.getInitiate(traceIndex));

			DoubleMatrix m = DoubleMatrix.zeros(3, 3);
			m.put(0, 0, (-cs[c].lambda1) * xI);
			m.put(0, 1, cs[c].lambda1 * xI);
			m.put(1, 1, (-cs[c].lambda2) * xI);
			m.put(1, 2, cs[c].lambda2 * xI);
			m.put(2, 2, (-cs[c].lambda3) * xI);
			DoubleMatrix m2 = MatrixFunctions.expm(m);

			return m2.get(0, 1);
		}
		return 0;
	}
}
