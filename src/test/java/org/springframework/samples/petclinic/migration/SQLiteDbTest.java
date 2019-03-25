package org.springframework.samples.petclinic.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;

import java.sql.ResultSet;

public class SQLiteDbTest {

    private SqlDB db;
    @Before
    public void setup() {
        db = new SQLiteDB("db_testing.db");
    }

    @After
    public void afterTest(){
        db.close();
    }

    @Test
    public void executeQueryTest(){
        try{
            db.execute("SELECT * FROM PETS");
        } catch (Exception e){
            assert(true);
        }


        try{
            db.execute("CREATE TABLE IF NOT EXISTS `vets` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `first_name` TEXT, `last_name` TEXT )");
            assert(true);
            db.execute("INSERT INTO vets (first_name, last_name) VALUES ('jon', 'mon')");
            assert(true);
            ResultSet resultSet = db.select("SELECT * FROM vets WHERE first_name = 'jon'");
            assert(true);
            while(resultSet.next()){
                assert(resultSet.getString("last_name").equals("mon"));
            }
        } catch (Exception e){
            assert(false);
        }


    }


}
