package se.martinuhlen.fishbase.utils;

import java.util.Collection;


public interface Cursor<T>
{
	static <X> Cursor<X> of(Collection<? extends X> elements, int currentIndex)
	{
		if (elements == null || elements.isEmpty())
		{
			return EmptyCursor.emptyCursor();
		}
		else
		{
			return new ListCursor<>(elements, currentIndex);
		}
	}

	T first();

	boolean isFirst();

	boolean hasPrevious();

	T previous();

	T peekPrevious();

	boolean hasCurrent();

	T current();

	int currentIndex();

	boolean hasNext();

	T next();

	T peekNext();

	T last();

	boolean isLast();

	int size();

	default boolean isEmpty()
	{
		return size() == 0;
	}
}
