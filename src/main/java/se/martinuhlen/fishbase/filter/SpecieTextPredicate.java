package se.martinuhlen.fishbase.filter;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import se.martinuhlen.fishbase.domain.Specie;

/**
 * Filters {@link Specie}s by user supplied text.
 */
public class SpecieTextPredicate extends TextPredicate<Specie>
{
	public SpecieTextPredicate(String text)
	{
		super(text);
	}

	@Override
	boolean matchesText(Specie specie, String text)
	{
		return containsIgnoreCase(specie.getName(), text);
	}
}
