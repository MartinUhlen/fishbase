package se.martinuhlen.fishbase.utils;

import java.util.NoSuchElementException;

public final class EmptyCursor<T> implements Cursor<T>
{
	private static final EmptyCursor<Object> EMPTY_CURSOR = new EmptyCursor<>();

	@SuppressWarnings("unchecked")
	public static <X> EmptyCursor<X> emptyCursor()
	{
		return (EmptyCursor<X>) EMPTY_CURSOR;
	}

	private EmptyCursor()
	{
	}

	@Override
	public T first()
	{
		return fail();
	}

	@Override
	public boolean isFirst()
	{
		return false;
	}

	@Override
	public boolean hasPrevious()
	{
		return false;
	}

	@Override
	public T previous()
	{
		return fail();
	}

	@Override
	public T peekPrevious()
	{
		return fail();
	}

	@Override
	public boolean hasCurrent()
	{
		return false;
	}

	@Override
	public T current()
	{
		return fail();
	}

	@Override
	public int currentIndex()
	{
		//return fail();
		return -1;
	}

	@Override
	public boolean hasNext()
	{
		return false;
	}

	@Override
	public T next()
	{
		return fail();
	}

	@Override
	public T peekNext()
	{
		return fail();
	}

	@Override
	public T last()
	{
		return fail();
	}

	@Override
	public boolean isLast()
	{
		return false;
	}

	@Override
	public int size()
	{
		return 0;
	}

    @Override
    public Cursor<T> copy()
    {
        return this;
    }

	private <X> X fail()
	{
		throw new NoSuchElementException("Cursor is empty");
	}
}
