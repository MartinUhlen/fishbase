package se.martinuhlen.fishbase.javafx.utils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public final class Constants
{
	private Constants()
	{
	}

	public static final String RIGHT_ALIGNMENT = "-fx-alignment: CENTER-RIGHT;";

	public static final DateTimeFormatter DATE_FORMAT =  DateTimeFormatter.ISO_LOCAL_DATE;

	public static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
			.appendPattern("HH:mm")
			.toFormatter();

	public static final DateTimeFormatter DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
			.append(DATE_FORMAT)
			.appendLiteral(' ')
			.append(TIME_FORMAT).toFormatter();
}
