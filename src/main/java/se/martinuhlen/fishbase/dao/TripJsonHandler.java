package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;
import java.time.LocalDate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Trip;

class TripJsonHandler extends JsonHandler<Trip>
{
	TripJsonHandler(Persistence persistence)
	{
		super(Trip.class, persistence);
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
		return json;
	}

	@Override
	public Trip deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject obj = json.getAsJsonObject();
		return Trip.asPersisted(obj.get("id").getAsString())
				.setDescription(obj.get("description").getAsString())
				.setStartDate(LocalDate.parse(obj.get("startDate").getAsString()))
				.setEndDate(LocalDate.parse(obj.get("endDate").getAsString()))
				.setText(obj.get("text").getAsString());
	}
}
