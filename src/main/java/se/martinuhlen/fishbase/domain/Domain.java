package se.martinuhlen.fishbase.domain;

import static org.apache.commons.lang3.StringUtils.isBlank;
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
	public final String getId()
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
	// FIXME Move this to Builder. Domain is always valid. Use Builder in UI.
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
			&& this.getId().equals(that.getId())
			&& this.isPersisted() == that.isPersisted();
	}

	/**
	 * Checks if this object has the same (equal) data as given object.
	 * 
	 * @param that object whose data to compare against this object's data
	 * @return {@code true} if they have equal data
	 */
	protected abstract boolean equalsData(D that);

	/**
	 * Checks that a value is not {@code null}.
	 * 
	 * @param <T> type of value to check
	 * @param value to check
	 * @param message in case value is blank
	 * @return {@code value} when not {@code null}
	 * @throws IllegalArgumentException if value is {@code null}
	 */
	protected static <T> T requireNonNull(T value, String message)
	{
		if (value == null)
		{
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	/**
	 * Checks that string is not {@code null}, empty or only contain whitespace.
	 * @param value to check
	 * @param message in case value is blank
	 * @return value when not blank
	 * @throws IllegalArgumentException if value is blank
	 */
	protected static String requireNonBlank(String value, String message)
	{
		if (isBlank(value))
		{
			throw new IllegalArgumentException(message);
		}
		return value;
	}
	
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

    protected abstract static class Builder<D extends Domain<D>>
    {
        protected final String id;
        protected final boolean persisted;

        protected Builder(String id, boolean persisted)
        {
            this.id = requireNonBlank(id, "id cannot be blank");
            this.persisted = persisted;
        }
    }
}
