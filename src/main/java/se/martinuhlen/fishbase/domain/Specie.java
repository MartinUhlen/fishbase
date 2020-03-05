package se.martinuhlen.fishbase.domain;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Represents a fish specie.
 *
 * @author Martin
 */
public final class Specie extends Domain<Specie>
{
	public static final Specie EMPTY_SPECIE = new Builder("#emptySpecie", false).build();

	public static NameBuilder asPersisted(String id)
	{
	    return new Builder(id, true);
	}

	public static Specie asNew()
	{
		return new Builder(UUID.randomUUID().toString(), false).build();
	}

    private Specie(String id, boolean persisted, String name, int regWeight, boolean freshWater)
    {
        super(id, persisted);
        this.name = requireNonNull(name, "name can't be null");
        this.regWeight = requireNonNegative(regWeight, "regWeight must be >= 0");
        this.freshWater = freshWater;
    }

	//@formatter:off
	private final String name;
	public String getName(){return name;}
	public Specie withName(String name){return with(this.name.equals(name), name, regWeight, freshWater);}

	private final int regWeight;
	public int getRegWeight(){return regWeight;}
	public Specie withRegWeight(int regWeight){return with(this.regWeight == regWeight, name, regWeight, freshWater);}

	private final boolean freshWater;
	public boolean isFreshWater(){return freshWater;}
	public Specie withFreshWater(boolean freshWater){return with(this.freshWater == freshWater, name, regWeight, freshWater);}
	//@formatter:off

	private <T> Specie with(boolean equals, String name, int regWeight, boolean freshWater)
	{
	    return equals
	            ? this
	            : new Specie(getId(), isPersisted(), name, regWeight, freshWater);
	}

	@Override
	public Stream<String> getValidationErrors()
	{
		return Stream.of(
					isBlank(name) ? "Name" : "",
					regWeight <= 0 ? "Registration weight" : "")
			.filter(str -> !str.isEmpty())
			.map(str -> str + " is required");
	}

	@Override
	public String getLabel()
	{
		return name;
	}

	@Override
	protected boolean equalsData(Specie that)
	{
		return new EqualsBuilder()
				.append(this.name, that.name)
				.append(this.regWeight, that.regWeight)
				.append(this.freshWater, that.freshWater)
				.isEquals();
	}

	@Override
	public Specie copy()
	{
		return new Specie(getId(), isPersisted(), name, regWeight, freshWater);
	}

	private static class Builder extends Domain.Builder<Specie> implements NameBuilder, RegWeightBuilder, FreshWaterBuilder
	{
        private String name = "";
        private int regWeight;
        private boolean freshWater = true;

	    Builder(String id, boolean persisted)
        {
	    	super(id, persisted);
        }

	    @Override
	    public RegWeightBuilder name(String name)
	    {
	        this.name = name;
	        return this;
	    }

        @Override
        public FreshWaterBuilder regWeight(int regWeight)
        {
            this.regWeight = regWeight;
            return this;
        }

        @Override
        public Specie freshWater(boolean freshWater)
        {
            this.freshWater = freshWater;
            return build();
        }

        private Specie build()
        {
            return new Specie(id, persisted, name, regWeight, freshWater);
        }
	}

	public interface NameBuilder
	{
	    RegWeightBuilder name(String name);
	}

    public interface RegWeightBuilder
    {
        FreshWaterBuilder regWeight(int regWeight);
    }

    public interface FreshWaterBuilder
    {
        Specie freshWater(boolean freshWater);
    }
}
