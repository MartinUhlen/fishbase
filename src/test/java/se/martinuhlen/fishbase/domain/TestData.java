package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.LocalDateTime;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

public class TestData
{
	public static Specie bream()
	{
		return Specie.asPersisted("#bream")
				.setName("Bream")
				.setRegWeight(4400)
				.setFreshWater(true);
	}

	public static Specie tench()
	{
		return Specie.asPersisted("#tench")
				.setName("Tench")
				.setRegWeight(3200)
				.setFreshWater(true);
	}

	public static Specie perch()
	{
		return Specie.asPersisted("#perch")
				.setName("Perch")
				.setRegWeight(1600)
				.setFreshWater(true);
	}

	public static Specie newSpecie()
	{
		return Specie.asNew()
				.setName("NewSpecie")
				.setRegWeight(1337)
				.setFreshWater(false);
	}

	public static Specimen bream5120()
	{
		return Specimen.asPersisted("#bream5120", "#trip1")
				.setSpecie(bream())
				.setWeight(5120)
				.setLength(69)
				.setLocation("Hossmoån")
				.setInstant(parse("2014-09-20T14:20"))
				.setMethod("Bottenmete")
				.setBait("Majs")
				.setWeather("Soligt")
				.setText("Nytt PB!");
	}

	public static Specimen perch1000()
	{
		return Specimen.asPersisted("#perch1000", "#trip2")
				.setSpecie(perch())
				.setWeight(1000)
				.setLength(0)
				.setLocation("Öxneredssjön")
				.setInstant(parse("2015-07-04T06:15"))
				.setMethod("Bottenmete")
				.setBait("Plastmajs")
				.setWeather("Soligt")
				.setText("");
	}

	public static Specimen tench3540()
	{
		return Specimen.asPersisted("#tench3540", "#trip2")
				.setSpecie(tench())
				.setWeight(3540)
				.setLength(58)
				.setLocation("Öxneredssjön")
				.setInstant(parse("2015-07-04T09:30"))
				.setMethod("Bottenmete")
				.setBait("Plastmajs")
				.setWeather("Soligt")
				.setText("");
	}

	public static Specimen newSpecimen(String tripId)
	{
		return Specimen.asNew(tripId)
				.setSpecie(perch())
				.setWeight(1460)
				.setLength(45)
				.setLocation("Kungsbackaån")
				.setInstant(LocalDateTime.now())
				.setMethod("Bottenmete")
				.setBait("Mört")
				.setWeather("Mulet")
				.setText("");

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
				.setDescription("Första besöket i Hossmoån")
				.setStartDate(LocalDate.parse("2014-09-20"))
				.setEndDate(LocalDate.parse("2014-09-20"))
				.setText("Första besöket i Hossmoån gav tre reggor på Braxen och nytt PB!")
				.setSpecimens(asList(bream5120()));
	}

	public static Trip trip2()
	{
		return Trip.asPersisted("#trip2")
				.setDescription("Första besöket i Öxneredsjön")
				.setStartDate(LocalDate.parse("2015-07-03"))
				.setEndDate(LocalDate.parse("2015-07-05"))
				.setText("Första besöket i Öxneredssjön gav regga och nytt PB på Sutare!")
				.setSpecimens(asList(perch1000(), tench3540()));
	}

	public static Trip newTrip()
	{
		return Trip.asNew()
			.setDescription("The description")
			.setStartDate(LocalDate.now().minusDays(1))
			.setEndDate(LocalDate.now())
			.setText("The text");
	}
}
