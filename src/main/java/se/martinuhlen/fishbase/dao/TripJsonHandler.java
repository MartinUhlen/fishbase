package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

class TripJsonHandler extends JsonHandler<Trip>
{
	private Function<String, Specimen> specimen;
	private Function<String, Photo> photo;

	TripJsonHandler(Persistence persistence, Function<String, Specimen> specimen, Function<String, Photo> photo)
	{
		super(Trip.class, persistence);
		this.specimen = specimen;
		this.photo = photo;
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
		json.add("specimens", serializeArray(trip.getSpecimens(), Specimen::getId));
		json.add("photos", serializeArray(trip.getPhotos(), Photo::getId));
		return json;
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
				.specimens(deserializeArray(obj, "specimens", specimen::apply))
				.photos(deserializeArray(obj, "photos", photo::apply));
	}
}
