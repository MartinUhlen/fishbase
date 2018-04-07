package se.martinuhlen.fishbase.filter;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Base predicate to filter elements by text from user input.
 *
 * @param <T> type of elements being filtered
 */
abstract class TextPredicate<T> implements Predicate<T>
{
	private final String text;
	private final String[] parts;

	TextPredicate(String text)
	{
		this.text = defaultIfBlank(text, "").toLowerCase().trim();
		this.parts = StringUtils.split(text);
	}

	@Override
	public final boolean test(T obj)
	{
		if (text.isEmpty())
		{
			return true;
		}
		else
		{
			return Stream.of(parts)
					.allMatch(str -> matchesText(obj, str));
		}
	}

	abstract boolean matchesText(T obj, String text);
}
