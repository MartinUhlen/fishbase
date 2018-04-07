package se.martinuhlen.fishbase.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.stream.Stream;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public abstract class Domain<D extends Domain<D>>
{
	private final String id;
	private boolean persisted;

	protected Domain(String id, boolean persisted)
	{
		this.id = requireNonNull(id, "id can't be null");
		this.persisted = persisted;
	}

	public String getId()
	{
		return id;
	}

	public final boolean isPersisted()
	{
		return persisted;
	}

	public final boolean isNew()
	{
		return !isPersisted();
	}

	public final void markPersisted()
	{
		this.persisted = true;
	}

	public abstract Stream<String> getValidationErrors();

	public abstract String getLabel();

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

	public final boolean equalsId(D that)
	{
		return that != null
			&& this.getId().equals(that.getId());
	}

	protected abstract boolean equalsData(D that);
}
