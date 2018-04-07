package se.martinuhlen.fishbase.javafx.utils;

public enum ImageSize
{
	SIZE_16(16),
	SIZE_32(32),
	SIZE_64(64),
	SIZE_256(256);

	private final int size;

	private ImageSize(int size)
	{
		this.size = size;
	}

	int getSize()
	{
		return size;
	}
}
