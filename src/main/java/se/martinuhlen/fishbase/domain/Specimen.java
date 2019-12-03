package se.martinuhlen.fishbase.domain;

import static java.time.LocalDateTime.MIN;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.BAIT;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.LOCATION;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.METHOD;
import static se.martinuhlen.fishbase.domain.AutoCompleteField.WEATHER;
import static se.martinuhlen.fishbase.domain.Specie.EMPTY_SPECIE;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Represents a caught specimen of a certain {@link Specie}.
 *
 * @author Martin
 */
public final class Specimen extends Domain<Specimen>
{
    public static final String SPECIE_IS_REQUIRED = "Specie is required";
    public static final String DATE_IS_REQUIRED = "Date is required";
    public static final String WEIGHT_IS_REQUIRED = "Weight is required";
    public static final String LOCATION_IS_REQUIRED = "Location is required";
    public static final String METHOD_IS_REQUIRED = "Method is required";
    public static final String BAIT_IS_REQUIRED = "Bait is required";
    public static final String WEATHER_IS_REQUIRED = "Weather is required";

	public static TripBuilder asPersisted(String id)
	{
	    return new Builder(id, true);
	}

	public static Specimen asNew(String tripId)
	{
	    return new Builder(UUID.randomUUID().toString(), false)
	            .tripId(tripId)
	            .build();
	}

    private Specimen(String id, boolean persisted, String tripId, Specie specie, int weight, float length, String location, LocalDateTime instant, String method, String bait, String weather, String text)
    {
        super(id, persisted);
        this.tripId = requireNonNull(tripId, "tripId can't be null");
        this.specie = requireNonNull(specie, "specie can't be null");
        this.weight = requireNonNegative(weight, "weight must be >= 0");
        this.length = requireNonNegative(length, "length must be >= 0.0");
        this.location = requireNonNull(location, "location can't be null");
        this.instant = requireNonNull(instant, "instant can't be null");
        this.method = requireNonNull(method, "method can't be null");
        this.bait = requireNonNull(bait, "bait can't be null");
        this.weather = requireNonNull(weather, "weather can't be null");
        this.text = requireNonNull(text, "text can't be null");
    }

	private Specimen(Specimen s)
	{
		this(s.getId(), s.isPersisted(), s.tripId, s.specie, s.weight, s.length, s.location, s.instant, s.method, s.bait, s.weather, s.text);
	}

	//@formatter:off
	private final String tripId;
	public String getTripId(){return tripId;}

