package org.springframework.samples.petclinic.migration;

import org.junit.Before;
import org.junit.Test;

public class SQLiteDbTest {

    private SqlDB db;
    @Before
    public void setup() {
        db = new SQLiteDB("db_testing.db");
    }
}
