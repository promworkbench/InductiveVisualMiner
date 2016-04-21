package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.UUID;
import java.util.concurrent.Executor;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

/**
 * Execute a task asynchronously. Kill and discard the result of the previous
 * tasks.
 * 
 * @author sleemans
 *
 */
public class ThreadedComputer<I, O> {
	private final Executor executor;
	private final FunctionCancellable<I, O> computation;
	private final InputFunction<O> onComplete;

	private IvMCanceller currentCanceller = null;
	private UUID currentExecution = null;

	public ThreadedComputer(Executor executor, FunctionCancellable<I, O> computation, InputFunction<O> onComplete) {
		this.executor = executor;
		this.computation = computation;
		this.onComplete = onComplete;
	}

	public void cancelCurrentComputation() {
		currentCanceller.cancel();
		currentExecution = null;
	}

	public synchronized void compute(final I input, IvMCanceller chainLinkCanceller) {
		if (currentCanceller != null) {
			currentCanceller.cancel();
		}

		final IvMCanceller newCanceller = new IvMCanceller(chainLinkCanceller);
		final UUID newExecution = UUID.randomUUID();
		currentExecution = newExecution;
		currentCanceller = newCanceller;
		executor.execute(new Runnable() {
			public void run() {
				try {
					O result = computation.call(input, newCanceller);
					processResult(newExecution, result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private synchronized void processResult(UUID newExecution, O result) throws Exception {
		if (newExecution.equals(currentExecution) && !currentCanceller.isCancelled()) {
			onComplete.call(result);
		}
	}
}
