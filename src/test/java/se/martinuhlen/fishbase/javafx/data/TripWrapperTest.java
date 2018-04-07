package se.martinuhlen.fishbase.javafx.data;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.martinuhlen.fishbase.domain.TestData.trip1;
import static se.martinuhlen.fishbase.domain.TestData.trip2;
import static se.martinuhlen.fishbase.domain.Trip.EMPTY_TRIP;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.drive.photo.FishingPhoto;
import se.martinuhlen.fishbase.drive.photo.Photo;

/**
 * Unit tests of {@link TripWrapper}.
 */
public class TripWrapperTest extends WrapperTestCase<Trip, TripWrapper>
{
	@Override
	protected TripWrapper createWrapper()
	{
		TripWrapper w = new TripWrapper();
		w.setWrapee(trip1());
		w.addListener(listener);
		return w;
	}

	@Test
	public void isEmpty()
	{
		wrapper.setWrapee(trip1());
		assertFalse(wrapper.isEmpty());

		wrapper.setWrapee(EMPTY_TRIP);
		assertTrue(wrapper.isEmpty());
	}

	@Test
	public void id()
	{
		ObservableValue<String> id = wrapper.id();
		assertEquals(trip1().getId(), id.getValue());

		InvalidationListener l = Mockito.mock(InvalidationListener.class);
		id.addListener(l);

		wrapper.setWrapee(trip2());
		assertEquals(trip2().getId(), id.getValue());
		verify(l).invalidated(id);
	}

	@Test
	public void descriptionProperty()
	{
		testProperty("description", wrapper::description, Trip::getDescription, "A", "B", "C");
	}

	@Test
	public void textProperty()
	{
		testProperty("text", wrapper::text, Trip::getText, "A", "B", "C");
	}

	@Test
	public void startDateProperty()
	{
		testProperty("startDate", wrapper::startDate, Trip::getStartDate, LocalDate.parse("2017-02-02"), LocalDate.parse("2017-04-10"), LocalDate.parse("2017-08-24"));
	}

	@Test
	public void endDateProperty()
	{
		testProperty("endDate", wrapper::endDate, Trip::getEndDate, LocalDate.parse("2017-02-02"), LocalDate.parse("2017-04-10"), LocalDate.parse("2017-08-24"));
	}

	@Test
	public void endDateFollowsStartDate()
	{
		wrapper.startDate().setValue(LocalDate.parse("2017-02-02"));
		assertEquals(LocalDate.parse("2017-02-02"), wrapper.startDate().getValue());
		assertEquals(LocalDate.parse("2017-02-02"), wrapper.endDate().getValue());

		wrapper.endDate().setValue(LocalDate.parse("2017-04-10"));
		assertEquals(LocalDate.parse("2017-02-02"), wrapper.startDate().getValue());
		assertEquals(LocalDate.parse("2017-04-10"), wrapper.endDate().getValue());
	}

	@Test
	public void specimens()
	{
		ObservableList<SpecimenWrapper> specimens = wrapper.specimenWrappers();
		wrapper.setWrapee(trip2());
		assertEquals(2, specimens.size());
		assertEquals(trip2().getSpecimens(), specimens.stream().map(SpecimenWrapper::getWrapee).collect(toList()));

		reset(listener);
		wrapper.addSpecimen();
		assertEquals(3, specimens.size());
		verify(listener).invalidated(wrapper);

		reset(listener);
		specimens.forEach(s -> s.locationProperty().setValue("X"));
		verify(listener, times(3)).invalidated(wrapper);

		reset(listener);
		wrapper.addSpecimen();
		assertEquals(4, specimens.size());
		verify(listener).invalidated(wrapper);

		reset(listener);
		specimens.forEach(s -> s.methodProperty().setValue("Y"));
		verify(listener, times(4)).invalidated(wrapper);

		reset(listener);
		specimens.remove(2);
		verify(listener).invalidated(wrapper);

		reset(listener);
		specimens.forEach(s -> s.baitProperty().setValue("Z"));
		verify(listener, times(3)).invalidated(wrapper);
	}

	@Test
	public void removeSpecimenWhenContainedInPhoto()
	{
		Specimen specimen0 = Specimen.asNew(wrapper.id().getValue());
		Specimen specimen1 = Specimen.asNew(wrapper.id().getValue());
		Specimen specimen2 = Specimen.asNew(wrapper.id().getValue());
		wrapper.specimens().setValue(asList(specimen0, specimen1, specimen2));
		wrapper.photos().setAll(photo("0"), photo("1").withSpecimen(specimen1.getId()), photo("2").withSpecimen(specimen2.getId()));

		wrapper.specimenWrappers().remove(1);

		assertEquals(asList(specimen0, specimen2), wrapper.specimens().getValue());
		assertEquals(asList(photo("0"), photo("1"), photo("2").withSpecimen(specimen2.getId())), wrapper.photos());
	}

