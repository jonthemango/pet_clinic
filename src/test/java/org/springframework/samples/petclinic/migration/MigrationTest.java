package org.springframework.samples.petclinic.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.validation.BindingResult;
import static org.mockito.BDDMockito.given;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.OwnerRepository;


public class MigrationTest {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 3;
    private Owner Robert;
  /*  private Pet Buddy;*/
    private ConsistencyChecker tempChecker;
    private SQLiteDB db;
    private TableDataGateway tdg;
    
    @MockBean
    private OwnerRepository owners = mock(OwnerRepository.class);

  /*  @MockBean
    private PetRepository pets = mock(PetRepository.class);
    */
    
    @After
    public void afterTest(){
        FeatureToggleManager.DOING_MIGRATION_TEST = false;
    }


    @Before
    public void setup() {
        FeatureToggleManager.DOING_MIGRATION_TEST = true;
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

     /*   Buddy = new Pet();
        Buddy.setId(3);
        Buddy.setName("hamster");
        Buddy.setOwner(Robert);
        given(this.pets.findPetTypes()).willReturn(Lists.newArrayList(Buddy));
        given(this.owners.findById(TEST_OWNER_ID)).willReturn(Robert);
        given(this.pets.findById(TEST_PET_ID)).willReturn(Buddy);*/
    }
    
    @Test
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

    /*@Test
    public void testPetChecker() {
        pets = mock(PetRepository.class);
        PetController controller = new PetController(pets);
        BindingResult resultMock = mock(BindingResult.class);
        ModelMap map = mock(ModelMap.class);
        when(resultMock.hasErrors()).thenReturn(false);

        controller.processCreationForm(Robert, Buddy, resultMock, map);

        assertEquals(0, tempChecker.getInconsistency());
    }*/
    
    @Test
    public void testVisitChecker(){

        
    }



}