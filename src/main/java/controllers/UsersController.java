package controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import models.Database;
import models.General;
import models.User;

public class UsersController implements Initializable {

    @FXML private AnchorPane anpane;

    @FXML private Button btnRefresh;
    @FXML private Button btnRegisterUser;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    @FXML private ComboBox < String > cmbCategory;
    @FXML private TextField txtUsername;

    @FXML private Label lblUsername;
    @FXML private Label lblCategory;

    @FXML private TableView < User > tblUsers;
    @FXML private TableColumn < User, Number > tblColUserID;
    @FXML private TableColumn < User, String > tblColUsername;
    @FXML private TableColumn < User, String > tblColEmail;
    @FXML private TableColumn < User, String > tblColCategory;

    private ObservableList < String > categories = FXCollections.observableArrayList();
    private ObservableList < User > users = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tblColUserID.setCellValueFactory(new PropertyValueFactory < > ("userID"));
        tblColUsername.setCellValueFactory(new PropertyValueFactory < > ("username"));
        tblColEmail.setCellValueFactory(new PropertyValueFactory < > ("email"));
        tblColCategory.setCellValueFactory(new PropertyValueFactory < > ("category"));
        tblUsers.setEditable(true);
        tblColUsername.setCellFactory(TextFieldTableCell. < User > forTableColumn());
        tblColEmail.setCellFactory(TextFieldTableCell. < User > forTableColumn());
        tblColCategory.setCellFactory(ComboBoxTableCell.forTableColumn(categories));

        tblColUsername.setOnEditCommit(e -> {
            TablePosition < User,
            String > pos = e.getTablePosition();
            int row = pos.getRow();
            User user = e.getTableView().getItems().get(row);

            String newValue = e.getNewValue();
            if (newValue.matches("[a-zA-Z0-9]+")) {
                user.setUsername(newValue);
                user.setModified(true);
                btnSave.setVisible(true);
                btnCancel.setVisible(true);
            } else {
                tblUsers.getColumns().get(0).setVisible(false);
                tblUsers.getColumns().get(0).setVisible(true);
            }
        });

        tblColEmail.setOnEditCommit(e -> {
            TablePosition < User,
            String > pos = e.getTablePosition();

            String email = e.getNewValue();
            if (!isValidEmail(email)) {
                tblUsers.refresh();
                return;
            }

            int row = pos.getRow();
            User user = e.getTableView().getItems().get(row);

            user.setEmail(email);
            user.setModified(true);
            btnSave.setVisible(true);
            btnCancel.setVisible(true);

        });

        tblColCategory.setOnEditCommit(e -> {
            TablePosition < User,
            String > pos = e.getTablePosition();
            String category = e.getNewValue();
            int count = 0;
            User user = e.getTableView().getItems().get(pos.getRow());
            if (user.getUserID() == General.activeUserID) {
                General.WARNING("Warning", "You can not change your category!");
                tblUsers.refresh();
            } else if (category.equals("User")) {
                for (int i = 0; i < e.getTableView().getItems().size(); i++) {
                    if (user.getCategory().equals("Administrator")) {
                        count++;
                    }
                }

                if (count > 1) {
                    user.setCategory(category);
                    user.setModified(true);
                    btnSave.setVisible(true);
                    btnCancel.setVisible(true);
                } else {
                    General.WARNING("Warning", "There must be at least one 'Administrator' in the system!");
                    ((User) e.getTableView().getItems().get(e.getTablePosition().getRow()))
                    .setCategory("Administrator");
                    tblUsers.refresh();
                }
            } else {
                user.setCategory(category);
                user.setModified(true);
                btnSave.setVisible(true);
                btnCancel.setVisible(true);
            }
        });

        txtUsername.textProperty().addListener(e -> getUsers());
        cmbCategory.valueProperty().addListener(e -> getUsers());

        getCategories();
        getUsers();

    }

    public void getUsers() {

        users.clear();
        PreparedStatement select = null;
        ResultSet result = null;
        String user = null;

        String sqlQuery =
            "SELECT userID, username, email, c.name FROM users u\n" +
            "INNER JOIN user_categories c ON c.categoryID = u.categoryID\n" +
            "WHERE universityID = ? AND username LIKE ? ";

        if (txtUsername.getText() == null) {
            user = "";
        } else {
            user = txtUsername.getText();
        }

        if (cmbCategory.getValue() != null &&
            !cmbCategory.getValue().equals("All")) {
            sqlQuery += " AND c.name = '" + cmbCategory.getValue() + "' ";
        }

        try {

            select = Database.statement(sqlQuery);
            select.setInt(1, General.univesityID);
            select.setString(2, user + "%");
            result = select.executeQuery();

            while (result.next()) {

                users.add(new User(

                    result.getInt(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4)

                ));
            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                tblUsers.setItems(users);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getCategories() {

        categories.clear();
        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT name FROM user_categories;";

        try {

            select = Database.statement(sqlQuery);
            result = select.executeQuery();

            while (result.next()) {
                categories.add(result.getString(1));
            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                cmbCategory.getItems().add("All");
                cmbCategory.getItems().addAll(categories);
                cmbCategory.setValue("All");

                anpane.requestFocus();

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public boolean isValidEmail(String email) {

        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            General.WARNING("Warning", "This email is not valid please recheck it");
            return false;
        }

        return true;
    }

    @FXML public void save() {

        if (!General.CONFIRMATION("Confirmation", "Are you sure ?")) {
            return;
        }

        PreparedStatement update = null;

        String sqlQuery =
            "UPDATE `users` SET  " +
            "`username` = ?, `email` = ?, `categoryID` = ? " +
            "WHERE `userID` = ?;\n";

        for (User user: users) {

            try {

                if (user.isModified()) {
                    update = Database.statement(sqlQuery);
                    update.setString(1, user.getUsername());
                    update.setString(2, user.getEmail());
                    update.setInt(3, getCategoryID(user.getCategory()));
                    update.setInt(4, user.getUserID());
                    update.executeUpdate();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        cancel();
        refresh();

    }

    public int getCategoryID(String category) {
        int roleId;
        switch (category) {
        case "Administrator":
            roleId = 1;
            break;
        case "User":
            roleId = 2;
            break;
        case "Student":
            roleId = 3;
            break;
        default:
            roleId = -1;
            break;
        }
        return roleId;
    }

    @FXML public void registerUser() throws IOException {
    	Stage stage = new Stage();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/views/RegisterUser.fxml"))));
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.showAndWait();
        getUsers();
    }

    @FXML public void cancel() {

        btnRegisterUser.setDisable(false);
        btnCancel.setVisible(false);
        btnSave.setVisible(false);

    }

    @FXML public void refresh() {

        cmbCategory.setValue("All");
        txtUsername.clear();
        cancel();

    }

}