package se.martinuhlen.fishbase.drive.photo;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FishingPhoto implements Photo
{
	private final Photo photo;
	private final String tripId;
	private final Set<String> specimenIds;
	private final boolean starred;

	private FishingPhoto(Photo photo, String tripId, Set<String> specimenIds, boolean starred)
	{
		this.photo = requireNonNull(photo, "photo can't be null");
		this.tripId = requireNonNull(tripId, "tripId can't be null");
		this.specimenIds = new HashSet<>(specimenIds);
		this.starred = starred;
	}

	FishingPhoto(Photo photo, String tripId, String specimenIds, boolean starred)
	{
		this(photo, tripId, asList(requireNonNull(specimenIds, "specimenIds can't be null")
				.split(","))
				.stream()
				.filter(StringUtils::isNotBlank)
				.collect(toSet()),
				starred);
	}

	public FishingPhoto(FishingPhoto source)
	{
		this(source.photo, source.tripId, source.specimenIds, source.starred);
	}

    public FishingPhoto(Photo photo, String tripId)
    {
        this(photo, tripId, emptySet(), false);
    }

	@Override
	public String getId()
	{
		return photo.getId();
	}

	@Override
	public String getName()
	{
		return photo.getName();
	}

	@Override
	public LocalDateTime getTime()
	{
		return photo.getTime();
	}

	@Override
	public boolean isVideo()
	{
		return photo.isVideo();
	}

	@Override
	public String getThumbnailUrl()
	{
	    return photo.getThumbnailUrl();
	}

	@Override
	public InputStream getThumbnailStream()
	{
		return photo.getThumbnailStream();
	}

    @Override
    public String getImageUrl()
    {
        return photo.getImageUrl();
    }

	@Override
	public InputStream getImageStream()
	{
		return photo.getImageStream();
	}

	@Override
	public InputStream getVideoStream()
	{
		return photo.getVideoStream();
	}

	public String getTripId()
	{
		return tripId;
	}

	String getSpecimenIds()
	{
		return specimenIds.stream().collect(joining(","));
	}

	public boolean containsSpecimen(String specimenId)
	{
		return specimenIds.contains(specimenId);
	}

	public FishingPhoto withSpecimen(String specimenId)
	{
		requireNonNull(specimenId, "specimenId can't be null");
		if (specimenIds.contains(specimenId))
		{
			return this;
		}
		else
		{
			FishingPhoto photo = new FishingPhoto(this);
			photo.specimenIds.add(specimenId);
			return photo;
		}
	}

	public FishingPhoto withoutSpecimen(String specimenId)
	{
		if (!specimenIds.contains(specimenId))
		{
			return this;
		}
		else
		{
			FishingPhoto photo = new FishingPhoto(this);
			photo.specimenIds.remove(specimenId);
			return photo;
		}
	}

    public FishingPhoto withoutSpecimens(Collection<String> specimenIds)
    {
        if (!specimenIds.stream().anyMatch(id -> specimenIds.contains(id)))
        {
            return this;
        }
        else
        {
            FishingPhoto photo = new FishingPhoto(this);
            photo.specimenIds.removeAll(specimenIds);
            return photo;
        }
    }

    /**
     * Gets if this photo is "starred" as a special photo.
     * 
     * @return {@code true} if this photo is starred
     */
    public boolean isStarred()
    {
        return starred;
    }

    /**
     * Gets a copy of this photo, as given {@link #isStarred() starred} value.
     * 
     * @param starred {@code true} to to get this photo as starred
     * @return a new photo with given starred value
     */
    public FishingPhoto asStarred(boolean starred)
    {
        if (starred == this.starred)
        {
            return this;
        }
        else
        {
            return new FishingPhoto(photo, tripId, specimenIds, starred);            
        }
    }

	@Override
	public int hashCode()
	{
		return Objects.hash(getId(), tripId);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FishingPhoto)
		{
			FishingPhoto that = (FishingPhoto) obj;
			return this.getId().equals(that.getId())
				&& this.getTripId().equals(that.getTripId())
				&& this.specimenIds.equals(that.specimenIds)
			    && this.starred == that.starred;
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		ToStringBuilder builder = new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", getId())
				.append("name", getName())
				.append("tripId", getTripId());

		if (!specimenIds.isEmpty())
		{
			builder.append("specimenIds", specimenIds);
		}

		return builder.toString();
	}
}
