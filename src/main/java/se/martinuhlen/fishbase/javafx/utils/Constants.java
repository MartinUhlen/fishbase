package se.martinuhlen.fishbase.javafx.utils;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.jar.Manifest;

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

	public static final LocalDateTime BUILD_TIME = readBuildTime();

    private static LocalDateTime readBuildTime()
    {
        try
        {
            URL resource = Constants.class.getResource("/META-INF/MANIFEST.MF");
            try (InputStream is = resource.openStream())
            {
                String buildValue = new Manifest(is)
                        .getMainAttributes()
                        .getValue("Build-Time");

                if (buildValue == null)
                {
                    return LocalDateTime.now();
                }

                return ZonedDateTime.parse(buildValue)
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
