package org.springframework.samples.petclinic.migration;

import java.sql.ResultSet;

public class Driver {
    public static String execute(){

        SqlDB db = new SQLiteDB();
        Forklift forklift = new Forklift(db);
        forklift.initSchema();

        return "Driver Executed.";
    }

    public static String forklift(){
        return "Forklift executed.";
    }

}
