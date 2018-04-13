package se.martinuhlen.fishbase.dao;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

class JsonDao implements FishBaseDao
{
	private final JsonHandler<Specie> specieHandler;
	private final JsonHandler<Specimen> specimenHandler;
	private final JsonHandler<Trip> tripHandler;

	private final Map<String, Specie> species;
	private final Map<String, Trip> trips;

	JsonDao(Persistence persistence)
	{
		specieHandler = new SpecieJsonHandler(persistence);
		specimenHandler = new SpecimenJsonHandler(persistence);
		tripHandler = new TripJsonHandler(persistence);

	    JsonHandler<Specie>.Reader specieReader = specieHandler.reader();
	    JsonHandler<Specimen>.Reader specimenReader = specimenHandler.reader();
	    JsonHandler<Trip>.Reader tripReader = tripHandler.reader();

        species = specieReader.read()
                .stream()
                .collect(toMap(Specie::getId, identity()));

        Map<String, Set<Specimen>> tripSpecimens = specimenReader.read()
                .stream()
                .map(s -> s.withSpecie(species.get(s.getSpecie().getId())))
                .collect(groupingBy(Specimen::getTripId, toSet()));

        this.trips = tripReader.read()
            .stream()
            .map(trip -> trip.withSpecimens(tripSpecimens.getOrDefault(trip.getId(), emptySet())))
            .collect(toMap(Trip::getId, identity()));
	}

	@VisibleForTesting
	JsonDao(Persistence persistence, Collection<Specie> testSpecies, Collection<Trip> testTrips)
    {
	    this(persistence);
	    this.species.clear();
	    this.species.putAll(testSpecies.stream().collect(toMap(Specie::getId, identity())));
	    this.trips.clear();
	    this.trips.putAll(testTrips.stream().collect(toMap(Trip::getId, identity())));
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
		specimenHandler.write(streamSpecimens()
				.sorted(comparing(Specimen::getInstant).thenComparing(Specimen::getId))
				.collect(toList()));
	}

	private void writeTrips()
	{
		tripHandler.write(streamTrips()
				.sorted(comparing(Trip::getStartDate).thenComparing(Trip::getDescription))
				.collect(toList()));
	}

	private Stream<Specimen> streamSpecimens()
	{
	    return streamTrips()
	            .flatMap(trip -> trip.getSpecimens().stream());
	}

	private Stream<Trip> streamTrips()
	{
	    return trips.values().stream();
	}

    private Stream<Specie> streamSpecies()
    {
        return species.values().stream();
    }

	@Override
	public List<Specie> getSpecies()
	{
		return streamSpecies()
		        .sorted(comparing(Specie::getName, CASE_INSENSITIVE_ORDER))
				.collect(toList());
	}

	@Override
	public void saveSpecies(Collection<Specie> species)
	{
	    if (!species.isEmpty())
	    {
    	    Map<String, Specie> specieMap = species.stream().collect(toMap(Specie::getId, identity()));
    	    this.species.putAll(specieMap);
            writeSpecies();
            species.forEach(Specie::markPersisted);

    	    Set<Specimen> newSpecimens = streamSpecimens()
        	        .filter(specimen -> specieMap.containsKey(specimen.getSpecie().getId()))
        	        .map(specimen -> specimen.withSpecie(specieMap.get(specimen.getSpecie().getId())))
        	        .collect(toSet());
    	    saveSpecimens(newSpecimens);
	    }
	}

	@Override
	public boolean isSpecieDeletable(Specie specie)
	{
	    return !streamSpecimens()
	            .anyMatch(s -> s.getSpecie().equalsId(specie));
	}

	@Override
	public void deleteSpecies(Collection<Specie> species)
	{
		species.forEach(s ->
		{
			if (!isSpecieDeletable(s))
			{
				throw new IllegalArgumentException("Specie is not deletable: " + s);
			}
		});

		species.forEach(s -> this.species.remove(s.getId()));
	}

	@Override
	public List<Specimen> getSpecimens()
	{
	    return streamSpecimens()
	            .sorted(comparing((Specimen s) -> s.getSpecie().getName())
	                    .thenComparing(comparing(Specimen::getWeight).reversed()))
	            .collect(toList());
	}

	@Override
	public void saveSpecimens(Collection<Specimen> specimens)
	{
	    if (!specimens.isEmpty())
	    {
    	    Map<String, Specimen> specimenMap = specimens.stream().collect(toMap(Specimen::getId, identity()));
    	    Map<String, Set<Specimen>> tripSpecimens = specimens.stream()
    	            .collect(groupingBy(Specimen::getTripId, toSet()));

            trips.putAll(tripSpecimens.entrySet().stream()
                    .map(e ->
                    {
                        Trip trip = getTrip(e.getKey());
                        return trip.withSpecimens(new ArrayList<>(trip.getSpecimens().stream().map(specimen -> specimenMap.getOrDefault(specimen.getId(), specimen)).collect(toList())));
                    })
                    .collect(toMap(Trip::getId, trip -> trip)));

            writeSpecimens();
            specimens.forEach(Specimen::markPersisted);
	    }
	}

	@Override
	public void deleteSpecimens(Collection<Specimen> specimens)
	{
	    if (!specimens.isEmpty())
        {
            Map<String, Set<Specimen>> tripSpecimens = specimens.stream()
                    .collect(groupingBy(Specimen::getTripId, toSet()));

            trips.putAll(tripSpecimens.entrySet().stream()
                    .map(e ->
                    {
                        Trip trip = getTrip(e.getKey());
                        List<Specimen> newSpecimens = new ArrayList<>(trip.getSpecimens());
                        newSpecimens.removeAll(tripSpecimens.get(trip.getId()));
                        return trip.withSpecimens(newSpecimens);
                    })
                    .collect(toMap(Trip::getId, trip -> trip)));

            writeSpecimens();
        }
	}

	@Override
	public List<Trip> getTrips()
	{
		return streamTrips()
				.sorted(comparing(Trip::getStartDate).reversed().thenComparing(Trip::getDescription))
				.collect(toList());
	}

	@Override
	public Trip getTrip(String id)
	{
	    Trip trip = trips.get(id);
	    if (trip == null)
	    {
	        throw new IllegalArgumentException("There's no trip with id="+id);
	    }
	    return trip;
	}

	@Override
	public void saveTrip(Trip trip)
	{
		boolean tripChanged = isTripChanged(trip);
		boolean specimensChanged = isSpecimensChanged(trip);
		trips.put(trip.getId(), trip);

		if (tripChanged)
		{
			writeTrips();
			trip.markPersisted();
		}
		if (specimensChanged)
		{
			writeSpecimens();
			trip.getSpecimens().forEach(Specimen::markPersisted);
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
	public void deleteTrip(Trip tripToDelete)
	{
	    String id = tripToDelete.getId();
        if (trips.containsKey(id))
	    {
	        Trip trip = trips.remove(id);
	        writeTrips();
	        if (trip.hasSpecimens())
	        {
	            writeSpecimens();
	        }
	    }
	}
}
