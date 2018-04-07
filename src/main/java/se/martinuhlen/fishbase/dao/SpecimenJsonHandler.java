package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;

class SpecimenJsonHandler extends JsonHandler<Specimen>
{
	static final Map<String, String> SPECIMEN_UUIDS = new TreeMap<>()
	{{
		put("1", "6fef027b-c875-4422-a3ee-c2fa0e907817");
		put("2", "39b0679c-d26b-4230-b69c-b92e348dd42d");
		put("3", "bc0e57ca-1532-424f-8267-22f1477f2536");
		put("4", "771bbf05-d413-49ac-8fd2-05e627ec2d96");
		put("5", "3ec3c774-7d4d-468a-9f39-7ab162bbe73f");
		put("6", "119761d8-e5bc-40e1-98d1-3fc89f20f6c9");
		put("7", "2ada7a3c-e368-4ae5-b6fe-bac004a7f727");
		put("8", "7ef50d6c-6bbf-48cd-8b6e-789daff2fb57");
		put("9", "242ee39d-0554-4907-856b-d0a1f9cd8b44");
		put("10", "fefb34ec-12de-4e09-8651-eae625401a03");
		put("11", "9d8e0277-f8a5-4951-af29-614b3201fb04");
		put("12", "52c770c5-bba8-474b-a40e-c58661b60c12");
		put("13", "d75ddb5c-a5c5-4cf2-9b36-bda1837dda77");
		put("14", "b82c4a59-e583-4d01-842f-8a31cf8ece5a");
		put("15", "0f78c2d1-e530-429c-a574-7eed34e93fa3");
		put("16", "8404eff7-6c62-4dd6-9cd0-8d42c531a82e");
		put("17", "e382bd85-6f21-4ff2-8f04-b4042dfec1de");
		put("18", "63c78392-f20e-4db1-a15b-8b36f2406092");
		put("19", "efd592e7-7fc7-4f5a-b05a-ad3f3cff2677");
		put("20", "efca4a14-48b9-4451-83b3-03ec3b4c8899");
		put("21", "6fce326d-7bb7-4d93-b824-1937828b63b8");
		put("22", "223fd97c-8075-4297-a0b0-39f748690253");
		put("23", "c5d35c15-6068-4fac-931a-26a5e879341e");
		put("24", "5416d73c-fd97-4e2d-a8d8-8c7678a9846f");
		put("25", "8d9f2660-d93a-4265-b054-4ad7f99a2a60");
		put("26", "524d25e1-b0a2-4e73-8fba-dfa9ac6cfd59");
		put("27", "94b5f7ad-ace5-459b-bda1-b83a30e2ec30");
		put("28", "2147b41c-293f-490e-9954-f1325adbcd67");
		put("29", "522d31f2-df60-491d-b7e6-296487302f61");
		put("30", "91cea551-794a-4141-b051-039133367eef");
		put("31", "25117ec8-a03b-4e83-816a-a348c4e1f6ef");
		put("32", "f8346991-b622-47c5-abb2-d75da6e700b7");
		put("33", "71cf3902-ece2-47f4-a8b1-c1ba2aebeef6");
		put("34", "e421f1f9-1f38-42f4-acf3-bf560bbe840c");
		put("35", "f1e1f147-10e4-4b9e-8707-c3f27e267603");
		put("36", "aec401f4-d47e-437f-b851-6f78e516477f");
		put("37", "2b4ace62-aa43-43b6-a490-62a9e2d0cd10");
		put("38", "758b5d69-d6e0-4f20-9c6f-a5c5a392be4c");
		put("39", "8ad26096-ef87-4c48-9226-2d34f70c6d4b");
		put("40", "faeb228e-822d-4bd3-be39-6421c98c2e44");
		put("41", "470616a1-72c0-4f28-91a4-dda3dab80b70");
		put("42", "455bea4a-d91b-481b-8ebf-3d99c3f10c80");
		put("43", "0b32e841-58b7-41bf-a999-11baea76b490");
		put("44", "5dcb145d-2826-4a2a-8b97-0a8fd7023169");
		put("45", "43347cca-3d69-4457-9c30-51a2dfe6d8fc");
		put("46", "26273f48-7712-4241-a961-4818b679e5f9");
		put("47", "97ff12f8-08f2-4749-8d14-e91d6ed6807f");
		put("48", "5d5e39f2-c662-4398-86be-a6315e27c638");
		put("49", "7723a13e-0d0e-4607-9da4-7d9d91819cf9");
		put("50", "bb7f3b30-b734-4be7-a754-5d60eedcaeec");
		put("51", "6d88891c-e1e7-4330-831a-edb37d91b00b");
		put("52", "78da4fef-df83-4fee-b706-a674964017ad");
		put("53", "a727b089-e0af-4eb0-9303-5bcbf9b30db6");
		put("54", "5455435a-7de1-4a36-b680-e1a307d5f92b");
		put("55", "f8633d6c-e01d-4913-92e0-a624feb85cdd");
		put("56", "5cf8c9fd-5761-4eb2-88d7-6c289d64687a");
		put("57", "47038dcb-0188-4e8b-8dd4-a08ef1975e73");
		put("58", "d1bc50bd-7cf3-4500-b123-5eb21930567c");
		put("59", "09dcaecc-521f-4559-9e61-25f15dbd49c0");
		put("60", "82021dcf-7e96-486c-93fd-d3d268b30c85");
		put("61", "5089051a-510e-488d-bf35-68139598f991");
		put("62", "1b52356c-67a5-4674-b8c1-abc8c8ce5e6e");
		put("63", "c4efffad-66c3-4e10-a1a8-04869f3afdea");
		put("64", "a7b34fd9-1e9f-4810-b536-f321d6afaec3");
		put("65", "d1141e8f-6b46-4ff2-93d1-144e96d65300");
		put("66", "6f9692c7-0417-4c03-b620-57f06a534f0d");
		put("67", "4a116d26-1fa3-44b7-837d-d3ab3857f4a8");
		put("68", "22d02c6c-356a-4037-b7b6-377751fb0f6f");
		put("69", "5b812c49-5730-44f2-aa74-3a6e39f78069");
		put("70", "662f6fb6-467b-4ee4-bf0a-43421f3a4774");
		put("71", "74480a0e-4bdd-4af7-afb7-e0b6118a6f4d");
		put("72", "0d8858bb-ade7-4db0-b7eb-42051ce602ae");
		put("73", "f4959eac-29af-40a9-9e7b-6adf11389fa1");
		put("74", "3e515733-a2c6-4bd7-9f85-47ddb6a925f0");
		put("75", "10610650-2b0c-4b5b-995f-079a303a4a80");
		put("76", "2cf6cae8-025c-43fe-bd0b-f1c2c2f69fd5");
		put("77", "5b6b9de6-b8f3-4886-be9c-fb46818323d6");
		put("78", "2b21b018-1715-4ce4-8051-e79fc52c7f26");
		put("79", "1d8bc6c8-74af-431b-a55b-a9cc15c448fe");
		put("80", "82448034-17ce-4613-b706-842ec2fa34c4");
		put("81", "907bce0f-aec3-4600-849e-461b1d720dfc");
		put("82", "7f53d6af-c0a2-424d-bfe3-e426bf4ddaed");
		put("83", "a85cd9e9-f41e-42ed-b19b-93806e7fb4d5");
		put("84", "407baea7-d972-40fe-a3a7-e7270456c64e");
		put("85", "66db40e2-bcd8-48f2-8426-12506fffc3f7");
		put("86", "5af3b00f-4d1d-4dde-acb7-89453e3a0a10");
		put("87", "e2c00b70-d962-4a17-9020-c332354617f8");
		put("88", "d8bb5177-736d-43e7-8795-586dcddaa6c2");
		put("89", "13213486-c0b4-40a9-9f9a-ec97bda48ec5");
		put("90", "f7620614-5612-4b08-bbb2-559c4206070c");
		put("91", "fcf18435-8a67-4904-8c2e-3697ad597729");
		put("92", "32b9211b-a286-4396-a1a1-82dbc2c778c1");
		put("93", "87b30c34-5a4d-44a6-a533-50727423e8bf");
		put("94", "27f784b8-43a2-4631-aaee-35eb8541fd94");
		put("95", "35a7dbbc-5ba7-4387-8922-6e6a5cfa96bd");
		put("96", "1687614b-67f6-4d98-80b5-394101550643");
		put("97", "bc073e49-79af-472f-a973-39c57a6fc3a2");
		put("98", "95358ebe-47b4-41eb-9c0c-8df0d4388a46");
		put("99", "2b9c1a4b-2df9-448b-8278-d1ad0d970332");
		put("100", "db773a5d-c6fc-4b00-8f28-80b349f14992");
	}};

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
//		json.addProperty("id", SPECIMEN_UUIDS.get(s.getId()));
//		json.addProperty("tripId", TripJsonHandler.TRIP_UUIDS.get(s.getTripId()));
		json.addProperty("specieId", s.getSpecie().getId());
//		json.addProperty("specieId", SpecieJsonHandler.SPECIE_UUIDS.get(s.getSpecie().getId()));
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
