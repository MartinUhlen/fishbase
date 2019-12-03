package se.martinuhlen.fishbase.dao;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.BAIT;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.LOCATION;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.METHOD;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.WEATHER;
import static se.martinuhlen.fishbase.domain.TestData.bream;
import static se.martinuhlen.fishbase.domain.TestData.bream5120;
import static se.martinuhlen.fishbase.domain.TestData.newSpecie;
import static se.martinuhlen.fishbase.domain.TestData.newSpecimen;
import static se.martinuhlen.fishbase.domain.TestData.newTrip;
import static se.martinuhlen.fishbase.domain.TestData.perch;
import static se.martinuhlen.fishbase.domain.TestData.perch1000;
import static se.martinuhlen.fishbase.domain.TestData.tench;
import static se.martinuhlen.fishbase.domain.TestData.tench3540;
import static se.martinuhlen.fishbase.domain.TestData.trip1;
import static se.martinuhlen.fishbase.domain.TestData.trip2;
import static se.martinuhlen.fishbase.domain.TestData.trip3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

/**
 * Unit tests of {@link JsonDao}.
 */
public class JsonDaoTest
{
	private File dataDir;
	private FishBaseDao dao;
	private Persistence persistence;

	@BeforeEach
	public void setUp() throws Exception
	{
		dataDir = Files.createTempDirectory(getClass().getSimpleName()).toFile();
		createDao();
	}

	private void createDao()
	{
		persistence = Mockito.spy(new LocalFilePersistence(dataDir));
		if (dao == null)
		{
		    JsonDao dao = new JsonDao(persistence,
		            Set.of(bream(), tench(), perch()),
		            Set.of(trip1(), trip2(), trip3()));
		    dao.writeAll();
		    this.dao = dao;
		}
		else
		{
		    dao = FishBaseDao.create(persistence);
		}
	}
	
	@Test
	public void getAllSpecies()
	{
		assertEquals(asList(bream(), perch(), tench()), dao.getSpecies());
	}

	@Test
	public void getAllSpecimens()
	{
		assertEquals(asList(bream5120(), perch1000(), tench3540()), dao.getSpecimens());
	}

	@Test
	public void getAllTrips()
	{
		assertEquals(asList(trip3(), trip2(), trip1()), dao.getTrips());
	}

	@Test
	public void getTrip()
	{
	    assertEquals(trip2(), dao.getTrip(trip2().getId()));
	}

    @Test
    public void gettingUnknownSpecieFails()
    {
        assertThrows(IllegalArgumentException.class, () -> dao.getSpecie("?"));
    }

    @Test
    public void gettingUnknownSpecimenFails()
    {
        assertThrows(IllegalArgumentException.class, () -> dao.getSpecimen("?"));
    }

    @Test
    public void gettingUnknownTripFails()
    {
        assertThrows(IllegalArgumentException.class, () -> dao.getTrip("?"));
    }

	@Test
	public void saveNewSpecie()
	{
		Specie newSpecie = newSpecie().withName("Qqq");

		dao.saveSpecies(asList(newSpecie));

		assertTrue(newSpecie.isPersisted());
		assertSpecieEquals(newSpecie);
		assertSpeciesEquals(asList(bream(), perch(), newSpecie, tench()));
	}

	@Test
	public void saveExistingSpecies() throws Exception
	{
		reset(persistence);
		Specie bream = bream().withName("Braxen");
		Specie tench = tench().withName("Sutare").withFreshWater(false);
		List<Specie> species = asList(bream, tench);

		dao.saveSpecies(species);

		verify(persistence, never()).output("Specimen.json");
		species.forEach(s -> assertSpecieEquals(s));
		assertEquals(bream, getSpecimen(bream5120().getId()).getSpecie());
		assertEquals(tench, getSpecimen(tench3540().getId()).getSpecie());
	}

	@Test
	public void saveNoSpeciesAreNotPersisted()
	{
        reset(persistence);
        dao.saveSpecies(emptyList());
        verifyZeroInteractions(persistence);
	}

	@Test
	public void saveExistingSpecimens() throws Exception
	{
		reset(persistence);
		Specimen perch = perch1000().withText("A nice perch");
		Specimen tench = tench3540().withText("A fat tench");
		List<Specimen> specimens = asList(perch, tench);

		dao.saveSpecimens(specimens);

		verify(persistence, never()).output("Trip.json");
		specimens.forEach(s -> assertSpecimenEquals(s));
		assertTripEquals(trip2().withSpecimens(asList(perch, tench)));
	}

	@Test
	public void saveNewSpecimen()
	{
		Trip trip = trip2();
		Specimen newSpecimen = newSpecimen(trip.getId());
		Collection<Specimen> specimens = new ArrayList<>(trip.getSpecimens());
		specimens.add(newSpecimen);
		trip = trip.withSpecimens(specimens);

		dao.saveSpecimens(asList(newSpecimen));

		assertTrue(newSpecimen.isPersisted());
		specimens.forEach(s -> assertSpecimenEquals(s));
		assertTripEquals(trip);
	}

