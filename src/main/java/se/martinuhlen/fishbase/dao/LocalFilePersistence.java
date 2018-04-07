package se.martinuhlen.fishbase.dao;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalFilePersistence implements Persistence
{
	private final File directory;

	public LocalFilePersistence(File directory)
	{
		this.directory = directory;
	}

	@Override
	public InputStream input(String name) throws IOException
	{
		File file = fileOf(name);
		return file.exists()
				? new FileInputStream(file)
				: new ByteArrayInputStream(EMPTY_BYTE_ARRAY);
	}

	@Override
	public OutputStream output(String name) throws IOException
	{
		return new FileOutputStream(fileOf(name));
	}

	private File fileOf(String name)
	{
		return new File(directory, name);
	}
}
