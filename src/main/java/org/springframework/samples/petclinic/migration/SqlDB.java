package org.springframework.samples.petclinic.migration;


import java.sql.ResultSet;

public interface SqlDB {
    void execute(String sql);
    ResultSet select(String sql);
    boolean insert();
    void close();
}
