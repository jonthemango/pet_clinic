package org.springframework.samples.petclinic.migration;


import java.sql.*;

public class SQLiteDB  implements SqlDB {
    Connection conn = null;
    Statement statement = null;

    /*
    If migrations.db does not exist at the root then this file will create it for you.
     */
    public SQLiteDB(){
        try{
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:migration.db");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public SQLiteDB(String dbLocation){
        try{
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + dbLocation);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    Basic insert method. Now deprecated in favor of .execute
     */
    public boolean insert(){
        try {
            this.statement = conn.createStatement();
            boolean inserted = statement.execute("INSERT INTO types (name) VALUES ('skander')");
            return inserted;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    Execute any SQL statement, this method does not return result sets.
     */
    public void execute(String sql){
        try{
            this.statement = conn.createStatement();
            statement.execute(sql);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    Use this method to invoke sql statements where there is a result set returns (ie. selects)
     */
    public ResultSet select(String sql) {
        try{
            this.statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void close(){
        if (this.conn != null){
            try {
                this.conn.close();
                this.conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}

