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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Group {
    private String name;
    private String year;

    public Group(String name, String year) {
        this.name = name;
        this.year = year;
    }

    public static Iterable<Group> getGroups(String year) {
        ArrayList<Group> groups = new ArrayList<>();
        try (PreparedStatement prep = DbInt.getPrep(year, "SELECT * FROM groups");
             ResultSet rs = prep.executeQuery()) {


            while (rs.next()) {

                groups.add(new Group(rs.getString("Name"), year));
                ////DbInt.pCon.close();
            }
        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }
        return groups;

    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public int getID() throws GroupNotFoundException {
        int gID = -1;
        try (PreparedStatement prep = DbInt.getPrep(year, "SELECT * FROM groups WHERE Name=?")) {

            prep.setString(1, name);
            try (ResultSet rs = prep.executeQuery()) {


                while (rs.next()) {

                    gID = rs.getInt("ID");
                    ////DbInt.pCon.close();
                }
            }
        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        }
        if (gID < 0) {
            throw new GroupNotFoundException();
        }
        return gID;
    }

    public Iterable<User> getUsers() {
        ArrayList<User> groups = new ArrayList<>();
        if (Objects.equals(name, "Ungrouped")) {
            try (PreparedStatement prep = DbInt.getPrep(year, "SELECT * FROM users WHERE groupId IS NULL")) {


                try (ResultSet rs = prep.executeQuery()) {


                    while (rs.next()) {

                        groups.add(new User(rs.getString("userName"), rs.getString("fullName"), rs.getString("uManage"), rs.getInt("groupID")));
                        ////DbInt.pCon.close();
                    }
                }
            } catch (SQLException e) {
                LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
            }
        }
        try (PreparedStatement prep = DbInt.getPrep(year, "SELECT * FROM users WHERE groupId=?")) {

            prep.setInt(1, getID());

            try (ResultSet rs = prep.executeQuery()) {


                while (rs.next()) {

                    groups.add(new User(rs.getString("userName"), rs.getString("fullName"), rs.getString("uManage"), rs.getInt("groupID")));
                    ////DbInt.pCon.close();
                }
            }
        } catch (SQLException e) {
            LogToFile.log(e, Severity.SEVERE, CommonErrors.returnSqlMessage(e));
        } catch (GroupNotFoundException e) {
            LogToFile.log(e, Severity.WARNING, "Group not found. Please retry the action.");
        }
        return groups;
    }

    public class GroupNotFoundException extends Exception {}
}
