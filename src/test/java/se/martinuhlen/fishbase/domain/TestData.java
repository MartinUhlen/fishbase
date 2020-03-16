package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
	}

	public static Trip trip1()
	{
		return Trip.asPersisted("#trip1")
				.description("Första besöket i Hossmoån")
				.startDate(LocalDate.parse("2014-09-20"))
				.endDate(LocalDate.parse("2014-09-20"))
				.text("Första besöket i Hossmoån gav tre reggor på Braxen och nytt PB!")
				.specimens(asList(bream5120()))
				.photos(List.of(photo1InTrip1(), photo2InTrip1()));
	}

	public static Trip trip2()
	{
		return Trip.asPersisted("#trip2")
				.description("Första besöket i Öxneredsjön")
				.startDate(LocalDate.parse("2015-07-03"))
				.endDate(LocalDate.parse("2015-07-05"))
				.text("Första besöket i Öxneredssjön gav regga och nytt PB på Sutare!")
				.specimens(asList(perch1000(), tench3540()))
				.photos(List.of(photo1InTrip2()));
	}

    public static Trip trip3()
    {
        return Trip.asPersisted("#trip3")
                .description("A trip without specimens")
                .startDate(LocalDate.parse("2018-04-17"))
                .endDate(LocalDate.parse("2018-04-17"))
                .text("...")
                .specimens(Set.of())
                .photos(List.of());
    }

	public static Trip newTrip()
	{
		return Trip.asNew()
			.withDescription("The description")
			.withStartDate(LocalDate.now().minusDays(1))
			.withEndDate(LocalDate.now())
			.withText("The text");
	}

	public static Photo newPhoto(String id, String tripId)
	{
		return newPhoto(id, tripId, LocalDateTime.now());
	}

	public static Photo newPhoto(String id, String tripId, LocalDateTime time)
	{
		return Photo.asNew(id)
				.tripId(tripId)
				.specimens(emptySet())
				.fileName("SomeFile.png")
				.time(time)
				.starred(false);
	}

	public static Photo photo1InTrip1()
	{
		return Photo.asPersisted("#photo1InTrip1")
				.tripId("#trip1")
				.specimens(Set.of())
				.fileName("SomePhoto.jpg")
				.time(LocalDateTime.parse("2014-09-20T08:37"))
				.starred(false);
	}

	public static Photo photo2InTrip1()
	{
		return Photo.asPersisted("#photo2InTrip1")
				.tripId("#trip1")
				.specimens(Set.of("#bream5120"))
				.fileName("BreamPhoto.jpg")
				.time(LocalDateTime.parse("2014-09-20T14:30"))
				.starred(true);
	}

	public static Photo photo1InTrip2()
	{
		return Photo.asPersisted("#photo1InTrip2")
				.tripId("#trip2")
				.specimens(Set.of("#tench3540"))
				.fileName("TenchPhoto.jpg")
				.time(parse("2015-07-04T09:38"))
				.starred(false);
	}	
}
