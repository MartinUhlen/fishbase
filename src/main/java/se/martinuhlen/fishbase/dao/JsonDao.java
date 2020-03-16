package se.martinuhlen.fishbase.dao;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySortedSet;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static se.martinuhlen.fishbase.domain.Trip.EMPTY_TRIP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import se.martinuhlen.fishbase.domain.AutoCompleteField;
import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

class JsonDao implements FishBaseDao
{
	private final JsonHandler<Photo> photoHandler;
	private final JsonHandler<Specie> specieHandler;
	private final JsonHandler<Specimen> specimenHandler;
	private final JsonHandler<Trip> tripHandler;

	private final Map<String, Photo> photos;
	private final Map<String, Specie> species;
	private final Map<String, Specimen> specimens;
	private final Map<String, Trip> trips;
    private Map<AutoCompleteField, SortedSet<String>> autoCompleteMap;

	JsonDao(Persistence persistence)
	{
		photoHandler = new PhotoJsonHandler(persistence);
		specieHandler = new SpecieJsonHandler(persistence);
		specimenHandler = new SpecimenJsonHandler(persistence, this::getSpecie);
		tripHandler = new TripJsonHandler(persistence, this::getSpecimen, this::getPhoto);

		JsonHandler<Photo>.Reader photoReader = photoHandler.reader();
	    JsonHandler<Specie>.Reader specieReader = specieHandler.reader();
	    JsonHandler<Specimen>.Reader specimenReader = specimenHandler.reader();
	    JsonHandler<Trip>.Reader tripReader = tripHandler.reader();

        photos = photoReader.read()
        		.stream()
        		.collect(toMap(Photo::getId, identity()));

        species = specieReader.read()
                .stream()
                .collect(toMap(Specie::getId, identity()));

        specimens = specimenReader.read()
        		.stream()
        		.collect(toMap(Specimen::getId, identity()));

        trips = tripReader.read()
            .stream()
            .collect(toMap(Trip::getId, identity()));
	}

	@VisibleForTesting
	JsonDao(Persistence persistence, Collection<Specie> testSpecies, Collection<Trip> testTrips)
    {
	    this(persistence);
	    this.photos.clear();
	    this.photos.putAll(testTrips.stream().flatMap(t -> t.getPhotos().stream()).collect(toMap(Photo::getId, identity())));
	    this.species.clear();
	    this.species.putAll(testSpecies.stream().collect(toMap(Specie::getId, identity())));
	    this.specimens.clear();
	    this.specimens.putAll(testTrips.stream().flatMap(t -> t.getSpecimens().stream()).collect(toMap(Specimen::getId, identity())));
	    this.trips.clear();
	    this.trips.putAll(testTrips.stream().collect(toMap(Trip::getId, identity())));
    }

	@VisibleForTesting
	void writeAll()
	{
		writePhotos();
		writeSpecies();
		writeSpecimens();
		writeTrips();
	}

	private void writePhotos()
	{
		photoHandler.write(photos.values());
	}

	private void writeSpecies()
	{
		specieHandler.write(getSpecies());
	}

	private void writeSpecimens()
	{
	    autoCompleteMap = null;
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
		return specimens.values().stream();
	}

	private Stream<Trip> streamTrips()
	{
	    return trips.values().stream();
	}

    private Stream<Specie> streamSpecies()
    {
        return species.values().stream();
    }

    public Photo getPhoto(String id)
    {
    	Photo photo = photos.get(id);
    	if (photo == null)
    	{
    		throw new IllegalArgumentException("There's no Photo with id="+id);
    	}
    	return photo;
    }

    @Override
    public List<Photo> getPhotos()
    {
    	return photos.values().stream().collect(toList());
    }

    @Override
    public Specie getSpecie(String id)
    {
	    Specie specie = species.get(id);
	    if (specie == null)
	    {
	    	throw new IllegalArgumentException("There's no Specie with id="+id);
	    }
	    return specie;
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

    	    saveSpecimens(newSpecimens, false);
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
	public Specimen getSpecimen(String id)
	{
	    Specimen specimen = specimens.get(id);
	    if (specimen == null)
	    {
	        throw new IllegalArgumentException("There's no Specimen with id="+id);
	    }
	    return specimen;
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
    	    saveSpecimens(specimens, true);
	    }
	}

