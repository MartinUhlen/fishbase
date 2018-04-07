package se.martinuhlen.fishbase.domain;

import static java.time.LocalDate.now;
import static java.time.LocalDate.parse;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.martinuhlen.fishbase.domain.TestData.trip1;
import static se.martinuhlen.fishbase.domain.TestData.trip2;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

public class TripTest
{
	@Test
	public void properties()
	{
		Trip trip = Trip.asNew()
				.setDescription("A good trip")
				.setStartDate(parse("2015-03-13"))
				.setEndDate(parse("2015-03-14"))
				.setText("Some text");

		assertEquals("A good trip", trip.getDescription());
		assertEquals(parse("2015-03-13"), trip.getStartDate());
		assertEquals(parse("2015-03-14"), trip.getEndDate());
		assertEquals("Some text", trip.getText());
	}

	@Test
	public void equalsAndHashCode()
	{
		assertEquals(trip1(), trip1());
		assertEquals(trip1().hashCode(), trip1().hashCode());

		assertNotEquals(trip1(), null);
		assertNotEquals(trip1(), "SomeOtherType");
		assertNotEquals(trip1(), trip2());
		assertNotEquals(Trip.asNew(), Trip.asNew());
		assertNotEquals(Trip.asPersisted("a"), Trip.asPersisted("b"));

		assertNotEquals(trip1(), trip1().setDescription("Test"));
		assertNotEquals(trip1(), trip1().setStartDate(now().minusDays(1)));
		assertNotEquals(trip1(), trip1().setEndDate(now()));
		assertNotEquals(trip1(), trip1().setText("ABC"));
		assertNotEquals(trip1(), trip1().setSpecimens(asList(Specimen.asNew(trip1().getId()))));
	}

	@Test
	public void asPersisted()
	{
		Trip t = Trip.asPersisted("x");
		assertEquals("x", t.getId());
		assertTrue(t.isPersisted());
		assertFalse(t.isNew());

		t.markPersisted();
		assertTrue(t.isPersisted());
		assertFalse(t.isNew());
	}

	@Test
	public void asNew()
	{
		Trip t = Trip.asNew();
		assertNotNull(t.getId());
		assertFalse(t.isPersisted());
		assertTrue(t.isNew());

		t.markPersisted();
		assertTrue(t.isPersisted());
		assertFalse(t.isNew());
	}

	@Test
	public void specimensAreSorted()
	{
		String tripId = "X";
		Trip trip = Trip.asPersisted(tripId);
		Specimen first = Specimen.asPersisted("a", tripId).setInstant(LocalDateTime.parse("2018-03-13T09:30"));
		Specimen second = Specimen.asPersisted("b", tripId).setInstant(LocalDateTime.parse("2018-03-13T14:45"));
		trip.setSpecimens(asList(second, first));

		assertEquals(asList(first, second), trip.getSpecimens(), "Expect specimens, re-sorted");
	}

	@Test
	public void specimensMustHaveTripId()
	{
		assertThrows(IllegalArgumentException.class, () -> trip1().setSpecimens(asList(Specimen.asNew("?"))));
	}
}
