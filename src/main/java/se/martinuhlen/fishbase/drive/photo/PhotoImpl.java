package se.martinuhlen.fishbase.drive.photo;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PhotoImpl implements Photo
{
	private final String id;
	private final String name;
	private final LocalDateTime time;
	private final boolean video;
	private final String thumbnailUrl;
	private final String contentUrl;
	private final Supplier<InputStream> contentStream;

	public PhotoImpl(String id, String name, LocalDateTime time, boolean video, String thumbnailUrl, String contentUrl, Supplier<InputStream> contentStream)
	{
		this.id = requireNonNull(id);
		this.name = requireNonNull(name);
		this.time = requireNonNull(time);
		this.video = video;
		this.thumbnailUrl = requireNonNull(thumbnailUrl);
		this.contentUrl = requireNonNull(contentUrl);
		this.contentStream = requireNonNull(contentStream);
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public LocalDateTime getTime()
	{
		return time;
	}

	@Override
	public boolean isVideo()
	{
		return video;
	}

	@Override
	public String getThumbnailUrl()
	{
		return thumbnailUrl;
	}

	@Override
	public String getContentUrl()
	{
		return contentUrl;
	}

	@Override
	public InputStream getContentStream()
	{
		return contentStream.get();
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof PhotoImpl)
		{
			return id.equals(((PhotoImpl) obj).id);
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("name", name)
				.toString();
	}
}
