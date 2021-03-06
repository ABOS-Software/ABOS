/*******************************************************************************
 * ABOS
 * Copyright (C) 2018 Patrick Magauran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package Controllers;

import Exceptions.AccessException;
import Launchers.AddGroup;
import Utilities.*;
import Workers.LoadUGYWorker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.commons.lang3.RandomStringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

//import org.w3c.dom.*;

public class UsersGroupsAndYearsController {
    @FXML
    TabPane tabPane;
    @FXML
    TitledPane enabledUsersPane;
    @FXML
    TitledPane disabledUserPane;
    @FXML
    VBox groupVbox;
    @FXML
    VBox enabledUserVbox;
    @FXML
    ScrollPane enabledUsersScrollPane;
    @FXML
    TitledPane archivedUsersPane;
    @FXML
    VBox archivedUserVbox;
    @FXML
    ScrollPane archivedUsersScrollPane;
    @FXML
    ScrollPane disabledUsersScrollPane;
    @FXML
    ScrollPane groupScrollPane;
    @FXML
    VBox disabledUserVbox;
    @FXML
    TreeView<TreeItemPair<String, Pair<String, Object>>> summaryList;
    @FXML
    MenuButton userMenuBtn;
    @FXML
    SplitMenuButton addUserSplitBtn;
    @FXML
    MenuButton groupMenuBtn;
    @FXML
    Tab productsTab;
    @FXML
    Tab usersTab;
    @FXML
    Tab groupsTab;
    Window parentWindow;
    @FXML
    private TableView<formattedProductProps> ProductTable;
    @FXML
    private TextField itemTb;
    @FXML
    private TextField sizeTb;
    @FXML
    private TextField rateTb;
    @FXML
    private TextField idTb;
    //private final JDialog parent;
    //UserName -> year -> UserObject
    private Map<String, Map<String, User>> cachedUsers = new HashMap<>();
    //year -> groups
    private Map<String, ArrayList<Group>> cachedGroups = new HashMap<>();

    private Collection<Year.category> rowsCats = new ArrayList<Year.category>();
    private ObservableList<String> categoriesTb = FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> categoriesCmbx;
    private ObservableList<formattedProductProps> data = FXCollections.observableArrayList();
    private Map<User, ArrayList<String>> checkedUsers = new HashMap();
    private Map<Group, ArrayList<String>> checkedGroups = new HashMap();
    private Map<User, User.STATUS> selectedUsers = new HashMap<User, User.STATUS>();
    private ArrayList<Group> selectedGroups = new ArrayList<>();
    private ArrayList<CheckBox> userPaneCheckboxes = new ArrayList<>();
    private ArrayList<CheckBox> groupPaneCheckboxes = new ArrayList<>();
    private Map<User, Node> allUsers = new HashMap();
    private Map<Group, Node> allGroups = new HashMap();
    // private Map<String, ArrayList<String>> checkedFullName = new HashMap();

    private Map<User, Integer> groups = new HashMap<>();
    private boolean isRightClick;
    private String curYear = "";

    public UsersGroupsAndYearsController() {

    }

    /**
     * Initialize the contents of the frame.
     */
    public void initUsersGroupsAndYears(Window parWindow) throws AccessException {
        parentWindow = parWindow;
        fillTreeView(null);

        summaryList.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isSecondaryButtonDown()) {
                isRightClick = true;

            }
            if (event.getClickCount() == 2) {

                Platform.runLater(() -> {
                    TreeItemPair<String, Pair<String, Object>> newValue = summaryList.getSelectionModel().getSelectedItem().getValue();
                    if (isRightClick) {
                        //reset the flag
                        isRightClick = false;
                    } else if (newValue != null && !Objects.equals(newValue.getValue().getValue(), "RootNode")) {

                        switch (newValue.getValue().getKey()) {
                            case "Year":
                                openYear(newValue.getKey());
                                break;
                            case "Group":
                                String year = "";
                                if (summaryList.getSelectionModel().getSelectedItem().getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = summaryList.getSelectionModel().getSelectedItem().getParent().getValue().getKey();
                                } else if (summaryList.getSelectionModel().getSelectedItem().getParent().getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = summaryList.getSelectionModel().getSelectedItem().getParent().getParent().getValue().getKey();

                                }
                                openGroup((Group) newValue.getValue().getValue(), year);
                                break;
                            case "User":
                                year = "";
                                if (summaryList.getSelectionModel().getSelectedItem().getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = summaryList.getSelectionModel().getSelectedItem().getParent().getValue().getKey();
                                } else if (summaryList.getSelectionModel().getSelectedItem().getParent().getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = summaryList.getSelectionModel().getSelectedItem().getParent().getParent().getValue().getKey();

                                }
                                openUser((User) newValue.getValue().getValue(), year);

                                break;


                        }
                    }
                });
            }
        });

        summaryList.setCellFactory(p -> new TreeCellImpl());
    }

    private void fillTreeView(actionCallback cb) {
        //ProgressDialog progDial = new ProgressDialog();
        ProgressForm progDial = new ProgressForm();
//Do check if new or not, send -1 as ID

        LoadUGYWorker loadWorker = new LoadUGYWorker(this);

        progDial.activateProgressBar(loadWorker);
        loadWorker.setOnSucceeded(event -> {
            summaryList.setRoot(loadWorker.getValue().getLeft());
            cachedUsers = loadWorker.getValue().getMiddle();
            cachedGroups = loadWorker.getValue().getRight();
            progDial.getDialogStage().close();
            if (cb != null) {
                cb.doAction();
            }
        });
        loadWorker.setOnFailed(event -> {
            progDial.getDialogStage().close();

            Throwable e = loadWorker.getException();

            if (e instanceof SQLException) {
                LogToFile.log((SQLException) e, Severity.SEVERE, CommonErrors.returnSqlMessage(((SQLException) loadWorker.getException())));

            }
            if (e instanceof InterruptedException) {
                if (loadWorker.isCancelled()) {
                    LogToFile.log((InterruptedException) e, Severity.FINE, "Load process canceled.");

                }
            }


        });


        progDial.getDialogStage().show();
        new Thread(loadWorker).start();
    }

    private void showTabs() {
        productsTab.setDisable(false);
        usersTab.setDisable(false);
        groupsTab.setDisable(false);
    }

    private void hideTabs() {
        productsTab.setDisable(true);
        usersTab.setDisable(true);
        groupsTab.setDisable(true);

    }

    private void openYear(String year) {
        archivedUserVbox.getChildren().clear();
        groupVbox.getChildren().clear();
        disabledUserVbox.getChildren().clear();
        enabledUserVbox.getChildren().clear();

        curYear = year;
        Year yearObj = new Year(year);
        //ArrayList<User> users = DbInt.getUsers();
        allUsers.clear();
        allGroups.clear();
        selectedUsers.clear();
        userPaneCheckboxes.clear();
        checkedUsers.clear();
        // checkedFullName.clear();
        cachedUsers.forEach((key, value) -> {
            User user = value.getOrDefault(year, null);
            User.STATUS status;

            TitledPane userPane = new TitledPane();
            if (user != null) {
                if (user.getYears().contains(year)) {
                    status = User.STATUS.ENABLED;
                    enabledUserVbox.getChildren().add(userPane);
                } else {
                    status = User.STATUS.ARCHIVED;
                    archivedUserVbox.getChildren().add(userPane);
                }
            } else {
                status = User.STATUS.DISABLED;
                user = value.get("DB");
                disabledUserVbox.getChildren().add(userPane);

            }
            CheckBox selectedCheckBox = new CheckBox(user.getFullName() + " (" + user.getUserName() + ")");
            selectedCheckBox.setMinSize(CheckBox.USE_PREF_SIZE, CheckBox.USE_PREF_SIZE);
            User finalUser = user;
            userPaneCheckboxes.add(selectedCheckBox);
            selectedCheckBox.selectedProperty().addListener((ob, oldVal, newVal) -> {
                if (newVal) {
                    if (!selectedUsers.containsKey(finalUser)) {
                        selectedUsers.put(finalUser, status);

                    }
                } else {
                    if (selectedUsers.containsKey(finalUser)) {
                        selectedUsers.remove(finalUser, status);

                    }
                }
            });
            //selectedCheckBox.setStyle("-fx-padding: 0px; -fx-border-color: red; -fx-border-width: 2px");
            Button editButton = new Button("Edit");
            editButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
            editButton.setOnAction(event -> {
                editUser(finalUser);
            });
            Button deleteBtn = new Button("Delete");
            deleteBtn.setOnAction(event -> {
                deleteUser(finalUser);
            });
            deleteBtn.getStyleClass().add("redBtn");
            deleteBtn.setStyle("-fx-background-color: #ff4d4d");

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMinSize(10, 1);
            spacer.setMaxWidth(Double.MAX_VALUE);
            //BorderPane header = new BorderPane();
            VBox headerRoot = new VBox();
            headerRoot.setFillWidth(true);
            HBox header = new HBox();
            //header.setStyle("-fx-border-color: orange; -fx-border-width: 2px");
            //header.setPrefWidth(HBox.USE_PREF_SIZE);
            header.minWidthProperty().bind(userPane.widthProperty().subtract(75));
            //userPane.prefWidthProperty().bindBidirectional(header.prefWidthProperty());
            //userPane.minWidthProperty().bindBidirectional(header.minWidthProperty());
            //selectedCheckBox.setAlignment(Pos.CENTER_LEFT);
            //editButton.setAlignment(Pos.);
            //header.setLeft(selectedCheckBox);
            //header.setRight(editButton);
            header.getChildren().setAll(selectedCheckBox, spacer, deleteBtn, editButton);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMinSize(10, 1);
            userPane.setText(user.getFullName() + " (" + user.getUserName() + ")");
            headerRoot.getChildren().setAll(header);
            userPane.setGraphic(header);
            userPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            userPane.setExpanded(false);
            //userPane.setPrefSize(Region.USE_COMPUTED_SIZE,700);
            // userPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            userPane.getStyleClass().add("informationPane");

            VBox.setVgrow(userPane, Priority.ALWAYS);
            FlowPane pane = new FlowPane();


            ComboBox<TreeItemPair<String, Integer>> groupBox = new ComboBox<>();
            TreeView<TreeItemPair<String, String>> yearTView;
            CheckBoxTreeItem<TreeItemPair<String, String>> yearItem = new CheckBoxTreeItem<TreeItemPair<String, String>>(new TreeItemPair<>(year, ""));
            User currentUser = user;
            if (status != User.STATUS.DISABLED) {
                cachedGroups.get(year).forEach(group -> {
                    CheckBoxTreeItem<TreeItemPair<String, String>> groupItem = new CheckBoxTreeItem<TreeItemPair<String, String>>(new TreeItemPair<>(group.getName(), ""));

                    group.getUsers().forEach(user2 -> {
                        CheckBoxTreeItem<TreeItemPair<String, String>> userItem = createUserTreeItem(new TreeItemPair<>(user2.getFullName(), user2.getUserName()), currentUser);
                        if (currentUser.getuManage().contains(user2.getUserName())) {
                            userItem.setSelected(true);
                            checkedUsers.computeIfPresent(currentUser, (k, v) -> {
                                if (!v.contains(user2.getUserName())) {

                                    v.add(user2.getUserName());
                                }
                                return v;
                            });
                            checkedUsers.computeIfAbsent(currentUser, k -> {
                                ArrayList<String> v = new ArrayList();
                                v.add(user2.getUserName());
                                return v;
                            });
                        /*checkedFullName.compute(year, (k, v) -> {
                            ArrayList<String> vArray = new ArrayList();
                            vArray.addAll(v);
                            vArray.add(user2.getFullName());
                            return vArray;
                        });
                       checkedFullName.computeIfAbsent(year, k -> {
                            ArrayList<String> v = new ArrayList();
                            v.add(user2.getFullName());
                            return v;
                        });*/
                        }
                        groupItem.getChildren().add(userItem);
                    });
                    yearItem.getChildren().add(groupItem);
                    try {
                        groupBox.getItems().add(new TreeItemPair<String, Integer>(group.getName(), group.getID()));
                        if (currentUser.getGroupId() == group.getID()) {
                            groupBox.getSelectionModel().selectLast();
                        } else if (currentUser.getGroupId() == 0) {
                            groupBox.getSelectionModel().selectFirst();

                        }

                    } catch (Group.GroupNotFoundException ignored) {
                    }

                });

                yearTView = new TreeView(yearItem);
                yearTView.setPrefHeight(TreeView.USE_COMPUTED_SIZE);
                yearTView.setMinHeight(70);
                yearTView.setPrefWidth(TreeView.USE_COMPUTED_SIZE);
                yearTView.getStyleClass().add("lightTreeView");
                yearItem.setExpanded(true);
                yearTView.setCellFactory(CheckBoxTreeCell.forTreeView());
                yearTView.setFixedCellSize(35);

                yearTView.expandedItemCountProperty().addListener((ob, oldVal, newVal) -> {
                    Platform.runLater(() -> {
                        yearTView.setPrefHeight(newVal.doubleValue() * (yearTView.getFixedCellSize()) + 10);
                        yearTView.setMinHeight(newVal.doubleValue() * (yearTView.getFixedCellSize()) + 10);
                        //yearTView.scrollTo(yearTView.getSelectionModel().getSelectedIndex());
                        yearTView.refresh();
                    });

                });
                yearTView.refresh();
                groupBox.getSelectionModel().selectedItemProperty().addListener(observable -> {

                    groups.put(currentUser, groupBox.getSelectionModel().getSelectedItem().getValue());
                });
                VBox.setVgrow(yearTView, Priority.ALWAYS);
                VBox center = new VBox(10, new Label("Users to manage"), yearTView);
                center.setFillWidth(true);

                BorderPane contents = new BorderPane(center, new HBox(10, new Label("Group to be a part of"), groupBox), null, null, null);
                contents.getStyleClass().add("containerPane");
                contents.setStyle("-fx-border: 0; -fx-border-insets: 0");
                contents.autosize();
                userPane.setContent(contents);


                groups.put(currentUser, groupBox.getSelectionModel().getSelectedItem().getValue());
            }
            allUsers.put(user, userPane);

        });


        cachedGroups.get(year).forEach(group -> {
            // TreeItem<TreeItemPair<String, String>> groupRoot = new TreeItem<TreeItemPair<String, String>>(new TreeItemPair<>(year, ""));

            TitledPane groupPane = new TitledPane();

            Label titleLabel = new Label(group.getName());
            titleLabel.setMinSize(CheckBox.USE_PREF_SIZE, CheckBox.USE_PREF_SIZE);

            SplitMenuButton editButton = new SplitMenuButton();
            editButton.setText("Edit");
            editButton.setOnAction(event -> {
                editGroup(group);
            });
            Button deleteBtn = new Button("Delete");
            deleteBtn.setOnAction(event -> {
                deleteGroup(group);

            });
            deleteBtn.getStyleClass().add("redBtn");
            deleteBtn.setStyle("-fx-background-color: #ff4d4d");

            MenuItem removeAllFromGroup = new MenuItem("Remove all From group");
            removeAllFromGroup.setOnAction(event -> {
                checkedGroups.forEach((grp, Users) -> {
                    Users.forEach(usrString -> {
                        User usr = new User(usrString, year, true);
                        usr.setGroupId(1);
                        usr.updateYear(year);
                    });
                });
                refresh(null);
            });
            editButton.getItems().add(removeAllFromGroup);
            editButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMinSize(10, 1);
            spacer.setMaxWidth(Double.MAX_VALUE);
            //BorderPane header = new BorderPane();
            VBox headerRoot = new VBox();
            headerRoot.setFillWidth(true);
            HBox header = new HBox();
            //header.setStyle("-fx-border-color: orange; -fx-border-width: 2px");
            //header.setPrefWidth(HBox.USE_PREF_SIZE);
            header.minWidthProperty().bind(groupPane.widthProperty().subtract(75));
            //userPane.prefWidthProperty().bindBidirectional(header.prefWidthProperty());
            //userPane.minWidthProperty().bindBidirectional(header.minWidthProperty());
            //selectedCheckBox.setAlignment(Pos.CENTER_LEFT);
            //editButton.setAlignment(Pos.);
            //header.setLeft(selectedCheckBox);
            //header.setRight(editButton);
            header.getChildren().setAll(titleLabel, spacer, deleteBtn, editButton);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMinSize(10, 1);
            headerRoot.getChildren().setAll(header);
            groupPane.setGraphic(header);
            groupPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            groupPane.setExpanded(false);
            //userPane.setPrefSize(Region.USE_COMPUTED_SIZE,700);
            // userPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            groupPane.getStyleClass().add("informationPane");

            groupVbox.getChildren().add(groupPane);


            CheckBoxTreeItem<TreeItemPair<String, String>> groupItem = createGroupTreeItem(new TreeItemPair<>(group.getName(), ""), group);
            group.getUsers().forEach(user2 -> {
                CheckBoxTreeItem<TreeItemPair<String, String>> userItem = createGroupTreeItem(new TreeItemPair<>(user2.getFullName(), user2.getUserName()), group);

                groupItem.getChildren().add(userItem);
            });
            // groupRoot.getChildren().add(groupItem);

            TreeView<TreeItemPair<String, String>> groupTvView = new TreeView(groupItem);
            groupTvView.setShowRoot(false);
            groupTvView.setPrefHeight(TreeView.USE_COMPUTED_SIZE);
            groupTvView.setMinHeight(70);
            groupTvView.setPrefWidth(TreeView.USE_COMPUTED_SIZE);
            groupTvView.getStyleClass().add("lightTreeView");
            groupItem.setExpanded(true);
            groupTvView.setCellFactory(CheckBoxTreeCell.forTreeView());
            groupTvView.setFixedCellSize(35);

            groupTvView.expandedItemCountProperty().addListener((ob, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    groupTvView.setPrefHeight(newVal.doubleValue() * (groupTvView.getFixedCellSize()) + 10);
                    groupTvView.setMinHeight(newVal.doubleValue() * (groupTvView.getFixedCellSize()) + 10);
                    //yearTView.scrollTo(yearTView.getSelectionModel().getSelectedIndex());
                    groupTvView.refresh();
                });

            });
            groupTvView.refresh();

            groupPane.setContent(new ScrollPane(groupTvView));
            allGroups.put(group, groupPane);

        });

        initProductsTab();
        showTabs();
    }

    private void openUser(User user, String year) {
        openYear(year);


        if (user.getYears().contains(year)) {

            TitledPane userPane = (TitledPane) allUsers.get(user);
            userPane.setExpanded(true);
            enabledUsersPane.setExpanded(true);
            double vvalue = userPane.getLayoutX() / (userPane.getHeight() - enabledUsersScrollPane.getHeight());

            enabledUsersScrollPane.setVvalue(vvalue);
        } else {
            Year yearObj = new Year(year);
            if (yearObj.getUsers().contains(user)) {
                TitledPane userPane = (TitledPane) allUsers.get(user);
                userPane.setExpanded(true);
                archivedUsersPane.setExpanded(true);
                double vvalue = userPane.getBoundsInParent().getMinY() / (userPane.getHeight() - archivedUsersScrollPane.getHeight());

                archivedUsersScrollPane.setVvalue(vvalue);
            } else {
                TitledPane userPane = (TitledPane) allUsers.get(user);
                userPane.setExpanded(true);
                disabledUserPane.setExpanded(true);
                double vvalue = userPane.getBoundsInParent().getMinY() / (userPane.getHeight() - disabledUsersScrollPane.getHeight());

                disabledUsersScrollPane.setVvalue(vvalue);
            }


        }
        tabPane.getSelectionModel().select(0);
    }

    private void openGroup(Group group, String year) {
        openYear(year);


        TitledPane groupPane = (TitledPane) allGroups.get(group);
        groupPane.setExpanded(true);
        double vvalue = groupPane.getBoundsInParent().getMinY() / (groupPane.getHeight() - disabledUsersScrollPane.getHeight());

        groupScrollPane.setVvalue(vvalue);
        tabPane.getSelectionModel().select(1);

    }


    @FXML
    private void addSelectedUsersToGroup(ActionEvent event) {

        Dialog<Group> dialog = new Dialog<>();
        dialog.setTitle("Select Group");

// Set the button types.
        ButtonType login = new ButtonType("Group", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(login, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Group> groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll(Group.getGroupCollection(curYear));

        grid.add(new Label("Group:"), 0, 0);
        grid.add(groupComboBox, 1, 0);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node loginButton = dialog.getDialogPane().lookupButton(login);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).

        groupComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue == null));

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> groupComboBox.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == login) {
                return groupComboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Group> result = dialog.showAndWait();

        result.ifPresent(group -> {
            Boolean alertDisabled = false;
            selectedUsers.forEach((user, status) -> {
                if (status != User.STATUS.DISABLED) {
                    try {
                        user.setGroupId(group.getID());
                    } catch (Group.GroupNotFoundException ignored) {
                    }
                    user.updateYear(curYear);
                }

            });
            if (selectedUsers.containsValue(User.STATUS.DISABLED)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Operation applied. Disabled User(s) were skipped.");
                alert.show();
            }
            selectedUsers.clear();
            unselectAllUserPanes();
        });


    }

    private void unselectAllUserPanes() {
        userPaneCheckboxes.forEach(checkBox -> {
            checkBox.setSelected(false);
        });
    }

    private void unselectAllGroupUsers() {

    }

    @FXML
    private void disableSelectedUsers(ActionEvent event) {
        Optional<Group> returnGroup = Optional.empty();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("REMOVE USERS FROM YEAR?");
        dialog.setHeaderText("This will delete ALL customers and data associated with these users for the selected year.");
// Set the button types.
        ButtonType addGrp = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addGrp, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField verifyUNameTF = new TextField();
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=").toCharArray();
        String randomStr = RandomStringUtils.random(7, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        grid.add(new Label("Please enter the verification code for confirmation:"), 0, 0);
        grid.add(verifyUNameTF, 1, 0);
        Label verificationCode = new Label(randomStr);
        verificationCode.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-color: black");
        grid.add(new Label("Verification Code: "), 0, 1);
        grid.add(verificationCode, 1, 1);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node deleteUserButton = dialog.getDialogPane().lookupButton(addGrp);
        deleteUserButton.setDisable(true);
        deleteUserButton.setStyle("fx-background-color: Red; fx-color: White");
// Do some validation (using the Java 8 lambda syntax).
        verifyUNameTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, randomStr)) {
                deleteUserButton.setDisable(false);
            } else {
                deleteUserButton.setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> verifyUNameTF.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addGrp) {
                return verifyUNameTF.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(res -> {
            if (res.equals(randomStr)) {
                selectedUsers.forEach((user, status) -> {
                    user.deleteFromYear(curYear);

                });
                hideTabs();
                openYear(curYear);
            }
        });
    }

    @FXML
    private void archiveSelectedUsers(ActionEvent event) {
        Optional<Group> returnGroup = Optional.empty();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("ARCHIVE SELECTED USERS?");
        dialog.setHeaderText("Selected users will no longer have access to the selected year.");
// Set the button types.
        ButtonType addGrp = new ButtonType("Archive", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addGrp, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField verifyUNameTF = new TextField();
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=").toCharArray();
        String randomStr = RandomStringUtils.random(7, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        grid.add(new Label("Please enter the verification code for confirmation:"), 0, 0);
        grid.add(verifyUNameTF, 1, 0);
        Label verificationCode = new Label(randomStr);
        verificationCode.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-color: black");
        grid.add(new Label("Verification Code: "), 0, 1);
        grid.add(verificationCode, 1, 1);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node deleteUserButton = dialog.getDialogPane().lookupButton(addGrp);
        deleteUserButton.setDisable(true);
        deleteUserButton.setStyle("fx-background-color: Red; fx-color: White");
// Do some validation (using the Java 8 lambda syntax).
        verifyUNameTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, randomStr)) {
                deleteUserButton.setDisable(false);
            } else {
                deleteUserButton.setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> verifyUNameTF.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addGrp) {
                return verifyUNameTF.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(res -> {
            if (res.equals(randomStr)) {
                selectedUsers.forEach((user, status) -> {
                    Set<String> yrs = user.getYears();
                    yrs.remove(curYear);
                    user.setYears(yrs);
                    user.updateYear(curYear);

                });
                hideTabs();
                openYear(curYear);
            }
        });
    }

    @FXML
    private void enableSelectedUsers(ActionEvent event) {
        selectedUsers.forEach((user, status) -> {
            if (status == User.STATUS.DISABLED) {
                ArrayList<String> uMan = new ArrayList<>();
                uMan.add(user.getUserName());
                user.setuManage(uMan);
                Set<String> yrs = user.getYears();
                yrs.add(curYear);
                user.setYears(yrs);
                user.updateYear(curYear);
            } else if (status == User.STATUS.ARCHIVED) {
                Set<String> yrs = user.getYears();
                yrs.add(curYear);
                user.setYears(yrs);
                user.updateYear(curYear);
            }
        });
        hideTabs();
        openYear(curYear);
    }

    @FXML
    private void addSelectedUsersToUser(ActionEvent event) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Select User as Manager");

// Set the button types.
        ButtonType login = new ButtonType("Set Manager", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(login, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<User> groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll(new Year(curYear).getUsers());

        grid.add(new Label("User:"), 0, 0);
        grid.add(groupComboBox, 1, 0);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node loginButton = dialog.getDialogPane().lookupButton(login);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).

        groupComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue == null));

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> groupComboBox.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == login) {
                return groupComboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            Boolean alertDisabled = false;

            ArrayList<String> uManage = user.getuManage();
            selectedUsers.forEach((selectedUser, status) -> {
                if (!uManage.contains(selectedUser.getUserName())) {
                    uManage.add(selectedUser.getUserName());
                }
            });
            user.setuManage(uManage);


            user.updateYear(curYear);


            hideTabs();
            openYear(curYear);
        });
    }

    @FXML
    private void cancelUser(ActionEvent event) {

    }

    private void deleteUser(User user) {
        Optional<Group> returnGroup = Optional.empty();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("REMOVE USER FROM YEAR?");
        dialog.setHeaderText("This will delete ALL customers and data associated with this user for the selected year.");
// Set the button types.
        ButtonType addGrp = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addGrp, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField verifyUNameTF = new TextField();
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=").toCharArray();
        String randomStr = RandomStringUtils.random(7, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        grid.add(new Label("Please enter the verification code for confirmation:"), 0, 0);
        grid.add(verifyUNameTF, 1, 0);
        Label verificationCode = new Label(randomStr);
        verificationCode.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-color: black");
        grid.add(new Label("Verification Code: "), 0, 1);
        grid.add(verificationCode, 1, 1);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node deleteUserButton = dialog.getDialogPane().lookupButton(addGrp);
        deleteUserButton.setDisable(true);
        deleteUserButton.setStyle("fx-background-color: Red; fx-color: White");
// Do some validation (using the Java 8 lambda syntax).
        verifyUNameTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, randomStr)) {
                deleteUserButton.setDisable(false);
            } else {
                deleteUserButton.setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> verifyUNameTF.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addGrp) {
                return verifyUNameTF.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(res -> {
            if (res.equals(randomStr)) {
                user.deleteFromYear(curYear);
                hideTabs();
                openYear(curYear);
            }
        });
    }

    private void deleteGroup(Group group) {
        Optional<Group> returnGroup = Optional.empty();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete Group");
        dialog.setHeaderText("This will move all the users to the ungrouped group.");
// Set the button types.
        ButtonType addGrp = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addGrp, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField verifyUNameTF = new TextField();
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=").toCharArray();
        String randomStr = RandomStringUtils.random(7, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        grid.add(new Label("Please enter the verification code for confirmation:"), 0, 0);
        grid.add(verifyUNameTF, 1, 0);
        Label verificationCode = new Label(randomStr);
        verificationCode.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-color: black");
        grid.add(new Label("Verification Code: "), 0, 1);
        grid.add(verificationCode, 1, 1);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node deleteUserButton = dialog.getDialogPane().lookupButton(addGrp);
        deleteUserButton.setDisable(true);
        deleteUserButton.setStyle("fx-background-color: Red; fx-color: White");
// Do some validation (using the Java 8 lambda syntax).
        verifyUNameTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(newValue, randomStr)) {
                deleteUserButton.setDisable(false);
            } else {
                deleteUserButton.setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> verifyUNameTF.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addGrp) {
                return verifyUNameTF.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(res -> {
            if (res.equals(randomStr)) {
                group.removeGroup();
                refresh(null);
            }
        });
    }

    private void editUser(User user) {
        User oldUser = user;
        Dialog<Pair<Pair<String, Boolean>, Pair<String, String>>> dialog = new Dialog<>();
        dialog.setTitle("Edit User - " + user.toString());

// Set the button types.
        ButtonType login = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(login, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField userNameTextField = new TextField();
        userNameTextField.setText(user.getUserName());
        userNameTextField.setEditable(false);
        TextField fullNameField = new TextField();
        fullNameField.setText(user.getFullName());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        CheckBox adminCheckBox = new CheckBox("Admin?");
        adminCheckBox.setSelected(user.isAdmin());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(userNameTextField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(adminCheckBox, 1, 3);


        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> userNameTextField.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == login) {
                return new Pair<Pair<String, Boolean>, Pair<String, String>>(new Pair<>(fullNameField.getText(), adminCheckBox.isSelected()), new Pair<>(userNameTextField.getText(), passwordField.getText()));
            }
            return null;
        });

        Optional<Pair<Pair<String, Boolean>, Pair<String, String>>> result = dialog.showAndWait();

        result.ifPresent(userInfo -> {
            Pattern p = Pattern.compile("[^a-zA-Z0-9]");
            String uName = userInfo.getValue().getKey();
            String pass = userInfo.getValue().getValue();
            String fName = userInfo.getKey().getKey();
            Boolean admin = userInfo.getKey().getValue();
            boolean hasSpecialChar = p.matcher(userInfo.getValue().getKey()).find();
            if (hasSpecialChar) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("");
                alert.setHeaderText("You have entered an invalid character in the username");
                alert.setContentText("Only Alphanumeric characters are aloud.");
                alert.show();
            } else {
                Set<String> years = new HashSet<>();
                //Utilities.User.createUser(uName, pass, fullNameField.getText(), admin);
                User.updateUser(uName, pass, fName, admin);
                user.setFullName(fName);
                user.setAdmin(admin);
                user.getYears().forEach(year -> {
                    user.updateYear(year);
                });
                refresh(() -> {
                    openUser(user, curYear);
                });


                ArrayList<ArrayList<String>> yearUsers = new ArrayList<>();


            }

        });


    }

    private void editGroup(Group group) {
        AddGroup.addGroup(curYear, group.getName(), (grp) -> {
            refresh(() -> {
                openGroup(grp, curYear);
            });

        });

    }

    @FXML
    private void saveUsers(ActionEvent event) {

        ArrayList<ArrayList<String>> yearUsers = new ArrayList<>();
        checkedUsers.forEach((user, userManage) -> {
            ArrayList<String> usersManage = new ArrayList<>();

            userManage.forEach((uMan) -> {
                if (!uMan.isEmpty()) {
                    usersManage.add(uMan);

                }
            });

            if (!usersManage.isEmpty()) {
                //years.add(year);
                //if (newUser) {
                user.setuManage(usersManage);
                user.updateYear(curYear);

            }
        });
        groups.forEach((user, groupId) -> {
            user.setGroupId(groupId);
            user.updateYear(curYear);
        });

        refresh(null);

    }

    private void refresh(actionCallback cb) {
        hideTabs();

        fillTreeView(() -> {
            openYear(curYear);
            if (cb != null) {
                cb.doAction();
            }
        });
    }

    @FXML
    private void addSingleUser(ActionEvent event) {
        Dialog<Pair<Pair<String, Boolean>, Pair<String, String>>> dialog = new Dialog<>();
        dialog.setTitle("Add User");

// Set the button types.
        ButtonType login = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(login, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField userNameTextField = new TextField();
        userNameTextField.setPromptText("Username");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        CheckBox adminCheckBox = new CheckBox("Admin?");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(userNameTextField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(adminCheckBox, 1, 3);

// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node loginButton = dialog.getDialogPane().lookupButton(login);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        userNameTextField.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> userNameTextField.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == login) {
                return new Pair<Pair<String, Boolean>, Pair<String, String>>(new Pair<>(fullNameField.getText(), adminCheckBox.isSelected()), new Pair<>(userNameTextField.getText(), passwordField.getText()));
            }
            return null;
        });

        Optional<Pair<Pair<String, Boolean>, Pair<String, String>>> result = dialog.showAndWait();

        result.ifPresent(userInfo -> {
            Pattern p = Pattern.compile("[^a-zA-Z0-9]");
            String uName = userInfo.getValue().getKey();
            String pass = userInfo.getValue().getValue();
            String fName = userInfo.getKey().getKey();
            Boolean admin = userInfo.getKey().getValue();
            boolean hasSpecialChar = p.matcher(userInfo.getValue().getKey()).find();
            if (hasSpecialChar) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("");
                alert.setHeaderText("You have entered an invalid character in the username");
                alert.setContentText("Only Alphanumeric characters are aloud.");
                alert.show();
            } else {
                Set<String> years = new HashSet<>();
                User user = User.createUser(uName, pass, fName, admin);
                ArrayList<String> uMan = new ArrayList<>();
                uMan.add(uName);
                years.add(curYear);
                user.setuManage(uMan);
                user.setYears(years);
                user.setGroupId(1);
                user.updateYear(curYear);
                refresh(() -> {
                    openUser(user, curYear);

                });
                //allUsersList.getItems().add(User.createUser(uName, pass, fName, admin));
     /*           } else {
                    Utilities.User.updateUser(uName, pass);

                }*/
                ArrayList<ArrayList<String>> yearUsers = new ArrayList<>();


            }

        });

    }

    @FXML
    private void addBulkUsers(ActionEvent event) {
//TODO Implement
    }

    @FXML
    private void addBulkGroups(ActionEvent event) {
        //TODO Implement
    }

    @FXML
    private void addSingleGroup(ActionEvent event) {
        Group newGroup = AddGroup.addGroup(curYear, (grp -> {
            refresh(() -> {
                openGroup(grp, curYear);

            });
        }));


    }


    private String getCurrentYear() {
        return curYear;
    }

    /*
     * PRODUCT TAB
     */
    private void initProductsTab() {
        boolean newYear = false;
        Year thisYear = new Year(getCurrentYear());
        //ProductTable = new TableView<>();

        categoriesCmbx.getItems().clear();
        categoriesTb.clear();
        categoriesTb.add("");
        String browse = "Add Category";
        rowsCats.clear();
        thisYear.getCategories().forEach((category) -> {
            categoriesTb.add(category.catName);
            rowsCats.add(category);
        });

        categoriesTb.add(browse);
        categoriesCmbx.getItems().setAll(categoriesTb);
        ProductTable.getColumns().clear();
        ProductTable.getItems().clear();
        String[][] columnNames = {{"ID", "productID"}, {"Item", "productName"}, {"Size", "productSize"}, {"Price/Item", "productUnitPriceString"}};
        //for (String[] column : columnNames) {
        {
            javafx.scene.control.TableColumn<formattedProductProps, String> idCol = new javafx.scene.control.TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("productID"));
            idCol.setCellFactory(TextFieldTableCell.forTableColumn());
            idCol.setOnEditCommit(t -> {
                t.getRowValue().productID.set(t.getNewValue());
                data.get(t.getTablePosition().getRow()).productID.set(t.getNewValue());
                t.getTableView().refresh();
            });
            ProductTable.getColumns().add(idCol);
            //}
        }
        {
            javafx.scene.control.TableColumn<formattedProductProps, String> nameCol = new javafx.scene.control.TableColumn<>("Item");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
            nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
            nameCol.setOnEditCommit(t -> {
                t.getRowValue().productName.set(t.getNewValue());
                data.get(t.getTablePosition().getRow()).productName.set(t.getNewValue());
                t.getTableView().refresh();
            });
            ProductTable.getColumns().add(nameCol);
            //}
        }
        {
            javafx.scene.control.TableColumn<formattedProductProps, String> sizeCol = new javafx.scene.control.TableColumn<>("Size");
            sizeCol.setCellValueFactory(new PropertyValueFactory<>("productSize"));
            sizeCol.setCellFactory(TextFieldTableCell.forTableColumn());
            sizeCol.setOnEditCommit(t -> {
                t.getRowValue().productSize.set(t.getNewValue());
                data.get(t.getTablePosition().getRow()).productSize.set(t.getNewValue());
                t.getTableView().refresh();
            });
            ProductTable.getColumns().add(sizeCol);
            //}
        }
        {
            javafx.scene.control.TableColumn<formattedProductProps, String> unitCostCol = new javafx.scene.control.TableColumn<>("Price/Item");
            unitCostCol.setCellValueFactory(new PropertyValueFactory<>("productUnitPriceString"));
            unitCostCol.setCellFactory(TextFieldTableCell.forTableColumn());
            unitCostCol.setOnEditCommit(t -> {
                try {
                    BigDecimal unitPrice = new BigDecimal(t.getNewValue());
                    t.getRowValue().productUnitPriceString.set(t.getNewValue());
                    t.getRowValue().productUnitPrice.set(new BigDecimal(t.getNewValue()));
                    data.get(t.getTablePosition().getRow()).productUnitPriceString.set(t.getNewValue());
                    data.get(t.getTablePosition().getRow()).productUnitPrice.set(new BigDecimal(t.getNewValue()));
                    t.getTableView().refresh();
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid number");
                    alert.setHeaderText("You have entered an invalid number.");
                    alert.show();
                    t.getRowValue().productUnitPriceString.set(t.getOldValue());
                }


            });
            ProductTable.getColumns().add(unitCostCol);
            //}
        }


        javafx.scene.control.TableColumn<formattedProductProps, String> categoryColumn = new javafx.scene.control.TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("productCategory"));

        categoryColumn.setCellFactory(ComboBoxTableCell.forTableColumn(categoriesTb));

        categoryColumn.setOnEditCommit(t -> {
            String newVal = catCmbxChanged(t.getNewValue());

            t.getRowValue().productCategory.set(newVal);
            data.get(t.getTablePosition().getRow()).productCategory.set(newVal);

        });
        ProductTable.getColumns().add(categoryColumn);
        // boolean updateDb = true;
        fillTable(getCurrentYear());
        productsTab.setDisable(false);
    }


    @FXML
    private void submit(ActionEvent event) {


        updateDb(getCurrentYear());
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Saved");
        alert.setHeaderText("Changes Saved.");
        alert.show();

    }

    @FXML
    private void tableFrmXML(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XML files", "*.xml", "*.XML");
        chooser.getExtensionFilters().add(filter);

        chooser.setSelectedExtensionFilter(filter);
//        logoLoc.setText(chooser.showOpenDialog(settings).getAbsolutePath());
        File xmlFile = chooser.showOpenDialog(parentWindow);
        if (xmlFile != null) {
            String path = xmlFile.getAbsolutePath();
            createTable(path);
        }
    }

    private void convert(String csvLoc, String xmlLoc) {
        List<String> headers = new ArrayList<>(5);


        File file = new File(csvLoc);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();

            Document newDoc = domBuilder.newDocument();
            // Root element
            Element rootElement = newDoc.createElement("LawnGarden");
            newDoc.appendChild(rootElement);

            int line = 0;

            String text;
            while ((text = reader.readLine()) != null) {

                StringTokenizer st = new StringTokenizer(text, ";", false);
                String[] rowValues = new String[st.countTokens()];
                int index = 0;
                while (st.hasMoreTokens()) {

                    String next = st.nextToken();
                    rowValues[index] = next;
                    index++;

                }

                //String[] rowValues = text.split(",");

                if (line == 0) { // Header row
                    Collections.addAll(headers, rowValues);
                } else { // Data row
                    Element rowElement = newDoc.createElement("Products");
                    rootElement.appendChild(rowElement);
                    Attr attr = newDoc.createAttribute("id");
                    attr.setValue(Integer.toString(line - 1));
                    rowElement.setAttributeNode(attr);
                    for (int col = 0; col < headers.size(); col++) {
                        String header = headers.get(col);
                        String value;

                        if (col < rowValues.length) {
                            value = rowValues[col].trim();
                        } else {
                            // ?? Default value
                            value = "";
                        }

                        Element curElement = newDoc.createElement(header);
                        curElement.appendChild(newDoc.createTextNode(value));
                        rowElement.appendChild(curElement);
                    }
                }
                line++;
            }

            OutputStreamWriter osw = null;

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                osw = new OutputStreamWriter(baos);

                TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer aTransformer = tranFactory.newTransformer();
                aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
                aTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
                //aTransformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
                aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                Source src = new DOMSource(newDoc);
                Result result = new StreamResult(osw);
                aTransformer.transform(src, result);

                osw.flush();
                //System.out.println(new String(baos.toByteArray()));

                try (OutputStream outStream = new FileOutputStream(xmlLoc)) {// writing bytes in to byte output stream

                    baos.writeTo(outStream);
                } catch (IOException e) {
                    LogToFile.log(e, Severity.SEVERE, "Error writing XML file. Please try again.");
                }


            } catch (Exception exp) {
                LogToFile.log(exp, Severity.SEVERE, "Error writing XML file. Please try again.");
            } finally {
                try {
                    if (osw != null) {
                        osw.close();
                    }
                } catch (IOException e) {
                    LogToFile.log(e, Severity.SEVERE, "Error closing file. Please try again.");
                }

            }
        } catch (Exception e) {
            LogToFile.log(e, Severity.SEVERE, "Error reading CSV file. Ensure the path exists, and the software has permission to read it.");
        }
    }

    @FXML
    private void csvToXml(ActionEvent event) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("CSV to XML conversion");

