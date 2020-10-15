package se.martinuhlen.fishbase.javafx.data;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.martinuhlen.fishbase.domain.TestData.newPhoto;
import static se.martinuhlen.fishbase.domain.TestData.trip1;
import static se.martinuhlen.fishbase.domain.TestData.trip2;
import static se.martinuhlen.fishbase.domain.Trip.EMPTY_TRIP;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

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
		String tripId = wrapper.id().getValue();
		Specimen specimen0 = Specimen.asNew(tripId);
		Specimen specimen1 = Specimen.asNew(tripId);
		Specimen specimen2 = Specimen.asNew(tripId);
		wrapper.specimens().setValue(asList(specimen0, specimen1, specimen2));
		wrapper.photos().setValue(List.of(
				newPhoto("1", tripId).addSpecimen(specimen1.getId()),
				newPhoto("2", tripId).addSpecimen(specimen2.getId())));

		wrapper.specimenWrappers().remove(1);

		assertEquals(asList(specimen0, specimen2), wrapper.specimens().getValue());
		assertEquals(List.of(newPhoto("1", tripId), newPhoto("2", tripId).addSpecimen(specimen2.getId())), wrapper.photos().getValue());
	}

	@Test
	public void removeSpecimenWhenNotContainedInPhoto()
	{
		String tripId = wrapper.id().getValue();
		Specimen specimen0 = Specimen.asNew(tripId);
		Specimen specimen1 = Specimen.asNew(tripId);
		Specimen specimen2 = Specimen.asNew(tripId);
		wrapper.specimens().setValue(asList(specimen0, specimen1, specimen2));
		wrapper.photos().setValue(List.of(newPhoto("1", tripId), newPhoto("2", tripId)));

		wrapper.specimenWrappers().remove(1);

		assertEquals(asList(specimen0, specimen2), wrapper.specimens().getValue());
		assertEquals(List.of(newPhoto("1", tripId), newPhoto("2", tripId)), wrapper.photos().getValue());
	}

	@Test
	public void photos()
	{
		Trip trip = trip1().withPhotos(Set.of());
		wrapper.setWrapee(trip);
		assertEquals(Set.of(), wrapper.getAddedPhotos());
		assertEquals(Set.of(), wrapper.getRemovedPhotos());

		wrapper.photos().setValue(List.of(newPhoto("photo#1", trip.getId()), newPhoto("photo#2", trip.getId()), newPhoto("photo#3", trip.getId())));
		assertEquals(Set.of("photo#1", "photo#2", "photo#3"), wrapper.getAddedPhotos().stream().map(Photo::getId).collect(toUnmodifiableSet()));
		assertEquals(Set.of(), wrapper.getRemovedPhotos());

		wrapper.setWrapee(wrapper.getWrapee());
		assertEquals(Set.of(), wrapper.getAddedPhotos());
		assertEquals(Set.of(), wrapper.getRemovedPhotos());

		wrapper.photos().setValue(List.of(newPhoto("photo#2", trip.getId()), newPhoto("photo#4", trip.getId()), newPhoto("photo#5", trip.getId())));
		assertEquals(Set.of("photo#4", "photo#5"), wrapper.getAddedPhotos().stream().map(Photo::getId).collect(toUnmodifiableSet()));
		assertEquals(Set.of("photo#1", "photo#3"), wrapper.getRemovedPhotos().stream().map(Photo::getId).collect(toUnmodifiableSet()));

		wrapper.setWrapee(wrapper.getWrapee());
		assertEquals(Set.of(), wrapper.getAddedPhotos());
		assertEquals(Set.of(), wrapper.getRemovedPhotos());
	}
}
