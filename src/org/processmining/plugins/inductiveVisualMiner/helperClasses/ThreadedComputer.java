package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.ChainLink.ResettableCanceller;

/**
 * Execute a task asynchronously. Kill and discard the result of the previous
 * tasks.
 * 
 * @author sleemans
 *
 */
public class ThreadedComputer<I, O> {
	private final ExecutorService executor;
	private final Function<Pair<ResettableCanceller, I>, O> computation;
	private final InputFunction<O> onComplete;
	
	private ResettableCanceller currentCanceller = null;
	private UUID currentExecution = null;

	public ThreadedComputer(Function<Pair<ResettableCanceller, I>, O> computation, InputFunction<O> onComplete) {
		executor = Executors.newFixedThreadPool(3);
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
		
		final ResettableCanceller newCanceller = new ResettableCanceller(globalCanceller);
		final UUID newExecution = UUID.randomUUID();
		currentExecution = newExecution;
		currentCanceller = newCanceller;
		executor.submit(new Runnable() {
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
