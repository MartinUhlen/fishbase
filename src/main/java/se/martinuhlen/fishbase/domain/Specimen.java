package se.martinuhlen.fishbase.domain;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static se.martinuhlen.fishbase.domain.Specie.EMPTY_SPECIE;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

public final class Specimen extends Domain<Specimen>
{
	public static Specimen asPersisted(String id, String tripId)
	{
		return new Specimen(id, tripId, true);
	}

	public static Specimen asNew(String tripId)
	{
		return new Specimen(UUID.randomUUID().toString(), tripId, false);
	}

	private Specimen(String id, String tripId, boolean persisted)
	{
		super(id, persisted);
		this.tripId = requireNonNull(tripId, "tripId can't be null");
	}

	private Specimen(Specimen s)
	{
		super(s.getId(), s.isPersisted());
		this.tripId = s.tripId;
		copyProperties(s);
	}

	private void copyProperties(Specimen s)
	{
		this.specie = new Specie(s.specie);
		this.weight = s.weight;
		this.length = s.length;
		this.location = s.location;
		this.instant = s.instant;
		this.method = s.method;
		this.bait = s.bait;
		this.weather = s.weather;
		this.text = s.text;
	}

	//@formatter:off
	private final String tripId;
	public String getTripId(){return tripId;}

	private Specie specie = EMPTY_SPECIE;
	public Specie getSpecie(){return specie;}
	public Specimen setSpecie(Specie specie){this.specie = requireNonNull(specie); return this;}

	private int weight;
	public int getWeight(){return weight;}
	public Specimen setWeight(int weight){this.weight = weight;	return this;}

	private float length;
	public float getLength(){return length;}
	public Specimen setLength(float length){this.length = length; return this;}

	private String location = "";
	public String getLocation(){return location;}
	public Specimen setLocation(String location){this.location = requireNonNull(location); return this;}

	private LocalDateTime instant = LocalDateTime.MIN;
	public LocalDateTime getInstant(){return instant;}
	public Specimen setInstant(LocalDateTime instant){this.instant = requireNonNull(instant); return this;}

	private String method = "";
	public String getMethod(){return method;}
	public Specimen setMethod(String method){this.method = requireNonNull(method); return this;}

	private String bait = "";
	public String getBait(){return bait;}
	public Specimen setBait(String bait){this.bait = requireNonNull(bait); return this;}

	private String weather = "";
	public String getWeather(){return weather;}
	public Specimen setWeather(String weather){this.weather = requireNonNull(weather); return this;}

	private String text = "";
	public String getText(){return text;}
	public Specimen setText(String text){this.text = requireNonNull(text); return this;}
	//@formatter:on

	public double getRatio()
	{
		return (double) weight / (double) specie.getRegWeight();
	}

	@Override
	public Stream<String> getValidationErrors()
	{
		return Stream.of(
				specie == EMPTY_SPECIE ? "Specie" : "",
				instant == LocalDateTime.MIN ? "Date" : "",
				weight <= 0 ? "Weight" : "",
				isBlank(location) ? "Location" : "",
				isBlank(method) ? "Method" : "",
				isBlank(bait) ? "Bait" : "",
				isBlank(weather) ? "Weather" : "")
		.filter(str -> !str.isEmpty())
		.map(str -> str + " is required");
	}

	@Override
	public String getLabel()
	{
		return specie.getName() + " " + weight + "g";
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
		Specimen copy = Specimen.asNew(tripId);
		copy.copyProperties(this);
		return copy;
	}
}
