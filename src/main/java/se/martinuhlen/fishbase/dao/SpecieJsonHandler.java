package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Specie;

class SpecieJsonHandler extends JsonHandler<Specie>
{
	static final Map<String, String> SPECIE_UUIDS = new TreeMap<>()
	{{
		put("1", "064e57b5-52e2-4d37-907a-e85945ea468b");
		put("2", "72bf3fb8-a1d2-45fc-99ad-610f369740c6");
		put("3", "032a5204-7e35-4e20-98cc-4484268541ab");
		put("4", "a33a1a90-e17d-4be0-ab5a-45f0d26e49bb");
		put("5", "8f73da66-1c54-45d9-a19b-3a8b2e164955");
		put("6", "1d7e861d-ae22-41e5-b548-425cbd6217bc");
		put("7", "d08a2b8d-4009-409a-9f79-433fa2f8677d");
		put("8", "5fcb1d3e-fa10-4570-a6bd-18f6c4a841e6");
		put("9", "12d511ca-8a98-4066-8026-298c86daa608");
		put("10", "205e94ff-5b78-4c6c-b66a-3814b46a19b1");
		put("11", "1e35a2f7-b7f6-49e0-90eb-77182a4c531a");
		put("12", "2e1187b7-c9c3-4108-a087-f14512a7acb5");
		put("13", "3d5cc587-dc0e-4143-b775-0b5c1bb7d7f3");
		put("14", "1384bd9f-d40c-4b41-a9a0-6d92f199399e");
		put("15", "43c51c53-47ab-4213-9c6d-8bf67c066a21");
		put("16", "056a574a-ebca-490f-8958-7b8ad24f98cd");
		put("17", "5a6e0eef-4fae-4925-9b91-b9a7fbab8b72");
		put("18", "aacbb7ed-2c9d-4e15-afa3-8f89a3da42ee");
		put("19", "028fc1cc-d92c-41e1-a89e-7c9e35427222");
		put("20", "a4e1cc9c-cd2a-4b94-9842-005217fbcb9d");
		put("21", "60075bb0-ae48-47dc-8ef6-627ac54f1442");
		put("22", "ab652373-3e03-4d00-817a-cdb42f135e4c");
		put("23", "8f67338e-e5e7-42d5-84b8-c2556d0b8e1b");
		put("24", "11e726ef-54f3-4e0c-b764-9ca3597daf62");
		put("25", "797204e4-8f24-4eb8-b085-b9078c3b9344");
		put("26", "31b73b3f-534c-46d7-945d-3fa35418bfe7");
		put("27", "b2245c74-4ad2-4fc5-90c5-47001f389c58");
		put("28", "fb240073-1c16-4ce4-a2a9-118215fa8c99");
		put("29", "78375690-933e-4a66-92b0-e8ffe873643c");
		put("30", "38eacddc-992e-4685-8755-9cf2e76bfdd2");
		put("31", "40cdf825-60c8-4884-a961-7661ddb5108f");
		put("32", "8ff25023-beaa-4ea6-92f6-9967820d3b89");
		put("33", "542e030f-70ab-40dd-933b-76e2de6e4876");
		put("34", "ab44fb0c-da0a-4d2d-b620-03ba6711c27b");
		put("35", "26e1435f-73e0-4b9a-8c8f-6c845e234dd2");
		put("36", "fa88434e-efbb-41e7-aee7-67ced7152c33");
		put("37", "c6d63c14-cdc0-474a-afaf-37e2607385df");
		put("38", "74203f16-27e6-4041-b4b0-1bd4c7bed647");
		put("39", "9965561d-e064-4d24-8270-6a4c2b74c109");
		put("40", "9fc3c835-156c-438f-9115-20331090cae4");
		put("41", "f90877ab-9496-4bb4-8b4b-cb8ad5cfff3f");
		put("42", "4a88e87f-fa45-4268-abea-bf166e045784");
		put("43", "5b727d89-562d-407b-bdb7-b64f44a5462d");
		put("44", "5759e87b-aaa1-4d58-ad14-f217eab14e97");
		put("45", "12464b4f-5eda-42eb-99cf-079164747553");
		put("46", "34cd6e13-4088-4bdb-a8d4-d01b1268aa79");
		put("47", "3cb33e18-1f6e-42bd-86e9-e569a32bb9f3");
		put("48", "73f9ef8a-6648-484c-a088-8e3d6712f52c");
		put("49", "72499e99-f3b8-47bb-aae9-c14064c49166");
		put("50", "dcc5662d-e8d9-4c02-9195-56743fdd8a7e");
	}};

	SpecieJsonHandler(Persistence persistence)
	{
		super(Specie.class, persistence);
	}

	@Override
	public JsonElement serialize(Specie specie, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", specie.getId());
//		json.addProperty("id", SPECIE_UUIDS.get(specie.getId()));
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
