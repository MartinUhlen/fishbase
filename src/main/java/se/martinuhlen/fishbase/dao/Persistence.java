package se.martinuhlen.fishbase.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Persistence
{
	InputStream input(String name) throws IOException;

	OutputStream output(String name) throws IOException;
}
