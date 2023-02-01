package controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.stage.Stage;
import models.Database;
import models.General;

public class RegisterUserController implements Initializable {

    @FXML private Button btnSave;
    @FXML private ComboBox < String > cmbCategory;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword, txtPassword2;
    @FXML private TextField txtUsername;

    private ObservableList < String > categories = FXCollections.observableArrayList();
    private String key = "lRqt1p2IZUqweqwe"; // 128 bit key
    private String initVector = "RandomInitVector"; // 16 bytes IV

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        UnaryOperator < Change > OnlyStringsAndNumbers = change -> {
            String input = change.getText();
            if (input.matches("[a-zA-Z0-9]*")) {
                return change;
            }
            return null;
        };

        txtUsername.setTextFormatter(new TextFormatter < String > (OnlyStringsAndNumbers));

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

                cmbCategory.setItems(categories);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public boolean isValidUser() {

        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery =
            "SELECT * FROM users WHERE username = ? AND universityID = ?;";

        try {
            select = Database.statement(sqlQuery);
            select.setString(1, txtUsername.getText());
            select.setInt(2, General.activeUserID);
            result = select.executeQuery();

            if (result.next()) {
                return false;
            } else {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            try {
                if (select != null)
                    select.close();
                if (result != null)
                    result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return false;
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

    public boolean isValidPassword(String password) {

        if (password.length() > 15 || password.length() < 8) {
            General.WARNING("Warning", "Password must be less than 20 and more than 8 characters in length.");
            return false;

        }
        String upperCaseChars = "(.*[A-Z].*)";
        if (!password.matches(upperCaseChars)) {
            General.WARNING("Warning", "Password must have atleast one uppercase character");
            return false;

        }
        String lowerCaseChars = "(.*[a-z].*)";
        if (!password.matches(lowerCaseChars)) {
            General.WARNING("Warning", "Password must have atleast one lowercase character");
            return false;

        }
        String numbers = "(.*[0-9].*)";
        if (!password.matches(numbers)) {
            General.WARNING("Warning", "Password must have atleast one number");
            return false;

        }
        String specialChars = "(.*[@,#,$,%].*$)";
        if (!password.matches(specialChars)) {
            General.WARNING("Warning", "Password must have atleast one special character among @#$%");
            return false;

        }

        return true;
    }

    @FXML public void save() {

        if (txtUsername.getText().trim() == null ||
            txtUsername.getText().trim().isEmpty() ||
            txtPassword.getText().trim() == null ||
            txtPassword.getText().trim().isEmpty() ||
            txtPassword2.getText().trim() == null ||
            txtPassword2.getText().trim().isEmpty() ||
            cmbCategory.getValue() == null ||
            cmbCategory.getValue().trim().isEmpty() ||
            txtEmail.getText().trim() == null ||
            txtEmail.getText().trim().isEmpty()
        ) {

            General.WARNING("Warning", "Please complete all the required fields to continue");
            return;
        }

        if (!isValidUser()) {
            General.WARNING("Warning", "This user exists !");
            return;
        }

        if (!isValidEmail(txtEmail.getText())) {
            return;
        }

        if (!isValidPassword(txtPassword.getText())) {
            return;
        }

        if (!txtPassword.getText().equals(txtPassword2.getText())) {
            General.WARNING("Warning", "Passwords do not match !");
            txtPassword.clear();
            txtPassword2.clear();
            return;
        }

        PreparedStatement insert = null;

        String sqlQuery =
            "INSERT INTO `users` \n" +
            "(`username`, `email`, `password`, `categoryID`, `universityID`)\n" +
            "VALUES (?, ?, ?, ?, ?);";

        try {
            insert = Database.statement(sqlQuery);
            insert.setString(1, txtUsername.getText());
            insert.setString(2, txtEmail.getText());
            insert.setString(3, General.encrypt(key, initVector, txtPassword.getText()));
            insert.setInt(4, getCategoryID(cmbCategory.getValue()));
            insert.setInt(5, General.univesityID);
            insert.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (insert != null)
                    insert.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ((Stage) btnSave.getScene().getWindow()).close();

        }

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

}