/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyon;

/**
 * JdbcHelper.java
 * A simple, light-weight JDBC utility class to interact with the DB
 * It supports both static statements and prepared statements.
 * 
 * Author: Hyoeun Lee (leehyoe@sheridancollege.ca)
 * Created: 19th Sep. 2017
 * Updated: 24st Sep. 2017
*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

//This class doesn't need accessors or mutators as it is a utility class
public class JdbcHelper {
    //instance variables
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String errorMessage;
    private String activeSql;
    private PreparedStatement activeStatement;


    //constructor doesn't have a return value
    public JdbcHelper() {
        //They cannot be null always
        connection = null;
        statement = null;
        resultSet = null;
        errorMessage = "no error";
    }
    
    //make connection to the DB and return true/false
    public boolean connect(String url, String user, String pass) {
        
        boolean connected = false; // default is not connected
        
        //validation
        if(url == null || url.isEmpty()) 
            return connected;
        if(user == null || user.isEmpty())
            return connected;
        if(pass == null || pass.isEmpty()) 
            return connected;
        
        //try to connect with Exception handling
        //Always 1 try and 2 catch blocks (1 for SQL, the other for general)
        try {
            connection = DriverManager.getConnection(url, user, pass);
            
            //try to create statement object automattically
            statement = connection.createStatement();
            
            //very important! it's not flase!
            connected = true;
        } catch(SQLException e) {
            System.err.println("[ERROR] " + e.getSQLState() + e.getMessage());
        } catch(Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
        return connected;       
    }
    
    //clear Jdblc resources
    public void disconnect() {
        try {
            resultSet.close();
        } catch(SQLException e) {
            System.err.println("[ERROR] " + e.getSQLState() + e.getMessage());
        } catch(Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }
    
    public ResultSet query(String sql) {
       //initialize return value
        resultSet = null;
        errorMessage = "";
        
        //validate input 
        if(sql == null || sql.isEmpty()){
            System.err.println("[WARNING] SQL string is null or empty in query()");
            return resultSet;
        }
        
        //check connection before executing SQL query       
        try {
            if(connection == null || connection.isClosed()){
                System.err.println("Connection is NOT established. Make connection "
                    + "to DB before calling query()");
                return resultSet;
            }
            resultSet = statement.executeQuery(sql);
        
        } catch(SQLException e) {
            System.err.println("[ERROR] " + e.getSQLState() + e.getMessage());
        } catch(Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
        } 
        return resultSet;
    }
    
    //execute static SQL query statement
    //returns Results object if successful.Otherwise, returns null
    public ResultSet query(String sql, ArrayList<Object> params) {
        resultSet = null;
        errorMessage = "";
        
        try {
            if(!sql.equals(activeSql)) {
                activeStatement = connection.prepareStatement(sql);
                activeSql = sql;
            }
            if(params != null) 
                setParametersForPreparedStatement(params);
            resultSet = activeStatement.executeQuery();
        } catch(SQLException e){
            errorMessage = e.getSQLState() + ": "+ e.getMessage();
            System.err.println(errorMessage);
        } catch(Exception e){
            errorMessage = e.getMessage();
            System.err.println(errorMessage);
        }
        return resultSet;
    }
    
    public int update(String sql) {
        int rows = -1;
        errorMessage = "";
        
        if(sql == null || sql.isEmpty()) {
            System.err.println("[WARNING] SQL string is null or empty in query()");
            return rows;
        }
        
        try {
            if(connection == null || connection.isClosed()) {
                System.err.println("Connection is NOT established. Make connection "
                    + "to DB before calling query()");
                return rows;
            }
            rows = statement.executeUpdate(sql);
        } catch(SQLException e) {
            System.err.println("[ERROR] " + e.getSQLState() + e.getMessage());
        } catch(Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
        return rows;
    }
    
    public int update(String sql, ArrayList<Object> params) {
        int rows = -1;
        errorMessage = "";

        try {
            if(!activeSql.equals(sql)) {
                activeStatement = connection.prepareStatement(sql);
                activeSql = sql;
            }
            if(params != null) 
                setParametersForPreparedStatement(params);
            rows = activeStatement.executeUpdate();
            
        } catch(SQLException e){
            errorMessage = e.getSQLState() + ": "+ e.getMessage();
            System.err.println(errorMessage);
        } catch(Exception e){
            errorMessage = e.getMessage();
            System.err.println(errorMessage);
        }
        return rows;
    }
    
    private void setParametersForPreparedStatement(ArrayList<Object> params) {
        errorMessage = "";
        Object param = null;
        
        try {
            for (int i = 0; i < params.size(); i++) {
                param = params.get(i);
                if(param instanceof Integer) 
                    activeStatement.setInt(i+1, (Integer)param);
                else if(param instanceof Double)
                    activeStatement.setDouble(i+1, (Double)param);
                else if(param instanceof String)
                    activeStatement.setString(i+1, (String)param);                    
            }
        } catch (SQLException e) {
            errorMessage = e.getSQLState() + ": " + e.getMessage();
            System.err.println(errorMessage);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            System.err.println(errorMessage);
        }
    }
}