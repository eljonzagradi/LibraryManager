package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Database;
import models.General;

public class RegisterUniversityController implements Initializable {

    @FXML private AnchorPane anpaneUni, anpaneAdmin;
    @FXML private BorderPane borderpane;
    @FXML private HBox hBoxImage;
    @FXML private ImageView imageview;

    @FXML private Button btnUpload, btnSaveUni, btnSaveAdmin;

    @FXML private Label lblRegisterAdmin, lblRegisterUni;
    @FXML private Label lblUniName, lblAddress, lblCity, lblCountry;
    @FXML private Label lblContact, lblEmailUni, lblEmailAdmin;
    @FXML private Label lblUsername, lblPassword;

    @FXML private TextField txtUniName, txtAddress, txtCity, txtCountry, txtEmailUni, txtContact;
    @FXML private TextField txtUsername, txtEmailAdmin;
    @FXML private PasswordField txtPassword;

    private File image = null;
    private String key = "lRqt1p2IZUqweqwe"; // 128 bit key
    private String initVector = "RandomInitVector"; // 16 bytes IV

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        UnaryOperator < Change > onlyStrings = change -> {
            String input = change.getText();
            if (input.matches("[a-z A-Z]*")) {
                return change;
            }
            return null;
        };

        UnaryOperator < Change > OnlyStringsAndNumbers = change -> {
            String input = change.getText();
            if (input.matches("[a-zA-Z0-9]*")) {
                return change;
            }
            return null;
        };

        UnaryOperator < Change > onlyNumbers = change -> {
            String input = change.getText();
            if (input.matches("[0-9 ]*")) {
                return change;
            }
            return null;
        };

        txtUniName.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtCity.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtCountry.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtAddress.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtContact.setTextFormatter(new TextFormatter < String > (onlyNumbers));
        txtUsername.setTextFormatter(new TextFormatter < String > (OnlyStringsAndNumbers));
        btnSaveUni.requestFocus();

    }

    @FXML public void saveUni() {

        if (txtUniName.getText().trim() == null ||
            txtUniName.getText().trim().isEmpty() ||
            txtAddress.getText().trim() == null ||
            txtAddress.getText().trim().isEmpty() ||
            txtCity.getText().trim() == null ||
            txtCity.getText().trim().isEmpty() ||
            txtCountry.getText().trim() == null ||
            txtCountry.getText().trim().isEmpty() ||
            txtEmailUni.getText().trim() == null ||
            txtEmailUni.getText().trim().isEmpty() ||
            txtContact.getText().trim() == null ||
            txtContact.getText().trim().isEmpty()) {

            General.WARNING("Warning", "Please complete all the required fields to continue");
            return;
        }

        if (isValidEmail(txtEmailUni.getText()) == false) {
            return;
        }

        try {
            Connection conn = Database.connect();
            conn.setAutoCommit(false);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        PreparedStatement insert = null;
        ResultSet generatedKeys = null;
        FileInputStream inputStream = null;

        String sqlQuery =
            "INSERT INTO `university`\n" +
            "(`name`, `address`, `city`, `country`, `contact`, `email`, `logo`)\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?);";

        try {

            insert = Database.connect().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            insert.setString(1, txtUniName.getText());
            insert.setString(2, txtAddress.getText());
            insert.setString(3, txtCity.getText());
            insert.setString(4, txtCountry.getText());
            insert.setString(5, txtContact.getText());
            insert.setString(6, txtEmailUni.getText());

            if (imageview.getImage() == null && image == null) {
                insert.setBinaryStream(7, null);
            } else {
                inputStream = new FileInputStream(image);
                insert.setBinaryStream(7, inputStream, inputStream.available());
            }

            insert.executeUpdate();
            generatedKeys = insert.getGeneratedKeys();

            if (generatedKeys.next()) {
                General.univesityID = generatedKeys.getInt(1);
            } else {
                General.ERROR("ERROR", "SOMETHING WENT WRONG\nPLEASE CONTACT SUPPORT");
                System.exit(1);
                return;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (insert != null)
                    insert.close();
                if (generatedKeys != null)
                    generatedKeys.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            anpaneUni.setDisable(true);
            anpaneAdmin.setDisable(false);

        }

    }

    @FXML public void saveAdmin(ActionEvent event) {

        if (txtUsername.getText().trim() == null ||
            txtUsername.getText().trim().isEmpty() ||
            txtEmailAdmin.getText().trim() == null ||
            txtEmailAdmin.getText().trim().isEmpty() ||
            txtPassword.getText().trim() == null ||
            txtPassword.getText().trim().isEmpty()) {
            General.WARNING("Warning", "Please complete all the required fields to continue !");
            return;
        }

        if (txtUsername.getText().length() > 20 || txtUsername.getText().length() < 3) {
            General.WARNING("Warning", "Username must be less than 20 and more than 3 characters in length !");
            return;
        }

        String specialChars = "[a-zA-Z0-9]*";
        if (!txtUsername.getText().matches(specialChars)) {
            General.WARNING("Warning", "Username must contain only characters and numbers !");
            return;
        }

        if (txtUsername.getText().matches("[0-9]+")) {
            General.WARNING("Warning", "Username can not contain only numbers !");
            return;
        }

        if (isValidEmail(txtEmailAdmin.getText()) == false) {
            return;
        }

        if (isValidPassword(txtPassword.getText()) == false) {
            return;
        }

        PreparedStatement insert = null;

        String sqlQuery =
            "INSERT INTO `users` \n" +
            "(`username`, `email`, `password`, `categoryID`, `universityID`)\n" +
            "VALUES (?, ?, ?, '1', ?);";

        try {
            insert = Database.statement(sqlQuery);
            insert.setString(1, txtUsername.getText());
            insert.setString(2, txtEmailAdmin.getText());
            insert.setString(3, General.encrypt(key, initVector, txtPassword.getText()));
            insert.setInt(4, General.univesityID);
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

            ((Stage)(((Button) event.getSource()).getScene().getWindow())).close();

        }

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

    public boolean isValidEmail(String email) {

        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        if (matcher.matches() == false) {
            General.WARNING("Warning", "This email is not valid please recheck it");
            return false;
        }

        return true;
    }

    @FXML public void upload() {

        Image uploadedImage = null;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Image");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        image = chooser.showOpenDialog(null);
        URL url;

        if (image != null) {
            try {
                url = image.toURI().toURL();
                uploadedImage = new Image(url.toExternalForm());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {
                imageview.setImage(uploadedImage);
            }

        }

    }

}