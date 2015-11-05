package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

/**
 * @author sleemans
 * 
 */
public class Chain {

	private final List<ChainLink<?, ?>> chain = new ArrayList<ChainLink<?, ?>>();
	private final Executor executor;
	private UUID currentExecutionId;
	private int currentExecutionLinkNumber;
	@SuppressWarnings("rawtypes")
	private ChainLink currentExecutionLink;
	private final ProMCanceller globalCanceller;
	
	private final InductiveVisualMinerState state;

	public Chain(Executor executor, InductiveVisualMinerState state, ProMCanceller canceller) {
		this.executor = executor;
		this.state = state;
		this.globalCanceller = canceller;
		currentExecutionId = null;
		currentExecutionLinkNumber = Integer.MAX_VALUE;
		currentExecutionLink = null;
	}

	public void add(ChainLink<?, ?> link) {
		link.setExecutor(executor, this);
		chain.add(link);
	}

	public synchronized void executeNext(UUID execution, final int indexInChain) {
		//execute next link in the chain
		if (currentExecutionId.equals(execution) && !globalCanceller.isCancelled()) {
			if (indexInChain + 1 < chain.size()) {
				currentExecutionLinkNumber = indexInChain + 1;
				currentExecutionLink = chain.get(indexInChain + 1);
				chain.get(indexInChain + 1).execute(currentExecutionId, indexInChain + 1, state);
			} else {
				currentExecutionLink = null;
			}
		}
	}

	public synchronized void execute(Class<? extends ChainLink<?, ?>> c) {
		for (int i = 0; i < chain.size(); i++) {
			ChainLink<?, ?> cl = chain.get(i);
			if (c.isInstance(cl)) {
				//see if this chain execution should overwrite (= starts earlier in the chain) than the next one
				//if we drop in ahead of an existing execution, our work will have to be redone again anyway 
				if (i <= currentExecutionLinkNumber) {

					//cancel current execution
					if (currentExecutionLink != null) {
						currentExecutionLink.cancel();
					}

					//replace execution
					currentExecutionId = UUID.randomUUID();
					currentExecutionLinkNumber = i;
					currentExecutionLink = cl;

					cl.execute(currentExecutionId, currentExecutionLinkNumber, state);
				}
				return;
			}
		}
	}

	public UUID getCurrentExecution() {
		return currentExecutionId;
	}
}