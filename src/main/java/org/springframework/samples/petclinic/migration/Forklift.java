package org.springframework.samples.petclinic.migration;


import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.vet.Vets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Forklift {

    /*
    Queries that drop the existing tables in order to prevent errors (killing data persistence after process ends)
     */
    private String dropTableStatements [] = {
        "DROP TABLE IF EXISTS vet_specialties",
        "DROP TABLE IF EXISTS vets",
        "DROP TABLE IF EXISTS specialties",
        "DROP TABLE IF EXISTS visits",
        "DROP TABLE IF EXISTS pets",
        "DROP TABLE IF EXISTS types",
        "DROP TABLE IF EXISTS owners",
    };

    /*
    Queries that create structure of the tables and indices.
     */
    private String  initSchemaStatements[] = {
        "CREATE TABLE IF NOT EXISTS `vets` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `first_name` TEXT, `last_name` TEXT )",
        "CREATE INDEX IF NOT EXISTS `last_name_vets` ON `vets` ( `last_name` )",
        "CREATE TABLE IF NOT EXISTS `specialties` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT )",
        "CREATE INDEX IF NOT EXISTS `name_specialities` ON `specialties` ( `name` )",
        "CREATE TABLE IF NOT EXISTS `vet_specialties` ( `vet_id` INTEGER NOT NULL, `specialty_id` INTEGER NOT NULL, FOREIGN KEY(`specialty_id`) REFERENCES `specialties`(`id`), FOREIGN KEY(`vet_id`) REFERENCES `vets`(`id`) )",
        "CREATE TABLE IF NOT EXISTS `types` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT )",
        "CREATE INDEX IF NOT EXISTS `name_types` ON `types` ( `name` )",
        "CREATE TABLE IF NOT EXISTS `owners` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `first_name` TEXT, `last_name` TEXT, `address` TEXT, `city` TEXT, `telephone` TEXT )",
        "CREATE INDEX IF NOT EXISTS `last_name` ON `owners` ( `last_name` )",
        "CREATE TABLE IF NOT EXISTS `pets` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `birth_date` TEXT, `type_id` INTEGER NOT NULL, `owner_id` INTEGER NOT NULL, FOREIGN KEY(`owner_id`) REFERENCES `owners`(`id`) )",
        "CREATE INDEX IF NOT EXISTS `name_pets` ON `pets` ( `name` )",
        "CREATE TABLE IF NOT EXISTS `visits` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `pet_id` INTEGER, `visit_date` TEXT, `description` TEXT, FOREIGN KEY(`pet_id`) REFERENCES `pets`(`id`) )"
    };

    /*
    Queries that insert the specialities into the table. No way to get these from old datastore at the moment.
     */
    private String specialitiesStatements [] = {
        "INSERT  INTO specialties VALUES (1, 'radiology')",
        "INSERT  INTO specialties VALUES (2, 'surgery')",
        "INSERT  INTO specialties VALUES (3, 'dentistry')"
    };

    private SqlDB db;
    private TableDataGateway tdg;
    private FeatureToggleManager featureToggleManager;

    public Forklift(SqlDB db){
        this.db = db;
        this.tdg = new TableDataGateway(db);
    }

    /*
    This method initilizes the database to the correct schema.
     */
    public void initSchema(){

        if (FeatureToggleManager.DO_DROP_TABLES_UPON_FORKLIFT) dropClinicTables();


        for (String statement : initSchemaStatements){
            db.execute(statement);
        }
        System.out.println("INIT SCHEMA COMPLETED.");
    }

    public void dropClinicTables(){
        for (String statement : dropTableStatements){
            db.execute(statement);
        }
    }

    /*
    This method forklifts the PetType table
     */
    public void liftTypes(PetRepository pets){
        Collection<PetType> typeCollection = pets.findPetTypes();
        for (PetType type : typeCollection){
            this.tdg.insertType(type);
        }
    }

    /*
    This method inserts all specialities
     */
    public void liftSpecialties(){
        for (String statement : specialitiesStatements){
            db.execute(statement);
        }
    }

    /*
    This method forklifts the Vets table.
     */
    public void liftVets(VetRepository vets){
        Collection<Vet> vetCollection = vets.findAll();
        for (Vet vet : vetCollection){
            this.tdg.insertVet(vet);
        }
    }
}
