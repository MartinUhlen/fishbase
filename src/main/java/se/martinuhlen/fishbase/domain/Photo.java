package se.martinuhlen.fishbase.domain;

import static java.util.Set.copyOf;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Photo extends Domain<Photo> // FIXME Rename to Media? And a media can either be a photo or a video.
{
	public static TripBuilder asPersisted(String id)
	{
	    return new Builder(id, true);
	}

	public static TripBuilder asNew(String id)
	{
	    return new Builder(id, false);
	}

	private Photo(String id, boolean persisted, String tripId, Set<String> specimenIds, String fileName, LocalDateTime time, boolean starred)
	{
		super(id, persisted);
		this.tripId = requireNonBlank(tripId, "tripId cannot be blank");
		this.specimenIds = copyOf(requireNonNull(specimenIds, "specimenIds cannot be blank"));
		this.fileName = requireNonBlank(fileName, "fileName cannot be blank");
		this.time = requireNonNull(time, "time cannot be null");
		this.starred = starred;
	}

	private final String tripId;
	public String getTripId()
	{
		return tripId;
	}

	private final Set<String> specimenIds;
	public Set<String> getSpecimenIds()
	{
		return specimenIds;
	}

	private final String fileName;
	public String getFileName()
	{
		return fileName;
	}

	private LocalDateTime time;
	public LocalDateTime getTime()
	{
		return time;
	}

	private final boolean starred;
	public boolean isStarred()
	{
		return starred;
	}
	public Photo withStarred(boolean starred)
	{
		return starred == this.starred
				? this
				: new Photo(getId(), isPersisted(), tripId, specimenIds, fileName, time, starred);
	}

	@Override
	public Stream<String> getValidationErrors()
	{
		return Stream.empty();
	}

	@Override
	public String getLabel()
	{
		return fileName;
	}

	@Override
	public Photo copy()
	{
		return new Photo(getId(), isPersisted(), tripId, specimenIds, fileName, time, starred);
	}

	@Override
	protected boolean equalsData(Photo that)
	{
		return new EqualsBuilder()
				.append(this.tripId, that.tripId)
				.append(this.specimenIds, that.specimenIds)
				.append(this.fileName, that.fileName)
				.append(this.time, that.time)
				.append(this.starred, that.starred)
				.isEquals();
	}

	private static class Builder extends Domain.Builder<Photo> implements TripBuilder, SpeciemensBuilder, FileNameBuilder, TimeBuilder, StarredBuilder
	{
		private String tripId = "";
		private Set<String> specimenIds = Set.of();
		private String fileName = "";
		private LocalDateTime time;

		Builder(String id, boolean persisted)
		{
			super(id, persisted);
		}

		@Override
		public SpeciemensBuilder tripId(String tripId)
		{
			this.tripId = tripId;
			return this;
		}

		@Override
		public FileNameBuilder specimenIds(Collection<String> specimenIds)
		{
			this.specimenIds = Set.copyOf(specimenIds);
			return this;
		}

		@Override
		public TimeBuilder fileName(String fileName)
		{
			this.fileName = fileName;
			return this;
		}

		@Override
		public StarredBuilder time(LocalDateTime time)
		{
			this.time = time;
			return this;
		}
		
		@Override
		public Photo starred(boolean starred)
		{
			return new Photo(id, persisted, tripId, specimenIds, fileName, time, starred);
		}
	}

	public interface TripBuilder
	{
		SpeciemensBuilder tripId(String tripId);
	}

	public interface SpeciemensBuilder
	{
		FileNameBuilder specimenIds(Collection<String> specimenIds);
	}

	public interface FileNameBuilder
	{
		TimeBuilder fileName(String fileName);
	}

	public interface TimeBuilder
	{
		StarredBuilder time(LocalDateTime time);
	}

	public interface StarredBuilder
	{
		Photo starred(boolean starred);
	}
}
