package se.martinuhlen.fishbase.javafx;

import static java.time.LocalTime.MIDNIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.martinuhlen.fishbase.domain.TestData.tench;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import javafx.util.StringConverter;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.javafx.utils.Converters;

/**
 * Unit tests of {@link Converters}.
 *
 * @author Martin
 */
public class ConvertersTest
{
    @Test
    public void specie()
    {
        StringConverter<Specie> c = Converters.specieConverter();
        assertEquals(tench().getName(), c.toString(tench()));
        assertThrows(RuntimeException.class, () -> c.fromString(""));
    }

    @Test
    public void length()
    {
        StringConverter<Float> c = Converters.lengthConverter();
        assertEquals(Float.valueOf(0.0F),  c.fromString(""));
        assertEquals(Float.valueOf(0.0F),  c.fromString(" "));
        assertEquals(Float.valueOf(0.0F),  c.fromString(null));

        assertEquals(Float.valueOf(53.5F), c.fromString("53.5"));
        assertEquals(Float.valueOf(53.0F), c.fromString("53.0"));
        assertEquals(Float.valueOf(53.0F), c.fromString("53.00"));

        assertEquals(Float.valueOf(53.0F), c.fromString("53"), "Expect implicit trailing zero");

        assertEquals("37.5", c.toString(37.5F));
        assertEquals("37", c.toString(37F));
        assertEquals("", c.toString(0F));
    }
   
    @Test
    public void date()
    {
        StringConverter<LocalDate> c = Converters.dateConverter();
        assertEquals(LocalDate.parse("2018-05-04"), c.fromString("2018-05-04"));
        assertEquals(LocalDate.parse("2018-05-04"), c.fromString(" 2018-05-04 "), "Expect input trimmed");
        assertThrows(RuntimeException.class, () -> c.fromString("abc"));
    }

    @Test
    public void time()
    {
        StringConverter<LocalTime> c = Converters.timeConverter();
        assertEquals(MIDNIGHT, c.fromString(""));
        assertEquals(MIDNIGHT, c.fromString(" "));
        assertEquals(MIDNIGHT, c.fromString(null));

        assertEquals(LocalTime.parse("00:05"), c.fromString("00:05"));
        assertEquals(LocalTime.parse("06:30"), c.fromString("06:30"));
        assertEquals(LocalTime.parse("12:55"), c.fromString("12:55"));
        assertEquals(LocalTime.parse("21:20"), c.fromString("21:20"));

        assertEquals(LocalTime.parse("21:20"), c.fromString(" 21:20 "), "Expect input trimmed");
        assertEquals(LocalTime.parse("09:30"), c.fromString("9:30"), "Expect implicit leading zero");
        assertEquals(LocalTime.parse("09:30"), c.fromString(" 9:30"), "Expect implicit leading zero");

        assertThrows(RuntimeException.class, () -> c.fromString("abc"));
        assertThrows(RuntimeException.class, () -> c.fromString("1:2"));
        assertThrows(RuntimeException.class, () -> c.fromString("14:5"));
        assertThrows(RuntimeException.class, () -> c.fromString("23:2"));

        assertEquals("21:15", c.toString(LocalTime.parse("21:15")));
        assertEquals("", c.toString(MIDNIGHT));
    }
}
