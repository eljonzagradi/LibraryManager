package controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Database;
import models.General;

public class ChangePasswordController implements Initializable {

    @FXML private Button btnSave;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;
    @FXML private TextField txtUsername;
    
    private String key = "lRqt1p2IZUqweqwe"; // 128 bit key
    private String initVector = "RandomInitVector"; // 16 bytes IV
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txtUsername.setText(General.activeUser);		
	}

    @FXML void save(ActionEvent event) {
    	
    	if (txtUsername.getText().trim() == null ||
                txtUsername.getText().trim().isEmpty() ||
                txtPassword.getText().trim() == null ||
                txtPassword.getText().trim().isEmpty() ||
                txtPassword2.getText().trim() == null ||
                txtPassword2.getText().trim().isEmpty() ||
                txtOldPassword.getText().trim() == null ||
                txtOldPassword.getText().trim().isEmpty()
            ) {

                General.WARNING("Warning", "Please complete all the required fields to continue");
                return;
            }
    	
    	if(!isOldPasswordValid()) {
    		General.WARNING("Warning", "Your OLD PASSWORD is not correct !");
    		return;
    	}
    	
    	if (!isValidPassword(txtPassword.getText())) {
            txtPassword.clear();
            txtPassword2.clear();
            return;
        }

        if (!txtPassword.getText().equals(txtPassword2.getText())) {
            General.WARNING("Warning", "Passwords do not match !");
            txtPassword.clear();
            txtPassword2.clear();
            return;
        }
        
        PreparedStatement update = null;

        String sqlQuery = "UPDATE `users` SET `password` = ? WHERE `userID` = ?;";

        try {
        	update = Database.statement(sqlQuery);
        	update.setString(1, General.encrypt(key, initVector, txtPassword.getText()));
        	update.setInt(2, General.activeUserID);
        	update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (update != null)
                	update.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            General.INFORMATION("Success", "Password Changed Successfully");
            ((Stage) btnSave.getScene().getWindow()).close();

        }

    }
    
    public boolean isOldPasswordValid() {

        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT * FROM users WHERE username = ? AND password = ?;";

        try {
            select = Database.statement(sqlQuery);
            select.setString(1, txtUsername.getText());
            select.setString(2, General.encrypt(key, initVector, txtOldPassword.getText()));
            result = select.executeQuery();

            if (result.next()) {
                return true;
            } else {
                return false;
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
    


}
