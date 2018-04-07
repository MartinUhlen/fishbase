package se.martinuhlen.fishbase.javafx.data;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javafx.collections.FXCollections.observableArrayList;
import static se.martinuhlen.fishbase.domain.Trip.EMPTY_TRIP;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.annotations.VisibleForTesting;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;
import se.martinuhlen.fishbase.drive.photo.FishingPhoto;

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

	@Override
	public void setWrapee(Trip wrapee)
	{
		super.setWrapee(wrapee);
		setInitialPhotos(emptyList());
	}

	public ObservableValue<String> id()
	{
		return getProperty("id", Trip::getId);
	}

	public Property<String> description()
	{
		return getProperty("description", Trip::getDescription, Trip::setDescription);
	}

	public Property<LocalDate> startDate()
	{
		return getProperty("startDate", Trip::getStartDate, Trip::setStartDate);
	}

	public Property<LocalDate> endDate()
	{
		return getProperty("endDate", Trip::getEndDate, Trip::setEndDate);
	}

	public Property<String> text()
	{
		return getProperty("text", Trip::getText, Trip::setText);
	}

	public Property<List<Specimen>> specimens()
	{
		return getProperty("specimens", Trip::getSpecimens, Trip::setSpecimens);
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
			specimenWrappers.setAll(property.getValue().stream().map(s -> new SpecimenWrapper(s)).collect(toList()));
		}));
	}

    private void removeSpecimensFromPhotos(List<? extends SpecimenWrapper> removedSpecimens)
    {
        Set<String> removedSpecimenIds = removedSpecimens.stream().map(sw -> sw.getWrapee().getId()).collect(toSet());
        List<FishingPhoto> photosWithoutSpecimens = photos()
                .stream()
                .map(photo -> photo.withoutSpecimens(removedSpecimenIds))
                .collect(toList());

        if (!photos().equals(photosWithoutSpecimens))
        {
            photos().setAll(photosWithoutSpecimens);
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

	private List<FishingPhoto> initialPhotos = emptyList();
	private ObservableList<FishingPhoto> currentPhotos;
	public ObservableList<FishingPhoto> photos()
	{
		if (currentPhotos == null)
		{
			currentPhotos = FXCollections.observableArrayList();
			currentPhotos.addListener((Observable obs) -> notifyListeners());
		}
		return currentPhotos;
	}

	public void setInitialPhotos(Collection<FishingPhoto> photos)
	{
		initialPhotos = new ArrayList<>(photos);
		photos().setAll(photos);
	}

	public Set<FishingPhoto> getAddedPhotos()
	{
		Set<FishingPhoto> modified = getModifiedPhotos();
		return photos().stream()
				.filter(p -> !initialPhotos.contains(p))
				.filter(p -> !modified.contains(p))
				.collect(toSet());
	}

	public Set<FishingPhoto> getRemovedPhotos()
	{
		Set<String> currentIds = idsOf(photos());
		return initialPhotos.stream()
				.filter(p -> !currentIds.contains(p.getId()))
				.collect(toSet());
	}

	public Set<FishingPhoto> getModifiedPhotos()
	{
		Set<String> initialIds = idsOf(initialPhotos);
		return photos().stream()
				.filter(p -> initialIds.contains(p.getId()))
				.filter(p -> !initialPhotos.contains(p))
				.collect(toSet());
	}

	private Set<String> idsOf(Collection<FishingPhoto> photos)
	{
		return photos.stream().map(FishingPhoto::getId).collect(toSet());
	}

	@Override
	public boolean hasChanges()
	{
		return super.hasChanges()
			|| hasPhotoChanges();

	}

	public boolean hasPhotoChanges()
	{
		return initialPhotos.size() != photos().size()
			|| !initialPhotos.containsAll(photos());
	}

	public boolean isEmpty()
	{
		return getWrapee() == EMPTY_TRIP;
	}
}
