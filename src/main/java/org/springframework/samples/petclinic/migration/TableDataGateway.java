package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

import java.util.List;

public class TableDataGateway {

    private SqlDB db;

    /*
    This class is used to insert and retrive rows/tables from the datastore.
     */
    public TableDataGateway(SqlDB db){
        this.db = db;
    }

    public void insertOwner(Owner owner){
        db.execute(String.format("INSERT INTO owners (first_name, last_name, address, city, telephone) VALUES ('%s','%s','%s','%s','%s')", owner.getFirstName(), owner.getLastName(), owner.getAddress(), owner.getCity(), owner.getTelephone()));
    }

    public void insertPet(Pet pet){
        db.execute(String.format("INSERT INTO pets (name, birth_date, type_id, owner_id) VALUES ('%s','%s',%d,%d)",pet.getName(), pet.getBirthDate().toString(), pet.getType().getId(), pet.getOwner().getId()));
    }

    public void insertSpecialty(Specialty specialty){
        db.execute(String.format("INSERT INTO specialties (name) VALUES ('%s')",specialty.getName()));
    }

    public void insertType(PetType type){
        db.execute(String.format("INSERT INTO types (name) VALUES ('%s')",type.getName()));
    }

    public void insertVetSpecialty(Vet vet, Specialty specialty){
        db.execute(String.format("INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (%d, %d)", vet.getId(), specialty.getId()));
    }

    public void insertVet(Vet vet){
        db.execute(String.format("INSERT INTO vets (first_name, last_name) VALUES ('%s', '%s')", vet.getFirstName(), vet.getLastName()));
        List<Specialty> specialties = vet.getSpecialties();
        for (Specialty specialty : specialties){
            this.insertVetSpecialty(vet, specialty);
        }
    }

    public void insertVisit(Visit visit){
        db.execute(String.format("INSERT INTO visits (pet_id, visit_date, description) VALUES (%d, '%s', '%s')", visit.getPetId(), visit.getDate().toString(), visit.getDescription()));
    }





}
