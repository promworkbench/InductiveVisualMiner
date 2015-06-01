package org.processmining.plugins.inductiveVisualMiner.performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplOutput {
	
	public QueueLengthsImplOutput(Map<UnfoldedNode, QueueActivityLog> queueActivityLogs) {
		for (Entry<UnfoldedNode, QueueActivityLog> e : queueActivityLogs.entrySet()) {
			try {
				File f = new File("d:\\output\\activity-queue-log " + e.getKey() + ".csv");
				PrintWriter w = new PrintWriter(f);
				QueueActivityLog l = e.getValue();
				w.write("resource,initiate,enqueue,start,complete");
				w.write("\n");
				for (int index = 0; index < l.size(); index++) {
					w.write(l.getResource(index) + "," + l.getInitiate(index) + "," + l.getEnqueue(index) + ","
							+ l.getStart(index) + "," + l.getComplete(index));
					w.write("\n");
				}
				w.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
}