	@Test
	public void removeSpecimenWhenNotContainedInPhoto()
	{
		Specimen specimen0 = Specimen.asNew(wrapper.id().getValue());
		Specimen specimen1 = Specimen.asNew(wrapper.id().getValue());
		Specimen specimen2 = Specimen.asNew(wrapper.id().getValue());
		wrapper.specimens().setValue(asList(specimen0, specimen1, specimen2));
		wrapper.photos().setAll(photo("0"), photo("1"));

		wrapper.specimenWrappers().remove(1);

		assertEquals(asList(specimen0, specimen2), wrapper.specimens().getValue());
		assertEquals(asList(photo("0"), photo("1")), wrapper.photos());
	}

	@Test
	public void photos()
	{
		ObservableList<FishingPhoto> photos = wrapper.photos();

		wrapper.setInitialPhotos(List.of(photo("1"), photo("2")));
		assertEquals(List.of(photo("1"), photo("2")), photos);
		assertFalse(wrapper.hasChanges());
		assertFalse(wrapper.hasPhotoChanges());
		verify(listener, times(1)).invalidated(wrapper);

		photos.setAll(photo("2"));
		assertTrue(wrapper.hasChanges());
		assertTrue(wrapper.hasPhotoChanges());
		verify(listener, times(2)).invalidated(wrapper);
	}

	@Test
	public void addFirstPhotos()
	{
		wrapper.photos().setAll(List.of(photo("1"), photo("2")));
		assertEquals(Set.of(photo("1"), photo("2")), wrapper.getAddedPhotos());
		assertTrue(wrapper.hasChanges());
		assertTrue(wrapper.hasPhotoChanges());
	}

	@Test
	public void addOnePhotoToExisting()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2")));
		wrapper.photos().add(photo("3"));
		assertEquals(Set.of(photo("3")), wrapper.getAddedPhotos());
	}

	@Test
	public void addTwoPhotosToExisting()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2")));
		wrapper.photos().setAll(List.of(photo("1"), photo("2"), photo("3"), photo("4")));
		assertEquals(Set.of(photo("3"), photo("4")), wrapper.getAddedPhotos());
	}

	@Test
	public void removeOnePhoto()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2"), photo("3")));
		wrapper.photos().remove(photo("2"));
		assertEquals(Set.of(photo("2")), wrapper.getRemovedPhotos());
		assertTrue(wrapper.hasChanges());
		assertTrue(wrapper.hasPhotoChanges());
	}

	@Test
	public void removeTwoPhotos()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2"), photo("3")));
		wrapper.photos().setAll(photo("2"));
		assertEquals(Set.of(photo("1"), photo("3")), wrapper.getRemovedPhotos());
	}

	@Test
	public void removeAllPhotos()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2")));
		wrapper.photos().clear();
		assertEquals(Set.of(photo("1"), photo("2")), wrapper.getRemovedPhotos());
	}

	@Test
	public void modifyOnePhoto()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2"), photo("3")));
		wrapper.photos().set(1, photo("2").withSpecimen("x"));
		assertEquals(Set.of(photo("2").withSpecimen("x")), wrapper.getModifiedPhotos());
		assertTrue(wrapper.hasChanges());
		assertTrue(wrapper.hasPhotoChanges());
	}

	@Test
	public void modifyTwoPhotos()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2"), photo("3")));
		wrapper.photos().set(0, photo("1").withSpecimen("x"));
		wrapper.photos().set(2, photo("3").withSpecimen("y"));
		assertEquals(Set.of(photo("1").withSpecimen("x"), photo("3").withSpecimen("y")), wrapper.getModifiedPhotos());
	}

	@Test
	public void addAndModifyAndRemovePhotos()
	{
		wrapper.setInitialPhotos(List.of(photo("unchanged"), photo("before-modification"), photo("removed")));

		wrapper.photos().add(photo("added"));
		wrapper.photos().set(1, photo("before-modification").withSpecimen("modified"));
		wrapper.photos().remove(2);

		assertEquals(Set.of(photo("added")), wrapper.getAddedPhotos());
		assertEquals(Set.of(photo("before-modification").withSpecimen("modified")), wrapper.getModifiedPhotos());
		assertEquals(Set.of(photo("removed")), wrapper.getRemovedPhotos());
	}

	@Test
	public void rearrangePhotoOrder()
	{
		wrapper.setInitialPhotos(List.of(photo("1"), photo("2"), photo("3")));
		wrapper.photos().setAll(List.of(photo("2"), photo("1"), photo("3")));
		assertFalse(wrapper.hasChanges());
		assertFalse(wrapper.hasPhotoChanges());
		assertTrue(wrapper.getAddedPhotos().isEmpty());
		assertTrue(wrapper.getRemovedPhotos().isEmpty());
		assertTrue(wrapper.getModifiedPhotos().isEmpty());
	}

	private FishingPhoto photo(String id)
	{
		Photo photo = mock(Photo.class);
		when(photo.getId()).thenReturn(id);
		return new FishingPhoto(photo, wrapper.id().getValue());
	}
}