	private final Specie specie;
	public Specie getSpecie(){return specie;}
	public Specimen withSpecie(Specie specie){return with(this.specie, specie, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final int weight;
	public int getWeight(){return weight;}
	public Specimen withWeight(int weight){return with(this.weight, weight, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final float length;
	public float getLength(){return length;}
	public Specimen withLength(float length){return with(this.length, length, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final String location;
	public String getLocation(){return location;}
	public Specimen withLocation(String location){return with(this.location, location, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final LocalDateTime instant;
	public LocalDateTime getInstant(){return instant;}
	public Specimen withInstant(LocalDateTime instant){return with(this.instant, instant, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final String method;
	public String getMethod(){return method;}
	public Specimen withMethod(String method){return with(this.method, method, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final String bait;
	public String getBait(){return bait;}
	public Specimen withBait(String bait){return with(this.bait, bait, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final String weather;
	public String getWeather(){return weather;}
	public Specimen withWeather(String weather){return with(this.weather, weather, tripId, specie, weight, length, location, instant, method, bait, weather, text);}

	private final String text;
	public String getText(){return text;}
	public Specimen withText(String text){return with(this.text, text, tripId, specie, weight, length, location, instant, method, bait, weather, text);}
	//@formatter:on

	private <T> Specimen with(T currentValue, T newValue, String tripId, Specie specie, int weight, float length, String location, LocalDateTime instant, String method, String bait, String weather, String text)
	{
	    return currentValue.equals(newValue)
	            ? this
	            : new Specimen(getId(), isPersisted(), tripId, specie, weight, length, location, instant, method, bait, weather, text);
	}
	
	public double getRatio()
	{
		return (double) weight / (double) specie.getRegWeight();
	}

	@Override
	public Stream<String> getValidationErrors()
	{
		return Stream.of(
				specie == EMPTY_SPECIE ? SPECIE_IS_REQUIRED : "",
				instant == LocalDateTime.MIN ? DATE_IS_REQUIRED : "",
				weight <= 0 ? WEIGHT_IS_REQUIRED : "",
				isBlank(location) ? LOCATION_IS_REQUIRED : "",
				isBlank(method) ? METHOD_IS_REQUIRED : "",
				isBlank(bait) ? BAIT_IS_REQUIRED : "",
				isBlank(weather) ? WEATHER_IS_REQUIRED : "")
		.filter(str -> !str.isEmpty());
	}

	@Override
	public String getLabel()
	{
		return specie.getName() + " " + weight + "g";
	}

	public Map<AutoCompleteField, String> getAutoCompletions()
	{
	    return Map.of(
	            LOCATION, location,
	            METHOD, method,
	            BAIT, bait,
	            WEATHER, weather);
	}
	
	@Override
	protected boolean equalsData(Specimen that)
	{
		return new EqualsBuilder()
				.append(this.tripId, that.tripId)
				.append(this.specie, that.specie)
				.append(this.weight, that.weight)
				.append(this.length, that.length)
				.append(this.location, that.location)
				.append(this.instant, that.instant)
				.append(this.method, that.method)
				.append(this.bait, that.bait)
				.append(this.weather, that.weather)
				.append(this.text, that.text)
				.isEquals();
	}

	@Override
	public Specimen copy()
	{
		return new Specimen(this);
	}

	public Specimen copyAsNew()
	{
	    return new Specimen(UUID.randomUUID().toString(), false, tripId, specie, weight, length, location, instant, method, bait, weather, text);
	}

	private static class Builder extends Domain.Builder<Specimen> implements TripBuilder, SpecieBuilder, WeightBuilder, LengthBuilder, LocationBuilder, InstantBuilder, MethodBuilder, BaitBuilder, WeatherBuilder, TextBuilder
	{
        private String tripId;
        private Specie specie = EMPTY_SPECIE;
        private int weight;
        private float length;
        private String location = "";
        private LocalDateTime instant = MIN;
        private String method = "";
        private String bait = "";
        private String weather = "";
        private String text = "";

        Builder(String id, boolean persisted)
        {
        	super(id, persisted);
        }

        @Override
        public Builder tripId(String tripId)
        {
            this.tripId = tripId;
            return this;
        }

        @Override
        public WeightBuilder specie(Specie specie)
        {
            this.specie = specie;
            return this;
        }

        @Override
        public LengthBuilder weight(int weight)
        {
            this.weight = weight;
            return this;
        }

        @Override
        public LocationBuilder length(float length)
        {
            this.length = length;
            return this;
        }

        @Override
        public InstantBuilder location(String location)
        {
            this.location = location;
            return this;
        }

        @Override
        public MethodBuilder instant(LocalDateTime instant)
        {
            this.instant = instant;
            return this;
        }

        @Override
        public BaitBuilder method(String method)
        {
            this.method = method;
            return this;
        }

        @Override
        public WeatherBuilder bait(String bait)
        {
            this.bait = bait;
            return this;
        }

        @Override
        public TextBuilder weather(String weather)
        {
            this.weather = weather;
            return this;
        }

        @Override
        public Specimen text(String text)
        {
            this.text = text;
            return build();
        }

        Specimen build()
        {
            return new Specimen(id, persisted, tripId, specie, weight, length, location, instant, method, bait, weather, text);
        }
	}

	public interface TripBuilder
	{
	    SpecieBuilder tripId(String tripId);
	}

    public interface SpecieBuilder
    {
        WeightBuilder specie(Specie specie);
    }

    public interface WeightBuilder
    {
        LengthBuilder weight(int weight);
    }

    public interface LengthBuilder
    {
        LocationBuilder length(float length);
    }

    public interface LocationBuilder
    {
        InstantBuilder location(String location);
    }

    public interface InstantBuilder
    {
        MethodBuilder instant(LocalDateTime instant);
    }

    public interface MethodBuilder
    {
        BaitBuilder method(String method);
    }

    public interface BaitBuilder
    {
        WeatherBuilder bait(String bait);
    }

    public interface WeatherBuilder
    {
        TextBuilder weather(String weather);
    }

    public interface TextBuilder
    {
        Specimen text(String text);
    }
}
