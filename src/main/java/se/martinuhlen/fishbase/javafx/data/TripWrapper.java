package se.martinuhlen.fishbase.javafx.data;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javafx.collections.FXCollections.observableArrayList;
import static se.martinuhlen.fishbase.domain.Trip.EMPTY_TRIP;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.annotations.VisibleForTesting;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

public class TripWrapper extends Wrapper<Trip>
{
	public TripWrapper()
	{
		super(EMPTY_TRIP);
		semiBindEndDateToStartDate();
		initSpecimenSync();
	}

	private void semiBindEndDateToStartDate()
	{
		startDate().addListener(obs ->
		{
			if (!isSettingWrapee)
			{
				endDate().setValue(startDate().getValue());
			}
		});
	}

	public ObservableValue<String> id()
	{
		return getProperty("id", Trip::getId);
	}

	public Property<String> description()
	{
		return getProperty("description", Trip::getDescription, Trip::withDescription);
	}

	public Property<LocalDate> startDate()
	{
		return getProperty("startDate", Trip::getStartDate, Trip::withStartDate);
	}

	public Property<LocalDate> endDate()
	{
		return getProperty("endDate", Trip::getEndDate, Trip::withEndDate);
	}

	public Property<String> text()
	{
		return getProperty("text", Trip::getText, Trip::withText);
	}

	public Property<List<Specimen>> specimens()
	{
		return getProperty("specimens", Trip::getSpecimens, Trip::withSpecimens);
	}

	private final ObservableList<SpecimenWrapper> specimenWrappers = observableArrayList();
	public ObservableList<SpecimenWrapper> specimenWrappers()
	{
		return specimenWrappers;
	}

	private void initSpecimenSync()
	{
		Property<List<Specimen>> property = specimens();
		AtomicBoolean syncing = new AtomicBoolean(false);

		Runnable listToPropertyAction = () -> property.setValue(specimenWrappers.stream().map(SpecimenWrapper::getWrapee).collect(toList()));
		InvalidationListener listToPropertyListener = (Observable obs) -> sync(syncing, listToPropertyAction);
		specimenWrappers.addListener(listToPropertyListener);
		specimenWrappers.addListener((Change<? extends SpecimenWrapper> change) ->
		{
			while(change.next())
			{
				change.getAddedSubList().forEach(s -> s.addListener(listToPropertyListener));
				change.getRemoved().forEach(s -> s.removeAllListeners());
			}
		});
		specimenWrappers.addListener((Change<? extends SpecimenWrapper> change) ->
		{
		    if (!syncing.get())
		    {
		        while(change.next())
		        {
		            removeSpecimensFromPhotos(change.getRemoved());
		        }
		    }
		});
		property.addListener(obs -> sync(syncing, () ->
		{
			List<SpecimenWrapper> wrappers = property.getValue().stream().map(s -> new SpecimenWrapper(s)).collect(toList());
            specimenWrappers.setAll(wrappers);
		}));
	}

    private void removeSpecimensFromPhotos(List<? extends SpecimenWrapper> removedSpecimens)
    {
        Set<String> removedSpecimenIds = removedSpecimens.stream().map(sw -> sw.getWrapee().getId()).collect(toSet());
        List<Photo> photosWithoutSpecimens = photos()
        		.getValue()
                .stream()
                .map(photo -> photo.removeSpecimens(removedSpecimenIds))
                .collect(toList());

        if (!photos().getValue().equals(photosWithoutSpecimens))
        {
            photos().setValue(photosWithoutSpecimens);
        }
    }

    private void sync(AtomicBoolean syncing, Runnable action)
	{
		if (!syncing.get())
		{
			syncing.set(true);
			action.run();
			syncing.set(false);
		}
	}

    @VisibleForTesting
	SpecimenWrapper addSpecimen()
	{
        Specimen newSpecimen = Specimen.asNew(getWrapee().getId());
        SpecimenWrapper newWrapper = new SpecimenWrapper(newSpecimen);
        specimenWrappers().add(newWrapper);
        return newWrapper;
	}

	public Property<List<Photo>> photos()
	{
		return getProperty("photos", Trip::getPhotos, Trip::withPhotos);
	}

	public boolean isEmpty()
	{
		return getWrapee() == EMPTY_TRIP;
	}
}
