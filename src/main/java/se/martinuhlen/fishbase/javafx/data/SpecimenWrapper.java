package se.martinuhlen.fishbase.javafx.data;

import java.time.LocalDate;
import java.time.LocalTime;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleWrapper;
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
		return getProperty("specie", Specimen::getSpecie, Specimen::setSpecie);
	}

	public Property<Integer> weightProperty()
	{
		return getProperty("weight", Specimen::getWeight, Specimen::setWeight);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ReadOnlyProperty<Double> ratioProperty() // FIXME Use ReadableProperty?
	{
		if (ratioProperty == null)
		{
			Property<Specie> specie = specieProperty();
			Property<Integer> weight = weightProperty();
			ReadOnlyDoubleWrapper property = new ReadOnlyDoubleWrapper(getWrapee().getRatio());
			InvalidationListener listener = obs -> property.set(getWrapee().getRatio());
			specie.addListener(listener);
			weight.addListener(listener);
			listener.invalidated(null);
			ratioProperty = (ReadOnlyProperty) property.getReadOnlyProperty();
		}
		return ratioProperty;

	}
	private ReadOnlyProperty<Double> ratioProperty;

	public Property<Float> lengthProperty()
	{
		return getProperty("length", Specimen::getLength, Specimen::setLength);
	}

	public Property<String> locationProperty()
	{
		return getProperty("location", Specimen::getLocation, Specimen::setLocation);
	}

	public Property<LocalDate> dateProperty()
	{
		return getProperty("date", s -> s.getInstant().toLocalDate(), (s, d) -> s.setInstant(d.atTime(s.getInstant().toLocalTime())));
	}

	public Property<LocalTime> timeProperty()
	{
		return getProperty("time", s -> s.getInstant().toLocalTime(), (s, d) -> s.setInstant(d.atDate(s.getInstant().toLocalDate())));
	}

	public Property<String> methodProperty()
	{
		return getProperty("method", Specimen::getMethod, Specimen::setMethod);
	}

	public Property<String> baitProperty()
	{
		return getProperty("bait", Specimen::getBait, Specimen::setBait);
	}

	public Property<String> weatherProperty()
	{
		return getProperty("weather", Specimen::getWeather, Specimen::setWeather);
	}

	public Property<String> textProperty()
	{
		return getProperty("text", Specimen::getText, Specimen::setText);
	}
}
