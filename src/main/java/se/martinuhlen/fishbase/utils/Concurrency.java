package se.martinuhlen.fishbase.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Concurrency
{
	public static ExecutorService newSingleThreadExecutor(boolean daemon, String threadName)
	{
		return newFixedThreadPool(1, daemon, threadName);
	}

	public static ExecutorService newFixedThreadPool(int threadSize, boolean daemon, String threadNamePrefix)
	{
		return Executors.newFixedThreadPool(threadSize, new ThreadFactory()
		{
			private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
			private final AtomicInteger cnt = new AtomicInteger();

			@Override
			public Thread newThread(Runnable task)
			{
				Thread t = defaultFactory.newThread(task);
				t.setDaemon(daemon);
				if (threadNamePrefix != null)
				{
					t.setName(threadNamePrefix + (threadSize == 1 ? "" : "#" + cnt.incrementAndGet()));
				}
				return t;
			}
		});
	}
}
