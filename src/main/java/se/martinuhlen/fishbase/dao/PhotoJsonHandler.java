package se.martinuhlen.fishbase.dao;

import static java.time.LocalDateTime.parse;
import static java.util.function.Function.identity;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Photo;

class PhotoJsonHandler extends JsonHandler<Photo>
{
	PhotoJsonHandler(Persistence persistence)
	{
		super(Photo.class, persistence);
	}

	@Override
	public JsonElement serialize(Photo photo, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", photo.getId());
		json.addProperty("trip", photo.getTripId());
		json.add("specimens", serializeArray(photo.getSpecimenIds(), identity()));
		json.addProperty("fileName", photo.getFileName());
		json.addProperty("time", photo.getTime().toString());
		json.addProperty("starred", photo.isStarred());
		return json;
	}

	@Override
	public Photo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject obj = json.getAsJsonObject();
		return Photo.asPersisted(obj.get("id").getAsString())
				.tripId(obj.get("trip").getAsString())
				.specimenIds(deserializeArray(obj, "specimens", identity()))
				.fileName(obj.get("fileName").getAsString())
				.time(parse(obj.get("time").getAsString()))
				.starred(obj.get("starred").getAsBoolean());
	}
}
