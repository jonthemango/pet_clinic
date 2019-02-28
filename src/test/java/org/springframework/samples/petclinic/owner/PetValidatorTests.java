package org.springframework.samples.petclinic.owner;

import java.text.ParseException;
import java.time.LocalDate;
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
import static org.mockito.Mockito.*;

import org.springframework.validation.Errors;

/**
 * Test class for {@link PetValidator}
 *
 * @author Jon Mongeau
 */
@RunWith(MockitoJUnitRunner.class)
public class PetValidatorTests {

    @Test
    public void shouldValidate(){
        PetValidator validator = new PetValidator(); // class under test
        Pet mockPet = mock(Pet.class);

        PetType cat = mock(PetType.class);

        when(mockPet.getName()).thenReturn("Coco");
        when(mockPet.getType()).thenReturn(cat);
        when(mockPet.getBirthDate()).thenReturn(LocalDate.of(2018,1,1));
        when(mockPet.isNew()).thenReturn(false);
        Errors errors = mock(Errors.class);

        validator.validate(mockPet,errors);
        verify(errors, Mockito.times(0)).rejectValue("name","required","required");
        verify(errors, Mockito.times(0)).rejectValue("type","required","required");
        verify(errors, Mockito.times(0)).rejectValue("birthDate","required","required");

        verify(mockPet).getName();
        verify(mockPet).isNew();
        verify(mockPet).getType();
        verify(mockPet).getBirthDate();

    }

    @Test
    public void shouldFailToValidateBecauseOfName(){
        PetValidator validator = new PetValidator(); // class under test
        Pet mockPet = mock(Pet.class);

        PetType cat = mock(PetType.class);

        when(mockPet.getName()).thenReturn("");
        when(mockPet.getType()).thenReturn(cat);
        when(mockPet.getBirthDate()).thenReturn(LocalDate.of(2018,1,1));
        when(mockPet.isNew()).thenReturn(false);
        Errors errors = mock(Errors.class);

        validator.validate(mockPet,errors);
        verify(errors, Mockito.times(1)).rejectValue("name","required","required");
        verify(mockPet).getName();
    }

    @Test
    public void shouldFailToValidateBecauseOfType(){
        PetValidator validator = new PetValidator(); // class under test
        Pet mockPet = mock(Pet.class);


        when(mockPet.getName()).thenReturn("Coco");
        when(mockPet.getType()).thenReturn(null);
        when(mockPet.isNew()).thenReturn(true);
        Errors errors = mock(Errors.class);

        validator.validate(mockPet,errors);
        verify(errors, Mockito.times(0)).rejectValue("name","required","required");
        verify(errors, Mockito.times(1)).rejectValue("type","required","required");

        verify(mockPet).getName();
        verify(mockPet).isNew();
        verify(mockPet).getType();

    }

    @Test
    public void shouldFailToValidateBecauseOf(){
        PetValidator validator = new PetValidator(); // class under test
        Pet mockPet = mock(Pet.class);

        PetType cat = mock(PetType.class);

        when(mockPet.getName()).thenReturn("Coco");
        when(mockPet.getType()).thenReturn(cat);
        when(mockPet.getBirthDate()).thenReturn(null);
        when(mockPet.isNew()).thenReturn(false);
        Errors errors = mock(Errors.class);

        validator.validate(mockPet,errors);
        verify(errors, Mockito.times(0)).rejectValue("name","required","required");
        verify(errors, Mockito.times(0)).rejectValue("type","required","required");
        verify(errors, Mockito.times(1)).rejectValue("birthDate","required","required");

        verify(mockPet).getName();
        verify(mockPet).isNew();
        verify(mockPet).getType();
        verify(mockPet).getBirthDate();
    }
}
