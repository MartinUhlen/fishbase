package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;

class SpecimenJsonHandler extends JsonHandler<Specimen>
{
	SpecimenJsonHandler(Persistence persistence)
	{
		super(Specimen.class, persistence);
	}

	@Override
	public JsonElement serialize(Specimen s, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", s.getId());
		json.addProperty("tripId", s.getTripId());
		json.addProperty("specieId", s.getSpecie().getId());
		json.addProperty("weight", s.getWeight());
		json.addProperty("length", s.getLength());
		json.addProperty("location", s.getLocation());
		json.addProperty("instant", s.getInstant().toString());
		json.addProperty("method", s.getMethod());
		json.addProperty("bait", s.getBait());
		json.addProperty("weather", s.getWeather());
		json.addProperty("text", s.getText());
		return json;
	}

	@Override
	public Specimen deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject obj = json.getAsJsonObject();
		return Specimen.asPersisted(obj.get("id").getAsString(), obj.get("tripId").getAsString())
				.setSpecie(Specie.asPersisted(obj.get("specieId").getAsString()))
				.setWeight(obj.get("weight").getAsInt())
				.setLength(obj.get("length").getAsFloat())
				.setLocation(obj.get("location").getAsString())
				.setInstant(LocalDateTime.parse(obj.get("instant").getAsString()))
				.setMethod(obj.get("method").getAsString())
				.setBait(obj.get("bait").getAsString())
				.setWeather(obj.get("weather").getAsString())
				.setText(obj.get("text").getAsString());
	}
}
