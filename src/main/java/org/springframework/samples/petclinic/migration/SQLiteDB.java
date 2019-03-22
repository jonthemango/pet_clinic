package org.springframework.samples.petclinic.migration;


import java.sql.*;

public class SQLiteDB  implements SqlDB {
    Connection conn = null;
    Statement statement = null;

    public SQLiteDB(){
        try{
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:migration.db");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

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

    public void execute(String sql){
        try{
            this.statement = conn.createStatement();
            statement.execute(sql);
        } catch (Exception e){
            e.printStackTrace();
        }
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

