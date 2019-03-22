package org.springframework.samples.petclinic.migration;


public interface SqlDB {
    void execute(String sql);
    boolean insert();
    void close();
}
