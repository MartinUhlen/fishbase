package se.martinuhlen.fishbase.javafx.utils;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class Images
{
	private Images()
	{
	}

	public static ImageView getImageView16(String name)
	{
		return getImageView(name, ImageSize.SIZE_16);
	}

	public static ImageView getImageView32(String name)
	{
		return getImageView(name, ImageSize.SIZE_32);
	}

	public static ImageView getImageView(String name, ImageSize size)
	{
		return new ImageView(getImage(name, size));
	}

	private static Image getImage(String name, ImageSize size)
	{
		return new Image(getImageUrl(name, size));
	}

	public static List<Image> getImages(String name)
	{
		return Stream.of(ImageSize.values())
			.map(size -> getImageUrl(name, size))
			.map(url -> Images.class.getResourceAsStream(url))
			.filter(is -> is != null)
			.map(is -> new Image(is))
			.collect(toList());
	}

	private static String getImageUrl(String name, ImageSize size)
	{
		return "/images/" + size.getSize() + "/" + name;
	}
}
