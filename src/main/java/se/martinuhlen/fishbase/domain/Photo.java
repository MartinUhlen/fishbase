package se.martinuhlen.fishbase.domain;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Set.copyOf;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Photo extends Domain<Photo>
{
	public static TripBuilder asPersisted(String id)
	{
	    return new Builder(id, true);
	}

	public static TripBuilder asNew(String id)
	{
	    return new Builder(id, false);
	}

	private Photo(String id, boolean persisted, String tripId, Set<String> specimens, String fileName, LocalDateTime time, boolean starred)
	{
		super(id, persisted);
		this.tripId = requireNonBlank(tripId, "tripId cannot be blank");
		this.specimens = copyOf(requireNonNull(specimens, "specimens cannot be blank"));
		this.fileName = requireNonBlank(fileName, "fileName cannot be blank");
		this.time = requireNonNull(time, "time cannot be null");
		this.starred = starred;
	}

	private final String tripId;
	public String getTripId()
	{
		return tripId;
	}

	private final Set<String> specimens;
	public Set<String> getSpecimens()
	{
		return specimens;
	}

	public Photo addSpecimen(String specimenId)
	{
		requireNonNull(specimenId, "specimenId can't be null");
		if (specimens.contains(specimenId))
		{
			return this;
		}
		else
		{
        	HashSet<String> newspecimens = new HashSet<>(this.specimens);
        	newspecimens.add(specimenId);
        	return new Photo(getId(), isPersisted(), tripId, newspecimens, fileName, time, starred);
		}
	}

	public Photo removeSpecimen(String specimenId)
	{
		requireNonNull(specimenId, "specimenId cannot be null");
		return removeSpecimens(Set.of(specimenId));
	}

    public Photo removeSpecimens(Collection<String> specimens)
    {
    	requireNonNull(specimens, "specimens cannot be null");
        if (!specimens.stream().anyMatch(id -> this.specimens.contains(id)))
        {
            return this;
        }
        else
        {
        	HashSet<String> newSpecimens = new HashSet<>(this.specimens);
        	newSpecimens.removeAll(specimens);
        	return new Photo(getId(), isPersisted(), tripId, newSpecimens, fileName, time, starred);
        }
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
				: new Photo(getId(), isPersisted(), tripId, specimens, fileName, time, starred);
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
		return new Photo(getId(), isPersisted(), tripId, specimens, fileName, time, starred);
	}

	@Override
	protected boolean equalsData(Photo that)
	{
		return new EqualsBuilder()
				.append(this.tripId, that.tripId)
				.append(this.specimens, that.specimens)
				.append(this.fileName, that.fileName)
				.append(this.time.truncatedTo(SECONDS), that.time.truncatedTo(SECONDS))
				.append(this.starred, that.starred)
				.isEquals();
	}

	private static class Builder extends Domain.Builder<Photo> implements TripBuilder, SpeciemensBuilder, FileNameBuilder, TimeBuilder, StarredBuilder
	{
		private String tripId = "";
		private Set<String> specimens = Set.of();
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
		public FileNameBuilder specimens(Collection<String> specimens)
		{
			this.specimens = Set.copyOf(specimens);
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
			return new Photo(id, persisted, tripId, specimens, fileName, time, starred);
		}
	}

	public interface TripBuilder
	{
		SpeciemensBuilder tripId(String tripId);
	}

	public interface SpeciemensBuilder
	{
		FileNameBuilder specimens(Collection<String> specimens);
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
