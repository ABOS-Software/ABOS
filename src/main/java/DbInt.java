/*
 * Copyright (c) Patrick Magauran 2017.
 *   Licensed under the AGPLv3. All conditions of said license apply.
 *       This file is part of ABOS.
 *
 *       ABOS is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Affero General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       ABOS is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Affero General Public License for more details.
 *
 *       You should have received a copy of the GNU Affero General Public License
 *       along with ABOS.  If not, see <http://www.gnu.org/licenses/>.
 */

//import javax.swing.*;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
@SuppressWarnings("unused")
public class DbInt {
    public static Connection pCon = null;

/*    *//*
      Gets Data with specifed command and DB

      @param Db      THe database to retireve data from
     * @param command The command To execute
     * @return and ArrayList of the resulting Data
     * @deprecated true
     *//*
    @Deprecated
    public static List<String> getData(String Db, String command) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {

            LogToFile.log(e, Severity.SEVERE, "Error loading database library. Please try reinstalling or contacting support.");
        }

        //String Db = String.format("L&G%3",year);
        String url = String.format("jdbc:derby:%s/%s", Config.getDbLoc(), Db);
        System.setProperty("derby.system.home",
                Config.getDbLoc());
        List<String> res = new ArrayList<>();
        command = command.replaceAll("''", "/0027");

        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(command)) {


            while (rs.next()) {

                res.add(rs.getString(1).replaceAll("/0027", "'"));

            }
            // DriverManager.getConnection("jdbc:derby:;shutdown=true");
            //return rs;
        } catch (SQLException ex) {


            if (((ex.getErrorCode() == 50000)
                    && ("XJ015".equals(ex.getSQLState())))) {

                LogToFile.log(ex, Severity.FINER, "Derby shut down normally");

            } else {

                LogToFile.log(ex, Severity.SEVERE, ex.getMessage());
            }

        }

        return res;
    }*/

    /**
     * Gets the specified Customer info
     *
     * @param yearL The year to search
     * @param name  The customer name
     * @param info  The info to search for
     * @return A string with the resulting data
     */
    public static String getCustInf(String yearL, String name, String info) {
        return getCustInf(yearL, name, info, "");
    }

    /**
     * Gets the specified Customer info
     *
     * @param yearL      The year to search
     * @param name       The customer name
     * @param info       The info to search for
     * @param defaultVal The default value to return if there is no data
     * @return A string with the resulting data
     */
    public static String getCustInf(String yearL, String name, String info, String defaultVal) {
        String ret = defaultVal;

        try (PreparedStatement prep = DbInt.getPrep(yearL, "SELECT * FROM CUSTOMERS WHERE NAME=?")) {


            prep.setString(1, name);
            try (ResultSet rs = prep.executeQuery()) {

                while (rs.next()) {

                    ret = rs.getString(info);

                }
            }
            ////DbInt.pCon.close();

        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }


        return ret;
    }

    /**
     * Creates a Prepared statemtn from provided Parameters.
     *
     * @param Db      The database to create the statement for
     * @param Command The Base command for the statement
     * @return the PreparedStatemtn that was created.
     */
    public static PreparedStatement getPrep(String Db, String Command) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {

            LogToFile.log(e, Severity.SEVERE, "Error loading database library. Please try reinstalling or contacting support.");
        }
        Statement st = null;
        ResultSet rs = null;
        pCon = null;
        //String Db = String.format("L&G%3",year);
        String url = String.format("jdbc:derby:%s/%s", Config.getDbLoc(), Db);
        System.setProperty("derby.system.home",
                Config.getDbLoc());
        try {


            pCon = DriverManager.getConnection(url);
            pCon.setAutoCommit(true);
            // DriverManager.getConnection("jdbc:derby:;shutdown=true");
            //return rs;


            return pCon.prepareStatement(Command, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        } catch (SQLException ex) {


            if (((ex.getErrorCode() == 50000)
                    && ("XJ015".equals(ex.getSQLState())))) {

                LogToFile.log(ex, Severity.FINER, "Derby shut down normally");

            } else {
                if (Objects.equals(ex.getSQLState(), "XJ004")) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("ERROR!");
                    alert.setHeaderText("The program cannot find the specified database");
                    alert.setContentText("Would you like to open the settings Dialog to create it?");


                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        new Settings(null);
                        return getPrep(Db, Command);
                    } else {
                        Alert closingWarning = new Alert(Alert.AlertType.WARNING);
                        closingWarning.setTitle("Warning!");
                        closingWarning.setHeaderText("The program cannot run withou the database");
                        closingWarning.setContentText("Application is closing. Please restart application and create the database in the setting dialog.");


                        closingWarning.showAndWait();
                        System.exit(0);
                    }
                    LogToFile.log(ex, Severity.SEVERE, "");
                } else {
                    LogToFile.log(ex, Severity.WARNING, "");
                }
            }

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }


            } catch (SQLException ex) {
                LogToFile.log(ex, Severity.WARNING, ex.getMessage());
            }
        }
        return null;
    }

