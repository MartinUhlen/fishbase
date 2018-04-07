package se.martinuhlen.fishbase.javafx.data;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import se.martinuhlen.fishbase.domain.Domain;

/**
 * Base test case of {@link Wrapper} implementations.
 *
 * @param <D> type of wrapped domain object
 * @param <W> type of wrapper
 */
public abstract class WrapperTestCase<D extends Domain<D>, W extends Wrapper<D>>
{
	protected InvalidationListener listener;
	protected W wrapper;

	@BeforeEach
	public void setUp()
	{
		listener = mock(InvalidationListener.class);
		wrapper = createWrapper();
	}

	protected abstract W createWrapper();

	@SafeVarargs
	protected final <P> void testProperty(String name, Supplier<Property<P>> propertyGetter, Function<D, P> valueGetter, P... values)
	{
		Property<P> property = propertyGetter.get();
		assertNotNull(property);
		assertSame(property, propertyGetter.get());

		assertSame(wrapper, property.getBean());
		assertEquals(name, property.getName());

		InvalidationListener invalidationListener = mock(InvalidationListener.class);
		ChangeListener<P> changeListener = mock(ChangeListener.class);
		property.addListener(invalidationListener);
		property.addListener(changeListener);

		asList(values).forEach(value ->
		{
			reset(listener, invalidationListener, changeListener);
			P oldValue = property.getValue();
			property.setValue(value);
			assertEquals(value, property.getValue());
			assertEquals(value, valueGetter.apply(wrapper.getWrapee()));
			verify(listener, atLeastOnce()).invalidated(wrapper);
			verify(invalidationListener, atLeastOnce()).invalidated(property);
			verify(changeListener, atLeastOnce()).changed(property, oldValue, value);

			reset(listener, invalidationListener, changeListener);
			property.setValue(property.getValue());
			verifyZeroInteractions(listener, invalidationListener, changeListener);
		});

		SimpleObjectProperty<P> prop = new SimpleObjectProperty<>(values[0]);
		property.bindBidirectional(prop);
		assertEquals(values[0], property.getValue());
		prop.setValue(values[1]);
		assertEquals(values[1], property.getValue());
		property.setValue(values[2]);
		assertEquals(values[2], prop.getValue());
		property.unbindBidirectional(prop);

		property.setValue(values[0]);
		D wrapee = wrapper.getWrapee().copy();
		property.setValue(values[1]);
		reset(listener, invalidationListener, changeListener);
		wrapper.setWrapee(wrapee);
		verify(listener, atLeastOnce()).invalidated(wrapper);
		verify(invalidationListener, atLeastOnce()).invalidated(property);
		verify(changeListener, atLeastOnce()).changed(eq(property), any(), eq(values[0]));
	}
}
