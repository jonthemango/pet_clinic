package org.springframework.samples.petclinic.migration;


import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;

public class ConsistencyChecker {

    private SqlDB db;
    private TableDataGateway tdg;
    
    private VetRepository vets;
    private OwnerRepository owners;
    private PetRepository pets;
    private VisitRepository visits;
    private int inconsistency;

    private String consistency;

    public ConsistencyChecker(SqlDB db) {
        this.db = db;
        this.tdg = new TableDataGateway(db);
    }

    /**
     * @return the inconsistency
     */
    public int getInconsistency() {
        return inconsistency;
    }

    /**
     * @param inconsistency the inconsistency to set
     */
    public void setInconsistency(int inconsistency) {
        this.inconsistency = inconsistency;
    }
    
    public void connectRepos(VetRepository vets, OwnerRepository owners, PetRepository pets, VisitRepository visits) {
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
        this.inconsistency += countInsert + countUpdate;
        consistency += "Number of inserted rows vet table : " + String.valueOf(countInsert) + "\n Number of updated rows vet table: " + String.valueOf(countUpdate);
        return consistency;
    }

    public String visitsChecker(){
        Collection<Visit> visitCollectionOld = visits.findAll(); // old data store
        // first retrive the same id;
        // if it exists then check theyre equal
        // else create it in new data store
        int countInsert = 0;
        int countUpdate = 0;
        for (Visit visit : visitCollectionOld) {

            int id = visit.getId();
            System.out.print(id);
            ResultSet resultSet = this.tdg.getById(id, "visits");
            boolean isSame;
            int petID;
            String date;          
            String description;
            
            try {
                petID = resultSet.getInt("pet_id");
                date = resultSet.getString("visit_date");
                System.out.println(date);
                description = resultSet.getString("description");
                
                isSame = visit.getPetId().equals(petID) && visit.getDate().toString().equals(date) 
                                && visit.getDescription().equals(description);
                
                System.out.println(visit.getDate().toString());

                if (!isSame) {
                    this.tdg.deleteById(resultSet.getInt("id"), "visits");
                    this.tdg.insertVisit(visit);
                    countUpdate++;
                 }
            } catch (Exception e) {
                e.printStackTrace();
                this.tdg.insertVisit(visit);
                countInsert++;
            }
        }
    
        this.inconsistency += countInsert + countUpdate;
        consistency += "Number of inserted rows: " + String.valueOf(countInsert) + "\nNumber of updated rows: "
                + String.valueOf(countUpdate);
        return "Number of inserted rows: " + String.valueOf(countInsert) + "\nNumber of updated rows: "
                + String.valueOf(countUpdate);
    }
    
    public String ownersChecker(){
        Collection<Owner> ownersCollectionOld = owners.findAll();
        // first retrive the same id;
        // if it exists then check theyre equal
        // else create it in new data store
        int countInsert = 0;
        int countUpdate = 0;
        for (Owner owner : ownersCollectionOld) {
            Collection<Pet> pets = owner.getPets();
            String petName;
            String birthDate;
            String type;
            int ownerId;
            Boolean samePet;
            for(Pet pet : pets){// name, birth_date, type_id, owner_id 
                int id = pet.getId();
                ResultSet newPet = this.tdg.getById(id, "pets");

                try{
                    petName = newPet.getString("name");
                    birthDate = newPet.getString("birth_date");
                    type = tdg.getPetType(newPet.getInt("type_id"));
                    ownerId = newPet.getInt("owner_id");

                    samePet=        pet.getName().equals(petName) 
                                && pet.getBirthDate().toString().equals(birthDate) 
                                && pet.getType().toString().equals(type)
                                && pet.getOwner().getId().equals(ownerId);
                    
                    System.out.println("Start of pet comparaison " + samePet);
                    System.out.println(pet.getBirthDate().toString());
                    System.out.println(birthDate);
                    System.out.println(pet.getType());
                    System.out.println(type);
                    System.out.println(pet.getOwner().getId());
                    System.out.println(ownerId);
                    System.out.println("end of pet comparaison");


                    if(!samePet){
                         this.tdg.deleteById(newPet.getInt("id"), "pets");
                         this.tdg.insertPet(pet);
                        countUpdate++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    this.tdg.insertPet(pet);
                    countInsert++;
                }
            }
            
            int id = owner.getId();
            System.out.print(id);
            ResultSet resultSet = this.tdg.getById(id, "owners");
            boolean isSame;
            String firstName;
            String lastName;
            String address;
            String city;
            String telephone; 

            try {
                firstName = resultSet.getString("first_name");
               // System.out.println("the first name is" + firstName);
                //System.out.println(owner.getFirstName());
                lastName = resultSet.getString("last_name");
            //    System.out.println("the last name is" +lastName);
            //    System.out.println(owner.getLastName());
                address = resultSet.getString("address");
            //    System.out.println(address);
            //    System.out.println(owner.getAddress());
                city = resultSet.getString("city");
            //    System.out.println(city);
            //    System.out.println(owner.getCity());
                telephone = resultSet.getString("telephone"); 
            //    System.out.println(telephone);
            //   System.out.println(owner.getTelephone());

                isSame = owner.getFirstName().equals(firstName) && owner.getLastName().equals(lastName)
                            && owner.getAddress().equals(address) 
                            && owner.getCity().equals(city)
                            && owner.getTelephone().equals(telephone);
                
                if (!isSame) {
                    this.tdg.deleteById(resultSet.getInt("id"), "visits");
                    this.tdg.insertOwner(owner);
                    countUpdate++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.tdg.insertOwner(owner);
                countInsert++;
            }
        }

        this.inconsistency += countInsert + countUpdate;
        consistency += "Number of inserted rows: " + String.valueOf(countInsert) + "\nNumber of updated rows: "
                + String.valueOf(countUpdate);
        return "Number of inserted rows: " + String.valueOf(countInsert) + "\nNumber of updated rows: "
                + String.valueOf(countUpdate);



        
    }

}
    

