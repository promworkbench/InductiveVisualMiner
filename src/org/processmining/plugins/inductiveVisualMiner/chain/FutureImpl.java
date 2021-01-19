package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureImpl<C> implements Future<C> {

	private boolean done = false;
	private C result = null;
	private Semaphore semaphore = new Semaphore(0);

	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	public C get() throws InterruptedException, ExecutionException {
		if (!isDone()) {
			semaphore.acquire();
		}
		return result;
	}

	public C get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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

	public void set(C result) {
		this.result = result;
		done = true;
		semaphore.release(Integer.MAX_VALUE);
	}
}