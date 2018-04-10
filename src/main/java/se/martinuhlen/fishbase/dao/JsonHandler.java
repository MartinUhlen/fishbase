package se.martinuhlen.fishbase.dao;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import se.martinuhlen.fishbase.domain.Domain;

abstract class JsonHandler<D extends Domain<D>> implements JsonSerializer<D>, JsonDeserializer<D>
{
	private final Persistence persistence;
	private final String fileName;

	private final Gson gson;
	private final Type listOfD;

	JsonHandler(Class<D> type, Persistence persistence)
	{
		this.persistence = persistence;
		this.fileName = type.getSimpleName() + ".json";
		this.listOfD = TypeToken.getParameterized(List.class, type).getType();
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(type, this)
				.create();
	}

	/**
	 * Gets a new reader.
	 * 
	 * @return new reader instance
	 */
	Reader reader()
	{
	    try
        {
            return new Reader(new InputStreamReader(persistence.input(fileName), UTF_8));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
	}

	/**
	 * Reads data from persistence.
	 * <p>
	 * The read operation is divided into two steps; 
	 * First a reader is constructed so that data from persistence can start to buffer immediately and then when desired, the data can be read.
	 */
	class Reader
	{
	    private final java.io.Reader reader;

        Reader(java.io.Reader reader)
        {
            this.reader = reader;
        }

	    List<D> read()
	    {
	        try (reader)
	        {
	            return requireNonNullElse(gson.fromJson(reader, listOfD), emptyList());
	        }
	        catch (IOException e)
	        {
	            throw new RuntimeException(e);
	        }
	    }
	}

	void write(Collection<D> objects)
	{
		requireNonNull(objects);
		try (Writer writer = new OutputStreamWriter(persistence.output(fileName), UTF_8))
		{
			gson.toJson(objects, writer);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
