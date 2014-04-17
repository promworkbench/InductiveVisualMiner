package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * @author sleemans
 * 
 */
public class Chain {

	private final List<ChainLink<?, ?>> chain = new ArrayList<ChainLink<?, ?>>();
	private final Executor executor;
	private UUID currentExecution;
	private int currentExecutionLink;

	public Chain(Executor executor) {
		this.executor = executor;
		currentExecution = null;
		currentExecutionLink = Integer.MAX_VALUE;
	}

	public void add(ChainLink<?, ?> link) {
		link.setExecutor(executor, this);
		chain.add(link);
	}

	public synchronized void executeNext(UUID execution, final int indexInChain) {
		//execute next link in the chain
		if (indexInChain + 1 < chain.size() && currentExecution.equals(execution)) {
			currentExecutionLink = indexInChain + 1;
			chain.get(indexInChain + 1).execute(currentExecution, indexInChain + 1);
		}
	}

	public void execute(Class<? extends ChainLink<?, ?>> c) {
		for (int i = 0; i < chain.size(); i++) {
			ChainLink<?, ?> cl = chain.get(i);
			if (c.isInstance(cl)) {
				//see if this chain execution should overwrite (= starts earlier in the chain) than the next one
				//if we drop in ahead of an existing execution, our work will have to be redone again anyway 
				if (i <= currentExecutionLink) {
					currentExecution = UUID.randomUUID();
					currentExecutionLink = i;
					cl.execute(currentExecution, currentExecutionLink);
				}
				return;
			}
		}
	}

	public UUID getCurrentExecution() {
		return currentExecution;
	}
}