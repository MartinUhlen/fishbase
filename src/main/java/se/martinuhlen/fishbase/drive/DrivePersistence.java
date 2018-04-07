package se.martinuhlen.fishbase.drive;

import static java.util.concurrent.TimeUnit.DAYS;
import static se.martinuhlen.fishbase.utils.Checked.$;
import static se.martinuhlen.fishbase.utils.Concurrency.newSingleThreadExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import se.martinuhlen.fishbase.dao.Persistence;

public class DrivePersistence implements Persistence
{
	private final DriveService service;
	private final Map<String, ExecutorService> executors;

	public DrivePersistence(DriveService service)
	{
		this.service = service;
		this.executors = new ConcurrentHashMap<>();
	}

	@Override
	public InputStream input(String name) throws IOException
	{
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream output = new PipedOutputStream(input);
		execute(name, () ->	service.download(name, output));
		return input;
	}

	@Override
	public OutputStream output(String name) throws IOException
	{
		PipedInputStream input = new PipedInputStream();
		PipedOutputStream output = new PipedOutputStream(input);
		execute(name, () -> service.upload(name, input));
		return output;
	}

	private void execute(String name, Runnable task)
	{
		executors
			.computeIfAbsent(name, n -> newSingleThreadExecutor(true, name))
			.execute(task);
	}

	/**
	 * Shuts down this persistence instance, blocks until all data have written/read.
	 */
	public void shutdown()
	{
		executors.values().forEach($(executor ->
		{
			executor.shutdown();
			executor.awaitTermination(1, DAYS);
		}));
	}
}
