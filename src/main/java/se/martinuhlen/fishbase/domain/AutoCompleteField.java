package se.martinuhlen.fishbase.domain;

/**
 * A set of data fields that could be auto-completed when entered by the user.
 *
 * @author Martin
 */
public enum AutoCompleteField
{
    /**
     * @see Specimen#withLocation(String)
     */
    LOCATION,

    /**
     * @see Specimen#withMethod(String)
     */    
    METHOD,

    /**
     * @see Specimen#withBait(String)
     */
    BAIT,

    /**
     * @see Specimen#withWeather(String)
     */
    WEATHER
}
