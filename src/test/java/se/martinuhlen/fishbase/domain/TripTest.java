package se.martinuhlen.fishbase.domain;

import static java.time.LocalDate.now;
import static java.time.LocalDate.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.martinuhlen.fishbase.domain.TestData.newTrip;
import static se.martinuhlen.fishbase.domain.TestData.trip1;
import static se.martinuhlen.fishbase.domain.TestData.trip2;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class TripTest
{
	@Test
	public void properties()
	{
		Trip trip = Trip.asNew()
				.withDescription("A good trip")
				.withStartDate(parse("2015-03-13"))
				.withEndDate(parse("2015-03-14"))
				.withText("Some text");

		assertEquals("A good trip", trip.getDescription());
		assertEquals(parse("2015-03-13"), trip.getStartDate());
		assertEquals(parse("2015-03-14"), trip.getEndDate());
		assertEquals("Some text", trip.getText());

		assertSame(trip, trip.withDescription(trip.getDescription()));
		assertSame(trip, trip.withStartDate(trip.getStartDate()));
		assertSame(trip, trip.withEndDate(trip.getEndDate()));
		assertSame(trip, trip.withText(trip.getText()));
		assertSame(trip, trip.withSpecimens(trip.getSpecimens()));
		assertSame(trip, trip.withSpecimens(emptyList()));

		assertEquals("ABC", trip.withDescription("ABC").getDescription());
		assertEquals(trip.getEndDate(), trip.withStartDate(trip.getEndDate()).getStartDate());
		assertEquals(trip.getStartDate(), trip.withEndDate(trip.getStartDate()).getEndDate());
		assertEquals("XYZ", trip.withText("XYZ").getText());
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

		assertNotEquals(trip1(), trip1().withDescription("Test"));
		assertNotEquals(trip1(), trip1().withStartDate(now().minusDays(1)));
		assertNotEquals(trip1(), trip1().withEndDate(now()));
		assertNotEquals(trip1(), trip1().withText("ABC"));
		assertNotEquals(trip1(), trip1().withSpecimens(asList(Specimen.asNew(trip1().getId()))));
	}

	@Test
	public void asPersisted()
	{
		Trip t = TestData.trip2();
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
		Specimen first = trip2().getSpecimens().get(0).withInstant(LocalDateTime.parse("2018-03-13T09:30"));
		Specimen second = trip2().getSpecimens().get(1).withInstant(LocalDateTime.parse("2018-03-13T14:45"));
		Trip trip = trip2().withSpecimens(asList(second, first));

		assertEquals(asList(first, second), trip.getSpecimens(), "Expect specimens, re-sorted");
	}

	@Test
	public void hasSpecimens()
	{
	    assertTrue(trip2().hasSpecimens());
	    assertFalse(newTrip().hasSpecimens());
	}

	@Test
	public void specimensMustHaveTripId()
	{
		assertThrows(IllegalArgumentException.class, () -> trip1().withSpecimens(asList(Specimen.asNew("?"))));
	}

    @Test
    public void invariants()
    {
        Trip t = Trip.asNew();
        assertThrows(NullPointerException.class, () -> Trip.asPersisted(null));
        assertThrows(NullPointerException.class, () -> t.withDescription(null));
        assertThrows(NullPointerException.class, () -> t.withStartDate(null));
        assertThrows(NullPointerException.class, () -> t.withEndDate(null));
        assertThrows(NullPointerException.class, () -> t.withText(null));
        assertThrows(NullPointerException.class, () -> t.withSpecimens(null));
    }
}
