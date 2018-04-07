package se.martinuhlen.fishbase.filter;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.stream.Stream;

import se.martinuhlen.fishbase.domain.Specimen;

/**
 * Filters {@link Specimen}s by user supplied text.
 */
public class SpecimenTextPredicate extends TextPredicate<Specimen>
{
	public SpecimenTextPredicate(String text)
	{
		super(text);
	}

	@Override
	boolean matchesText(Specimen s, String text)
	{
		return Stream.of(
				s.getSpecie().getName(),
				s.getLocation(),
				s.getInstant().toString(),
				s.getMethod(),
				s.getBait(),
				s.getWeather(),
				s.getText())
						.anyMatch(str -> containsIgnoreCase(str, text));
	}
}
