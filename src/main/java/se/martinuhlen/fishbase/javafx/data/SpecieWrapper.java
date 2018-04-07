package se.martinuhlen.fishbase.javafx.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import se.martinuhlen.fishbase.domain.Specie;

public class SpecieWrapper extends Wrapper<Specie>
{
	public SpecieWrapper(Specie wrapee, InvalidationListener listener)
	{
		super(wrapee, listener);
	}

	public SpecieWrapper(Specie wrapee)
	{
		super(wrapee);
	}

	public Property<String> nameProperty()
	{
		return getProperty("name", Specie::getName, Specie::setName);
	}

	public Property<Integer> regWeightProperty()
	{
		return getProperty("regWeight", Specie::getRegWeight, Specie::setRegWeight);
	}

	public BooleanProperty freshWaterProperty()
	{
		if (freshWaterProperty == null)
		{
			freshWaterProperty = new SimpleBooleanProperty(this, "freshWater");
			freshWaterProperty.bindBidirectional(getProperty("freshWater", Specie::isFreshWater, Specie::setFreshWater));
		}
		return freshWaterProperty;
	}
	private BooleanProperty freshWaterProperty;
}
