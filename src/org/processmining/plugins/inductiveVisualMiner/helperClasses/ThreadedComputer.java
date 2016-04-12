package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.UUID;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.ChainLinkCanceller;

/**
 * Execute a task asynchronously. Kill and discard the result of the previous
 * tasks.
 * 
 * @author sleemans
 *
 */
public class ThreadedComputer<I, O> {
	private final Executor executor;
	private final Function<Pair<ChainLinkCanceller, I>, O> computation;
	private final InputFunction<O> onComplete;
	
	private ChainLinkCanceller currentCanceller = null;
	private UUID currentExecution = null;

	public ThreadedComputer(Executor executor, Function<Pair<ChainLinkCanceller, I>, O> computation, InputFunction<O> onComplete) {
		this.executor = executor;
		this.computation = computation;
		this.onComplete = onComplete;
	}
	
	public void cancelCurrentComputation() {
		currentCanceller.cancel();
		currentExecution = null;
	}

	public synchronized void compute(final I input, ProMCanceller globalCanceller) {
		if (currentCanceller != null) {
			currentCanceller.cancel();
		}
		
		final ChainLinkCanceller newCanceller = new ChainLinkCanceller(globalCanceller);
		final UUID newExecution = UUID.randomUUID();
		currentExecution = newExecution;
		currentCanceller = newCanceller;
		executor.execute(new Runnable() {
			public void run() {
				try {
					O result = computation.call(Pair.of(newCanceller, input));
					processResult(newExecution, result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private synchronized void processResult(UUID newExecution, O result) throws Exception {
		if (newExecution.equals(currentExecution)) {
			onComplete.call(result);
		}
	}
}
