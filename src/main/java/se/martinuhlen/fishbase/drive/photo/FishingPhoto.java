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

import com.google.common.annotations.VisibleForTesting;

public class FishingPhoto implements Photo
{
	private final Photo photo;
	private final String tripId;
	private final Set<String> specimenIds;

	private FishingPhoto(Photo photo, String tripId, Set<String> specimenIds)
	{
		this.photo = requireNonNull(photo, "photo can't be null");
		this.tripId = requireNonNull(tripId, "tripId can't be null");
		this.specimenIds = new HashSet<>(specimenIds);
	}

	FishingPhoto(Photo photo, String tripId, String specimenIds)
	{
		this(photo, tripId, asList(requireNonNull(specimenIds, "specimenIds can't be null")
				.split(","))
				.stream()
				.filter(StringUtils::isNotBlank)
				.collect(toSet()));
	}

	@VisibleForTesting
	public FishingPhoto(Photo photo, String tripId)
	{
		this(photo, tripId, emptySet());
	}

	public FishingPhoto(FishingPhoto source)
	{
		this(source.photo, source.tripId, source.specimenIds);
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
	public String getContentUrl()
	{
		return photo.getContentUrl();
	}

	@Override
	public InputStream getContentStream()
	{
		return photo.getContentStream();
	}

	String getTripId()
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
				&& this.specimenIds.equals(that.specimenIds);
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
