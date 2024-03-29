package com.mkl.eu.service.service.persistence;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.xml.FlatXmlWriter;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Class used to create DataSet from a real database.
 *
 * @author MKL
 */
public class ExportDataSet {
    /** Extension of XML files. */
    private static final String XML_EXTENSION = ".xml";
    /** Extension of DTD files. */
    private static final String DTD_EXTENSION = ".dtd";

    /**
     * Method to launch to create DataSet.
     *
     * @param args no args.
     * @throws Exception exception.
     */
    public static void main(String[] args) throws Exception {
        IDatabaseConnection connection = null;
        try {
            // The driver doesn't seem to be mandatory.
            // Class driverClass = Class.forName("com.mysql.jdbc.Driver");
            // database connection, try to avoid the commit of the password, even if it is a localhost database.
            Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/eu?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
                    "eu", "");
            connection = new DatabaseConnection(jdbcConnection);

            // DBUnit does not like circular reference. So we drop the foreign key and recreate it later.
            Statement statement = connection.getConnection().createStatement();
            statement.execute("ALTER TABLE COUNTRY DROP FOREIGN KEY FK_COUNTRY_MONARCH");

            exportTables(connection);
            exportProvinces(connection);
            exportChat(connection);
            exportReferentiel(connection);
        } finally {
            if (connection != null) {
                Statement statement = connection.getConnection().createStatement();
                statement.execute("ALTER TABLE COUNTRY ADD CONSTRAINT FK_COUNTRY_MONARCH FOREIGN KEY (ID_MONARCH) REFERENCES MONARCH (ID)");
            }
        }
    }

    /**
     * Create a DataSet for the tables objects.
     *
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void exportTables(IDatabaseConnection connection) throws Exception {
        export(new String[]{"T_PERIOD", "T_TECH", "T_TRADE", "T_BASIC_FORCE", "T_UNIT", "T_LIMIT", "T_RESULT", "T_BATTLE_TECH", "T_COMBAT_RESULT", "T_ARMY_CLASS", "T_ARMY_ARTILLERY", "T_ARTILLERY_SIEGE", "T_FORTRESS_RESISTANCE", "T_ASSAULT_RESULT", "T_EXCHEQUER", "T_LEADER", "T_DISCOVERY", "T_ATTRITION_OTHER", "T_ATTRITION_LAND_EUROPE"},
               "src/test/resources/com/mkl/eu/service/service/persistence/tables", connection);
    }

    /**
     * Create a DataSet for the provinces objects.
     *
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void exportProvinces(IDatabaseConnection connection) throws Exception {
        export(new String[]{"R_PROVINCE", "R_PROVINCE_BOX", "R_PROVINCE_EU", "R_PROVINCE_ROTW", "R_PROVINCE_SEA",
                            "R_PROVINCE_TZ", "R_REGION", "R_BORDER"},
               "src/test/resources/com/mkl/eu/service/service/persistence/provinces", connection);
    }

    /**
     * Create a DataSet for the chat objects.
     *
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void exportChat(IDatabaseConnection connection) throws Exception {
        export(new String[]{"C_CHAT", "C_MESSAGE", "C_MESSAGE_GLOBAL", "C_PRESENT", "C_ROOM",
                            "C_ROOM_GLOBAL", "GAME", "COUNTRY"},
               "src/test/resources/com/mkl/eu/service/service/persistence/chatExport", connection);
    }

    /**
     * Create a DataSet for the referentiel objects.
     *
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void exportReferentiel(IDatabaseConnection connection) throws Exception {
        export(new String[]{"R_COUNTRY", "R_BASIC_FORCE", "R_LIMIT", "R_REINFORCEMENTS"},
                "src/test/resources/com/mkl/eu/service/service/persistence/referentiel", connection);
    }

    /**
     * Create a DataSet for the given tables on the given filename with the given database connection.
     *
     * @param tables     the name of the tables we want to export in the DataSet.
     * @param filename   the name of the file to export the DataSet.
     * @param connection to the database.
     * @throws Exception exception.
     */
    private static void export(String[] tables, String filename, IDatabaseConnection connection) throws Exception {
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        for (String table : tables) {
            partialDataSet.addTable(table);
        }

        ITableFilter filter = new DatabaseSequenceFilter(connection);
        IDataSet orderedDataSet = new FilteredDataSet(filter, partialDataSet);

        FlatXmlWriter dataSetWriter = new FlatXmlWriter(new FileOutputStream(filename + XML_EXTENSION));
        // DBUnit does not work when dtd is in the same folder as the dataSet. Using setColumnSensing instead.
        // FlatDtdDataSet.write(partialDataSet, new FileOutputStream(filename + DTD_EXTENSION));
        // dataSetWriter.setDocType(filename.substring(filename.lastIndexOf('/') + 1) + DTD_EXTENSION);
        dataSetWriter.write(orderedDataSet);
    }
}


