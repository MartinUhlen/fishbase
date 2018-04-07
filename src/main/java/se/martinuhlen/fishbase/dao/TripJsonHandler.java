package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import se.martinuhlen.fishbase.domain.Trip;

class TripJsonHandler extends JsonHandler<Trip>
{
	static final Map<String, String> TRIP_UUIDS = new TreeMap<>()
	{{
		put("1", "9d325c4b-a245-41f2-b059-72bb455f85a5");
		put("2", "7785e9d9-69f1-4e18-814e-24b89c9c305b");
		put("3", "3f73c922-d70c-46b1-a7d8-1ca28af7b860");
		put("4", "de53a021-08e3-4bf4-9aa3-cb69a1a818e4");
		put("5", "9a91ac31-5df0-4153-9059-0937b5938983");
		put("6", "b19d67af-bfba-4504-867b-490d5c3a7524");
		put("7", "e3bd8b33-8cde-4a23-ab67-b18c9b3fec84");
		put("8", "27179188-69f6-4795-8c6b-83e618eb754f");
		put("9", "5569c3c2-a9d0-49f2-ac09-def7d74858a6");
		put("10", "2e9878d4-66d5-457f-85d9-2d307a66c1eb");
		put("11", "10b77abc-3871-48ec-b8c1-618c4bc7d6df");
		put("12", "650ebe20-4734-49dd-afe3-d62148671961");
		put("13", "6b3b623e-88d7-4572-b667-0eba62621a5f");
		put("14", "7a99848d-57e5-4057-8cdd-07bfd2ffbd63");
		put("15", "9ffb02c3-1229-4120-9aa8-9bd5c861c2a0");
		put("16", "5374b303-6312-471f-8cfe-f58fcd033aeb");
		put("17", "650d4210-ed5a-4447-8c6f-96a2469511ac");
		put("18", "a0c440af-c57b-4d68-ac8c-b6931ba215b7");
		put("19", "1d1323b1-a481-4d16-a335-4705de83185d");
		put("20", "b34646c3-70d3-49f2-9913-865901627108");
		put("21", "7080afd5-08b5-44a3-9acc-c2d43beaeca4");
		put("22", "f0f33a2f-31cd-4cf1-8008-d8c4835e4e70");
		put("23", "d854c2d5-6566-493f-b207-706e41594f2b");
		put("24", "92d611e7-dcc5-4566-bf7a-952a26766c50");
		put("25", "8c0aba91-cb30-438b-8153-bc5f5382cf0a");
		put("26", "8a7f7872-13c0-43b1-bb5e-7b66199fd8b9");
		put("27", "74d07ed5-fb42-447d-9716-046751546725");
		put("28", "92b2194e-c5fd-46dd-9f51-7b84ade07140");
		put("29", "41e41bea-e68e-4ac7-8766-26ec644113c2");
		put("30", "8db10412-e206-4c8b-a4bd-b9333825d6c5");
		put("31", "3b1c24d6-0c84-4deb-8e78-14e6ba999c93");
		put("32", "fda190e0-7e49-4636-af52-4034ef7ae1c9");
		put("33", "fcdcd4d2-0c8a-4f60-a1a8-a2aa4e281051");
		put("34", "cb60cc85-96e7-4b51-bc6b-1c65e3c1a0a9");
		put("35", "e1ad6d7d-9999-4b48-a15b-634e671961d6");
		put("36", "6eb49b98-daef-485f-a217-807d621fe99d");
		put("37", "c531d5fa-71ea-4791-a26e-eea5deac4cf5");
		put("38", "b4280346-0c3d-4ab9-a2fc-c7c524cdaff0");
		put("39", "d6a500be-012b-49bd-b838-522b3034d913");
		put("40", "5f5f8864-8e99-4051-a47f-0993bea1bcee");
		put("41", "869d0eec-5132-435f-8f45-d6a88470923c");
		put("42", "d1576975-89cb-46d4-8f17-9161ac28981a");
		put("43", "0d51b552-f7b3-4462-a76d-be4ad3210197");
		put("44", "f5546a39-7628-42aa-be52-c3dcaa247308");
		put("45", "93d26411-2f64-4812-a761-37d76bf90859");
		put("46", "c9e91cde-7404-4182-a5f3-91d4c5ccccc5");
		put("47", "7d643fd6-f6ff-40f6-a546-2d8265d8fbd4");
		put("48", "05807002-0b97-4757-95ed-6d35af55a3d0");
		put("49", "d50c58a9-84c0-4fc0-891c-38f5074a26cb");
		put("50", "87d97c5a-ca32-4df8-a9da-02e869733871");
		put("51", "feef4742-ad6d-43c8-bf8b-c91f86a4c2fa");
		put("52", "011c8ba7-0ed1-46b9-adec-745000764a88");
		put("53", "627226bd-adba-49f1-b496-8b2949c908bd");
		put("54", "d1733fca-ebc9-4ef0-b9f5-b4f48971e8c9");
		put("55", "c46c3629-7b99-45c2-af3f-31a418e04d72");
		put("56", "941cbedb-243e-42cd-9971-8b08fc60aa9d");
		put("57", "2d085096-7ba2-4045-8653-2e85b5a67dec");
		put("58", "04d5db12-7fec-440e-b26f-b4f35917f74f");
		put("59", "c9376d4c-2e3a-46e2-8f82-4b6709d5c204");
		put("60", "d19ca3bd-8d2d-4b27-8b59-25bd62c19cbb");
		put("61", "027007a5-b639-4431-83e0-1f444711ee6b");
		put("62", "b30c56ee-01f7-485e-8112-1cb83a7f687d");
		put("63", "75c35e8f-5325-44af-8eed-9132dc862921");
		put("64", "b84dab79-b59a-41be-9482-3e4fa7e0ff07");
		put("65", "e410600d-bdf6-4d18-a0be-f0efbb4d6144");
		put("66", "2a8ec039-6523-4f64-81b3-be4bd27074f7");
		put("67", "4c232e4b-e061-4b76-bad7-32e3bed91796");
		put("68", "bbe28bb3-3429-4270-b7c7-e62dd16e160a");
		put("69", "42cd801e-d018-4c52-afb4-02ffc17c71f8");
		put("70", "117bbd9e-ce18-4472-bc09-ba07b45af90b");
		put("71", "8a2d47df-d136-4fac-8155-305b0d78fe6d");
		put("72", "99168191-64b4-44b5-bee1-7298a295858d");
		put("73", "eff32055-810b-460a-bbcb-51206ce2c36d");
		put("74", "6b6641ff-3903-4772-bef3-ba5ebd2d9d89");
		put("75", "c01ef8a7-c910-44ee-ac16-a23bba3e8b4d");
		put("76", "a9370341-4e3e-4ac5-92cd-16f0a761bc07");
		put("77", "6cebbbbc-afc5-4d8c-9c24-b1fdddf548e6");
		put("78", "ff3962d6-9783-47d3-abc7-cb3ee12f04df");
		put("79", "2faed75e-a504-43ac-ab61-ece4e50b3f47");
		put("80", "fc94b2cd-be9a-401b-aefe-a0e2088c1373");
		put("81", "e2fb6e20-5de6-42b4-b0b6-ffced17dbe8d");
		put("82", "a3eb0fad-eddb-4c98-be9a-df41fda7fba9");
		put("83", "2fb340d9-79c4-46fb-9b4e-c4215058ce1d");
		put("84", "df9302cb-68ec-4ac5-b17e-3e2333b708bb");
		put("85", "95291bf1-882d-4500-8856-ce40172d3d99");
		put("86", "f2bfb876-5876-4547-aba7-97bf913175d4");
		put("87", "fbd0b10f-7337-4981-b827-0ac334750a98");
		put("88", "02af8e53-aeff-4d24-bd4f-3996587a8658");
		put("89", "f2682f16-b06e-4f2a-9ec1-3b726d678aa4");
		put("90", "980f693e-e15c-4ce3-9c33-4c637ce8683e");
		put("91", "cdbfd107-c87c-4daa-8224-68be57b38487");
		put("92", "9118870f-1343-4bc0-bb7f-3e72c00c3c16");
		put("93", "a762f105-808a-430a-91aa-e920fd9e4832");
		put("94", "19d82e39-d1d2-4456-ac91-d87c64b8c8c9");
		put("95", "b296ae5b-94d3-49f4-9dab-e696ec55b7b4");
		put("96", "233f1593-ee5f-4cb2-a649-217d852f4046");
		put("97", "bd4ba10f-199f-4840-8cf6-348a22e5f657");
		put("98", "0bc13532-6ac6-4d57-9732-f70bbd65ed36");
		put("99", "313ee688-5561-4ad8-9203-4fe22d9d5bad");
		put("100", "58cf8fd0-d4d6-4a1e-8db8-db41de4cd696");
		put("101", "05ca5ef7-67f9-48fe-af30-5d9e1ef7bd16");
		put("102", "5a690c05-5bc1-4f98-8d9d-456537dff78b");
		put("103", "451a12ef-4357-4332-ba0f-0465d2234185");
		put("104", "e4a691a4-7a10-4a29-ab00-58b9149d029e");
		put("105", "f1ff95fb-c120-4341-855d-39e018c64cb8");
		put("106", "2af01e90-0cc2-4e1a-b576-ea0fb5b937a1");
		put("107", "73f2890f-0aa7-47c7-a97a-461bd3c0f6d3");
		put("108", "780e0999-dabe-4b90-a9bb-a410ce9ae4eb");
		put("109", "8181806c-79c0-45f2-b2fa-134682331058");
		put("110", "b10f6ade-4ac5-4ca8-8946-69cd963871f7");
		put("111", "9089735d-096a-4c02-a047-3155d192b844");
		put("112", "425c8802-7403-46c9-804a-6d81e0bb7bfe");
		put("113", "54d7c638-9530-4abc-b58e-2e70c70ec7af");
		put("114", "0f1c5e4f-494c-4c65-8b37-ea07c45c0865");
		put("115", "a0904816-7f3e-4c8b-ac12-e16bdff88a22");
		put("116", "bd1cffac-5d76-48fa-b4d4-5ccbca916bee");
		put("117", "a7fd3065-b9a1-4734-92d6-803452129645");
		put("118", "6e048562-bb31-42e0-89a5-e711d771fc96");
		put("119", "e4bf1815-d3c0-4eef-8e49-f4fbbeac0f5d");
		put("120", "e69757bf-13bb-42f0-8991-ae574bb43f44");
		put("121", "29891f89-3d50-419c-b7f2-dafa3fff70d4");
		put("122", "bceae202-7126-41da-9343-3df3198130d8");
		put("123", "dbfae579-acf6-4df0-b0db-350b6a3ca69e");
		put("124", "4b1c82ed-716b-4671-b739-9a10cf67b320");
		put("125", "5efbd2f3-c81a-4355-9787-a20cc1af1ef4");
		put("126", "5c8c19b6-9465-41dc-8979-ca76aa94a60c");
		put("127", "ca3d8a76-016f-4aad-a955-9cb49156467c");
		put("128", "b9eae632-bde4-4ab0-bc6c-76d023c90d25");
		put("129", "6d52c362-5380-4718-b0df-b58beef4b937");
		put("130", "852ae4c8-08e2-496f-a75c-fa8af8030a55");
		put("131", "1a052090-a892-4f74-ab27-d03c42a4e733");
		put("132", "39cf1358-5c98-41e9-9a28-0806a2314854");
		put("133", "aac298b6-4559-413a-9cdc-9de4439a7a4e");
		put("134", "e4ce0865-71ba-4187-9969-f93584361b73");
		put("135", "22a31d9c-2a6c-4734-80d1-46e1de19e23a");
		put("136", "f4f27470-6ac5-4494-a2fb-3ecc78c49445");
		put("137", "17f237a5-d5b7-4caf-9f2e-38b8f1a29aff");
		put("138", "1d79250f-e871-4ed5-9bbf-48d54e17d809");
		put("139", "ce10b618-e3d6-4d77-a1d2-20a89b728d92");
		put("140", "3ec88390-760e-4337-9fc5-982383582728");
		put("141", "7edd4386-92f4-4804-ae03-73897ede709d");
		put("142", "db5c9454-c05a-4542-94cb-9f5bd1c95f54");
		put("143", "2cf837d7-06be-4a59-9f87-2582f1143697");
		put("144", "803b5b4b-40ff-4da7-a79b-bbe9eaff9355");
		put("145", "afa5245e-c0ec-4dc1-aabb-f5dce6ea9a46");
		put("146", "ea223649-ebc4-402c-b9e2-0a625ca28014");
		put("147", "7dc2b067-8190-4ee4-adc5-aaa4f3d726b6");
		put("148", "56bc2208-b311-4e41-90e1-146cf1853bf9");
		put("149", "4efb420b-967c-46c9-853b-d3463be55d51");
		put("150", "5557a15e-da03-40fa-badd-904ec7d6243f");
		put("151", "86b25778-feb0-484f-afae-95d03f1efd49");
		put("152", "0103d57f-fd0e-402e-b263-ab9a62618502");
		put("153", "5ff81f24-5c43-4e2a-b91d-0b2467eaa224");
		put("154", "7a3581a9-53cd-474c-99d6-b02f0197cfe0");
		put("155", "90ede49b-8061-463c-8d4b-a813b1ca8237");
		put("156", "9e8c7906-ab07-4f0c-b525-8639900d2dd5");
		put("157", "67036b96-8aa8-4b3e-9e50-ed3215c6f0ad");
		put("158", "e01fce83-c023-4a5f-8706-111310acede2");
		put("159", "1976ff4e-4bbb-4dd2-b227-cc3945c8c3d6");
		put("160", "f84a16cc-0045-4e16-b06d-a4299852239b");
		put("161", "dfe73142-4b6f-4b11-a86e-ebdcdd319913");
		put("162", "c06196ed-1050-4dcf-8957-5e0ce1b7f3c9");
		put("163", "3cde7a25-fabd-43a3-87ae-502b88edbf42");
		put("164", "758684d6-18ed-4337-8391-a464cb348457");
		put("165", "cd97bab6-2b3c-44df-9f31-c2860c6261e0");
		put("166", "97a0f605-bc3a-4f04-8616-dbe1e8882239");
		put("167", "f8ac1ae0-509e-4c6e-9c63-637f16bae9b1");
		put("168", "a05b4844-75d9-49b6-8b43-2fd55c666ed4");
		put("169", "97d6a70e-75e6-4fc9-9450-12a0843c0465");
		put("170", "ed7badc6-fd98-44d0-a89c-4d1ab63131e3");
		put("171", "04da1516-ece5-4537-a281-d95f8b4376fb");
		put("172", "5a46e238-d927-4413-a0d1-636e2604b73c");
		put("173", "fa2aa000-6114-4fff-b471-ff59856e7470");
		put("174", "6e19a554-13b4-409f-a509-b54748558c1d");
		put("175", "d53c7c78-332f-4b13-93e7-be6200ffed1c");
		put("176", "8634ba7d-c1a7-41c8-baf6-d033e1214e63");
		put("177", "c2489fb0-bf15-401b-80c4-8f6b81c1033e");
		put("178", "04aeccb0-2517-477c-a15a-bc18cc6a0e1f");
		put("179", "fc3f5434-3a6a-4a1f-b76b-c9fbf3291d5d");
		put("180", "63f023e9-b6ae-4c5a-9dec-bbeaef087ce5");
		put("181", "15c89973-6771-49eb-a78e-ba08587f3cab");
		put("182", "b1f0ca88-4e74-4316-83e2-0e22092f03e7");
		put("183", "80b638ab-f041-4503-8b35-f7c2293eb216");
		put("184", "6b8a53ac-ff56-4b9f-b0bb-1d2c8f9f134d");
		put("185", "c2c4b472-b5eb-43a9-9406-84d3243e2128");
		put("186", "25437e8c-21ee-44bb-a108-edeac1df1f37");
		put("187", "7cb294e3-9b45-450e-8145-e2d8b87d2920");
		put("188", "121a753c-2461-4915-87a2-38afffcf5a94");
		put("189", "51252f16-eb44-483e-82e4-612679c95533");
		put("190", "624e7a70-9703-42fd-980c-cbc9562f8921");
		put("191", "4562cb40-9cfa-4280-b06d-2bc66963b569");
		put("192", "ecf5e607-6682-412d-b278-544a46d66d86");
		put("193", "1f8f75b9-92b0-4c5b-80b5-e2616a9faef0");
		put("194", "a291ad2a-73b9-4520-94aa-f1c4f68a0938");
		put("195", "620dbffd-cf03-43c0-8065-0d38ed5d4c86");
		put("196", "95b949d3-bec5-4633-a9c1-5097479c6f36");
		put("197", "55ac4f4e-6a8a-479b-b1da-9cd4b4e8d487");
		put("198", "d7b17900-ec64-41ef-bcf4-4417018de753");
		put("199", "3ad21460-2f35-4ad5-bed6-80a15b7f084d");
		put("200", "43abaf44-65ce-404f-9429-9825ba9aafb0");
		put("201", "846031d8-5a27-4793-a81a-bb5b4066baf4");
		put("202", "abd8b199-0fde-48ff-9c4f-b699beeb1d8f");
		put("203", "4cc64eb1-92cf-4354-a049-8b6d7f484ca6");
		put("204", "190751e3-f1db-4de3-91c7-e4f58e503862");
		put("205", "f02551f4-d0ac-4d87-89f4-532ff2b9520f");
		put("206", "e930f980-e122-4977-b4b2-f20c92a2a421");
		put("207", "ac9ce0c3-e3f1-43e6-b7bb-3ab2a9eae7f6");
		put("208", "e56bf833-2b70-445b-a4ed-4b239028addb");
		put("209", "4c844c19-2879-453b-98c9-4d8bad9157c4");
		put("210", "e04c93c6-0d3b-4efd-880f-7df136952425");
		put("211", "12b6225f-f9ea-46a5-958e-db5498126e17");
		put("212", "8ff63ed9-0152-483e-9cd7-cee6c8d36449");
		put("213", "192d80e7-9924-4b3d-8f8a-2060ad4bd8cf");
		put("214", "f068976d-db34-466e-983a-f668c33c4db6");
		put("215", "f0d72cba-9755-4ec8-a4d6-b6c30faf21f9");
		put("216", "0e42f949-c2e0-457b-8b8f-04c9b0137cd6");
		put("217", "e71e74d7-7503-4c1d-addb-e3ce8b087e89");
		put("218", "cadd1a31-ce4b-4911-97b8-74919d0aa388");
		put("219", "8478a044-529d-4f41-aaba-676fdfe8df2f");
		put("220", "3798e96c-a498-4365-8436-763cbce4d079");
		put("221", "5b03571f-542b-415d-8793-7f9dfb991491");
		put("222", "554440f2-b529-4419-9b70-4e2b558c8b21");
		put("223", "b0443409-4da7-46be-ae86-8a81c8d3007e");
		put("224", "5964fad7-67a0-4281-8143-f242a1baf512");
		put("225", "a00ee274-bcd5-4e65-bb6b-afce02fce125");
		put("226", "68ffcb53-9107-4530-bf79-a9a87bdb4426");
		put("227", "de6203ef-709d-4eeb-a714-25e30e0c3487");
		put("228", "3aa98e1e-a884-4496-a70f-07957c5794f3");
		put("229", "41545243-bb31-49da-8cfb-daf89de04396");
		put("230", "52fa9e7a-ef33-499f-98ea-81a540287e19");
		put("231", "64a26542-9d27-4fa4-8c9d-fecca36b723b");
		put("232", "2eb71e0b-7b11-4cc0-926e-a508fdb233a6");
		put("233", "c9d73292-d89b-43a0-b661-1b4144537cbf");
		put("234", "7d7daff4-a52a-40d4-9c91-e1e76c79bcce");
		put("235", "796e3c87-c0d8-47a1-a4b9-b2fe3c8cc3e2");
		put("236", "f6ae3d2c-b581-4455-a3dd-8b441e11d2ac");
		put("237", "f22bd854-12c4-4df9-8e7e-68460e469ec1");
		put("238", "651fbcbb-a910-4442-ab02-0894b80a237c");
		put("239", "f5e74cf5-4258-4b5c-a71c-d997c3ac8ba3");
		put("240", "c6fc92a3-8df9-4d27-8189-24f8dc65593a");
		put("241", "2796fe77-abc2-4446-966d-8c2678feab6b");
		put("242", "20f0c08e-ef29-4bd8-a76f-dbb2c0ba104c");
		put("243", "1056bb83-6b61-479b-93de-d1ed38fa1d1b");
		put("244", "0277d7f1-d42c-4050-b0b5-4947d8eb7d8e");
		put("245", "015442ea-5a76-40d2-9870-2cdc8f56e93e");
		put("246", "89e080cb-1fe3-453a-97fb-f6b1c6d1f25f");
		put("247", "f09877d0-ae60-4fbf-93df-709c8e6b7c6f");
		put("248", "efa72e47-84e9-4400-9d85-9f8167010354");
		put("249", "27df4026-441a-4f2e-ae37-3d56a90c4e59");
		put("250", "a52b529a-6dbf-4f82-8950-2b3bfd51ed8b");
	}};

	TripJsonHandler(Persistence persistence)
	{
		super(Trip.class, persistence);
	}

	@Override
	public JsonElement serialize(Trip trip, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", trip.getId());
//		json.addProperty("id", TRIP_UUIDS.get(trip.getId()));
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
