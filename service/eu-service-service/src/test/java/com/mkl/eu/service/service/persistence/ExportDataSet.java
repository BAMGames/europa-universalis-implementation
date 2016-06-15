package com.mkl.eu.service.service.persistence;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class used to create DataSet from a real database.
 *
 * @author MKL
 */
public class ExportDataSet {
    /**
     * Method to launch to create DataSet.
     * @param args no args.
     * @throws Exception exception.
     */
    public static void main(String[] args) throws Exception {
        // The driver doesn't seem to be mandatory.
        // Class driverClass = Class.forName("com.mysql.jdbc.Driver");
        // database connection, try to avoid the commit of the password, even if it is a localhost database.
        Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eu", "eu", "");
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        exportTables(connection);
    }

    /**
     * Create a DataSet for the tables objects.
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void exportTables(IDatabaseConnection connection) throws Exception {
        export(new String[]{"T_PERIOD", "T_TECH", "T_TRADE", "T_BASIC_FORCE", "T_UNIT", "T_LIMIT"},
               "src/test/resources/com/mkl/eu/service/service/persistence/tables.xml", connection);
    }

    /**
     * Create a DataSet for the given tables on the given filename with the given database connection.
     * @param tables the name of the tables we want to export in the DataSet.
     * @param filename the name of the file to export the DataSet.
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void export(String[] tables, String filename, IDatabaseConnection connection) throws Exception {
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        for (String table : tables) {
            partialDataSet.addTable(table);
        }

        FlatXmlDataSet.write(partialDataSet, new FileOutputStream(filename));
    }
}


