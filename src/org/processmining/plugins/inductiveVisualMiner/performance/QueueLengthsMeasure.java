package org.processmining.plugins.inductiveVisualMiner.performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsMeasure {

	public static void measure(Map<UnfoldedNode, QueueActivityLog> queueActivityLogs, QueueLengths method) {

		QueueLengthsImplUPEnqueueStartComplete qReal = new QueueLengthsImplUPEnqueueStartComplete();

		for (UnfoldedNode unode : queueActivityLogs.keySet()) {

			//find min and max
			long min = Long.MAX_VALUE;
			long max = Long.MIN_VALUE;
			QueueActivityLog l = queueActivityLogs.get(unode);
			for (int i = 0; i < l.size(); i++) {
				min = Math.min(min, l.getEnqueue(i));
				max = Math.max(max, l.getStart(i));
			}

			//compute mean square error
			Pair<Double, Double> mse = rmse(unode, qReal, method, min, max, queueActivityLogs);

			System.out.println("RMSE for activity " + unode + ": " + mse.getA());
			System.out.println("bias for activity " + unode + ": " + mse.getB());
			System.out.println("with method " + method.getName());
			System.out.println("");
		}
	}

	public static Pair<Double, Double> rmse(UnfoldedNode unode, QueueLengthsImplUPEnqueueStartComplete real,
			QueueLengths method, long min, long max, Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		long sum = 0;
		long bias = 0;
		long count = 0;

		File f = new File("d:\\output\\graph-" + unode.getNode().getName() + "-" + method.getName() + ".csv");
		PrintWriter w;
		try {
			w = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return Pair.of(-1d, -1d);
		}

		w.write("minute," + method.getName());
		w.write("\n");
		for (long t = min; t <= max; t += 60000) {
			long sumReal = 0;
			long sumMethod = 0;
			for (int s = 0; s < 60; s++) {
				sumReal += real.getQueueLength(unode, t, queueActivityLogs);
				sumMethod += method.getQueueLength(unode, t, queueActivityLogs);
			}

			w.write(t + "," + (sumReal / 60.0) + "," + (sumMethod / 60.0));
			w.write("\n");

			sum += Math.pow((sumReal / 60.0) - (sumMethod / 60.0), 2);
			bias += (sumReal / 60.0) - (sumMethod / 60.0);
			count++;
		}
		w.close();

		return Pair.of(Math.pow(sum / (count * 1.0), 0.5), bias / (count * 1.0));
	}
}
