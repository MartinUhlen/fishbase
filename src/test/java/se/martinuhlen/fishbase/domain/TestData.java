package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestData
{
	public static Specie bream()
	{
		return Specie.asPersisted("#bream")
				.name("Bream")
				.regWeight(4400)
				.freshWater(true);
	}

	public static Specie tench()
	{
		return Specie.asPersisted("#tench")
				.name("Tench")
				.regWeight(3200)
				.freshWater(true);
	}

	public static Specie perch()
	{
		return Specie.asPersisted("#perch")
				.name("Perch")
				.regWeight(1600)
				.freshWater(true);
	}

	public static Specie newSpecie()
	{
		return Specie.asNew()
				.withName("NewSpecie")
				.withRegWeight(1337)
				.withFreshWater(false);
	}

	public static Specimen bream5120()
	{
		return Specimen.asPersisted("#bream5120")
		        .tripId("#trip1")
				.specie(bream())
				.weight(5120)
				.length(69)
				.location("Hossmoån")
				.instant(parse("2014-09-20T14:20"))
				.method("Bottenmete")
				.bait("Majs")
				.weather("Soligt")
				.text("Nytt PB!");
	}

	public static Specimen perch1000()
	{
		return Specimen.asPersisted("#perch1000")
		        .tripId("#trip2")
				.specie(perch())
				.weight(1000)
				.length(0)
				.location("Öxneredssjön")
				.instant(parse("2015-07-04T06:15"))
				.method("Bottenmete")
				.bait("Plastmajs")
				.weather("Soligt")
				.text("");
	}

	public static Specimen tench3540()
	{
		return Specimen.asPersisted("#tench3540")
		        .tripId("#trip2")
				.specie(tench())
				.weight(3540)
				.length(58)
				.location("Öxneredssjön")
				.instant(parse("2015-07-04T09:30"))
				.method("Bottenmete")
				.bait("Plastmajs")
				.weather("Soligt")
				.text("");
	}

	public static Specimen newSpecimen(String tripId)
	{
		return Specimen.asNew(tripId)
				.withSpecie(perch())
				.withWeight(1460)
				.withLength(45)
				.withLocation("Kungsbackaån")
				.withInstant(LocalDateTime.now())
				.withMethod("Bottenmete")
				.withBait("Mört")
				.withWeather("Mulet")
				.withText("");

//		public static final Specimen NEW_SPECIMEN = Specimen.asNew(1)
//				.withSpecie(PERCH)
//				.withWeight(1460)
//				.withLength(45)
//				.withLocation("Kungsbackaån")
//				.withInstant(LocalDateTime.now())
//				.withMethod("Bottenmete")
//				.withBait("Mört")
//				.withWeather("Mulet")
//				.withText("");
	}

	public static Trip trip1()
	{
		return Trip.asPersisted("#trip1")
				.description("Första besöket i Hossmoån")
				.startDate(LocalDate.parse("2014-09-20"))
				.endDate(LocalDate.parse("2014-09-20"))
				.text("Första besöket i Hossmoån gav tre reggor på Braxen och nytt PB!")
				.specimens(asList(bream5120()));
	}

	public static Trip trip2()
	{
		return Trip.asPersisted("#trip2")
				.description("Första besöket i Öxneredsjön")
				.startDate(LocalDate.parse("2015-07-03"))
				.endDate(LocalDate.parse("2015-07-05"))
				.text("Första besöket i Öxneredssjön gav regga och nytt PB på Sutare!")
				.specimens(asList(perch1000(), tench3540()));
	}

	public static Trip newTrip()
	{
		return Trip.asNew()
			.withDescription("The description")
			.withStartDate(LocalDate.now().minusDays(1))
			.withEndDate(LocalDate.now())
			.withText("The text");
	}
}
