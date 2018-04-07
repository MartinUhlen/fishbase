package se.martinuhlen.fishbase.domain;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Specie extends Domain<Specie>
{
	public static final Specie EMPTY_SPECIE = new ImmutableSpecie("", false);

	public static Specie asPersisted(String id)
	{
		return new Specie(id, true);
	}

	public static Specie asNew()
	{
		return new Specie(UUID.randomUUID().toString(), false);
	}

	private Specie(String id, boolean persisted)
	{
		super(id, persisted);
	}

	Specie(Specie s)
	{
		super(s.getId(), s.isPersisted());
		this.name = s.name;
		this.regWeight = s.regWeight;
		this.freshWater = s.freshWater;
	}

	//@formatter:off
	private String name = "";
	public String getName(){return name;}
	public Specie setName(String name){this.name = name; return this;}

	private int regWeight = 0;
	public int getRegWeight(){return regWeight;}
	public Specie setRegWeight(int regWeight){this.regWeight = regWeight; return this;}

	private boolean freshWater = true;
	public boolean isFreshWater(){return freshWater;}
	public Specie setFreshWater(boolean freshWater){this.freshWater = freshWater; return this;}
	//@formatter:off

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
		return new Specie(this);
	}

	private static final class ImmutableSpecie extends Specie
	{
		private ImmutableSpecie(String id, boolean persisted)
		{
			super(id, persisted);
		}

		@Override
		public Specie setName(String name)
		{
			return fail();
		}

		@Override
		public Specie setRegWeight(int regWeight)
		{
			return fail();
		}

		@Override
		public Specie setFreshWater(boolean freshWater)
		{
			return fail();
		}

		private Specie fail()
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " can't be modified");
		}

		@Override
		public Specie copy()
		{
			return this;
		}
	}
}
