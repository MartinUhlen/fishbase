package se.martinuhlen.fishbase.dao;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

class TripJsonHandler extends JsonHandler<Trip>
{
	private Function<String, Specimen> specimen;

	TripJsonHandler(Persistence persistence, Function<String, Specimen> specimen)
	{
		super(Trip.class, persistence);
		this.specimen = specimen;
	}

	@Override
	public JsonElement serialize(Trip trip, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", trip.getId());
		json.addProperty("description", trip.getDescription());
		json.addProperty("startDate", trip.getStartDate().toString());
		json.addProperty("endDate", trip.getEndDate().toString());
		json.addProperty("text", trip.getText());
		json.add("specimens", serializeSpecimens(trip));
		return json;
	}

	private JsonArray serializeSpecimens(Trip trip)
	{
		return trip
				.getSpecimens()
				.stream()
				.map(Specimen::getId)
				.collect(Collector.of(
						() -> new JsonArray(trip.getSpecimens().size()),
						(array, id) -> array.add(id),
						(array1, array2) ->
						{
							array1.addAll(array2);
							return array1;
						}));
	}

	@Override
	public Trip deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject obj = json.getAsJsonObject();
		return Trip.asPersisted(obj.get("id").getAsString())
				.description(obj.get("description").getAsString())
				.startDate(LocalDate.parse(obj.get("startDate").getAsString()))
				.endDate(LocalDate.parse(obj.get("endDate").getAsString()))
				.text(obj.get("text").getAsString())
				.specimens(deserializeSpecimens(obj));
	}

	private Collection<Specimen> deserializeSpecimens(JsonObject obj)
	{
		JsonArray jsonArray = obj.get("specimens").getAsJsonArray();
		return StreamSupport.stream(jsonArray.spliterator(), false)
				.map(element -> element.getAsString())
				.map(specimenId -> specimen.apply(specimenId))
				.collect(toList());
	}
}
