package se.martinuhlen.fishbase.javafx.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.martinuhlen.fishbase.domain.TestData.bream;
import static se.martinuhlen.fishbase.domain.TestData.bream5120;
import static se.martinuhlen.fishbase.domain.TestData.perch;
import static se.martinuhlen.fishbase.domain.TestData.tench;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import se.martinuhlen.fishbase.domain.Specimen;

/**
 * Unit tests of {@link SpecimenWrapper}.
 *
 */
public class SpecimenWrapperTest extends WrapperTestCase<Specimen, SpecimenWrapper>
{
	@Override
	protected SpecimenWrapper createWrapper()
	{
		return new SpecimenWrapper(bream5120(), listener);
	}

	@Test
	public void specieProperty()
	{
		testProperty("specie", wrapper::specieProperty, Specimen::getSpecie, perch(), bream(), tench());
	}

	@Test
	public void weightProperty()
	{
		testProperty("weight", wrapper::weightProperty, Specimen::getWeight, 3875, 4470, 5120);
	}

	@Test
	public void lengthProperty()
	{
		testProperty("length", wrapper::lengthProperty, Specimen::getLength, 64f, 66.5f, 68f);
	}

	@Test
	public void ratioProperty()
	{
		wrapper.specieProperty().setValue(perch().setRegWeight(1000));

		wrapper.weightProperty().setValue(500);
		assertEquals(0.5, wrapper.ratioProperty().getValue().doubleValue());

		wrapper.weightProperty().setValue(1200);
		assertEquals(1.2, wrapper.ratioProperty().getValue().doubleValue());
	}

	@Test
	public void locationProperty()
	{
		testProperty("location", wrapper::locationProperty, Specimen::getLocation, "A", "B", "C");
	}

	@Test
	public void dateProperty()
	{
		testProperty("date", wrapper::dateProperty, s -> s.getInstant().toLocalDate(), LocalDate.parse("2017-04-10"), LocalDate.parse("2017-08-24"), LocalDate.parse("2018-02-12"));

		wrapper.setWrapee(wrapper.getWrapee().setInstant(LocalDateTime.parse("2017-02-09T19:53")));
		wrapper.dateProperty().setValue(LocalDate.parse("2018-06-07"));
		assertEquals(LocalDateTime.parse("2018-06-07T19:53"), wrapper.getWrapee().getInstant());
	}

	@Test
	public void timeProperty()
	{
		testProperty("time", wrapper::timeProperty, s -> s.getInstant().toLocalTime(), LocalTime.parse("20:04"), LocalTime.parse("13:37"), LocalTime.parse("21:45"));

		wrapper.setWrapee(wrapper.getWrapee().setInstant(LocalDateTime.parse("2017-02-09T19:53")));
		wrapper.timeProperty().setValue(LocalTime.parse("20:30"));
		assertEquals(LocalDateTime.parse("2017-02-09T20:30"), wrapper.getWrapee().getInstant());
	}

	@Test
	public void methodProperty()
	{
		testProperty("method", wrapper::methodProperty, Specimen::getMethod, "A", "B", "C");
	}

	@Test
	public void baitProperty()
	{
		testProperty("bait", wrapper::baitProperty, Specimen::getBait, "A", "B", "C");
	}

	@Test
	public void weatherProperty()
	{
		testProperty("weather", wrapper::weatherProperty, Specimen::getWeather, "A", "B", "C");
	}

	@Test
	public void textProperty()
	{
		testProperty("text", wrapper::textProperty, Specimen::getText, "A", "B", "C");
	}
}
