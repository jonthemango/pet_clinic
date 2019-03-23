package org.springframework.samples.petclinic.migration;

import java.util.Collection;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.VisitRepository;

public class ConsistencyChecker {

    private SqlDB db;
    private TableDataGateway tdg;
    
    private VetRepository vets;
    private OwnerRepository owners;
    private PetRepository pets;
    private VisitRepository visits;

    public ConsistencyChecker(SqlDB db){
        this.db = db;
        this.tdg = new TableDataGateway(db);
    }


    public void connectRepos(VetRepository vets, OwnerRepository owners, PetRepository pets, VisitRepository visits){
        this.vets = vets;
        this.owners = owners;
        this.pets = pets;
        this.visits = visits; 
    }

    public void vetsChecker(){
        Collection<Vet> vetCollectionOld = vets.findAll(); // old data store
        Collection<Vet> vetCollectionNew = tdg.getVets();


    }


    
    
    
    
}