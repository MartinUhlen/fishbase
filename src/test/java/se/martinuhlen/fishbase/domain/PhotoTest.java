package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.martinuhlen.fishbase.domain.TestData.photo1InTrip1;
import static se.martinuhlen.fishbase.domain.TestData.photo1InTrip2;
import static se.martinuhlen.fishbase.domain.TestData.photo2InTrip1;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link Photo}.
 *
 * @author Martin
 */
public class PhotoTest
{
	@Test
	public void properties()
	{
		Photo photo = Photo.asPersisted("thePhotoId")
				.tripId("theTripId")
				.specimens(Set.of("s1", "s2"))
				.fileName("SomeFile.jpg")
				.time(parse("2020-02-29T16:29:37"))
				.starred(false);

		assertEquals("thePhotoId", photo.getId());
		assertTrue(photo.isPersisted());
		assertEquals("theTripId", photo.getTripId());
		assertEquals(Set.of("s1", "s2"), photo.getSpecimens());
		assertEquals("SomeFile.jpg", photo.getFileName());
		assertFalse(photo.isStarred());
	}

	@Test
	public void starred()
	{
		assertTrue(photo1InTrip1().withStarred(true).isStarred());
		assertFalse(photo1InTrip1().withStarred(false).isStarred());
	}

	@Test
	public void specimens()
	{
		Photo photo = TestData.newPhoto("photoId", "tripId");
		assertTrue(photo.getSpecimens().isEmpty());

		photo = photo.addSpecimen("a").addSpecimen("b").addSpecimen("c").addSpecimen("d");
		assertEquals(Set.of("a", "b", "c", "d"), photo.getSpecimens());

		photo = photo.addSpecimen("b");
		assertEquals(Set.of("a", "b", "c", "d"), photo.getSpecimens());

		photo = photo.removeSpecimen("X");
		assertEquals(Set.of("a", "b", "c", "d"), photo.getSpecimens());

		photo = photo.removeSpecimen("b");
		assertEquals(Set.of("a", "c", "d"), photo.getSpecimens());

		photo = photo.removeSpecimens(Set.of("a", "X", "d"));
		assertEquals(Set.of("c"), photo.getSpecimens());
	}

	@Test
	public void equalsAndHashCode()
	{
		assertEquals(photo1InTrip1(), photo1InTrip1());
		assertEquals(photo2InTrip1(), photo2InTrip1());
		assertEquals(photo1InTrip2(), photo1InTrip2());
		assertEquals(photo1InTrip2().hashCode(), photo1InTrip2().hashCode());

		assertNotEquals(photo1InTrip1(), photo2InTrip1());
		assertNotEquals(photo1InTrip1(), "SomeOtherType");
		assertNotEquals(photo1InTrip1(), null);
	}
}
