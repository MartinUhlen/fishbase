package se.martinuhlen.fishbase.dao;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static se.martinuhlen.fishbase.utils.Checked.run;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import se.martinuhlen.fishbase.domain.Domain;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

class JsonDao implements FishBaseDao
{
	private final JsonHandler<Specie> specieHandler;
	private final JsonHandler<Specimen> specimenHandler;
	private final JsonHandler<Trip> tripHandler;

	private Table<Specie> species;
	private Table<Specimen> specimens;
	private TripTable trips;

	private JsonDao(Persistence persistence, boolean read)
	{
		specieHandler = new SpecieJsonHandler(persistence);
		specimenHandler = new SpecimenJsonHandler(persistence);
		tripHandler = new TripJsonHandler(persistence);
		if (read)
		{
			readAll(() -> specieHandler.read(), () -> specimenHandler.read(), () -> tripHandler.read());
		}
	}

	JsonDao(Persistence persistence)
	{
		this(persistence, true);
	}

	@VisibleForTesting
	JsonDao(Persistence persistence, Collection<Specie> species, Collection<Specimen> specimens, Collection<Trip> trips)
	{
		this(persistence, false);
		readAll(() -> species, () -> specimens, () -> trips);
	}

	private void readAll(Supplier<Collection<Specie>> specieSupplier, Supplier<Collection<Specimen>> specimenSupplier, Supplier<Collection<Trip>> tripSupplier)
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(() -> species = new Table<>(Specie.class, specieSupplier.get()));
		executor.execute(() -> specimens = new SpecimenTable(specimenSupplier.get()));
		executor.execute(() -> trips = new TripTable(tripSupplier.get()));
		executor.shutdown();
		run(() -> executor.awaitTermination(1, MINUTES));
	}

	@VisibleForTesting
	void writeAll()
	{
		writeSpecies();
		writeSpecimens();
		writeTrips();
	}

	private void writeSpecies()
	{
		specieHandler.write(getSpecies());
	}

	private void writeSpecimens()
	{
		specimenHandler.write(specimens
				.stream()
				.sorted(comparing(Specimen::getInstant).thenComparing(Specimen::getId))
				.collect(toList()));
	}

	private void writeTrips()
	{
		tripHandler.write(trips
				.stream()
				.sorted(comparing(Trip::getStartDate).thenComparing(Trip::getId))
				.collect(toList()));
	}

	@Override
	public List<Specie> getSpecies()
	{
		return species
				.stream()
				.sorted(comparing(Specie::getName, CASE_INSENSITIVE_ORDER))
				.collect(toList());
	}

	@Override
	public Specie getSpecie(String id)
	{
		return species.get(id);
	}

	@Override
	public void saveSpecies(Collection<? extends Specie> species)
	{
		species.forEach(s -> saveSpecie(s, false));
		writeSpecies();
	}

	@Override
	public void saveSpecie(Specie specie)
	{
		saveSpecie(specie, true);
	}

	private void saveSpecie(Specie specie, boolean write)
	{
		if (specie.isPersisted())
		{
			updateSpecimens(getSpecie(specie.getId()), specie);
		}

		species.put(specie);

		if (write)
		{
			writeSpecies();
		}
	}

	private void updateSpecimens(Specie oldSpecie, Specie newSpecie)
	{
		specimens.putAll(specimens.stream()
				.filter(s -> s.getSpecie().equals(oldSpecie))
				.map(s -> s.setSpecie(newSpecie))
				.collect(toList()));
	}

	@Override
	public boolean isSpecieDeletable(Specie specie)
	{
		return !specimens.stream()
				.anyMatch(s -> s.getSpecie().equalsId(specie));
	}

	@Override
	public void deleteSpecies(Collection<? extends Specie> species)
	{
		species.forEach(s ->
		{
			if (!isSpecieDeletable(s))
			{
				throw new IllegalArgumentException("Specie is not deletable: " + s);
			}
		});

		species.forEach(s ->
		{
			this.species.remove(s.getId());
		});
	}

	@Override
	public Specimen getSpecimen(String id)
	{
		return specimens.get(id);
	}

	@Override
	public List<Specimen> getSpecimens()
	{
		return specimens.stream()
				.sorted(comparing((Specimen s) -> s.getSpecie().getName())
						.thenComparing(comparing(Specimen::getWeight).reversed()))
				.collect(toList());
	}

	@Override
	public void saveSpecimens(Collection<? extends Specimen> specimens)
	{
	    if (!specimens.isEmpty())
	    {
    		specimens.forEach(s -> saveSpecimen(s, false));
    		writeSpecimens();
	    }
	}

	@Override
	public void saveSpecimen(Specimen specimen)
	{
		saveSpecimen(specimen, true);
	}

	private void saveSpecimen(Specimen specimen, boolean write)
	{
		specimens.put(specimen);

		if (write)
		{
			writeSpecimens();
		}
	}

	@Override
	public void deleteSpecimens(Collection<? extends Specimen> specimens)
	{
	    if (!specimens.isEmpty())
	    {
	        specimens.forEach(s -> this.specimens.remove(s.getId()));
	        writeSpecimens();
	    }
	}

	@Override
	public List<Trip> getTrips()
	{
		return trips.stream()
				.sorted(comparing(Trip::getStartDate).reversed())
				.collect(toList());
	}

	@Override
	public Trip getTrip(String id)
	{
		return trips.get(id);
	}

	@Override
	public void saveTrip(Trip trip)
	{
		boolean tripChanged = isTripChanged(trip);
		boolean specimensChanged = isSpecimensChanged(trip);

		if (tripChanged)
		{
			trips.put(trip);
			writeTrips();
		}
		if (specimensChanged)
		{
			specimens.removeIf(s -> s.getTripId().equals(trip.getId()));
			specimens.putAll(trip.getSpecimens());
			writeSpecimens();
		}
	}

	private boolean isTripChanged(Trip trip)
	{
		return trip.isNew()
			|| !trips.get(trip.getId()).equalsWithoutSpecimens(trip);
	}

	private boolean isSpecimensChanged(Trip trip)
	{
		return trip.isNew()
			|| !trips.get(trip.getId()).getSpecimens().equals(trip.getSpecimens());
	}

	@Override
	public void deleteTrip(Trip trip)
	{
		if (trips.containsId(trip.getId()))
		{
			trips.remove(trip.getId());
			writeTrips();
		}

		if (!trip.getSpecimens().isEmpty())
		{
			trip.getSpecimens().forEach(specimen -> specimens.remove(specimen.getId()));
			writeSpecimens();
		}
	}

	private static class Table<D extends Domain<D>>
	{
		private final Map<String, D> map;
		private final Class<D> klass;

		Table(Class<D> klass, Collection<? extends D> objects)
		{
			this.klass = klass;
			map = objects
					.stream()
					.filter(e -> e != null)
					.collect(toMap(e -> e.getId(), e -> e));
		}

		Stream<D> stream()
		{
			return map.values()
					.stream()
					.map(this::copy);
		}

		D get(String id)
		{
			return Optional.ofNullable(map.get(id))
					.map(this::copy)
					.orElseThrow(() -> new IllegalArgumentException(klass.getSimpleName() + " not found for id=" + id));
		}

		D copy(D obj)
		{
			return obj.copy();
		}

		void put(D object)
		{
			object.markPersisted();
			map.put(object.getId(), object.copy());
		}

		void putAll(Iterable<? extends D> objects)
		{
			objects.forEach(o -> put(o));
		}

		void remove(String id)
		{
			map.remove(id);
		}

		boolean removeIf(Predicate<? super D> filter)
		{
			return map.values().removeIf(filter);
		}
	}

	private class SpecimenTable extends Table<Specimen>
	{
		SpecimenTable(Collection<? extends Specimen> specimens)
		{
			super(Specimen.class, specimens);
		}

		@Override
		Specimen copy(Specimen obj)
		{
			Specimen copy = super.copy(obj);
			copy.setSpecie(species.get(obj.getSpecie().getId()));
			return copy;
		}
	}

	private class TripTable extends Table<Trip>
	{
		private Map<String, Set<Specimen>> tripSpecimens;

		TripTable(Collection<? extends Trip> trips)
		{
			super(Trip.class, trips);
		}

		@Override
		Stream<Trip> stream()
		{
			groupSpecimens();
			return super.stream();
		}

		@Override
		Trip get(String id)
		{
			groupSpecimens();  // Really inefficient for one get, but only called from test
			return super.get(id);
		}

		private void groupSpecimens()
		{
			tripSpecimens = specimens.stream()
					.collect(groupingBy(Specimen::getTripId, toSet()));
		}

		@Override
		Trip copy(Trip trip)
		{
			Trip copy = super.copy(trip);
			copy.setSpecimens(tripSpecimens.getOrDefault(trip.getId(), emptySet()));
			return copy;
		}

		boolean containsId(String id)
		{
			return super.map.containsKey(id);
		}
	}
}
