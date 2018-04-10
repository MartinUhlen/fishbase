package se.martinuhlen.fishbase.dao;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyZeroInteractions;
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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

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
			dao = new JsonDao(persistence,
					asList(bream(), tench(), perch()),
					asList(bream5120(), perch1000(), tench3540()),
					asList(trip1(), trip2()));
			((JsonDao) dao).writeAll();
		}
		else
		{
		    dao = FishBaseDao.create(persistence);
		}
	}

	@Test
	public void test_get_all_species()
	{
		assertEquals(asList(bream(), perch(), tench()), dao.getSpecies());
	}

	@Test
	public void test_get_all_specimens()
	{
		assertEquals(asList(bream5120(), perch1000(), tench3540()), dao.getSpecimens());
	}

	@Test
	public void test_get_all_trips()
	{
		assertEquals(asList(trip2(), trip1()), dao.getTrips());
	}

	@Test
	public void test_save_existing_specie()
	{
		Specie bream = bream().setRegWeight(4900);

		dao.saveSpecie(bream);

		assertSpecieEquals(bream);
		assertSpeciesEquals(asList(bream, perch(), tench()));
	}

	@Test
	public void test_save_new_specie()
	{
		Specie newSpecie = newSpecie().setName("Qqq");

		dao.saveSpecie(newSpecie);

		assertTrue(newSpecie.isPersisted());
		assertSpecieEquals(newSpecie);
		assertSpeciesEquals(asList(bream(), perch(), newSpecie, tench()));
	}

	@Test
	public void test_save_several_species()
	{
		Specie bream = bream().setName("Braxen");
		Specie tench = tench().setName("Sutare").setFreshWater(false);
		List<Specie> species = asList(bream, tench);

		dao.saveSpecies(species);

		species.forEach(s -> assertSpecieEquals(s));
	}

	@Test
	public void test_save_existing_specimen()
	{
		Specimen specimen = perch1000().setText("Test");

		dao.saveSpecimen(specimen);

		assertSpecimenEquals(specimen);
	}

	@Test
	public void test_save_several_specimens()
	{
		Specimen perch = perch1000().setText("A nice perch");
		Specimen tench = tench3540().setText("A fat tench");
		List<Specimen> specimens = asList(perch, tench);

		dao.saveSpecimens(specimens);

		specimens.forEach(s -> assertSpecimenEquals(s));
	}

    @Test
    public void test_save_no_specimens_are_not_persisted()
    {
        reset(persistence);
        dao.saveSpecimens(emptyList());
        verifyZeroInteractions(persistence);
    }

	@Test
	public void test_is_specie_deletable()
	{
		assertFalse(dao.isSpecieDeletable(bream()));
		assertTrue(dao.isSpecieDeletable(newSpecie()));
	}

	@Test
	public void test_delete_specie()
	{
		Specie specie = newSpecie();
		dao.saveSpecie(specie);

		dao.deleteSpecies(asList(specie));

		assertFalse(dao.getSpecies().contains(specie));
	}

	@Test
	public void test_delete_specie_when_disallowed()
	{
		assertThrows(IllegalArgumentException.class, () -> dao.deleteSpecies(asList(tench())));
	}

	@Test
	public void test_delete_specimen()
	{
		Specimen specimen = perch1000();

		dao.deleteSpecimens(asList(specimen));

		assertFalse(dao.getSpecimens().contains(specimen));
		createDao();
		assertFalse(dao.getSpecimens().contains(specimen));
	}

    @Test
    public void test_delete_no_specimens_are_not_persisted()
    {
        reset(persistence);
        dao.deleteSpecimens(emptyList());
        verifyZeroInteractions(persistence);
    }

	@Test
	public void save_existing_trip()
	{
		Trip trip1 = trip1().setDescription("A new description");

		dao.saveTrip(trip1);

		assertTripEquals(trip1);
		assertTripsEquals(asList(trip2(), trip1));
	}

	@Test
	public void save_new_trip()
	{
		Trip newTrip = newTrip();

		dao.saveTrip(newTrip);

		assertTrue(newTrip.isPersisted());
		assertTripsEquals(asList(newTrip, trip2(), trip1()));
	}

	@Test
	public void test_save_new_trip_with_specimen()
	{
		Trip newTrip = newTrip();
		newTrip.setSpecimens(asList(newSpecimen(newTrip.getId())));

		dao.saveTrip(newTrip);
		assertTrue(newTrip.isPersisted());

		Specimen newSpecimen = newTrip.getSpecimens().get(0);
		assertTrue(newSpecimen.isPersisted());
		assertTripEquals(newTrip);
		assertTripsEquals(asList(newTrip, trip2(), trip1()));
		assertSpecimenEquals(newSpecimen);
	}

	@Test
	public void test_save_existing_trip_with_added_specimen()
	{
		Trip trip = trip2();
		Collection<Specimen> specimens = new ArrayList<>(trip.getSpecimens());
		specimens.add(newSpecimen(trip.getId()));
		trip.setSpecimens(specimens);

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
	public void test_save_existing_trip_with_removed_specimens()
	{
		List<Specimen> removedSpecimens = trip2().getSpecimens();
		Trip trip = trip2().setSpecimens(emptySet());

		dao.saveTrip(trip);

		assertEquals(emptyList(), trip.getSpecimens());
		assertTripEquals(trip);
		removedSpecimens.forEach(s -> assertFalse(dao.getSpecimens().contains(s)));
	}

	@Test
	public void test_save_existing_trip_with_edited_specimen()
	{
		Trip trip = trip2();
		trip.getSpecimens().forEach(s -> s.setText("" + s.hashCode()));
		List<Specimen> editedSpecimens = trip.copy().getSpecimens();

		dao.saveTrip(trip);

		assertTripEquals(trip);
		editedSpecimens.forEach(s -> assertSpecimenEquals(s));
		assertTrue(dao.getSpecimens().containsAll(editedSpecimens));
	}

	@Test
	public void test_save_specie_and_compare_specimen()
	{
		Specie specie = bream().setName("Braxen");

		dao.saveSpecie(specie);

		assertEquals(specie, dao.getSpecimen(bream5120().getId()).getSpecie());
	}

	@Test
	public void test_delete_trip()
	{
		dao.deleteTrip(trip2());
		assertTripsEquals(asList(trip1()));
		assertSpecimensEquals(asList(bream5120()));
	}

	@Test
	public void test_delete_unpersisted_trip_is_no_op()
	{
		List<Trip> trips = dao.getTrips();
		List<Specimen> specimens = dao.getSpecimens();

		dao.deleteTrip(Trip.asNew());

		assertTripsEquals(trips);
		assertSpecimensEquals(specimens);
	}

	private void assertSpecieEquals(Specie expected)
	{
		assertEquals(expected, dao.getSpecie(expected.getId()));
		createDao();
		assertEquals(expected, dao.getSpecie(expected.getId()));
	}

	private void assertSpeciesEquals(List<Specie> expected)
	{
		assertEquals(expected, dao.getSpecies());
		createDao();
		assertEquals(expected, dao.getSpecies());
	}

	private void assertSpecimenEquals(Specimen expected)
	{
		assertEquals(expected, dao.getSpecimen(expected.getId()));
		createDao();
		assertEquals(expected, dao.getSpecimen(expected.getId()));
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
