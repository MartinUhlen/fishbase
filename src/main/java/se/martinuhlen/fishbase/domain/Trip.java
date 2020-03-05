package se.martinuhlen.fishbase.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Represents a fishing trip.
 *
 * @author Martin
 */
public final class Trip extends Domain<Trip>
{
	public static final String DESCRIPTION_IS_MANDATORY = "Description is mandatory";
	public static final String DATES_IN_RANGE = "End date cannot be less than start date";
	public static final String DATE_NOT_IN_FUTURE = "Date cannot be in the future";
	public static final String TEXT_IS_MANDATORY = "Text is mandatory";
	
    public static final Trip EMPTY_TRIP = new Builder("#emptyTrip", false).build();

	public static DescriptionBuilder asPersisted(String id)
	{
	    return new Builder(id, true);
	}

	public static Trip asNew()
	{
		return new Builder(UUID.randomUUID().toString(), false)
		        .startDate(now())
		        .endDate(now())
		        .build();
	}

    private Trip(String id, boolean persisted, String description, LocalDate startDate, LocalDate endDate, String text, List<Specimen> specimens, List<Photo> photos)
    {
        super(id, persisted);
        this.description = requireNonNull(description, "description can't be null");
        this.startDate = requireNonNull(startDate, "startDate can't be null");
        this.endDate = requireNonNull(endDate, "endDate can't be null");
        this.text = requireNonNull(text, "text can't be null");
        this.specimens = specimens;
        this.photos = photos;
    }

	private Trip(String id, boolean persisted, String description, LocalDate startDate, LocalDate endDate, String text, Collection<Specimen> specimens, Collection<Photo> photos)
	{
	    this(id, persisted, description, startDate, endDate, text, applySpecimens(id, specimens), applyPhotos(id, photos));
	}

	private static List<Specimen> applySpecimens(String id, Collection<Specimen> specimens)
    {
		requireNonBlank(id, "id cannot be blank");
        return requireNonNull(specimens, "specimens cannot be null")
                .stream()
                .peek(s -> requireNonNull(s, "specimen cannot be null"))
                .peek(s -> checkArgument(s.getTripId().equals(id), "Specimen#tripId must be equal to Trip#id"))
                .sorted(comparing(Specimen::getInstant))
                .collect(toUnmodifiableList());
    }

    private static List<Photo> applyPhotos(String id, Collection<Photo> photos)
    {
    	requireNonBlank(id, "id cannot be blank");
    	requireNonNull(photos, "photos cannot be null");
    	photos.forEach(photo -> requireNonNull(photo, "photo cannot be null"));
    	photos.forEach(photo -> checkArgument(id.equals(photo.getTripId()), "Photo#tripId must be equal to Trip#id"));
    	return List.copyOf(photos);
	}

	//@formatter:off
	private final String description;
	public String getDescription(){return description;}
	public Trip withDescription(String description){return with(this.description, description, description, startDate, endDate, text, specimens);}

	private final LocalDate startDate;
	public LocalDate getStartDate(){return startDate;}
	public Trip withStartDate(LocalDate startDate){return with(this.startDate, startDate, description, startDate, endDate, text, specimens);}

	private final LocalDate endDate;
	public LocalDate getEndDate(){return endDate;}
	public Trip withEndDate(LocalDate endDate){return with(this.endDate, endDate, description, startDate, endDate, text, specimens);}

	private final String text;
	public String getText(){return text;}
	public Trip withText(String text){return with(this.text, text, description, startDate, endDate, text, specimens);}