// Set the button types.
        ButtonType convertButtonType = new ButtonType("Convert", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(convertButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField csvLoc = new TextField();
        csvLoc.setPromptText("CSV file Location");
        TextField xmlLoc = new TextField();
        xmlLoc.setPromptText("XML Location");
        Button getCsvLoc = new Button("...");
        getCsvLoc.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CSV files", "*.csv", "*.CSV");
            chooser.getExtensionFilters().add(filter);
            chooser.setSelectedExtensionFilter(filter);
            File csv = chooser.showOpenDialog(grid.getScene().getWindow());
            if (csv != null) {
                String path = csv.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) {
                    path += ".csv";
                }
                csvLoc.setText(path);
            }
        });
        Button getXmlLoc = new Button("...");
        getXmlLoc.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XML files", "*.xml", "*.XML");
            chooser.getExtensionFilters().add(filter);
            chooser.setSelectedExtensionFilter(filter);
            File XML = chooser.showSaveDialog(grid.getScene().getWindow());
            if (XML != null) {
                String path = XML.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".xml")) {
                    path += ".xml";
                }
                xmlLoc.setText(path);
            }
        });
        grid.add(new Label("CSV file Location:"), 0, 0);
        grid.add(csvLoc, 1, 0);
        grid.add(getCsvLoc, 2, 0);
        grid.add(new Label("XML Location:"), 0, 1);
        grid.add(xmlLoc, 1, 1);
        grid.add(getXmlLoc, 2, 1);


