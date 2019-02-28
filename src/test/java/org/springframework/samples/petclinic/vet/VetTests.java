/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import org.junit.Test;

import org.springframework.util.SerializationUtils;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Dave Syer
 *
 */
public class VetTests {

    @Test
    public void testSerialization() {
        Vet mockVet = mock(Vet.class, withSettings().serializable());

        when(mockVet.getFirstName()).thenReturn("Zaphod");
        when(mockVet.getLastName()).thenReturn("Beeblebrox");
        when(mockVet.getId()).thenReturn(123);

        mockVet.setFirstName("Zaphod");
        mockVet.setLastName("Beeblebrox");
        mockVet.setId(123);

        Vet other = (Vet) SerializationUtils.deserialize(SerializationUtils.serialize(mockVet));

        assertThat(other.getFirstName()).isEqualTo(mockVet.getFirstName());
        assertThat(other.getLastName()).isEqualTo(mockVet.getLastName());
        assertThat(other.getId()).isEqualTo(mockVet.getId());
    }

    @Test
    public void testAddSpecialty() {

        Vet vet = new Vet();

        Specialty radiology = mock(Specialty.class);
        Specialty doctor = mock(Specialty.class);

        vet.addSpecialty(doctor);
        vet.addSpecialty(radiology);

       
//        assertThat(vet.getSpecialties().get(0)).isEqualTo(radiology);
        assertThat(vet.getSpecialties().get(1)).isEqualTo(doctor);

    }

}
