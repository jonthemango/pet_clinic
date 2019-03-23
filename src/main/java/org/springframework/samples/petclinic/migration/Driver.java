package org.springframework.samples.petclinic.migration;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.VisitRepository;

import java.sql.ResultSet;

public class Driver {

    private VetRepository vets;
    private OwnerRepository owners;
    private PetRepository pets;
    private VisitRepository visits;

    /*
    Gives us access to the old database, pass these along to your objects/methods to use them.
     */
    public Driver(VetRepository vets, OwnerRepository owners, PetRepository pets, VisitRepository visits){
        this.vets = vets;
        this.owners = owners;
        this.pets = pets;
        this.visits = visits;
    }

    /*
    do any arbritary stuff here and call it in MigrationController
     */
    public String execute(){

        return "Driver Executed.";
    }

    /*
    Lists out the content of the table sqlite_seq, queries the db and returns are a formatted json string.
     */
    public String listDB(){
        SqlDB db = new SQLiteDB();
        ResultSet resultSet = db.select("SELECT * FROM sqlite_sequence");
        System.out.println(resultSet);
        String response = "";
        String name;
        String rows;
        try {
            while(resultSet.next()){
                name = resultSet.getString("name");
                rows = String.valueOf(resultSet.getInt("seq"));
                response += String.format("{ 'name': '%s', 'number_of_rows': %s } \n", name, rows);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    /*
    This method invokes the forklift methods, copying all the contents of the old db to the new at a specific moment in time.
     */
    public String forklift(){
        String now = java.time.LocalTime.now().toString();

        SqlDB db = new SQLiteDB();
        Forklift forklift = new Forklift(db);
        forklift.initSchema();
        forklift.liftPetTypes(pets);
        forklift.liftVets(vets);
        forklift.liftOwnersAndPets(owners);
        forklift.liftVisits(visits);
        forklift.liftSpecialties();
        db.close();
        return "Forklift executed at: " + now;
    }

    public String emptyDB(){
        String now = java.time.LocalTime.now().toString();
        SqlDB db = new SQLiteDB();
        Forklift forklift = new Forklift(db);
        forklift.dropClinicTables();
        return "Dropped tables at: " + now;
    }

	public String consistencyChecker() {
        String now = java.time.LocalTime.now().toString();
        SqlDB db = new SQLiteDB();
        ConsistencyChecker checker = new ConsistencyChecker(db);
        checker.connectRepos(vets, owners, pets, visits);
        

        return now + "\n" + checker.vetsChecker();
	}




}