	private final List<Specimen> specimens;
	public List<Specimen> getSpecimens(){return specimens;}
	public Trip withSpecimens(Collection<Specimen> specimens)
	{
	    requireNonNull(specimens, "specimens can't be null");
	    return ((this.specimens.isEmpty() && specimens.isEmpty()) || this.specimens.equals(specimens))
	            ? this
	            : new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens, photos);
	}

	private List<Photo> photos;
	public List<Photo> getPhotos(){return photos;}
	public Trip withPhotos(Collection<Photo> photos)
	{
	    requireNonNull(photos, "photos cannot be null");
	    return ((this.photos.isEmpty() && photos.isEmpty()) || this.photos.equals(photos))
	            ? this
	            : new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens, photos);
	}
	//@formatter:on

	private <T> Trip with(T currentValue, T newValue, String description, LocalDate startDate, LocalDate endDate, String text, List<Specimen> specimens)
	{
	    return currentValue.equals(newValue)
	            ? this
	            : new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens, photos);
	}

	@Override
	public Stream<String> getValidationErrors()
	{
		return Stream.concat(getTripErrors(), getSpecimenErrors());
	}

	private Stream<String> getTripErrors()
	{
		List<String> errors = new LinkedList<>();
		if (isBlank(description))
		{
			errors.add(DESCRIPTION_IS_MANDATORY);
		}
		if (startDate.isAfter(endDate))
		{
			errors.add(DATES_IN_RANGE);
		}
		if (startDate.isAfter(now()))
		{
		    errors.add(DATE_NOT_IN_FUTURE);
		}
        if (isBlank(text))
        {
            errors.add(TEXT_IS_MANDATORY);
        }
		return errors.stream();
	}

	private Stream<String> getSpecimenErrors()
	{
		return specimens.stream()
			.map(specimen -> getSpecimenErrors(specimen))
			.filter(error -> error != null);
	}

	private String getSpecimenErrors(Specimen specimen)
	{
		List<String> errors = specimen.getValidationErrors().collect(toList());
		if (!errors.isEmpty())
		{
			return specimen.getLabel() + ":\n" +
					errors.stream()
					.map(error -> "    " + error)
					.collect(joining("\n"));
		}
		else
		{
			return null;
		}
	}

	@Override
	public String getLabel()
	{
		String description = isNew() && isBlank(getDescription()) ? "New trip" : getDescription();
		return description + " " + getStartDate();
	}

	@Override
	protected boolean equalsData(Trip that)
	{
		return equalsWithoutCollections(that)
				&& this.specimens.equals(that.specimens);
	}

	public boolean equalsWithoutCollections(Trip that)
	{
		return that != null && new EqualsBuilder()
				.append(this.photos, that.photos)
				.append(this.description, that.description)
				.append(this.startDate, that.startDate)
				.append(this.endDate, that.endDate)
				.append(this.text, that.text)
				.isEquals();
	}

	@Override
	public Trip copy()
	{
		return new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens, photos);
	}

    public boolean hasSpecimens()
    {
        return !specimens.isEmpty();
    }

    public boolean hasPhotos()
    {
        return !photos.isEmpty();
    }

    private static class Builder extends Domain.Builder<Trip> implements DescriptionBuilder, StartDateBuilder, EndDateBuilder, TextBuilder, SpecimenBuilder, PhotoBuilder
    {
        private String description = "";
        private LocalDate startDate = LocalDate.MIN;
        private LocalDate endDate = LocalDate.MAX;
        private String text = "";
        private Collection<Specimen> specimens = Set.of();
        private List<Photo> photos = List.of();

        private Builder(String id, boolean persisted)
        {
        	super(id, persisted);
        }

        @Override
        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        @Override
        public Builder startDate(LocalDate startDate)
        {
            this.startDate = startDate;
            return this;
        }

        @Override
        public Builder endDate(LocalDate endDate)
        {
            this.endDate = endDate;
            return this;
        }

        @Override
        public Builder text(String text)
        {
            this.text = text;
            return this;
        }

        @Override
        public Builder specimens(Collection<Specimen> specimens)
        {
            this.specimens = specimens;
            return this;
        }

        @Override
        public Trip photos(List<Photo> photos)
        {
        	this.photos = photos;
        	return build();
        }
        
        private Trip build()
        {
            return new Trip(id, persisted, description, startDate, endDate, text, specimens, photos);
        }
    }

    public interface DescriptionBuilder
    {
        StartDateBuilder description(String description);
    }

    public interface StartDateBuilder
    {
        EndDateBuilder startDate(LocalDate startDate);
    }

    public interface EndDateBuilder
    {
        TextBuilder endDate(LocalDate endDate);
    }

    public interface TextBuilder
    {
        SpecimenBuilder text(String text);
    }

    public interface SpecimenBuilder
    {
        PhotoBuilder specimens(Collection<Specimen> specimens);
    }

	public interface PhotoBuilder
	{
		Trip photos(List<Photo> photos);
	}
}
