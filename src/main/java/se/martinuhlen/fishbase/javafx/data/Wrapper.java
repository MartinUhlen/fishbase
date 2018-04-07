package se.martinuhlen.fishbase.javafx.data;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import se.martinuhlen.fishbase.domain.Domain;
import se.martinuhlen.fishbase.utils.Logger;

/**
 * Abstract JavaFX bean that wraps an object, the wrapee, and provides properties for it.
 *
 * @param <D> type of wrapped domain object
 */
public abstract class Wrapper<D extends Domain<D>> implements Observable
{
	private final Logger logger = Logger.getLogger(getClass());
	private final Map<String, ReadableProperty<?>> properties;
	private final List<InvalidationListener> listeners;

	protected D initialWrapee;
	protected D currentWrapee;
	protected boolean isSettingWrapee;

	Wrapper(D wrapee)
	{
		this.properties = new HashMap<>();
		this.listeners = new LinkedList<>();
		setWrapee(wrapee);
	}

	Wrapper(D wrapee, InvalidationListener listener)
	{
		this(wrapee);
		addListener(listener);
	}

	public boolean hasChanges()
	{
		// TODO Unit tests
		// TODO Add hasChangesProperty() and bind to saveAction?
		return !currentWrapee.equals(initialWrapee);
	}

	public void setWrapee(D wrapee)
	{
		requireNonNull(wrapee, "wrapee can't be null");
		this.initialWrapee = wrapee.copy();
		this.currentWrapee = wrapee.copy();
		isSettingWrapee = true;
		properties.values().forEach(p -> p.notifyListeners());
		isSettingWrapee = false;
		notifyListeners();
	}

	public D getWrapee()
	{
		return currentWrapee;
	}

	@Override
	public void addListener(InvalidationListener listener)
	{
		listeners.add(requireNonNull(listener, "listener can't be null"));
	}

	@Override
	public void removeListener(InvalidationListener listener)
	{
		listeners.remove(listener);
	}

	public void removeAllListeners()
	{
		listeners.clear();
		properties.values().forEach(p -> p.removeAllListeners());
	}

	<V> ReadOnlyProperty<V> getProperty(String name, Function<D, V> getter)
	{
		return getProperty(name, () -> new ReadableProperty<>(name, getter));
	}

	<V> Property<V> getProperty(String name, Function<D, V> getter, BiConsumer<D, V> setter)
	{
		return getProperty(name, () -> new WritableProperty<>(name, getter, setter));
	}

	@SuppressWarnings("unchecked")
	private <P extends ReadableProperty<?>> P getProperty(String name, Supplier<P> creator)
	{
		return (P) properties.computeIfAbsent(name, n ->
		{
			P property = creator.get();
			property.addListener(obs -> notifyListeners());
			return property;
		});
	}

	protected void notifyListeners()
	{
		if (!isSettingWrapee)
		{
			listeners.forEach(l -> l.invalidated(this));
		}
	}

	private class ReadableProperty<V> implements ReadOnlyProperty<V> // FIXME Renamw to WritableProperty + add ReadableProperty
	{
		private final List<InvalidationListener> invalidationListeners = new LinkedList<>();
		private final List<ChangeListener<? super V>> changeListeners = new LinkedList<>();

		final String name;
		final Function<D, V> getter;

		ReadableProperty(String name, Function<D, V> getter)
		{
			this.name = name;
			this.getter = getter;
		}

		@Override
		public void addListener(InvalidationListener listener)
		{
			requireNonNull(listener);
			invalidationListeners.add(listener);
		}

		@Override
		public void removeListener(InvalidationListener listener)
		{
			requireNonNull(listener);
			invalidationListeners.remove(listener);
		}

		@Override
		public void addListener(ChangeListener<? super V> listener)
		{
			requireNonNull(listener);
			changeListeners.add(listener);
		}

		@Override
		public void removeListener(ChangeListener<? super V> listener)
		{
			requireNonNull(listener);
			changeListeners.remove(listener);
		}

		void removeAllListeners()
		{
			invalidationListeners.clear();
			changeListeners.clear();
		}

		@Override
		public V getValue()
		{
			return getter.apply(currentWrapee);
		}

		void notifyListeners()
		{
			notifyListeners(null, getValue());
		}

		void notifyListeners(V oldValue, V newValue)
		{
			invalidationListeners.forEach(l -> l.invalidated(this));
			changeListeners.forEach(l -> l.changed(this, oldValue, newValue));
		}

		@Override
		public Object getBean()
		{
			return Wrapper.this;
		}

		@Override
		public String getName()
		{
			return name;
		}
	}

	private class WritableProperty<V> extends ReadableProperty<V> implements Property<V>
	{
		private final BiConsumer<D, V> setter;

		WritableProperty(String name, Function<D, V> getter, BiConsumer<D, V> setter)
		{
			super(name, getter);
			this.setter = setter;
		}

		@Override
		public void setValue(V value)
		{
			V oldValue = getValue();
			if (!Objects.equals(oldValue, value))
			{
				logger.log("Changing '" + name + "' from '" + oldValue + "' to '" + value + "'");
				setter.accept(currentWrapee, value);
				notifyListeners(oldValue, value);
			}
		}

		@Override
		public void bindBidirectional(Property<V> property)
		{
			Bindings.bindBidirectional(this, property);
		}

		@Override
		public void unbindBidirectional(Property<V> property)
		{
			Bindings.unbindBidirectional(this, property);
		}

		@Override
		public void bind(ObservableValue<? extends V> value)
		{
			throw new UnsupportedOperationException("Uni-directional binding is not supported by " + getClass().getName());
		}

		@Override
		public void unbind()
		{
		}

		@Override
		public boolean isBound()
		{
			return false;
		}
	}
}