	private void saveSpecimens(Collection<Specimen> specimens, boolean writeSpecimens)
	{
		Map<String, Specimen> specimenMap = specimens.stream().collect(toMap(Specimen::getId, identity()));
		Map<String, Set<Specimen>> tripSpecimens = specimens.stream()
		        .collect(groupingBy(Specimen::getTripId, toSet()));

		Map<String, Trip> modifiedTrips = tripSpecimens.entrySet().stream()
		        .map(e ->
		        {
		            Trip trip = getTrip(e.getKey());
		            ArrayList<Specimen> newSpecimens = new ArrayList<>(trip.getSpecimens());
		            newSpecimens.removeIf(s -> specimenMap.containsKey(s.getId()));
		            newSpecimens.addAll(e.getValue());
		            return trip.withSpecimens(newSpecimens);
		        })
		        .collect(toMap(Trip::getId, trip -> trip));

		boolean writeTrips = modifiedTrips
				.entrySet()
				.stream()
				.anyMatch(e -> getSpecimenIds(getTrip(e.getKey())).size() != e.getValue().getSpecimens().size());

		trips.putAll(modifiedTrips);

		specimens.forEach(specimen -> this.specimens.put(specimen.getId(), specimen));

		if (writeTrips)
		{
			writeTrips();
		}

		if (writeSpecimens)
		{
			writeSpecimens();
		}

		specimens.forEach(Specimen::markPersisted);
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

            specimens.forEach(specimen -> this.specimens.remove(specimen.getId()));

            writeSpecimens();
            writeTrips();
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
	        throw new IllegalArgumentException("There's no Trip with id="+id);
	    }
	    return trip;
	}

	@Override
	public void saveTrip(Trip trip)
	{
		requireNonNull(trip, "trip cannot be null");
		checkPhotoIntegrity(trip);

		boolean tripChanged = isTripChanged(trip);
		boolean specimensChanged = isSpecimensChanged(trip);
		boolean photosChanged = isPhotosChanged(trip);

		ofNullable(trips.get(trip.getId()))
				.map(Trip::getSpecimens)
				.orElse(emptyList())
				.stream()
				.map(Specimen::getId)
				.forEach(specimens::remove);
		trip.getSpecimens().forEach(specimen -> specimens.put(specimen.getId(), specimen));

		ofNullable(trips.get(trip.getId()))
				.map(Trip::getPhotos)
				.orElse(emptyList())
				.stream()
				.map(Photo::getId)
				.forEach(photos::remove);
		trip.getPhotos().forEach(photo -> photos.put(photo.getId(), photo));

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
		if (photosChanged)
		{
			writePhotos();
			trip.getPhotos().forEach(Photo::markPersisted);
		}
	}

	private void checkPhotoIntegrity(Trip trip)
	{
		trip.getPhotos()
				.stream()
				.map(p -> photos.get(p.getId()))
				.filter(p -> p != null)
				.filter(p -> !p.getTripId().equals(trip.getId()))
				.map(p -> trips.get(p.getTripId()))
				.findAny()
				.ifPresent(t ->
				{
					throw new IllegalArgumentException("Photo is already contained in another trip: " + t.getLabel());
				});
	}

	private boolean isTripChanged(Trip trip)
	{
		return trip.isNew()
			|| !trips.get(trip.getId()).equalsWithoutCollections(trip)
			|| !getSpecimenIds(trips.get(trip.getId())).equals(getSpecimenIds(trip))
			|| !getPhotoIds(trips.get(trip.getId())).equals(getPhotoIds(trip));
	}

	private Set<String> getSpecimenIds(Trip trip)
	{
		return trip.getSpecimens().stream().map(Specimen::getId).collect(toSet());
	}

	private Set<String> getPhotoIds(Trip trip)
	{
		return trip.getPhotos().stream().map(Photo::getId).collect(toSet());
	}

	private boolean isSpecimensChanged(Trip trip)
	{
		return (trip.isNew() && trip.hasSpecimens())
			|| !trips.getOrDefault(trip.getId(), EMPTY_TRIP).getSpecimens().equals(trip.getSpecimens());
	}

	private boolean isPhotosChanged(Trip trip)
	{
		return (trip.isNew() && trip.hasPhotos())
			|| !trips.getOrDefault(trip.getId(), EMPTY_TRIP).getPhotos().equals(trip.getPhotos());
	}

	@Override
	public void deleteTrip(Trip tripToDelete)
	{
	    String id = tripToDelete.getId();
        if (trips.containsKey(id))
	    {
	        Trip trip = trips.remove(id);
	        trip.getSpecimens().stream().map(Specimen::getId).forEach(specimens::remove);
	        trip.getPhotos().stream().map(Photo::getId).forEach(photos::remove);

	        writeTrips();
	        if (trip.hasSpecimens())
	        {
	            writeSpecimens();
	        }
	        if (trip.hasPhotos())
	        {
	        	writePhotos();
	        }
	    }
	}

	@Override
	public SortedSet<String> getAutoCompletions(AutoCompleteField field)
	{
	    if (autoCompleteMap == null)
	    {
    	    autoCompleteMap = streamSpecimens()
    	            .map(Specimen::getAutoCompletions)
    	            .flatMap(map -> map.entrySet().stream())
    	            .collect(groupingBy(
    	                    Entry::getKey,
    	                    mapping(Entry::getValue, 
    	                            collectingAndThen(
    	                                    toCollection(TreeSet::new), Collections::unmodifiableSortedSet))));
	    }
	    return autoCompleteMap.getOrDefault(field, emptySortedSet());
	}
}
