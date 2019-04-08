package org.springframework.samples.petclinic.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.Ignore;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.VisitController;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import static org.mockito.BDDMockito.given;
import org.springframework.samples.petclinic.owner.PetController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.OwnerRepository;


public class MigrationTest {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 3;
    private Owner Robert;
    private Visit Visitation;
    private Pet Buddy;
    private ConsistencyChecker tempChecker;
    private SQLiteDB db;
    private TableDataGateway tdg;
    
    @MockBean
    private OwnerRepository owners = mock(OwnerRepository.class);
    private VisitRepository visit = mock(VisitRepository.class);
    private PetRepository pets;

    @Before
    public void setup() {
        FeatureToggleManager.DOING_MIGRATION_TEST = true;
        FeatureToggleManager.DO_RUN_CONSISTENCY_CHECKER = true;
        db = mock(SQLiteDB.class);
        tdg = mock(TableDataGateway.class);
        tempChecker = new ConsistencyChecker(db);

        Robert = new Owner();
        Robert.setId(TEST_OWNER_ID);
        Robert.setFirstName("Robert");
        Robert.setLastName("Lewandowski");
        Robert.setAddress("110 W. Liberty St.");
        Robert.setCity("La Street");
        Robert.setTelephone("6085551023");
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(Robert);      

        Buddy = new Pet();
        Buddy.setId(TEST_PET_ID);
        Buddy.setName("hamster");

        //Buddy.setOwner(Robert);
        //given(this.pets.findPetTypes()).willReturn(Lists.newArrayList(Buddy));
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(Robert);
        //given(this.pets.findById(TEST_PET_ID)).willReturn(Buddy);

        Visitation = new Visit();
        Visitation.setDescription("Quick Checkup");
        Visitation.setPetId(3);


    }

    @After
    public void afterTest(){
        FeatureToggleManager.DOING_MIGRATION_TEST = false;
        FeatureToggleManager.DO_RUN_CONSISTENCY_CHECKER = false;
        db.close();
    }
    
    @Test
    @Ignore
    public void testOwnerMigration() {
        owners = mock(OwnerRepository.class);
    	OwnerController controller = new OwnerController(owners);
        BindingResult resultMock = mock(BindingResult.class);
        when(resultMock.hasErrors()).thenReturn(false);
        
        controller.setDbForTest(db,tdg);
        
        controller.processCreationForm(Robert, resultMock);

        // verify that owner was saved to old database
        verify(owners).save(Robert);

        // verify that owner was saved to new database
        verify(tdg).insertOwner(Robert);
    }

    @Test
    @Ignore
    public void testPetMigration() {
        pets = mock(PetRepository.class);
        //owners = mock(OwnerRepository.class);
        PetController controller = new PetController(pets, owners);
        BindingResult resultMock = mock(BindingResult.class);
        ModelMap map = mock(ModelMap.class);
        when(resultMock.hasErrors()).thenReturn(false);

        controller.setDbForTest(db,tdg);

        controller.processCreationForm(Robert, Buddy, resultMock, map);

        // verify that owner was saved to old database
        verify(pets).save(Buddy);

        // verify that owner was saved to new database
        verify(tdg).insertPet(Buddy);

        // assert that the pet being tested was added to the owner's pets
        assertEquals(Buddy.getOwner(), Robert);
    }
    
    @Test
    public void testVisitMigration(){
        visit = mock(VisitRepository.class);
        pets = mock(PetRepository.class);
    	VisitController controller = new VisitController(visit, pets);
        BindingResult resultMock = mock(BindingResult.class);
        when(resultMock.hasErrors()).thenReturn(false);
        
        controller.setDbForTest(db,tdg);
        
        controller.processNewVisitForm(Visitation, resultMock);

        // verify that owner was saved to old database
        verify(visit).save(Visitation);

        // verify that owner was saved to new database
        verify(tdg).insertVisit(Visitation);

    }



}
