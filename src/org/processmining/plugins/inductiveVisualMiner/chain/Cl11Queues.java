package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengths;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplBPComplete;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCPHComplete;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplCPHStartComplete;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplPHComplete;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsImplPHStartComplete;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsMeasure;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueMineActivityLog;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Cl11Queues extends ChainLink<IvMLog, QueueLengthsWrapper> {

	protected IvMLog generateInput(InductiveVisualMinerState state) {
		return state.getIvMLog();
	}

	protected QueueLengthsWrapper executeLink(IvMLog input) {
		Map<UnfoldedNode, QueueActivityLog> queueActivityLogs = QueueMineActivityLog.mine(input);
		
		int k = 4;
		List<QueueLengths> methods = new ArrayList<QueueLengths>(Arrays.asList(
//			new QueueLengthsImplBPComplete(queueActivityLogs),
//			new QueueLengthsImplUPEnqueueStartComplete(),
//			new QueueLengthsImplUPComplete(),
//			new QueueLengthsImplUPStartComplete(),
			new QueueLengthsImplPHComplete(),
			new QueueLengthsImplPHStartComplete(),
//			new QueueLengthsImplCLIComplete(queueActivityLogs, k),
//			new QueueLengthsImplCLIStartComplete(queueActivityLogs, k)//,
			new QueueLengthsImplCPHComplete(queueActivityLogs, k),
			new QueueLengthsImplCPHStartComplete(queueActivityLogs, k)
		));
		
		for (QueueLengths method : methods) {
			QueueLengthsMeasure.measure(queueActivityLogs, method);
		}
				
		
		QueueLengths method = new QueueLengthsImplBPComplete(queueActivityLogs);
		return new QueueLengthsWrapper(method, queueActivityLogs);
	}

	protected void processResult(QueueLengthsWrapper result, InductiveVisualMinerState state) {
		state.setQueueLengths(result);
	}

	public void cancel() {

	}
}
