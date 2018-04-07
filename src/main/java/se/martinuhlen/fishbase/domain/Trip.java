package se.martinuhlen.fishbase.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
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

public class Trip extends Domain<Trip>
{
	public static final Trip EMPTY_TRIP = new ImmutableTrip("", false);

	public static Trip asPersisted(String id)
	{
		return new Trip(id, true);
	}

	public static Trip asNew()
	{
		Trip trip = new Trip(UUID.randomUUID().toString(), false);
		trip.setStartDate(LocalDate.now());
		trip.setEndDate(LocalDate.now());
		return trip;
	}

	private Trip(String id, boolean persisted)
	{
		super(id, persisted);
	}

	private Trip(Trip trip)
	{
		super(trip.getId(), trip.isPersisted());
		this.description = trip.description;
		this.startDate = trip.startDate;
		this.endDate = trip.endDate;
		this.text = trip.text;
		this.specimens = applySpecimens(trip.specimens);
	}

	//@formatter:off
	private String description = "";
	public String getDescription(){return description;}
	public Trip setDescription(String description){this.description = requireNonNull(description); return this;}

	private LocalDate startDate = LocalDate.MIN;
	public LocalDate getStartDate(){return startDate;}
	public Trip setStartDate(LocalDate startDate){this.startDate = requireNonNull(startDate); return this;}

	private LocalDate endDate = LocalDate.MAX;
	public LocalDate getEndDate(){return endDate;}
	public Trip setEndDate(LocalDate endDate){this.endDate = requireNonNull(endDate); return this;}

	private String text = "";
	public String getText(){return text;}
	public Trip setText(String text){this.text = requireNonNull(text); return this;}

	private List<Specimen> specimens = emptyList();
	public List<Specimen> getSpecimens(){return specimens;}
	public Trip setSpecimens(Collection<Specimen> specimens){this.specimens = applySpecimens(specimens); return this;}
	//@formatter:on

	private List<Specimen> applySpecimens(Collection<Specimen> specimens)
	{
		return unmodifiableList(requireNonNull(specimens).stream()
				.peek(s -> checkArgument(s.getTripId().equals(getId()), "tripId must be equal"))
				.map(Specimen::copy)
				.sorted(comparing(Specimen::getInstant))
				.collect(toList()));
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
		return new Trip(this);
	}

	private static final class ImmutableTrip extends Trip
	{
		private ImmutableTrip(String id, boolean persisted)
		{
			super(id, persisted);
		}

		@Override
		public Trip setDescription(String description)
		{
			return fail();
		}

		@Override
		public Trip setStartDate(LocalDate startDate)
		{
			return fail();
		}

		@Override
		public Trip setEndDate(LocalDate endDate)
		{
			return fail();
		}

		@Override
		public Trip setSpecimens(Collection<Specimen> specimens)
		{
			return fail();
		}

		private Trip fail()
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " can't be modified");
		}

		@Override
		public Trip copy()
		{
			return this;
		}
	}
}
