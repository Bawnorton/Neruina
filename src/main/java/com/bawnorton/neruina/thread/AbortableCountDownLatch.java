package com.bawnorton.neruina.thread;

import com.bawnorton.neruina.exception.AbortedException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AbortableCountDownLatch extends CountDownLatch {
	private boolean aborted = false;

	public AbortableCountDownLatch(int count) {
		super(count);
	}

	public void abort() {
		if (aborted || getCount() == 0) return;

		this.aborted = true;
		while (getCount() > 0) {
			countDown();
		}
	}

	@Override
	public void await() throws InterruptedException {
		super.await();
		if (aborted) {
			throw new AbortedException();
		}
	}

	@Override
	public boolean await(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		boolean result = super.await(timeout, unit);
		if (aborted) {
			throw new AbortedException();
		}
		return result;
	}
}
