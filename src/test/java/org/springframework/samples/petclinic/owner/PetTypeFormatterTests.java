package org.springframework.samples.petclinic.owner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link PetTypeFormatter}
 *
 * @author Colin But
 */
@RunWith(MockitoJUnitRunner.class)
public class PetTypeFormatterTests {

    @Mock
    private PetRepository pets;

    private PetTypeFormatter petTypeFormatter;

    @Before
    public void setup() {
        this.petTypeFormatter = new PetTypeFormatter(pets);
    }

    @Test
    public void testPrint() {
        PetType petType = mock(PetType.class);
        when(petType.getName()).thenReturn("Hamster");

        String petTypeName = this.petTypeFormatter.print(petType, Locale.ENGLISH);
        assertEquals("Hamster", petTypeName);
    }

    @Test
    public void shouldParse() throws ParseException {
        List<PetType> petTypes = new ArrayList<>();

        PetType dogPetType = mock(PetType.class);
        PetType birdPetType = mock(PetType.class);

        when(dogPetType.getName()).thenReturn("Dog");
        when(birdPetType.getName()).thenReturn("Bird");

        petTypes.add(dogPetType);
        petTypes.add(birdPetType);

        Mockito.when(this.pets.findPetTypes()).thenReturn(petTypes);

        PetType petType = petTypeFormatter.parse("Bird", Locale.ENGLISH);
        assertEquals("Bird", petType.getName());
    }

    @Test(expected = ParseException.class)
    public void shouldThrowParseException() throws ParseException {
        petTypeFormatter.parse("Fish", Locale.ENGLISH);
    }

}
