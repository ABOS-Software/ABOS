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

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class UsersGroupsAndYearsController {
    @FXML
    ListView<String> yearsList;
    @FXML
    ListView<User> allUsersList;
    @FXML
    ListView<User> yearUserList;
    @FXML
    ListView<Group> groupList;
    @FXML
    TreeView managedUserList;
    @FXML
    ComboBox<Group> userGroup;

    public UsersGroupsAndYearsController() {

    }

    /**
     * Initialize the contents of the frame.
     */
    public void initUsersGroupsAndYears(Window parWindow) throws Exception {
        yearsList.getItems().addAll(DbInt.getYears());
        allUsersList.getItems().addAll(DbInt.getUsers());
        allUsersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        yearUserList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        yearsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Year year = new Year(newValue);
            yearUserList.getItems().clear();
            try {
                Collection<User> users = year.getUsers();
                yearUserList.getItems().addAll(users);
                allUsersList.getItems().removeIf(user -> {
                    final boolean[] ret = {false};
                    String uName = user.getUserName();
                    users.forEach(user1 -> {
                        if (Objects.equals(uName, user1.getUserName())) {
                            ret[0] = true;
                        }
                    });
                    return ret[0];
                });
            } catch (Exception ignored) {
            }
            groupList.getItems().clear();
            groupList.getItems().addAll(Group.getGroupCollection(newValue));

        });
        yearUserList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //enable bottom pane
            //Add&Move managed Users
            User currentUser = observable.getValue();
            if (currentUser != null) {
                CheckBoxTreeItem<Object> yearItem = new CheckBoxTreeItem<>("");
                userGroup.getItems().clear();
                userGroup.getItems().addAll(Group.getGroupCollection(yearsList.getSelectionModel().getSelectedItem()));
                Group.getGroups(yearsList.getSelectionModel().getSelectedItem()).forEach(group -> {
                    try {
                        if (currentUser.getGroupId() == group.getID()) {
                            userGroup.getSelectionModel().select(group);
                        }
                    } catch (Group.GroupNotFoundException ignored) {
                    }

                    CheckBoxTreeItem<Object> groupItem = new CheckBoxTreeItem<>(group.getName());
                    final int[] numSelected = {0};
                    group.getUsers().forEach(user -> {

                        CheckBoxTreeItem<Object> userItem = new CheckBoxTreeItem<>(user);
                        if (currentUser.getuManage().contains(user.getUserName())) {
                            //userItem.setSelected(true);
                            numSelected[0]++;
                        }
                        userItem.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                            User user2 = yearUserList.getSelectionModel().getSelectedItem();
                            if (t1) {

                                ArrayList<String> uManage = user2.getuManage();
                                if (!uManage.contains(((User) userItem.getValue()).getUserName())) {
                                    uManage.add(((User) userItem.getValue()).getUserName());
                                    user2.setuManage(uManage);
                                }
                            } else {
                                ArrayList<String> uManage = user2.getuManage();
                                while (uManage.contains(((User) userItem.getValue()).getUserName())) {

                                    uManage.remove(((User) userItem.getValue()).getUserName());
                                }
                                user2.setuManage(uManage);
                            }
                        });
                        groupItem.getChildren().add(userItem);

                    });

                    groupItem.setExpanded(true);
                    yearItem.getChildren().add(groupItem);

                });
                managedUserList.setRoot(yearItem);
                managedUserList.setShowRoot(false);
                yearItem.setExpanded(true);
                managedUserList.setCellFactory(CheckBoxTreeCell.forTreeView());
                Platform.runLater(() -> {
                    for (TreeItem<Object> group : yearItem.getChildren()) {
                        for (TreeItem<Object> UserItem : group.getChildren()) {
                            User user = (User) UserItem.getValue();
                            if (yearUserList.getSelectionModel().getSelectedItem().getuManage().contains(user.getUserName())) {
                                ((CheckBoxTreeItem<Object>) UserItem).setSelected(true);
                            }
                        }
                    }
                });


                managedUserList.refresh();
            }
        });
        yearUserList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super User>) change -> {

            managedUserList.setDisable(yearUserList.getSelectionModel().getSelectedIndices().size() > 1);
        });
        userGroup.getSelectionModel().selectedItemProperty().addListener((observableValue, group, t1) -> {
            yearUserList.getSelectionModel().getSelectedItems().forEach(user -> {
                try {
                    user.setGroupId(t1.getID());
                } catch (Group.GroupNotFoundException e) {
                    e.printStackTrace();
                }
            });
        });

    }



    @FXML
    private void saveUser(ActionEvent event) {
        //add users to year
        // update uManage
        //update group
        Year year = new Year(yearsList.getSelectionModel().getSelectedItem());
        ArrayList<User> usersInYear = new ArrayList<User>();
        try {
            usersInYear = year.getUsers();
        } catch (Exception ignored) {
        }
        usersInYear.removeIf(user -> {
            final boolean[] ret = {false};
            String uName = user.getUserName();
            yearUserList.getItems().forEach(user1 -> {
                if (Objects.equals(uName, user1.getUserName())) {
                    ret[0] = true;
                }
            });
            return ret[0];
        });
        yearUserList.getItems().forEach(user -> {

            user.updateYear(yearsList.getSelectionModel().getSelectedItem());


            // user.removeFromYear(yearsList.getSelectionModel().getSelectedItem());


        });
        usersInYear.forEach(user -> {
            user.removeFromYear(yearsList.getSelectionModel().getSelectedItem());
        });


        //saved
        //Close OR put up dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Saved");
        alert.setHeaderText("Changes Saved.");
        alert.show();
    }

    @FXML
    private void moveUserToYear(ActionEvent event) {
        allUsersList.getSelectionModel().getSelectedItems().forEach(user -> {
            yearUserList.getItems().add(user);
            allUsersList.getItems().removeIf(user1 -> {
                final boolean[] ret = {false};
                String uName = user.getUserName();
                if (Objects.equals(uName, user1.getUserName())) {
                    ret[0] = true;
                }

                return ret[0];
            });
            allUsersList.getItems().remove(user);
        });
    }
    @FXML
    private void removeUserFromYear(ActionEvent event) {
        yearUserList.getSelectionModel().getSelectedItems().forEach(user -> {
            allUsersList.getItems().add(user);
            yearUserList.getItems().removeIf(user1 -> {
                final boolean[] ret = {false};
                String uName = user.getUserName();
                if (Objects.equals(uName, user1.getUserName())) {
                    ret[0] = true;
                }

                return ret[0];
            });
        });
    }

    @FXML
    private void deleteYear(ActionEvent event) {

    }

    @FXML
    private void editYear(ActionEvent event) {

    }

    @FXML
    private void addYear(ActionEvent event) {

    }

    @FXML
    private void deleteUser(ActionEvent event) {

    }

    @FXML
    private void editUser(ActionEvent event) {

    }

    @FXML
    private void addUser(ActionEvent event) {

    }

    @FXML
    private void deleteGroup(ActionEvent event) {

    }

    @FXML
    private void editGroup(ActionEvent event) {

    }

    @FXML
    private void addGroup(ActionEvent event) {

    }
}
