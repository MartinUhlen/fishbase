package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.martinuhlen.fishbase.domain.TestData.bream;
import static se.martinuhlen.fishbase.domain.TestData.bream5120;
import static se.martinuhlen.fishbase.domain.TestData.perch1000;
import static se.martinuhlen.fishbase.domain.TestData.tench;
import static se.martinuhlen.fishbase.domain.TestData.tench3540;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class SpecimenTest
{
	@Test
	public void properties()
	{
		LocalDateTime instant = parse("2018-05-13T05:15");
		Specimen s = Specimen.asPersisted("1")
		        .tripId("2")
				.specie(tench())
				.weight(3920)
				.length(61)
				.location("The lake")
				.instant(instant)
				.method("Bottenmete")
				.bait("Boilies")
				.weather("Sunny")
				.text("ABC");

		assertEquals("1", s.getId());
		assertEquals("2", s.getTripId());
		assertEquals(tench(), s.getSpecie());
		assertEquals(3920, s.getWeight());
		assertEquals(61, s.getLength());
		assertEquals("The lake", s.getLocation());
		assertEquals(instant, s.getInstant());
		assertEquals("Bottenmete", s.getMethod());
		assertEquals("Boilies", s.getBait());
		assertEquals("Sunny", s.getWeather());
		assertEquals("ABC", s.getText());

		assertSame(s, s.withSpecie(s.getSpecie()));
		assertSame(s, s.withWeight(s.getWeight()));
	    assertSame(s, s.withLength(s.getLength()));
	    assertSame(s, s.withLocation(s.getLocation()));
	    assertSame(s, s.withInstant(s.getInstant()));
	    assertSame(s, s.withMethod(s.getMethod()));
	    assertSame(s, s.withBait(s.getBait()));
	    assertSame(s, s.withWeather(s.getWeather()));
	    assertSame(s, s.withText(s.getText()));

        assertEquals(bream(), s.withSpecie(bream()).getSpecie());
        assertEquals(6265, s.withWeight(6265).getWeight());
        assertEquals(75,5, s.withLength(75.5F).getLength());
        assertEquals("SomeRiver", s.withLocation("SomeRiver").getLocation());
        assertEquals(instant.plusDays(1), s.withInstant(instant.plusDays(1)).getInstant());
        assertEquals("Trolling", s.withMethod("Trolling").getMethod());
        assertEquals("Wobbler", s.withBait("Wobbler").getBait());
        assertEquals("Cloudy", s.withWeather("Cloudy").getWeather());
        assertEquals("XYZ", s.withText("XYZ").getText());
	}

	@Test
	public void equalsAndHashCode()
	{
		Specimen bream = bream5120();
        assertEquals(bream, bream);
		assertEquals(bream.hashCode(), bream.hashCode());

		assertNotEquals(bream, null);
		assertNotEquals(bream, "SomeOtherType");
		assertNotEquals(bream, tench3540());
		assertNotEquals(Specimen.asNew("tripId"), Specimen.asNew("tripId"));

		assertNotEquals(bream, bream.withSpecie(tench()));
		assertNotEquals(bream, bream.withWeight(1337));
		assertNotEquals(bream, bream.withLength(55));
		assertNotEquals(bream, bream.withLocation("X"));
		assertNotEquals(bream, bream.withInstant(LocalDateTime.now()));
		assertNotEquals(bream, bream.withMethod("X"));
		assertNotEquals(bream, bream.withBait("X"));
		assertNotEquals(bream, bream.withWeather("X"));
		assertNotEquals(bream, bream.withText("X"));
	}

	@Test
	public void asPersisted()
	{
		Specimen s = perch1000();
		assertTrue(s.isPersisted());
		assertFalse(s.isNew());

		s.markPersisted();
		assertTrue(s.isPersisted());
		assertFalse(s.isNew());
	}

	@Test
	public void asNew()
	{
		Specimen s = Specimen.asNew("A");
		assertNotNull(s.getId());
		assertEquals("A", s.getTripId());
		assertFalse(s.isPersisted());
		assertTrue(s.isNew());

		s.markPersisted();
		assertTrue(s.isPersisted());
		assertFalse(s.isNew());
	}

    @Test
    public void invariants()
    {
        Specimen s = Specimen.asNew("tripId");
        assertThrows(NullPointerException.class, () -> s.withSpecie(null));
        assertThrows(NullPointerException.class, () -> s.withLocation(null));
        assertThrows(NullPointerException.class, () -> s.withInstant(null));
        assertThrows(NullPointerException.class, () -> s.withMethod(null));
        assertThrows(NullPointerException.class, () -> s.withBait(null));
        assertThrows(NullPointerException.class, () -> s.withWeather(null));
        assertThrows(NullPointerException.class, () -> s.withText(null));
        assertThrows(IllegalArgumentException.class, () -> s.withWeight(-500));
        assertThrows(IllegalArgumentException.class, () -> s.withLength(-1));
    }
}
