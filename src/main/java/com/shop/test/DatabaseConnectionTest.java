package com.shop.test;

import com.shop.database.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test class to verify database connection
 */
public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing database connection...");
            
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            Connection conn = dbConfig.getConnection();
            
            System.out.println("Connected to: " + dbConfig.getDatabaseType().toUpperCase() + " database");
            
            // Test query to verify tables exist
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'shopdb'");
            
            if (rs.next()) {
                int tableCount = rs.getInt("table_count");
                System.out.println("Found " + tableCount + " tables in shopdb database");
            }
            
            // Test categories table
            rs = stmt.executeQuery("SELECT COUNT(*) as category_count FROM categories");
            if (rs.next()) {
                int categoryCount = rs.getInt("category_count");
                System.out.println("Categories table has " + categoryCount + " records");
            }
            
            System.out.println("Database connection test successful!");
            
        } catch (Exception e) {
            System.err.println("Database connection test failed:");
            e.printStackTrace();
        }
    }
}
