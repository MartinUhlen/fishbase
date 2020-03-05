package se.martinuhlen.fishbase.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
				.withName("Chub")
				.withRegWeight(2500)
				.withFreshWater(true);

		assertEquals("Chub", specie.getName());
		assertEquals(2500, specie.getRegWeight());
		assertTrue(specie.isFreshWater());

		assertSame(specie, specie.withName(specie.getName()));
		assertSame(specie, specie.withRegWeight(specie.getRegWeight()));
		assertSame(specie, specie.withFreshWater(specie.isFreshWater()));

		assertEquals("Test", specie.withName("Test").getName());
		assertEquals(1400, specie.withRegWeight(1400).getRegWeight());
		assertEquals(false, specie.withFreshWater(false).copy().isFreshWater());
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

		assertNotEquals(bream(), bream().withName("Test"));
		assertNotEquals(bream(), bream().withRegWeight(5000));
		assertNotEquals(bream(), bream().withFreshWater(false));
	}

	@Test
	public void asPersisted()
	{
		Specie s = TestData.perch();
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

	@Test
	public void invariants()
	{
	    Specie s = Specie.asNew();
	    assertThrows(IllegalArgumentException.class, () -> s.withName(null));
	    assertThrows(IllegalArgumentException.class, () -> s.withRegWeight(-500));
	}
}
