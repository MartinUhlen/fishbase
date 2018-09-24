package se.martinuhlen.fishbase.filter;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.stream.Stream;

import se.martinuhlen.fishbase.domain.Specimen;

/**
 * Filters {@link Specimen}s by user supplied text.
 */
public class SpecimenTextPredicate extends TextPredicate<Specimen>
{
    private final SpecieTextPredicate speciePredicate;

	public SpecimenTextPredicate(String text)
	{
		super(text);
		this.speciePredicate = new SpecieTextPredicate(text);
	}

	@Override
	boolean matchesText(Specimen s, String text)
	{
		return speciePredicate.test(s.getSpecie())
	        || Stream.of(
    			s.getLocation(),
    			s.getInstant().toString(),
    			s.getMethod(),
    			s.getBait(),
    			s.getWeather(),
    			s.getText())
    					.anyMatch(str -> containsIgnoreCase(str, text));
	}
}
