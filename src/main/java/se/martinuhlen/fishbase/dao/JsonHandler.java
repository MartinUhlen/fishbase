package se.martinuhlen.fishbase.dao;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
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

	protected <E> JsonArray serializeArray(Collection<E> collection, Function<? super E, String> mapper)
	{
		return collection
				.stream()
				.map(mapper)
				.collect(Collector.of(
						() -> new JsonArray(collection.size()),
						(array, value) -> array.add(value),
						(array1, array2) ->
						{
							array1.addAll(array2);
							return array1;
						}));
	}

	protected <E> List<E> deserializeArray(JsonObject obj, String name, Function<String, E> mapper)
	{
		JsonArray jsonArray = obj.get(name).getAsJsonArray();
		return StreamSupport.stream(jsonArray.spliterator(), false)
				.map(element -> element.getAsString())
				.map(photoId -> mapper.apply(photoId))
				.collect(toUnmodifiableList());
	}
}
