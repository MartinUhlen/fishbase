package se.martinuhlen.fishbase.javafx.utils;

import static java.time.LocalTime.MIDNIGHT;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static se.martinuhlen.fishbase.javafx.utils.Constants.DATE_FORMAT;
import static se.martinuhlen.fishbase.javafx.utils.Constants.TIME_FORMAT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Function;

import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import se.martinuhlen.fishbase.domain.Specie;

/**
 * A set of {@link StringConverter converters}.
 * 
 * @author Martin
 */
public final class Converters
{
	private Converters()
	{}

	private static abstract class ReadOnlyStringConverter<T> extends StringConverter<T>
	{
        @Override
        public T fromString(String string)
        {
            throw new IllegalStateException(getClass().getSimpleName() + "is read only");
        }
	}

	public static <T> ReadOnlyStringConverter<T> converter(Function<T, String> function)
	{
	    return new ReadOnlyStringConverter<>()
        {
	        @Override
	        public String toString(T value)
	        {
	            return function.apply(value);
	        }
        };
	}

	public static StringConverter<Specie> specieConverter()
	{
	    return converter(Specie::getName);
	}

	public static StringConverter<Float> lengthConverter()
	{
		return new FloatStringConverter()
		{
			@Override
			public String toString(Float value)
			{
				return value.intValue() <= 0 ? "" : removeEnd(super.toString(value), ".0");
			}

			@Override
			public Float fromString(String value)
			{
				return defaultIfNull(super.fromString(value), 0f);
			}
		};
	}

	public static StringConverter<LocalDate> dateConverter()
	{
		return new LocalDateStringConverter(DATE_FORMAT, DATE_FORMAT);
	}

	public static StringConverter<LocalTime> timeConverter()
	{
		return new LocalTimeStringConverter(TIME_FORMAT, TIME_FORMAT)
		{
			@Override
			public String toString(LocalTime time)
			{
				return MIDNIGHT.equals(time) ? "" : super.toString(time);
			}

			@Override
			public LocalTime fromString(String value)
			{
				return isBlank(value) ? MIDNIGHT : super.fromString(leftPad(value.trim(), 5, "0"));
			}
		};
	}
}
