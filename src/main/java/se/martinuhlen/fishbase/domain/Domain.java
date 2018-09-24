package se.martinuhlen.fishbase.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.stream.Stream;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Abstract class for persisted domain objects.
 *
 * @author Martin
 */
public abstract class Domain<D extends Domain<D>>
{
	private final String id;
	private boolean persisted;

	protected Domain(String id, boolean persisted)
	{
		this.id = requireNonNull(id, "id can't be null");
		this.persisted = persisted;
	}

	/**
	 * Gets the unique ID of this domain object.
	 * 
	 * @return ID that uniquely identifies this object.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Gets whether this object is persisted or only exists in local memory.
	 * 
	 * @return {@code true} if persisted
	 */
	public final boolean isPersisted()
	{
		return persisted;
	}

	/**
	 * @return the opposite of {@link #isPersisted()}
	 */
	public final boolean isNew()
	{
		return !isPersisted();
	}

	/**
	 * Marks this object as {@link #isPersisted() persisted}.
	 */
	public final void markPersisted()
	{
		this.persisted = true;
	}

	/**
	 * Gets a stream of validation errors that prevents this object from being saved.
	 * 
	 * @return stream of validation errors
	 */
	public abstract Stream<String> getValidationErrors();

	/**
	 * Gets a user friendly label of this object.
	 * 
	 * @return user friendly label
	 */
	public abstract String getLabel();

	/**
	 * Creates a <b>deep</b> copy of this object.
	 * 
	 * @return a deep copy of this object
	 */
	public abstract D copy();

	@Override
	public final String toString()
	{
		return ReflectionToStringBuilder.toString(this, SHORT_PREFIX_STYLE);
	}

	@Override
	public final int hashCode()
	{
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj == null || obj.getClass() != this.getClass())
		{
			return false;
		}
		else
		{
			@SuppressWarnings("unchecked")
			D that = (D) obj;
			return equalsId(that)
				&& equalsData(that);
		}
	}

	/**
	 * Checks if this object has the same (equal) ID as given object.
	 * 
	 * @param that object whose ID to compare against this object's ID
	 * @return {@code true} if they have equal ID
	 */
	public final boolean equalsId(D that)
	{
		return that != null
			&& this.getId().equals(that.getId());
	}

	/**
	 * Checks if this object has the same (equal) data as given object.
	 * 
	 * @param that object whose data to compare against this object's data
	 * @return {@code true} if they have equal data
	 */
	protected abstract boolean equalsData(D that);

	/**
	 * Requires a value that is {@code >= 0}, otherwise throws {@link IllegalArgumentException}.
	 * 
	 * @param value to check
	 * @param message in case value is negative
	 * @return given {@code value}
	 */
	protected static int requireNonNegative(int value, String message)
	{
	    return (int) requireNonNegative((float) value, message);
	}

    /**
     * Requires a value that is {@code >= 0.0}, otherwise throws {@link IllegalArgumentException}.
     * 
     * @param value to check
     * @param message in case value is negative
     * @return given {@code value}
     */
    protected static float requireNonNegative(float value, String message)
    {
        if (value < 0.0f)
        {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
