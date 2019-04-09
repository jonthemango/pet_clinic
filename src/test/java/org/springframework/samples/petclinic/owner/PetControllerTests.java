package org.springframework.samples.petclinic.owner;



import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetController;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeFormatter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.samples.petclinic.toggles.ABTestingLogger;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.samples.petclinic.owner.Owner;
/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 */
@RunWith(SpringRunner.class)
@WebMvcTest(value = PetController.class,
    includeFilters = @ComponentScan.Filter(
                            value = PetTypeFormatter.class,
                            type = FilterType.ASSIGNABLE_TYPE))
public class PetControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetRepository pets;

    @MockBean
    private OwnerRepository owners;


    @Before
    public void setup() {
        FeatureToggleManager.DO_REDIRECT_TO_NEW_VISIT_PAGE_AFTER_PET_CREATION = false;

        PetType cat = new PetType();
        cat.setId(3);
        cat.setName("hamster");
        given(this.pets.findPetTypes()).willReturn(Lists.newArrayList(cat));
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(new Owner());
        given(this.pets.findById(TEST_PET_ID)).willReturn(new Pet());

    }

    @Test
    public void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"))
            .andExpect(model().attributeExists("pet"));
    }

    @Test
    public void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
            .param("name", "Betty")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(model().attributeHasNoErrors("owner"))
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(model().attributeHasFieldErrors("pet", "type"))
            .andExpect(model().attributeHasFieldErrorCode("pet", "type", "required"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    public void testInitUpdateForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("pet"))
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }

    @Test
    public void testProcessUpdateFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("type", "hamster")
            .param("birthDate", "2015-02-12")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testProcessUpdateFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
            .param("name", "Betty")
            .param("birthDate", "2015/02/12")
        )
            .andExpect(model().attributeHasNoErrors("owner"))
            .andExpect(model().attributeHasErrors("pet"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdatePetForm"));
    }


    @Test
    public void DO_REDIRECT_TO_NEW_VISIT_PAGE_AFTER_PET_CREATION() throws Exception {
        // Reset logs
        ABTestingLogger.resetLogger();

        // Use Feature A
        FeatureToggleManager.DO_REDIRECT_TO_NEW_VISIT_PAGE_AFTER_PET_CREATION = false;

        // Execute experiment A
        this.experimentA();

        // Use Feature B
        FeatureToggleManager.DO_REDIRECT_TO_NEW_VISIT_PAGE_AFTER_PET_CREATION = true;

        // Execute experiment B
        this.experimentB();

        // Rollback Feature back to A
        FeatureToggleManager.DO_REDIRECT_TO_NEW_VISIT_PAGE_AFTER_PET_CREATION = false;

        // Show that feature can be rolled back to experiment A
        this.experimentA();
    }


    public void experimentA() throws Exception{
        // Log start of experiment A
        ABTestingLogger.log("Experiment for Visit A Start", "", "a");

        // Make post request on /owners/new and check redirect occurs to pet page
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
        .param("name", "Betty")
        .param("type", "hamster")
        .param("birthDate", "2015-02-12")
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/owners/{ownerId}"));
         
        // End experiment A
        ABTestingLogger.log("Experiment for Visit A End", "", "a");
    }
    

    public void experimentB() throws Exception{
        // Start experiment B
        ABTestingLogger.log("Experiment for Visit B Start", "", "b");

        // Make post request on /owners/new and check redirect occurs to pet form page
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
        .param("name", "Betty")
        .param("type", "hamster")
        .param("birthDate", "2015-02-12")
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/owners/{ownerId}/pets/null/visits/new"));
        
        // End experiment B
        ABTestingLogger.log("Experiment for Visit B End", "", "b");
    }



}