// Enable/Disable login button depending on whether a username was entered.
        javafx.scene.Node convertButton = dialog.getDialogPane().lookupButton(convertButtonType);
        convertButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        csvLoc.textProperty().addListener((observable, oldValue, newValue) -> convertButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> csvLoc.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == convertButtonType) {
                return new Pair<>(csvLoc.getText(), xmlLoc.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(fileLocations -> {
            convert(fileLocations.getKey(), fileLocations.getValue());
            createTable(fileLocations.getValue());
        });




/*        CSV2XML csv = new CSV2XML(parent);
        String xmlFile = csv.getXML();
        if (!xmlFile.isEmpty()) {
            createTable(xmlFile);
        }*/
    }

    @FXML
    private void catCmbxChanged(ActionEvent event) {
        if (Objects.equals(categoriesCmbx.getSelectionModel().getSelectedItem(), "Add Category")) {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Add new category");

// Set the button types.
            ButtonType addCat = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addCat, ButtonType.CANCEL);

// Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField catName = new TextField();
            catName.setPromptText("Category Name");
            DatePicker catDate = new DatePicker(LocalDate.now());
            catDate.setPromptText("Category Due Date");

            grid.add(new Label("Category Name:"), 0, 0);
            grid.add(catName, 1, 0);
            grid.add(new Label("Category Due Date:"), 0, 1);
            grid.add(catDate, 1, 1);


// Enable/Disable login button depending on whether a username was entered.
            javafx.scene.Node addCatButton = dialog.getDialogPane().lookupButton(addCat);
            addCatButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
            catName.textProperty().addListener((observable, oldValue, newValue) -> addCatButton.setDisable(newValue.trim().isEmpty()));

            dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
            Platform.runLater(() -> catName.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addCat) {
                    return new Pair<String, String>(catName.getText(), catDate.getValue().toString());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            result.ifPresent(category -> {
                rowsCats.add(new Year.category(category.getKey(), category.getValue()));
                Platform.runLater(() -> refreshCmbx());

            });


        }

    }

    private String catCmbxChanged(String newVal) {
        final Year.category newCat = new Year.category("", "");
        if (Objects.equals(newVal, "Add Category")) {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Add new category");

// Set the button types.
            ButtonType addCat = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addCat, ButtonType.CANCEL);

// Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField catName = new TextField();
            catName.setPromptText("Category Name");
            DatePicker catDate = new DatePicker(LocalDate.now());
            catDate.setPromptText("Category Due Date");

            grid.add(new Label("Category Name:"), 0, 0);
            grid.add(catName, 1, 0);
            grid.add(new Label("Category Due Date:"), 0, 1);
            grid.add(catDate, 1, 1);


// Enable/Disable login button depending on whether a username was entered.
            javafx.scene.Node addCatButton = dialog.getDialogPane().lookupButton(addCat);
            addCatButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
            catName.textProperty().addListener((observable, oldValue, newValue) -> addCatButton.setDisable(newValue.trim().isEmpty()));

            dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
            Platform.runLater(() -> catName.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addCat) {
                    return new Pair<String, String>(catName.getText(), catDate.getValue().toString());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();
            result.ifPresent(category -> {
                newCat.catName = category.getKey();
                newCat.catDate = category.getValue();
                rowsCats.add(newCat);
                Platform.runLater(() -> refreshCmbx());

            });


        }

        return newCat.catName;
    }

    @FXML
    private void addBtnPressed(ActionEvent event) {
        int count = ProductTable.getItems().size() + 1;
        data.add(new formattedProductProps(0, idTb.getText(), itemTb.getText(), sizeTb.getText(), new BigDecimal(rateTb.getText()), categoriesCmbx.getSelectionModel().getSelectedItem(), 0, BigDecimal.ZERO));
        ProductTable.setItems(data);
    }


    private void refreshCmbx() {
        categoriesCmbx.getItems().clear();
        categoriesTb.clear();
        categoriesTb.add("");
        String browse = "Add Category";

        rowsCats.forEach(cat -> categoriesTb.add(cat.catName));


        categoriesTb.add(browse);
        categoriesCmbx.getItems().setAll(categoriesTb);

    }

    private void updateDb(String year) {
        Year yearToUpdate = new Year(year);
        yearToUpdate.updateDb(year, ProductTable.getItems(), rowsCats);
    }

    /**
     * Parses XML file to insert into products table on screen
     *
     * @param FLoc the location of the XML file
     */
    private void createTable(String FLoc) {
        try {

            File fXmlFile = new File(FLoc);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nListCats = doc.getElementsByTagName("Categories");

            // Collection<String[]> rowsCatsL = new ArrayList<>();

            for (int temp = 0; temp < nListCats.getLength(); temp++) {

                org.w3c.dom.Node nNode = nListCats.item(temp);


                if ((int) nNode.getNodeType() == (int) org.w3c.dom.Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    rowsCats.add(new Year.category(eElement.getElementsByTagName("CategoryName").item(0).getTextContent(), eElement.getElementsByTagName("CategoryDate").item(0).getTextContent()));
                }
            }
            //rowsCats = rowsCatsL;
            NodeList nList = doc.getElementsByTagName("Products");

            Object[][] rows = new Object[nList.getLength()][5];

            for (int temp = 0; temp < nList.getLength(); temp++) {

                org.w3c.dom.Node nNode = nList.item(temp);


                if ((int) nNode.getNodeType() == (int) org.w3c.dom.Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;


                    //String productID, String productName, String productSize, String productUnitPrice, String productCategory, int orderedQuantity, BigDecimal extendedCost
                    formattedProductProps prodProps = new formattedProductProps(0, eElement.getElementsByTagName(
                            "ProductID").item(0).getTextContent(),
                            eElement.getElementsByTagName("ProductName").item(0).getTextContent(),
                            eElement.getElementsByTagName("Size").item(0).getTextContent(),
                            new BigDecimal(eElement.getElementsByTagName("UnitCost").item(0).getTextContent()),
                            (eElement.getElementsByTagName("Category").item(0) != null) ? eElement.getElementsByTagName("Category").item(0).getTextContent() : "",
                            0,
                            BigDecimal.ZERO
                    );
                    data.add(prodProps);
                    ProductTable.setItems(data);

                }


            }
        } catch (Exception e) {
            LogToFile.log(e, Severity.SEVERE, "Error Converting XML file to table. Please try again or contact support.");
        }
        refreshCmbx();
    }

    /**
     * Creates an XML file from the table
     *
     * @param SavePath Path to save the created XML file
     */
    private void createXML(String SavePath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;

            docBuilder = docFactory.newDocumentBuilder();


            // root elements
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("LawnGarden");
            doc.appendChild(rootElement);
            Iterable<Year.category> caters;
            caters = rowsCats;
            int[] i = {0};
            //caters = getCategories(yearText.getText());
            caters.forEach(cat -> {
                        Element cats = doc.createElement("Categories");
                        rootElement.appendChild(cats);
                        Attr attr = doc.createAttribute("id");
                        attr.setValue(Integer.toString(i[0]));
                        cats.setAttributeNode(attr);


                        //CateName elements
                        Element ProductID = doc.createElement("CategoryName");
                        ProductID.appendChild(doc.createTextNode(cat.catName));
                        cats.appendChild(ProductID);

                        //CatDate elements
                        Element ProductName = doc.createElement("CategoryDate");
                        ProductName.appendChild(doc.createTextNode(cat.catDate));
                        cats.appendChild(ProductName);
                        i[0]++;
                    }
            );

            // staff elements


            // set attribute to staff element
            for (int i2 = 0; i2 < ProductTable.getItems().size(); i2++) {

                Element staff = doc.createElement("Products");
                rootElement.appendChild(staff);
                Attr attr = doc.createAttribute("id");
                attr.setValue(Integer.toString(i2));
                staff.setAttributeNode(attr);

                //ProductID elements
                Element ProductID = doc.createElement("ProductID");
                ProductID.appendChild(doc.createTextNode(ProductTable.getItems().get(i2).getProductID()));
                staff.appendChild(ProductID);

                // Prodcut Name elements
                Element ProductName = doc.createElement("ProductName");
                ProductName.appendChild(doc.createTextNode(ProductTable.getItems().get(i2).getProductName()));
                staff.appendChild(ProductName);

                // Unit COst elements
                Element UnitCost = doc.createElement("UnitCost");
                UnitCost.appendChild(doc.createTextNode(ProductTable.getItems().get(i2).getProductUnitPrice().toPlainString()));
                staff.appendChild(UnitCost);

                // Size elements
                Element Size = doc.createElement("Size");
                Size.appendChild(doc.createTextNode(ProductTable.getItems().get(i2).getProductSize()));
                staff.appendChild(Size);

                // Category elements

                String cat = (ProductTable.getItems().get(i2).getProductCategory() != null) ? ProductTable.getItems().get(i2).getProductCategory() : "";
                Element category = doc.createElement("Category");
                category.appendChild(doc.createTextNode(cat));
                staff.appendChild(category);
            }


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileOutputStream(SavePath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            //System.out.println("File saved!");
        } catch (ParserConfigurationException e) {
            LogToFile.log(e, Severity.SEVERE, "Error creating XML file: Parser error. Contact support.");
        } catch (TransformerException e) {
            LogToFile.log(e, Severity.SEVERE, "Error creating XML file: Parser Error. Contact support.");
        } catch (FileNotFoundException e) {
            LogToFile.log(e, Severity.SEVERE, "Error creating XML file: Error writing to file. Make sure the directory is readable by the software.");
        }
    }

    /**
     * Fills the table from a DB table
     */
    private void fillTable(String year) {
        Year yearInfo = new Year(year);

        formattedProduct[] productArray = yearInfo.getAllProducts();
        Object[][] rows = new Object[productArray.length][6];
        // data = FXCollections.observableArrayList();

        int i = 0;
        for (formattedProduct productOrder : productArray) {
            //String productID, String productName, String productSize, String productUnitPrice, String productCategory, int orderedQuantity, BigDecimal extendedCost
            formattedProductProps prodProps = new formattedProductProps(productOrder.productKey, productOrder.productID, productOrder.productName, productOrder.productSize, productOrder.productUnitPrice, productOrder.productCategory, productOrder.orderedQuantity, productOrder.extendedCost);
            data.add(prodProps);
            i++;
        }

        ProductTable.setItems(data);

    }

    @FXML
    private void tablefromDb(ActionEvent event) {
        fillTable(getCurrentYear());
    }

    @FXML
    private void xmlFromTable(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XML files", "*.xml", "*.XML");
        chooser.getExtensionFilters().add(filter);
        chooser.setSelectedExtensionFilter(filter);
        File XML = chooser.showSaveDialog(parentWindow);
        if (XML != null) {
            String path = XML.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xml")) {
                path += ".xml";
            }
            createXML(path);
        }
    }

    private String arrayToCSV(Collection<String> array) {
        final String[] ret = {""};
        array.forEach(value -> {
            if (!ret[0].isEmpty()) {
                ret[0] = ret[0] + ", " + value;
            } else {
                ret[0] = value;
            }
        });
        return ret[0];
    }

    private ContextMenu createContextMenu(TreeItem<TreeItemPair<String, Pair<String, Object>>> cell) {
        ContextMenu cm = new ContextMenu();
        ContextMenu cmContent = new ContextMenu();
        Pane newPane = null;
        FXMLLoader loader;
        Pane finalNewPane;
        // String tabTitle = "";
        if (cell != null && cell.getValue() != null && !Objects.equals(cell.getValue().getValue().getKey(), "RootNode")) {
            switch (cell.getValue().getValue().getKey()) {
                case "Year":
                    cmContent = createContextMenuContent(
                            //Open
                            () -> {
                                openYear(cell.getValue().getKey());

                            }, null,  //Open In New Window
                            null);

                    break;
                case "Group":
                    cmContent = createContextMenuContent(
                            //Open
                            () -> {
                                String year = "";
                                if (cell.getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = cell.getParent().getValue().getKey();
                                } else if (cell.getParent().getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = cell.getParent().getParent().getValue().getKey();

                                }
                                openGroup((Group) cell.getValue().getValue().getValue(), year);

                            }, null, null); //Open In New W
                    break;
                case "User":
                    cmContent = createContextMenuContent(
                            //Open
                            () -> {
                                String year = "";
                                if (cell.getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = cell.getParent().getValue().getKey();
                                } else if (cell.getParent().getParent().getValue().getValue().getKey().equals("Year")) {
                                    year = cell.getParent().getParent().getValue().getKey();

                                }
                                openUser((User) cell.getValue().getValue().getValue(), year);

                            }, null, null);  //Open In New W

                    break;


            }


        }
        cm.getItems().addAll(cmContent.getItems());
        MenuItem refresh = new MenuItem("Refresh");
        refresh.setOnAction(event -> refresh(null));
        cm.getItems().add(refresh);
        // other menu items...
        return cm;
    }

    private <T> CheckBoxTreeItem<TreeItemPair<String, String>> createUserTreeItem(TreeItemPair<String, String> value, User user) {

        CheckBoxTreeItem<TreeItemPair<String, String>> item = new CheckBoxTreeItem<TreeItemPair<String, String>>(value);
        if (!value.getValue().isEmpty()) {
            item.selectedProperty().addListener((obs, wasChecked, isNowChecked) -> {
                if (isNowChecked) {
                    checkedUsers.computeIfPresent(user, (k, v) -> {
                        if (!v.contains(value.getValue())) {
                            v.add(value.getValue());
                        }
                        return v;
                    });
                    checkedUsers.computeIfAbsent(user, k -> {
                        ArrayList<String> v = new ArrayList();
                        v.add(value.getValue());
                        return v;
                    });
                   /* checkedFullName.computeIfPresent(year, (k, v) -> {
                        v.add(value.getKey());
                        return v;
                    });
                    checkedFullName.computeIfAbsent(year, k -> {
                        ArrayList<String> v = new ArrayList();
                        v.add(value.getKey());
                        return v;
                    });*/

                } else {
                    checkedUsers.compute(user, (k, v) -> {
                        v.remove(value.getValue());
                        return v;
                    });
  /*                checkedFullName.compute(year, (k, v) -> {
                        v.remove(value.getKey());
                        return v;
                    });*/
                }


            });
        }

        return item;
    }

    private <T> CheckBoxTreeItem<TreeItemPair<String, String>> createGroupTreeItem(TreeItemPair<String, String> value, Group group) {

        CheckBoxTreeItem<TreeItemPair<String, String>> item = new CheckBoxTreeItem<TreeItemPair<String, String>>(value);
        if (!value.getValue().isEmpty()) {
            item.selectedProperty().addListener((obs, wasChecked, isNowChecked) -> {
                if (isNowChecked) {
                    checkedGroups.computeIfPresent(group, (k, v) -> {
                        if (!v.contains(value.getValue())) {
                            v.add(value.getValue());
                        }
                        return v;
                    });
                    checkedGroups.computeIfAbsent(group, k -> {
                        ArrayList<String> v = new ArrayList();
                        v.add(value.getValue());
                        return v;
                    });
                   /* checkedFullName.computeIfPresent(year, (k, v) -> {
                        v.add(value.getKey());
                        return v;
                    });
                    checkedFullName.computeIfAbsent(year, k -> {
                        ArrayList<String> v = new ArrayList();
                        v.add(value.getKey());
                        return v;
                    });*/

                } else {
                    checkedGroups.compute(group, (k, v) -> {
                        v.remove(value.getValue());
                        return v;
                    });
  /*                checkedFullName.compute(year, (k, v) -> {
                        v.remove(value.getKey());
                        return v;
                    });*/
                }


            });
        }

        return item;
    }

    private ContextMenu createContextMenuContent(actionCallback open, actionCallback delete, actionCallback edit) {
        ContextMenu cm = new ContextMenu();
        if (open != null) {
            MenuItem openItem = new MenuItem("Open");
            openItem.setOnAction(event -> open.doAction());
            cm.getItems().add(openItem);
        }


        if (edit != null) {
            MenuItem openItem = new MenuItem("Edit");
            openItem.setOnAction(event -> edit.doAction());
            cm.getItems().add(openItem);
        }
        if (delete != null) {
            MenuItem openItem = new MenuItem("Delete");
            openItem.setOnAction(event -> delete.doAction());
            cm.getItems().add(openItem);
        }
        return cm;
    }

    interface actionCallback {
        void doAction();
    }

    abstract class AbstractTreeItem extends TreeItem {

        protected abstract ContextMenu getMenu();
    }

    public class contextTreeItem<K, V> extends AbstractTreeItem {
        // make class vars here like psswd

        public contextTreeItem(String key, Pair<String, String> value) {
            this.setValue(new TreeItemPair<>(key, value));
        }

        public contextTreeItem(String key, String value) {
            this.setValue(new TreeItemPair<>(key, new Pair<String, String>(value, "")));
        }

        public contextTreeItem(String key) {
            this.setValue(new TreeItemPair<>(key, null));
        }

        @Override
        public ContextMenu getMenu() {

            return createContextMenu(this);
        }
    }

    private final class TreeCellImpl<K, V> extends TreeCell<TreeItemPair<String, Pair<String, String>>> {

        @Override
        public void updateItem(TreeItemPair<String, Pair<String, String>> item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(getItem() == null ? "" : getItem().getKey());
                setGraphic(getTreeItem().getGraphic());
                setContextMenu(((AbstractTreeItem) getTreeItem()).getMenu());
            }
        }
    }
}

