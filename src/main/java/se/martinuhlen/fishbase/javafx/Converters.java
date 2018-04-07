package se.martinuhlen.fishbase.javafx;

import static java.time.LocalTime.MIDNIGHT;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static se.martinuhlen.fishbase.javafx.utils.Constants.DATE_FORMAT;
import static se.martinuhlen.fishbase.javafx.utils.Constants.TIME_FORMAT;

import java.time.LocalDate;
import java.time.LocalTime;

import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import se.martinuhlen.fishbase.domain.Specie;

final class Converters
{
	private Converters()
	{}

	static StringConverter<Specie> specieConverter()
	{
		return new StringConverter<>()
		{
			@Override
			public String toString(Specie specie)
			{
				return specie == null ? "" : specie.getName();
			}

			@Override
			public Specie fromString(String string)
			{
				throw new IllegalStateException("Specie cannot be converted to String");
			}
		};
	}

	static StringConverter<Float> lengthConverter()
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

	static StringConverter<LocalDate> dateConverter()
	{
		return new LocalDateStringConverter(DATE_FORMAT, DATE_FORMAT);
	}

	static StringConverter<LocalTime> timeConverter()
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
				return isBlank(value) ? MIDNIGHT : super.fromString(value);
			}
		};
	}
}