    @Test
    public void saveNoSpecimensAreNotPersisted()
    {
        reset(persistence);
        dao.saveSpecimens(emptyList());
        verifyZeroInteractions(persistence);
    }

    @Test
    public void saveOrphanSpecimenFails()
    {
    	assertThrows(IllegalArgumentException.class, () -> dao.saveSpecimens(asList(newSpecimen("UnknownTrip"))));
    }

	@Test
	public void isSpecieDeletable()
	{
		assertFalse(dao.isSpecieDeletable(bream()));
		assertTrue(dao.isSpecieDeletable(newSpecie()));
	}

	@Test
	public void deleteSpecie()
	{
		Specie specie = newSpecie();
		dao.saveSpecies(asList(specie));

		dao.deleteSpecies(asList(specie));

		assertFalse(dao.getSpecies().contains(specie));
	}

	@Test
	public void deleteSpecieWhenDisallowedFails()
	{
		assertThrows(IllegalArgumentException.class, () -> dao.deleteSpecies(asList(tench())));
	}

	@Test
	public void deleteSpecimen()
	{
		Specimen specimen = perch1000();

		dao.deleteSpecimens(asList(specimen));

		assertFalse(dao.getSpecimens().contains(specimen));
		assertFalse(dao.getTrip(trip2().getId()).getSpecimens().contains(specimen));
		createDao();
		assertFalse(dao.getSpecimens().contains(specimen));
		assertFalse(dao.getTrip(trip2().getId()).getSpecimens().contains(specimen));
	}

    @Test
    public void deleteNoSpecimensAreNotPersisted()
    {
        reset(persistence);
        dao.deleteSpecimens(emptyList());
        verifyZeroInteractions(persistence);
    }

	@Test
	public void saveExistingTrip()
	{
		Trip trip1 = trip1().withDescription("A new description");

		dao.saveTrip(trip1);

		assertTripEquals(trip1);
		assertTripsEquals(asList(trip3(), trip2(), trip1));
	}

	@Test
	public void saveNewTrip()
	{
		Trip newTrip = newTrip();

		dao.saveTrip(newTrip);

		assertTrue(newTrip.isPersisted());
		assertTripsEquals(asList(newTrip, trip3(), trip2(), trip1()));
	}

    @Test
    public void saveNewTripWithoutSpecimensDoesNotPersistSpecimens() throws IOException
    {
        reset(persistence);

        dao.saveTrip(newTrip());

        verify(persistence, times(1)).output("Trip.json");
        verifyNoMoreInteractions(persistence);
    }

	@Test
	public void saveNewTripWithSpecimen()
	{
		Trip newTrip = newTrip();
		newTrip = newTrip.withSpecimens(asList(newSpecimen(newTrip.getId())));

		dao.saveTrip(newTrip);
		assertTrue(newTrip.isPersisted());

		Specimen newSpecimen = newTrip.getSpecimens().get(0);
		assertTrue(newSpecimen.isPersisted());
		assertTripEquals(newTrip);
		assertTripsEquals(asList(newTrip, trip3(), trip2(), trip1()));
		assertSpecimenEquals(newSpecimen);
	}

	@Test
	public void saveExistingTripWithAddedSpecimen()
	{
		Trip trip = trip2();
		Collection<Specimen> specimens = new ArrayList<>(trip.getSpecimens());
		specimens.add(newSpecimen(trip.getId()));
		trip = trip.withSpecimens(specimens);

		dao.saveTrip(trip);

		assertTrue(trip.isPersisted());
		assertTripEquals(trip);
		assertEquals(specimens.size(), trip.getSpecimens().size());
		trip.getSpecimens().forEach(s ->
		{
			assertTrue(s.isPersisted());
			assertSpecimenEquals(s);
		});
	}

	@Test
	public void saveExistingTripWithRemovedSpecimens()
	{
		List<Specimen> removedSpecimens = trip2().getSpecimens();
		Trip trip = trip2().withSpecimens(emptySet());

		dao.saveTrip(trip);

		assertEquals(emptyList(), dao.getTrip(trip.getId()).getSpecimens());
		assertTripEquals(trip);
		removedSpecimens.forEach(s -> assertFalse(dao.getSpecimens().contains(s)));
	}

	@Test
	public void saveExistingTripWithEditedSpecimen()
	{
		Trip trip = trip2();
		List<Specimen> editedSpecimens = trip.getSpecimens()
		        .stream()
		        .map(s -> s.withText("" + s.hashCode()))
		        .collect(toList());
		trip = trip.withSpecimens(editedSpecimens);

		dao.saveTrip(trip);

		assertTripEquals(trip);
		editedSpecimens.forEach(s -> assertSpecimenEquals(s));
		assertTrue(dao.getSpecimens().containsAll(editedSpecimens));
	}

