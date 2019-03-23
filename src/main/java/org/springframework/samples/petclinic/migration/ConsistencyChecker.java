package org.springframework.samples.petclinic.migration;

import java.sql.ResultSet;
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
        int countInsert = 0;
        int countUpdate = 0;
        for (Vet vet : vetCollectionOld){
            
            int id = vet.getId();
            System.out.print(id);
            ResultSet resultSet = this.tdg.getById(id, "vets");
            boolean isSame;
            String firstName;
            String lastName;           
            try{
                firstName = resultSet.getString("first_name");
                System.out.println(firstName);
                lastName = resultSet.getString("last_name");
                System.out.println(lastName);
                isSame = vet.getFirstName().equals(firstName);
                System.out.println(vet.getFirstName());
                isSame = isSame && vet.getLastName().equals(lastName);
                System.out.println(vet.getLastName());
                if (!isSame){
                    this.tdg.deleteById(resultSet.getInt("id"), "vets");
                    this.tdg.insertVet(vet);
                    countUpdate ++; 
                }
            } catch (Exception e){
                e.printStackTrace();
                this.tdg.insertVet(vet);
                countInsert++;
            }
        }

        return "Number of inserted rows: " + String.valueOf(countInsert) + "\nNumber of updated rows: " + String.valueOf(countUpdate);
    }

    


    
}