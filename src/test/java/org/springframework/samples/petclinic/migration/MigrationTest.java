package org.springframework.samples.petclinic.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.*;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import static org.mockito.BDDMockito.given;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.owner.OwnerRepository;

import java.util.Arrays;


public class MigrationTest {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 3;
    private Owner Robert;
    private Vet vet;
    private Visit Visitation;
    private Pet Buddy;
    private ConsistencyChecker tempChecker;
    private SQLiteDB db;
    private TableDataGateway tdg;
    private Forklift forklift;

    @MockBean
    private OwnerRepository owners = mock(OwnerRepository.class);
    private VisitRepository visits = mock(VisitRepository.class);
    private PetRepository pets = mock(PetRepository.class);
    private VetRepository vets = mock(VetRepository.class);

    @Before
    public void setup() {
        FeatureToggleManager.DOING_MIGRATION_TEST = true;
        db = mock(SQLiteDB.class);
        tdg = mock(TableDataGateway.class);
        tempChecker = new ConsistencyChecker(db);
        forklift = new Forklift(db,tdg);
        vet = mock(Vet.class);

        Robert = mock(Owner.class);
        Robert.setId(TEST_OWNER_ID);
        Robert.setFirstName("Robert");
        Robert.setLastName("Lewandowski");
        Robert.setAddress("110 W. Liberty St.");
        Robert.setCity("La Street");
        Robert.setTelephone("6085551023");

        Buddy = mock(Pet.class);
        Buddy.setId(TEST_PET_ID);
        Buddy.setName("hamster");

        Visitation = mock(Visit.class);
        Visitation.setDescription("Quick Checkup");
        Visitation.setPetId(TEST_PET_ID);

        given(Buddy.getOwner()).willReturn(Robert);
        given(owners.findById(TEST_OWNER_ID)).willReturn(Robert);
        given(owners.findAll()).willReturn(Arrays.asList(Robert));
        given(visits.findAll()).willReturn(Arrays.asList(Visitation));
        given(vets.findAll()).willReturn(Arrays.asList(vet));
        given(Robert.getPets()).willReturn(Arrays.asList(Buddy));

    }

    @After
    public void afterTest(){
        FeatureToggleManager.DOING_MIGRATION_TEST = false;
    }

    @Test
    public void testForklift() {

        this.forklift.initSchema();

        // Perform the lifts
        forklift.liftOwnersAndPets(owners);
        forklift.liftPetTypes(pets);
        forklift.liftVets(vets);
        forklift.liftVisits(visits);
        forklift.liftSpecialties();

        // Assert insertions occurred
        verify(tdg).insertVisit(Visitation);
        verify(tdg).insertOwner(Robert);
        verify(tdg).insertPet(Buddy);
        verify(tdg).insertVet(vet);
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

    @Test
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
        visits = mock(VisitRepository.class);
        pets = mock(PetRepository.class);
    	VisitController controller = new VisitController(visits, pets);
        BindingResult resultMock = mock(BindingResult.class);
        when(resultMock.hasErrors()).thenReturn(false);

        controller.setDbForTest(db,tdg);

        controller.processNewVisitForm(Visitation, resultMock);

        // verify that owner was saved to old database
        verify(visits).save(Visitation);

        // verify that owner was saved to new database
        verify(tdg).insertVisit(Visitation);
    }



}
