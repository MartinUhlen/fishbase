package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Specie;

class SpecieJsonHandler extends JsonHandler<Specie>
{
	SpecieJsonHandler(Persistence persistence)
	{
		super(Specie.class, persistence);
	}

	@Override
	public JsonElement serialize(Specie specie, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", specie.getId());
		json.addProperty("name", specie.getName());
		json.addProperty("regWeight", specie.getRegWeight());
		json.addProperty("freshWater", specie.isFreshWater());;
		return json;
	}

	@Override
	public Specie deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject obj = json.getAsJsonObject();
		return Specie.asPersisted(obj.get("id").getAsString())
				.setName(obj.get("name").getAsString())
				.setRegWeight(obj.get("regWeight").getAsInt())
				.setFreshWater(obj.get("freshWater").getAsBoolean());
	}
}
