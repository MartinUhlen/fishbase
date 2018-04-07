package se.martinuhlen.fishbase.filter;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.function.Predicate;
import java.util.stream.Stream;

import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

/**
 * Filters {@link Trip}s by user supplied text.
 */
public class TripTextPredicate extends TextPredicate<Trip>
{
	private final Predicate<Specimen> specimenPredicate;

	public TripTextPredicate(String text)
	{
		super(text);
		this.specimenPredicate = new SpecimenTextPredicate(text);
	}

	@Override
	boolean matchesText(Trip trip, String text)
	{
		return tripMatches(trip, text)
			|| specimensMatches(trip);
	}

	private boolean tripMatches(Trip trip, String text)
	{
		return Stream.of(trip.getDescription(), trip.getStartDate().toString(), trip.getText())
					.anyMatch(str -> containsIgnoreCase(str, text));
	}

	private boolean specimensMatches(Trip trip)
	{
		return trip.getSpecimens()
				.stream()
				.anyMatch(specimenPredicate);
	}
}
