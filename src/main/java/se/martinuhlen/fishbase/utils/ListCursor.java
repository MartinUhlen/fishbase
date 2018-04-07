package se.martinuhlen.fishbase.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

class ListCursor<T> implements Cursor<T>
{
	private final List<T> list;
	private int index;

	ListCursor(Collection<? extends T> elements, int currentIndex)
	{
		requireNonNull(elements, "elements cannot be null");
		checkArgument(!elements.isEmpty(), "elements cannot be empty");
		checkArgument(currentIndex >= -1 && currentIndex < elements.size());
		this.list = new ArrayList<>(elements);
		this.index = currentIndex;
	}

	@Override
	public T first()
	{
		index = 0;
		return current();
	}

	@Override
	public boolean isFirst()
	{
		return index == 0;
	}

	@Override
	public boolean hasPrevious()
	{
		return index > 0;
	}

	@Override
	public T previous()
	{
		int previousIndex = index - 1;
		checkElementAt(previousIndex);
		index = previousIndex;
		return current();
	}

	@Override
	public T peekPrevious()
	{
		int previousIndex = index - 1;
		checkElementAt(previousIndex);
		return list.get(previousIndex);
	}

	@Override
	public boolean hasCurrent()
	{
		return index >= 0;
	}

	@Override
	public T current()
	{
		checkElementAt(index);
		return list.get(index);
	}

	@Override
	public int currentIndex()
	{
		//checkElementAt(index);
		return index;
	}

	@Override
	public boolean hasNext()
	{
		return index < (list.size() - 1);
	}

	@Override
	public T next()
	{
		int nextIndex = index + 1;
		checkElementAt(nextIndex);
		index = nextIndex;
		return current();
	}

	@Override
	public T peekNext()
	{
		int nextIndex = index + 1;
		checkElementAt(nextIndex);
		return list.get(nextIndex);
	}

	@Override
	public T last()
	{
		index = list.size() - 1;
		return current();
	}

	@Override
	public boolean isLast()
	{
		return index == size() - 1;
	}

	@Override
	public int size()
	{
		return list.size();
	}

	private void checkElementAt(int i)
	{
		if (i < 0 || i >= list.size())
		{
			throw new NoSuchElementException();
		}
	}
}
