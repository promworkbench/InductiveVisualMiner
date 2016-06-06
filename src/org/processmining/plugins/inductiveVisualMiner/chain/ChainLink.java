package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.UUID;
import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public abstract class ChainLink<I, O> {

	private Chain chain;
	private Executor executor;
	private Runnable onStart;
	private Runnable onComplete;
	private Function<Exception, Object> onException;

	/**
	 * 
	 * @return
	 * 
	 *         Gathers all inputs required for the computation
	 */
	protected abstract I generateInput(InductiveVisualMinerState state);

	/**
	 * Performs the computation, given the input. Side-effects not allowed;
	 * should be thread-safe and static.
	 * 
	 * @param input
	 * @param canceller
	 * @return
	 * @throws Exception
	 */
	protected abstract O executeLink(I input, IvMCanceller canceller) throws Exception;

	/**
	 * 
	 * @param result
	 * 
	 *            Processes the result of the computation. Guarantee: if
	 *            executed, then all inputs are still relevant and have not been
	 *            replaced.
	 */
	protected abstract void processResult(O result, InductiveVisualMinerState state);

	public void execute(final UUID execution, final int indexInChain, final InductiveVisualMinerState state,
			final IvMCanceller canceller) {
		final I input = generateInput(state);

		executor.execute(new Runnable() {
			public void run() {
				if (onStart != null) {
					SwingUtilities.invokeLater(onStart);
				}
				final O result;
				try {
					result = executeLink(input, canceller);
				} catch (Exception e) {
					try {
						onException.call(e);
						e.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					return;
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (chain.getCurrentExecution().equals(execution) && !canceller.isCancelled()) {
							processResult(result, state);
							if (onComplete != null) {
								onComplete.run();
							}
							chain.executeNext(execution, indexInChain);
						}
					}
				});
			}
		});
	}

	/**
	 * Sets a callback that is executed on start of execution. Will be executed
	 * in the main (gui) thread.
	 * 
	 * @param onStart
	 */
	public void setOnStart(Runnable onStart) {
		this.onStart = onStart;
	}

	/**
	 * Sets a callback that is executed on completion of execution. Will be
	 * executed in the main (gui) thread.
	 * 
	 * @param onStart
	 */
	public void setOnComplete(Runnable onComplete) {
		this.onComplete = onComplete;
	}

	public void setOnException(Function<Exception, Object> onException) {
		this.onException = onException;
	}

	public void setExecutor(Executor executor, Chain chain) {
		this.executor = executor;
		this.chain = chain;
	}
}