package se.martinuhlen.fishbase.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link ListCursor}.
 */
public class ListCursorTest
{
	@Test
	public void indexInTheMiddleAndMoveBackwards()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), 2);
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasCurrent());
		assertTrue(cursor.hasNext());

		assertEquals("2", cursor.current());
		assertEquals(2, cursor.currentIndex());
		assertFalse(cursor.isFirst());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());
		assertFalse(cursor.isLast());

		assertEquals("1", cursor.previous());
		assertEquals(1, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("0", cursor.peekPrevious());
		assertEquals(1, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("0", cursor.previous());
		assertEquals(0, cursor.currentIndex());
		assertTrue(cursor.isFirst());
		assertFalse(cursor.hasPrevious());
		assertTrue(cursor.hasNext());
		assertFalse(cursor.isLast());
	}

	@Test
	public void indexInTheMiddleAndMoveForward()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), 2);
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("2", cursor.current());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("3", cursor.next());
		assertEquals(3, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("4", cursor.peekNext());
		assertEquals(3, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("4", cursor.next());
		assertEquals(4, cursor.currentIndex());
		assertFalse(cursor.isFirst());
		assertTrue(cursor.hasPrevious());
		assertFalse(cursor.hasNext());
		assertTrue(cursor.isLast());
	}

	@Test
	public void firstIndex()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), 0);

		assertEquals("0", cursor.current());
		assertEquals(0, cursor.currentIndex());
		assertFalse(cursor.hasPrevious());
		assertTrue(cursor.hasCurrent());
		assertTrue(cursor.hasNext());

		assertEquals("1", cursor.next());
		assertEquals("1", cursor.current());
		assertEquals(1, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("0", cursor.previous());
		assertEquals("0", cursor.current());
		assertEquals(0, cursor.currentIndex());
		assertFalse(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("1", cursor.peekNext());
		assertEquals(0, cursor.currentIndex());
		assertFalse(cursor.hasPrevious());
		assertTrue(cursor.hasNext());
	}

	@Test
	public void lastIndex()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), 4);

		assertEquals("4", cursor.current());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasCurrent());
		assertFalse(cursor.hasNext());

		assertEquals("3", cursor.previous());
		assertEquals("3", cursor.current());
		assertEquals(3, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertTrue(cursor.hasNext());

		assertEquals("4", cursor.next());
		assertEquals("4", cursor.current());
		assertEquals(4, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertFalse(cursor.hasNext());

		assertEquals("3", cursor.peekPrevious());
		assertEquals(4, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertFalse(cursor.hasNext());
	}

	@Test
	public void beforeFirstElement()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), -1);

		assertFalse(cursor.hasPrevious());
		assertFalse(cursor.hasCurrent());
		assertTrue(cursor.hasNext());

		assertThrows(NoSuchElementException.class, () -> cursor.current());
		assertThrows(NoSuchElementException.class, () -> cursor.previous());

		assertEquals(-1, cursor.currentIndex());
		assertEquals("0", cursor.next());
		assertEquals("0", cursor.current());
		assertEquals(0, cursor.currentIndex());
	}

	@Test
	public void first()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), 2);

		cursor.first();
		assertEquals("0", cursor.current());
		assertEquals(0, cursor.currentIndex());
		assertFalse(cursor.hasPrevious());
		assertTrue(cursor.hasNext());
		assertThrows(NoSuchElementException.class, () -> cursor.previous());
	}

	@Test
	public void last()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0", "1", "2", "3", "4"), 2);

		cursor.last();
		assertEquals("4", cursor.current());
		assertEquals(4, cursor.currentIndex());
		assertTrue(cursor.hasPrevious());
		assertFalse(cursor.hasNext());
		assertThrows(NoSuchElementException.class, () -> cursor.next());
	}

	@Test
	public void oneElement()
	{
		ListCursor<String> cursor = new ListCursor<>(asList("0"), 0);

		assertEquals("0", cursor.current());
		assertFalse(cursor.hasPrevious());
		assertFalse(cursor.hasNext());
	}

	@Test
	public void size()
	{
		assertEquals(1, new ListCursor<>(asList("_"), 0).size());
		assertEquals(3, new ListCursor<>(asList("a", "b", "c"), 2).size());
	}

	@Test
	public void containsAtLeastOneElement()
	{
		assertThrows(RuntimeException.class, () -> new ListCursor<>(null, -1));
		assertThrows(RuntimeException.class, () -> new ListCursor<>(emptySet(), -1));
	}

	@Test
	public void indexIsInRange()
	{
		assertThrows(RuntimeException.class, () -> new ListCursor<>(asList("0, 1, 2"), -2));
		assertThrows(RuntimeException.class, () -> new ListCursor<>(asList("0, 1, 2"), 3));
	}
}
