package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureImpl implements Future<IvMObjectValues> {

	private boolean done = false;
	private IvMObjectValues result = null;
	private Semaphore semaphore = new Semaphore(0);
	private boolean allObjectsPresent;

	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	public IvMObjectValues get() throws InterruptedException {
		if (!isDone()) {
			semaphore.acquire();
		}
		return result;
	}

	public IvMObjectValues get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		if (!isDone()) {
			semaphore.tryAcquire(timeout, unit);
		}
		if (isDone()) {
			return result;
		}
		return null;
	}

	public boolean isCancelled() {
		return false;
	}

	public boolean isDone() {
		return done;
	}

	public boolean isAllObjectsPresent() throws InterruptedException {
		if (!isDone()) {
			semaphore.acquire();
		}
		return allObjectsPresent;
	}

	public void set(IvMObjectValues result, boolean allObjectsPresent) {
		this.result = result;
		this.allObjectsPresent = allObjectsPresent;
		done = true;
		semaphore.release(Integer.MAX_VALUE);
	}
}