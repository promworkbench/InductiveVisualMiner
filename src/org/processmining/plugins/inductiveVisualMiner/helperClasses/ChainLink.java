package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.UUID;
import java.util.concurrent.Executor;

public abstract class ChainLink<I, O> {

	private Chain chain;
	private Executor executor;

	/**
	 * 
	 * @return
	 * 
	 *         Gathers all inputs required for the computation
	 */
	protected abstract I generateInput();

	/**
	 * 
	 * @param input
	 * @return
	 * 
	 *         Performs the computation, given the input. Side-effects not
	 *         allowed; should be thread-safe and static
	 */
	protected abstract O executeLink(I input);

	/**
	 * 
	 * @param result
	 * 
	 *            Processes the result of the computation.
	 */
	protected abstract void processResult(O result);

	public void execute(final UUID execution, final int indexInChain) {
		final I input = generateInput();
		executor.execute(new Runnable() {
			public void run() {
				O result = executeLink(input);
				if (chain.getCurrentExecution().equals(execution)) {
					processResult(result);
					chain.executeNext(execution, indexInChain);
				}
			}
		});
	}

	public void setExecutor(Executor executor, Chain chain) {
		this.executor = executor;
		this.chain = chain;
	}
}