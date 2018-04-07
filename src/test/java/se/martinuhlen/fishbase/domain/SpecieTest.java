package se.martinuhlen.fishbase.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.martinuhlen.fishbase.domain.TestData.bream;
import static se.martinuhlen.fishbase.domain.TestData.tench;

import org.junit.jupiter.api.Test;

public class SpecieTest
{
	@Test
	public void properties()
	{
		Specie specie = Specie.asNew()
				.setName("Chub")
				.setRegWeight(2500)
				.setFreshWater(true);

		assertEquals("Chub", specie.getName());
		assertEquals(2500, specie.getRegWeight());
		assertTrue(specie.isFreshWater());
	}

	@Test
	public void equalsAndHashCode()
	{
		assertEquals(bream(), bream());
		assertEquals(bream().hashCode(), bream().hashCode());

		assertNotEquals(bream(), null);
		assertNotEquals(bream(), "SomeOtherType");
		assertNotEquals(bream(), tench());
		assertNotEquals(Specie.asNew(), Specie.asNew());
		assertNotEquals(Specie.asPersisted("a"), Specie.asPersisted("b"));

		assertNotEquals(bream(), bream().setName("Test"));
		assertNotEquals(bream(), bream().setRegWeight(5000));
		assertNotEquals(bream(), bream().setFreshWater(false));
	}

	@Test
	public void asPersisted()
	{
		Specie s = Specie.asPersisted("x");
		assertEquals("x", s.getId());
		assertTrue(s.isPersisted());
		assertFalse(s.isNew());

		s.markPersisted();
		assertTrue(s.isPersisted());
		assertFalse(s.isNew());
	}

	@Test
	public void asNew()
	{
		Specie s = Specie.asNew();
		assertNotNull(s.getId());
		assertFalse(s.isPersisted());
		assertTrue(s.isNew());

		s.markPersisted();
		assertTrue(s.isPersisted());
		assertFalse(s.isNew());
	}
}
