package se.martinuhlen.fishbase.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.LocalDate.now;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
	public static final Trip EMPTY_TRIP = new Builder("", false).build();

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

    private Trip(String id, boolean persisted, String description, LocalDate startDate, LocalDate endDate, String text, List<Specimen> specimens)
    {
        super(id, persisted);
        this.description = requireNonNull(description, "description can't be null");
        this.startDate = requireNonNull(startDate, "startDate can't be null");
        this.endDate = requireNonNull(endDate, "endDate can't be null");
        this.text = requireNonNull(text, "text can't be null");
        this.specimens = specimens;
    }

	private Trip(String id, boolean persisted, String description, LocalDate startDate, LocalDate endDate, String text, Collection<Specimen> specimens)
	{
	    this(id, persisted, description, startDate, endDate, text, applySpecimens(id, specimens));
	}

    private static List<Specimen> applySpecimens(String id, Collection<Specimen> specimens)
    {
        return unmodifiableList(requireNonNull(specimens, "specimens can't be null")
                .stream()
                .peek(s -> checkArgument(s.getTripId().equals(id), "Specimen#tripId must be equal to Trip#id"))
                .sorted(comparing(Specimen::getInstant))
                .collect(toList()));
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
	            : new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens);
	}
	//@formatter:on

	private <T> Trip with(T currentValue, T newValue, String description, LocalDate startDate, LocalDate endDate, String text, List<Specimen> specimens)
	{
	    return currentValue.equals(newValue)
	            ? this
	            : new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens);
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
			errors.add("Description is mandatory");
		}
		if (startDate.isAfter(endDate))
		{
			errors.add("Start date must be <= end date");
		}
		if (startDate.isAfter(now()))
		{
		    errors.add("Date cannot be in the future.");
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
		return equalsWithoutSpecimens(that)
				&& this.specimens.equals(that.specimens);
	}

	public boolean equalsWithoutSpecimens(Trip that)
	{
		return that != null && new EqualsBuilder()
				.append(this.description, that.description)
				.append(this.startDate, that.startDate)
				.append(this.endDate, that.endDate)
				.append(this.text, that.text)
				.isEquals();
	}

	@Override
	public Trip copy()
	{
		return new Trip(getId(), isPersisted(), description, startDate, endDate, text, specimens);
	}

    public boolean hasSpecimens()
    {
        return !specimens.isEmpty();
    }

    private static class Builder implements DescriptionBuilder, StartDateBuilder, EndDateBuilder, TextBuilder, SpecimenBuilder
    {
        private final String id;
        private final boolean persisted;
        private String description = "";
        private LocalDate startDate = LocalDate.MIN;
        private LocalDate endDate = LocalDate.MAX;
        private String text = "";
        private Collection<Specimen> specimens = emptySet();

        Builder(String id, boolean persisted)
        {
            this.id = requireNonNull(id);
            this.persisted = persisted;
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
        public Trip specimens(Collection<Specimen> specimens)
        {
            this.specimens = specimens;
            return build();
        }

        private Trip build()
        {
            return new Trip(id, persisted, description, startDate, endDate, text, specimens);
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
        Trip specimens(Collection<Specimen> specimens);
    }
}
