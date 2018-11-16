package se.martinuhlen.fishbase.drive.photo;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.ToStringBuilder;

class PhotoImpl implements Photo
{
	private final String id;
	private final String name;
	private final LocalDateTime time;
	private final boolean video;
	private final PhotoSource thumbnail;
	private final PhotoSource image;
	private final Supplier<InputStream> videoStream;

	PhotoImpl(String id, String name, LocalDateTime time, boolean video, PhotoSource thumbnail, PhotoSource image, Supplier<InputStream> videoStream)
	{
        this.id = requireNonNull(id);
		this.name = requireNonNull(name);
		this.time = requireNonNull(time);
		this.video = video;
		this.thumbnail = requireNonNull(thumbnail);
		this.image = requireNonNull(image);
		this.videoStream = requireNonNull(videoStream);
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
	    return thumbnail.getUrl();
	}

	@Override
	public InputStream getThumbnailStream()
	{
		return thumbnail.getInputStream();
	}

    @Override
    public String getImageUrl()
    {
        checkState(isImage(), "is not an image");
        return image.getUrl();
    }

	@Override
	public InputStream getImageStream()
	{
	    checkState(isImage(), "is not an image");
	    return image.getInputStream();
	}

	@Override
	public InputStream getVideoStream()
	{
	    checkState(isVideo(), "is not a video+");
		return videoStream.get();
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
