package se.martinuhlen.fishbase.javafx.data;

import java.time.LocalDate;
import java.time.LocalTime;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;

public class SpecimenWrapper extends Wrapper<Specimen>
{
	public SpecimenWrapper(Specimen wrapee, InvalidationListener listener)
	{
		super(wrapee, listener);
	}

	public SpecimenWrapper(Specimen wrapee)
	{
		super(wrapee);
	}

	public Property<Specie> specieProperty()
	{
		return getProperty("specie", Specimen::getSpecie, Specimen::withSpecie);
	}

	public Property<Integer> weightProperty()
	{
		return getProperty("weight", Specimen::getWeight, Specimen::withWeight);
	}

	public ReadOnlyProperty<Double> ratioProperty()
	{
	    ReadOnlyProperty<Double> ratio = getProperty("ratio", Specimen::getRatio, specieProperty(), weightProperty());
	    return ratio;
	}

	public Property<Float> lengthProperty()
	{
		return getProperty("length", Specimen::getLength, Specimen::withLength);
	}

	public Property<String> locationProperty()
	{
		return getProperty("location", Specimen::getLocation, Specimen::withLocation);
	}

	public Property<LocalDate> dateProperty()
	{
		return getProperty("date", s -> s.getInstant().toLocalDate(), (s, d) -> s.withInstant(d.atTime(s.getInstant().toLocalTime())));
	}

	public Property<LocalTime> timeProperty()
	{
		return getProperty("time", s -> s.getInstant().toLocalTime(), (s, d) -> s.withInstant(d.atDate(s.getInstant().toLocalDate())));
	}

	public Property<String> methodProperty()
	{
		return getProperty("method", Specimen::getMethod, Specimen::withMethod);
	}

	public Property<String> baitProperty()
	{
		return getProperty("bait", Specimen::getBait, Specimen::withBait);
	}

	public Property<String> weatherProperty()
	{
		return getProperty("weather", Specimen::getWeather, Specimen::withWeather);
	}

	public Property<String> textProperty()
	{
		return getProperty("text", Specimen::getText, Specimen::withText);
	}
}