    @Test
    public void saveSpeciesAndCompareSpecimens()
    {
        Specie bream = bream().withName("Braxen");
        Specie perch = perch().withName("Abborre");
        Specie tench = tench();

        dao.saveSpecies(asList(bream, perch));

        Specimen breamSpecimen = getSpecimen(bream5120().getId());
        assertEquals(bream, breamSpecimen.getSpecie(), "Expect specimen contains updated specie");
        assertTrue(dao.getTrip(trip1().getId()).getSpecimens().contains(breamSpecimen), "Expect trip contains specimen with updated specie");

        Specimen perchSpecimen = getSpecimen(perch1000().getId());
        assertEquals(perch, perchSpecimen.getSpecie(), "Expect specimen contains updated specie");
        assertTrue(dao.getTrip(trip2().getId()).getSpecimens().contains(perchSpecimen), "Expect trip contains specimen with updated specie");

        Specimen tenchSpecimen = getSpecimen(tench3540().getId());
        assertEquals(tench, tenchSpecimen.getSpecie(), "Expect specimen contains *not* updated specie");
        assertTrue(dao.getTrip(trip2().getId()).getSpecimens().contains(tenchSpecimen), "Expect trip contains specimen with *not* updated specie");
    }

	@Test
	public void deleteTripWithSpecimens()
	{
		dao.deleteTrip(trip2());
		assertTripsEquals(asList(trip3(), trip1()));
		assertSpecimensEquals(asList(bream5120()));
	}

    @Test
    public void deleteTripWithoutSpecimens() throws Exception
    {
        reset(persistence);
        dao.deleteTrip(trip3());
        verify(persistence, times(1)).output("Trip.json");
        verifyNoMoreInteractions(persistence);
        assertTripsEquals(asList(trip2(), trip1()));
    }

	@Test
	public void deleteUnpersistedTripIsNoop()
	{
		List<Trip> trips = dao.getTrips();
		List<Specimen> specimens = dao.getSpecimens();

		dao.deleteTrip(Trip.asNew());

		assertTripsEquals(trips);
		assertSpecimensEquals(specimens);
	}

	@Test
	public void autoCompletions()
	{
	    dao.saveSpecimens(Set.of(bream5120()
	            .withLocation("AAA")
	            .withMethod("BBB")
	            .withBait("CCC")
	            .withWeather("DDD")));

	    assertTrue(dao.getAutoCompletions(LOCATION).contains("AAA"));
	    assertTrue(dao.getAutoCompletions(METHOD).contains("BBB"));
	    assertTrue(dao.getAutoCompletions(BAIT).contains("CCC"));
	    assertTrue(dao.getAutoCompletions(WEATHER).contains("DDD"));

        dao.saveSpecimens(Set.of(bream5120()
                .withLocation("EEE")
                .withMethod("FFF")
                .withBait("GGG")
                .withWeather("HHH")));

        assertTrue(dao.getAutoCompletions(LOCATION).contains("EEE"));
        assertTrue(dao.getAutoCompletions(METHOD).contains("FFF"));
        assertTrue(dao.getAutoCompletions(BAIT).contains("GGG"));
        assertTrue(dao.getAutoCompletions(WEATHER).contains("HHH"));
	}

	private Specie getSpecie(String id)
	{
	    return dao.getSpecies().stream().filter(s -> s.getId().equals(id)).findAny().orElseThrow(() -> new AssertionError("Specie with id=" + id + " not found"));
	}

    private Specimen getSpecimen(String id)
    {
    	Specimen byLookup = dao.getSpecimen(id);
        Specimen byStream = dao.getSpecimens().stream().filter(s -> s.getId().equals(id)).findAny().orElseThrow(() -> new AssertionError("Specimen with id=" + id + " not found"));
        assertEquals(byLookup, byStream, "Expect equal specimen found by lookup and stream");
        return byLookup;
    }

	private void assertSpecieEquals(Specie expected)
	{
		assertEquals(expected, getSpecie(expected.getId()));
		createDao();
		assertEquals(expected, getSpecie(expected.getId()));
	}

	private void assertSpeciesEquals(List<Specie> expected)
	{
		assertEquals(expected, dao.getSpecies());
		createDao();
		assertEquals(expected, dao.getSpecies());
	}

	private void assertSpecimenEquals(Specimen expected)
	{
		assertEquals(expected, getSpecimen(expected.getId()));
		createDao();
		assertEquals(expected, getSpecimen(expected.getId()));
	}

	private void assertSpecimensEquals(List<Specimen> expected)
	{
		assertEquals(expected, dao.getSpecimens());
		createDao();
		assertEquals(expected, dao.getSpecimens());
	}

	private void assertTripEquals(Trip expected)
	{
		assertEquals(expected, dao.getTrip(expected.getId()));
		createDao();
		assertEquals(expected, dao.getTrip(expected.getId()));
	}

	private void assertTripsEquals(List<Trip> expected)
	{
		assertEquals(expected, dao.getTrips());
		createDao();
		assertEquals(expected, dao.getTrips());
	}

	@AfterEach
	public void tearDown() throws Exception
	{
		FileUtils.deleteDirectory(dataDir);
	}
}
