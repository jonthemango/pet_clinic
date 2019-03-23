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

    public String vetsChecker(){
        Collection<Vet> vetCollectionOld = vets.findAll(); // old data store
          // first retrive the same id;
            // if it exists then check theyre equal
            // else create it in new data store
        int count = 0;
            
        for (Vet vet : vetCollectionOld){
            
            int id = vet.getId();
            
            Vet newVet = (Vet) this.tdg.getById(id, "vets"); 
            if (newVet == null){
                // insert it
                this.tdg.insertVet(vet);
                count++;
            }
            else if (!vet.equals(newVet)){
                this.tdg.deleteById(newVet.getId(), "vets");
                this.tdg.insertVet(vet);
                count ++; 
            }       

        }

        return "Number of inconsistant rows: " + String.valueOf(count);
    }
}