/*    *//*
      Gets # of collumns in a table

      @param Db    The DB the table is in
     * @param Table the Table to get number of columns from
     * @return An integer with number of columns
     *//*
    public static int getNoCol(String Db, String Table) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {

            LogToFile.log(e, Severity.SEVERE, "Error loading database driver. Please try reinstalling the software or contacting support.");
        }
        int columnsNumber = 0;

        //String Db = String.format("L&G%3",year);
        String url = String.format("jdbc:derby:%s/%s", Config.getDbLoc(), Db);
        System.setProperty("derby.system.home",
                Config.getDbLoc());
        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(String.format("SELECT * FROM %s", Table))) {


            ResultSetMetaData rsmd = rs.getMetaData();

            columnsNumber = rsmd.getColumnCount();
            // DriverManager.getConnection("jdbc:derby:;shutdown=true");
            //return rs;
        } catch (SQLException ex) {


            if (((ex.getErrorCode() == 50000)
                    && ("XJ015".equals(ex.getSQLState())))) {

                LogToFile.log(ex, Severity.FINER, "Derby shut down normally");

            } else {

                LogToFile.log(ex, Severity.SEVERE, ex.getMessage());
            }

        }

        return columnsNumber;
    }*/

/*    *//*
      Writes data to A DB

      @param Db      The DB to write to
     * @param command THe command to execute
     * @deprecated true
     *//*
    @Deprecated
    public static void writeData(String Db, String command) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {

            LogToFile.log(e, Severity.SEVERE, "Error loading database driver. Please try reinstalling the software or contacting support.");
        }

        //String Db = String.format("L&G%3",year);
        String url = String.format("jdbc:derby:%s/%s", Config.getDbLoc(), Db);
        System.setProperty("derby.system.home",
                Config.getDbLoc());

        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement()
        ) {

            st.executeUpdate(command);


            // DriverManager.getConnection("jdbc:derby:;shutdown=true");
            //return rs;
        } catch (SQLException ex) {


            if (((ex.getErrorCode() == 50000)
                    && ("XJ015".equals(ex.getSQLState())))) {

                LogToFile.log(ex, Severity.FINER, "Derby shut down normally");

            } else {

                LogToFile.log(ex, Severity.SEVERE, ex.getMessage());
            }

        }


    }*/

    /**
     * Creates a database with specified name
     *
     * @param DB The name of the DB to create
     */
    public static Boolean createDb(String DB) {


        String url = String.format("jdbc:derby:%s/%s;create=true", Config.getDbLoc(), DB);//;create=true

        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement()) {


        } catch (SQLException ex) {


            if (((ex.getErrorCode() == 50000)
                    && ("XJ015".equals(ex.getSQLState())))) {

                LogToFile.log(ex, Severity.FINER, "Derby shut down normally");

            } else if (ex.getErrorCode() == 1007) {
                return false;
            } else {

                LogToFile.log(ex, Severity.SEVERE, ex.getMessage());
            }

        }
        return true;
    }

    public static void createSetAndTables() {
        DbInt.createDb("Set");

        try (PreparedStatement prep = DbInt.getPrep("Set", "CREATE TABLE Customers(CustomerID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), Address varchar(255), Town VARCHAR(255), STATE VARCHAR(255), ZIPCODE VARCHAR(6), Lat float(15), Lon float(15), Ordered VARChAR(255), NI VARChAR(255), NH VARChAR(255))")) {
            prep.execute();
        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }
        try (PreparedStatement prep = DbInt.getPrep("Set", "CREATE TABLE YEARS(ID int PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), YEARS varchar(255))")) {
            prep.execute();
        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }
    }

    public static void deleteDb(String DB) {


        String url = String.format("%s/%s", Config.getDbLoc(), DB);
        File oldName = new File(url);
        DateFormat df = new SimpleDateFormat("MMDDYYYY-HH:MM:SS");
        java.util.Date dateobj = new java.util.Date();
        //create destination File object
        File newName = new File(url + ".bak-" + df.format(dateobj));
        boolean isFileRenamed = oldName.renameTo(newName);


    }

    public static Iterable<String> getAllCustomers() {
        Collection<String> ret = new ArrayList<>();
        Iterable<String> years = getYears();
        for (String year : years) {

            try (PreparedStatement prep = DbInt.getPrep(year, "SELECT NAME FROM Customers");
                 ResultSet rs = prep.executeQuery()
            ) {


                while (rs.next()) {
                    String name = rs.getString("NAME");
                    if (!ret.contains(name)) {
                        ret.add(name);
                    }

                }
                ////DbInt.pCon.close();

            } catch (SQLException e) {
                LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
            }
        }


        return ret;
    }

    public static Iterable<String> getYears() {
        Collection<String> ret = new ArrayList<>();
        try (PreparedStatement prep = DbInt.getPrep("Set", "SELECT YEARS FROM Years");
             ResultSet rs = prep.executeQuery()) {


            while (rs.next()) {

                ret.add(rs.getString("YEARS"));

            }
            ////DbInt.pCon.close();

        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }


        return ret;
    }

    public static String getCategoryDate(String catName, String year) {
        Date ret = null;
        try (PreparedStatement prep = DbInt.getPrep(year, "SELECT Date FROM Categories WHERE Name=?")) {


            prep.setString(1, catName);

            try (ResultSet rs = prep.executeQuery()) {

                while (rs.next()) {

                    ret = rs.getDate(1);

                }
            }
            ////DbInt.pCon.close();

        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }
        String output;
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy");
        output = formatter.format(ret);
        return output;
    }


// --Commented out by Inspection START (1/2/2016 12:01 PM):
//    /**
//     * Closes the database connection.
//     */
//    public void close() {
//        try {
//            DriverManager.getConnection("jdbc:derby:;shutdown=true");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
// --Commented out by Inspection STOP (1/2/2016 12:01 PM)

}