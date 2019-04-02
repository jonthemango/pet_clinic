package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TableDataGateway {

    private SqlDB db;

    /*
     * This class is used to insert and retrive rows/tables from the datastore.
     */
    public TableDataGateway(SqlDB db) {
        this.db = db;
    }

    private HashMap<String, String> id(BaseEntity entity){

        HashMap<String, String> map = new HashMap<String,String>();
        if (entity.isNew()){
            map.put("hasId","");
            map.put("valueOfId", "");
        } else {
            map.put("hasId", "id,");
            map.put("valueOfId", String.valueOf(entity.getId()) + ",");
        }

        return map; 
    }

    public void insertOwner(Owner owner) {
        String sql;
        HashMap<String,String> map = id(owner);

        sql = String.format(
                "INSERT INTO owners (%s first_name, last_name, address, city, telephone) VALUES (%s '%s','%s','%s','%s','%s')",
                map.get("hasId"), map.get("valueOfId"),
                owner.getFirstName(), owner.getLastName(), owner.getAddress(), owner.getCity(), owner.getTelephone());
        db.execute(sql);
    }


    public void insertPet(Pet pet) {
        String sql;
        HashMap<String,String> map = id(pet);

        sql = String.format("INSERT INTO pets (%s name, birth_date, type_id, owner_id) VALUES (%s '%s','%s',%d,%d)",
        map.get("hasId"), map.get("valueOfId"),
        pet.getName(), pet.getBirthDate().toString(), pet.getType().getId(), pet.getOwner().getId());
        
        db.execute(sql);
    }

    public void insertSpecialty(Specialty specialty) {
        String sql;
        HashMap<String,String> map = id(specialty);
        
        sql= String.format("INSERT INTO specialties (%s name) VALUES (%s '%s')", 
                map.get("hasId"), map.get("valueOfId"), 
                specialty.getName());
        
        db.execute(sql);
    }

    public void insertType(PetType type) {
        String sql;
        HashMap<String,String> map = id(type);

        sql = String.format("INSERT INTO types (%s name) VALUES (%s '%s')",
                 map.get("hasId"), map.get("valueOfId"),
                type.getName());

        db.execute(sql);
    }

    public String getPetType(int id) throws SQLException {
        String sql;
        sql = String.format("SELECT * FROM types WHERE id = %d", id);
        String result = db.select(sql).getString("name");
        
        return result;
    }

    public void insertVetSpecialty(Vet vet, Specialty specialty) {
        String sql;

        sql = String.format("INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (%d, %d)",

        vet.getId(), specialty.getId());
        
        db.execute(sql);
    }

    public void insertVet(Vet vet) {
        String sql;
        HashMap<String, String> map = id(vet);
        
        sql = String.format("INSERT INTO vets (%s first_name, last_name) VALUES (%s '%s', '%s')", 
        map.get("hasId"), map.get("valueOfId"),
        vet.getFirstName(), vet.getLastName());
        db.execute(sql);

        List<Specialty> specialties = vet.getSpecialties();
        for (Specialty specialty : specialties) {
            this.insertVetSpecialty(vet, specialty);
        }
    }

    public void insertVisit(Visit visit) {
        String sql;
        HashMap<String,String> map = id(visit);
        
        sql = String.format("INSERT INTO visits (%s pet_id, visit_date, description) VALUES (%s %d, '%s', '%s')",
             map.get("hasId"), map.get("valueOfId"),
             visit.getPetId(), visit.getDate().toString(), visit.getDescription());

        db.execute(sql);
    }


    public ResultSet getById(Integer id, String tableName) {
        String sql; 
        sql = String.format("SELECT * FROM %s WHERE id = %d", tableName, id);
        ResultSet resultSet = db.select(sql);
        return resultSet;
    }

    public void deleteById(Integer id, String tableName){        
        db.execute(String.format("DELETE FROM %s WHERE id = %d", tableName, id));
    }

    public ResultSet getOwnersByLastName(String lastName){
        return db.select(String.format("SELECT * FROM owners WHERE last_name = '%s'", lastName));
    }

    public void updateInconsistencies(Integer id, String tableName, String column, String newValue){
        db.execute(String.format("UPDATE %s SET %s = '%s' WHERE id = %d", tableName, column, newValue, id));
    }

    public void updateInconsistencies(Integer id, String tableName, String column, Integer newId){
        db.execute(String.format("UPDATE %s SET %s = '%s' WHERE id = %d", tableName, column, newId, id));
    }

    public ResultSet selectTable(String tableName){
        return db.select(String.format("SELECT * FROM %s", tableName));
    }
}
