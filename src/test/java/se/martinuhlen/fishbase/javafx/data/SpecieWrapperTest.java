package se.martinuhlen.fishbase.javafx.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static se.martinuhlen.fishbase.domain.TestData.bream;
import static se.martinuhlen.fishbase.domain.TestData.newSpecie;

import org.junit.jupiter.api.Test;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import se.martinuhlen.fishbase.domain.Specie;

/**
 * Unit tests of {@link SpecieWrapper}.
 */
public class SpecieWrapperTest extends WrapperTestCase<Specie, SpecieWrapper>
{
	@Override
	protected SpecieWrapper createWrapper()
	{
		return new SpecieWrapper(bream(), listener);
	}

	@Test
	public void nameProperty()
	{
		testProperty("name", wrapper::nameProperty, Specie::getName, "A", "B", "C");
	}

	@Test
	public void regWeightProperty()
	{
		testProperty("regWeight", wrapper::regWeightProperty, Specie::getRegWeight, 1000, 2000, 3000);
	}

	@Test
	public void freshWaterProperty()
	{
		testProperty("freshWater", wrapper::freshWaterProperty, Specie::isFreshWater, false, true, false, true);
	}

	@Test
	public void wrapeeEquals()
	{
		assertEquals(bream(), wrapper.getWrapee());
	}

	@Test
	public void removeAllListeners()
	{
		Property<String> property = wrapper.nameProperty();

		InvalidationListener invalidationListener = mock(InvalidationListener.class);
		ChangeListener<String> changeListener = mock(ChangeListener.class);
		wrapper.nameProperty().addListener(invalidationListener);
		property.addListener(changeListener);

		wrapper.removeAllListeners();

		property.setValue("Test");
		verifyZeroInteractions(listener, invalidationListener, changeListener);

		wrapper.setWrapee(newSpecie());
		verifyZeroInteractions(listener, invalidationListener, changeListener);
	}
}
