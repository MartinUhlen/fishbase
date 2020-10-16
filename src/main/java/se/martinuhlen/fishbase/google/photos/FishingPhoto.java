package se.martinuhlen.fishbase.google.photos;

import java.util.Collection;
import java.util.function.Consumer;

import se.martinuhlen.fishbase.domain.Photo;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

/**
 * A {@link GooglePhoto photo} taken during a fishing {@link Trip trip}.
 * 
 * @author Martin
 */
public interface FishingPhoto extends GooglePhoto
{
	/**
	 * Gets the ID of the {@link Trip} this photo refers to.
	 * 
	 * @return trip ID
	 */
	String getTripId();

	/**
	 * Gets if this photo contains {@link Specimen} with given ID.
	 * 
	 * @param specimenId ID of specimen to check
	 * @return {@code true} if this photo contains specimen
	 */
	boolean containsSpecimen(String specimenId);

	/**
	 * Adds {@link Specimen} with given ID to this photo.
	 * 
	 * @param specimenId ID of specimen to add to this photo
	 */
	void addSpecimen(String specimenId);

	/**
	 * Removes {@link Specimen} with given ID from this photo.
	 * 
	 * @param specimenId ID of specimen to remove from this photo
	 */
	void removeSpecimen(String specimenId);

	/**
	 * Removes {@link Specimen}s with given IDs from this photo.
	 * 
	 * @param specimenIds IDs of specimens to remove from this photo
	 */
	void removeSpecimens(Collection<String> specimenIds);

    /**
     * Gets if this photo is "starred" as a special photo.
     * 
     * @return {@code true} if this photo is starred
     */
    boolean isStarred();

    /**
     * Sets whether this photo is {@link #isStarred() starred} or not.
     * 
     * @param starred {@code true} if this photo is starred
     */
    void setStarred(boolean starred);

    /**
     * Gets the domain of this fishing photo.
     * 
     * @return photo domain
     */
    Photo getDomain();

    /**
     * Adds a listener to be notified whenever this photo is modified.
     * 
     * @param listener to notify on modification
     */
    void addListener(Consumer<? super FishingPhoto> listener);
}
