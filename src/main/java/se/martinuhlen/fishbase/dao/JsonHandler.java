package se.martinuhlen.fishbase.dao;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
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
		this.persistence = persistence;;
		this.fileName = type.getSimpleName() + ".json";
		this.listOfD = TypeToken.getParameterized(List.class, type).getType();
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(type, this)
				.create();
	}

	List<D> read()
	{
		try (Reader reader = new InputStreamReader(persistence.input(fileName), UTF_8))
		{
			return requireNonNullElse(gson.fromJson(reader, listOfD), emptyList());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
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
