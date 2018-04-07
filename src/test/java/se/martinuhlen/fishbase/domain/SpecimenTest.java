package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.martinuhlen.fishbase.domain.TestData.bream5120;
import static se.martinuhlen.fishbase.domain.TestData.tench;
import static se.martinuhlen.fishbase.domain.TestData.tench3540;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.martinuhlen.fishbase.domain.Specimen;

public class SpecimenTest
{
	@Test
	public void properties()
	{
		LocalDateTime instant = parse("2018-05-13T05:15");
		Specimen s = Specimen.asPersisted("1", "2")
				.setSpecie(tench())
				.setWeight(3920)
				.setLength(61)
				.setLocation("The lake")
				.setInstant(instant)
				.setMethod("Bottenmete")
				.setBait("Boilies")
				.setWeather("Sunny")
				.setText("ABC");

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
	}

	@Test
	public void equalsAndHashCode()
	{
		assertEquals(bream5120(), bream5120());
		assertEquals(bream5120().hashCode(), bream5120().hashCode());

		assertNotEquals(bream5120(), null);
		assertNotEquals(bream5120(), "SomeOtherType");
		assertNotEquals(bream5120(), tench3540());
		assertNotEquals(Specimen.asNew("X"), Specimen.asNew("X"));
		assertNotEquals(Specimen.asPersisted("a", "b"), Specimen.asPersisted("c", "d"));

		assertNotEquals(bream5120(), bream5120().setSpecie(tench()));
		assertNotEquals(bream5120(), bream5120().setWeight(1337));
		assertNotEquals(bream5120(), bream5120().setLength(55));
		assertNotEquals(bream5120(), bream5120().setLocation("X"));
		assertNotEquals(bream5120(), bream5120().setInstant(LocalDateTime.now()));
		assertNotEquals(bream5120(), bream5120().setMethod("X"));
		assertNotEquals(bream5120(), bream5120().setBait("X"));
		assertNotEquals(bream5120(), bream5120().setWeather("X"));
		assertNotEquals(bream5120(), bream5120().setText("X"));
	}

	@Test
	public void asPersisted()
	{
		Specimen s = Specimen.asPersisted("a", "b");
		assertEquals("a", s.getId());
		assertEquals("b", s.getTripId());
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
}
