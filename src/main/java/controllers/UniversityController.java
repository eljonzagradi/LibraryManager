package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import models.Database;
import models.General;
import models.StudyProgram;

public class UniversityController implements Initializable {

    @FXML private AnchorPane anpane;
    @FXML private HBox hbox, hBoxSP;
    @FXML private ImageView imageviewer;
    @FXML private Button btnUploadImage;

    @FXML private Button btnEditUni, btnSaveUni, btnCancelUni;

    @FXML private ComboBox < String > cmbFaculties;
    @FXML private Pane paneStudyPrograms;
    @FXML private Label lblFaculty, lblStudyPrograms;
    @FXML private Button btnAddStudyProgram, btnEditStudyProgram, btnDeleteStudyProgram,
    btnSaveStudyProgram, btnCancelStudyProgram;

    @FXML private Label lblUni, lblAddress, lblCity, lblCountry, lblEmail, lblContact, lblDuration;
    @FXML private TextField txtUniName, txtAddress, txtCity, txtCountry, txtEmail, txtContact;
    @FXML private TextField txtStudyProgram, txtDuration;

    @FXML private TableView < StudyProgram > tblStudyPrograms;
    @FXML private TableColumn < StudyProgram, String > tblColStudyProgram;
    @FXML private TableColumn < StudyProgram, Number > tblColDuration;
    @FXML private TableColumn < StudyProgram, String > tblColFaculty;
    @FXML private TableColumn < StudyProgram, Number > tblColNoStudents;

    private ObservableList < String > faculties = FXCollections.observableArrayList();
    private ObservableList < StudyProgram > studyprograms = FXCollections.observableArrayList();

    private File image;
    private String metoda;
    private int studyprogramID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	if(General.userCategory != 1) {
    		btnEditUni.setVisible(false);
    		hBoxSP.setVisible(false);
    	}

        tblColStudyProgram.setCellValueFactory(new PropertyValueFactory < > ("name"));
        tblColDuration.setCellValueFactory(new PropertyValueFactory < > ("duration"));
        tblColFaculty.setCellValueFactory(new PropertyValueFactory < > ("faculty"));
        tblColNoStudents.setCellValueFactory(new PropertyValueFactory < > ("noStudents"));

        UnaryOperator < Change > onlyStrings = change -> {
            String input = change.getText();
            if (input.matches("[a-z A-Z]*")) {
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
        txtDuration.setTextFormatter(new TextFormatter < String > (onlyNumbers));
        txtStudyProgram.setTextFormatter(new TextFormatter < String > (onlyStrings));

        getUniDetails();
        getFaculties();
        getStudyPrograms();

    }

    public void getUniDetails() {

        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery =
            "SELECT name, address, city, country, contact, email, logo " +
            "FROM university WHERE universityID = ?;";

        try {

            select = Database.statement(sqlQuery);
            select.setInt(1, General.univesityID);
            result = select.executeQuery();

            if (result.next()) {

                txtUniName.setText(result.getString(1));
                txtAddress.setText(result.getString(2));
                txtCity.setText(result.getString(3));
                txtCountry.setText(result.getString(4));
                txtContact.setText(result.getString(5));
                txtEmail.setText(result.getString(6));

                Blob blob = result.getBlob(7);

                if (blob != null) {
                    InputStream ins = blob.getBinaryStream();
                    imageviewer.setImage(new Image(ins));
                }

            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                anpane.requestFocus();

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getFaculties() {

        PreparedStatement select = null;
        ResultSet result = null;
        faculties.clear();

        String sqlQuery = "SELECT name FROM faculties WHERE universityID = ?;";

        try {

            select = Database.statement(sqlQuery);
            select.setInt(1, General.univesityID);
            result = select.executeQuery();

            while (result.next()) {
                faculties.add(result.getString(1));
            }
        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                cmbFaculties.setItems(faculties);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getStudyPrograms() {

        PreparedStatement select = null;
        ResultSet result = null;
        studyprograms.clear();

        String sqlQuery =
            "SELECT studyprogramID, sp.name AS 'studyprogram', duration, f.name AS 'faculty', \n" +
            "(SELECT count(studentID) FROM students s WHERE s.studyprogramID = sp.studyprogramID) AS 'No. Students'\n" +
            "FROM studyprograms sp INNER JOIN faculties f ON sp.facultyID = f.facultyID WHERE universityID = ?;";

        try {

            select = Database.statement(sqlQuery);
            select.setInt(1, General.univesityID);
            result = select.executeQuery();

            while (result.next()) {
                studyprograms.add(
                    new StudyProgram(
                        result.getInt(1),
                        result.getString(2),
                        result.getInt(3),
                        result.getString(4),
                        result.getInt(5)
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

                tblStudyPrograms.setItems(studyprograms);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    @FXML public void editUni() {
        tblStudyPrograms.setDisable(true);
        hBoxSP.setDisable(true);
        set(true);
        General.needsSave = true;
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
            txtEmail.getText().trim() == null ||
            txtEmail.getText().trim().isEmpty() ||
            txtContact.getText().trim() == null ||
            txtContact.getText().trim().isEmpty()) {

            General.WARNING("Warning", "Please complete all the required fields to continue");
            return;
        }

        if (!isValidEmail(txtEmail.getText())) {
            return;
        }

        PreparedStatement update = null, updateLogo = null;

        String sqlQuery =
            "UPDATE `university`\n" +
            "SET `name` = ?, `address` = ?, `city` = ?,\n" +
            "`country` = ?, `contact` = ?, `email` = ?\n" +
            "WHERE `universityID` = ?;";

        String sqlQueryLogo =
            "UPDATE `university`\n" +
            "SET `logo` = ?\n" +
            "WHERE `universityID` = ?;";

        try {

            update = Database.statement(sqlQuery);
            update.setString(1, txtUniName.getText());
            update.setString(2, txtAddress.getText());
            update.setString(3, txtCity.getText());
            update.setString(4, txtCountry.getText());
            update.setString(5, txtContact.getText());
            update.setString(6, txtEmail.getText());
            update.setInt(7, General.univesityID);
            update.executeUpdate();

            updateLogo = Database.statement(sqlQueryLogo);
            if (imageviewer.getImage() == null && image == null) {
                updateLogo.setBinaryStream(1, null);
                updateLogo.setInt(2, General.univesityID);
                updateLogo.executeUpdate();
            } else if (imageviewer.getImage() != null && image != null) {
                FileInputStream inputStream = new FileInputStream(image);
                updateLogo.setBinaryStream(1, inputStream, inputStream.available());
                updateLogo.setInt(2, General.univesityID);
                updateLogo.executeUpdate();
            } else if (imageviewer.getImage() != null && image == null) {
                updateLogo = null;
            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } catch (FileNotFoundException e) {
            General.ERROR("Error", e.getMessage());
        } catch (IOException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (update != null)
                    update.close();

                if (updateLogo != null)
                    updateLogo.close();

                getUniDetails();
                set(false);
                General.needsSave = false;
                tblStudyPrograms.setDisable(false);
                hBoxSP.setDisable(false);
                General.INFORMATION("Success !", "Changes saved successfully !");

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    @FXML public void cancelUni() {
        getUniDetails();
        tblStudyPrograms.setDisable(false);
        hBoxSP.setDisable(false);
        set(false);
        General.needsSave = false;
    }

    @FXML public void addStudyProgram() {
        getFaculties();
        txtStudyProgram.clear();
        txtDuration.clear();
        cmbFaculties.setValue(null);
        btnEditUni.setDisable(true);
        tblStudyPrograms.setDisable(true);
        paneStudyPrograms.setVisible(true);
        hBoxSP.setDisable(true);
        metoda = "SHTO";
        General.needsSave = true;
    }

    @FXML public void deleteStudyProgram() {

        if (tblStudyPrograms.getSelectionModel().getSelectedItem() != null &&
            General.CONFIRMATION("Confirmation !", "Do you want to delete this record !")) {

            studyprogramID = tblStudyPrograms.getSelectionModel().getSelectedItem().getStudyprogramID();

            PreparedStatement delete = null;

            String sqlQuery = "DELETE FROM `studyprograms` WHERE `studyprogramID` = ?;\n";

            try {
                delete = Database.statement(sqlQuery);
                delete.setInt(1, studyprogramID);
                delete.execute();
            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            } finally {
                try {

                    if (delete != null)
                        delete.close();
                    getStudyPrograms();

                } catch (SQLException e) {
                    General.ERROR("Error", e.getMessage());
                }
            }

        } else {
            General.WARNING("Warning", "Please select the record you want to delete !");
        }

    }

    @FXML public void editStudyProgram() {

        if (tblStudyPrograms.getSelectionModel().getSelectedItem() != null) {
            getFaculties();
            StudyProgram record = tblStudyPrograms.getSelectionModel().getSelectedItem();

            txtStudyProgram.setText(record.getName());
            cmbFaculties.setValue(record.getFaculty());
            txtDuration.setText(record.getDuration() + "");
            studyprogramID = record.getStudyprogramID();

            btnEditUni.setDisable(true);
            tblStudyPrograms.setDisable(true);
            paneStudyPrograms.setVisible(true);
            hBoxSP.setDisable(true);
            metoda = "EDIT";
            General.needsSave = true;
        } else {
            General.WARNING("Warning", "Please select the record you want to edit !");
        }
    }

    @FXML public void cancelStudyProgram() {
        btnEditUni.setDisable(false);
        tblStudyPrograms.setDisable(false);
        paneStudyPrograms.setVisible(false);
        hBoxSP.setDisable(false);
        metoda = null;
        General.needsSave = false;
        tblStudyPrograms.getSelectionModel().clearSelection();
    }

    @FXML public void saveStudyProgram() {

        if (txtStudyProgram.getText() == null ||
            txtStudyProgram.getText().trim().isEmpty() ||
            txtDuration.getText() == null ||
            txtDuration.getText().trim().isEmpty() ||
            cmbFaculties.getValue() == null ||
            cmbFaculties.getValue().trim().isEmpty()) {

            General.WARNING("Warning", "Please complete all the required fields to continue");
            return;
        }

        PreparedStatement insert = null, update = null;

        String sqlQueryInsert = "INSERT INTO `studyprograms` (`name`, `duration`, `facultyID`) VALUES(?,?,?);";
        String sqlQueryUpdate =
            "UPDATE `studyprograms` " +
            "SET `name` =?, `duration` = ?, `facultyID` = ? " +
            "WHERE `studyprogramID` = ?;\n";

        try {

            if (metoda.equals("SHTO")) {
                insert = Database.statement(sqlQueryInsert);
                insert.setString(1, txtStudyProgram.getText());
                insert.setInt(2, Integer.parseInt(txtDuration.getText()));
                insert.setInt(3, getFacultyID());
                insert.execute();
            } else if (metoda.equals("EDIT") && studyprogramID != 0) {
                update = Database.statement(sqlQueryUpdate);
                update.setString(1, txtStudyProgram.getText());
                update.setInt(2, Integer.parseInt(txtDuration.getText()));
                update.setInt(3, getFacultyID());
                update.setInt(4, studyprogramID);
                update.executeUpdate();
            } else {
                General.ERROR("ERROR", "SOMETHING WENT WRONG\nPLEASE CONTACT SUPPORT");
                return;
            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
            return;
        } finally {

            try {

                if (insert != null)
                    insert.close();

                if (update != null)
                    update.close();

                getStudyPrograms();
                cancelStudyProgram();
                General.INFORMATION("Success !", "Changes saved successfully !");

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public int getFacultyID() {
        int id = 0;

        String sqlQuerySelect = "SELECT facultyID FROM faculties WHERE name = ? AND universityID = ?;";
        String sqlQueryInsert = "INSERT INTO `faculties` (`name`, `universityID`) VALUES(?,?);";

        PreparedStatement select = null, insert = null;
        ResultSet result = null, generatedKeys = null;

        try {
            select = Database.statement(sqlQuerySelect);
            select.setString(1, cmbFaculties.getValue());
            select.setInt(2, General.univesityID);
            result = select.executeQuery();

            if (result.next()) {
                id = result.getInt(1);
            } else {

                insert = Database.connect().prepareStatement(sqlQueryInsert, Statement.RETURN_GENERATED_KEYS);
                insert.setString(1, cmbFaculties.getValue());
                insert.setInt(2, General.univesityID);
                insert.execute();

                generatedKeys = insert.getGeneratedKeys();

                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                } else {
                    General.ERROR("ERROR", "SOMETHING WENT WRONG\nPLEASE CONTACT SUPPORT");
                }

            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        }

        return id;
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
                General.ERROR("Error", e.getMessage());
            } finally {
                imageviewer.setImage(uploadedImage);
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

    public void set(boolean x) {

        txtUniName.setEditable(x);
        txtAddress.setEditable(x);
        txtCity.setEditable(x);
        txtCountry.setEditable(x);
        txtContact.setEditable(x);
        txtEmail.setEditable(x);

        btnUploadImage.setDisable(!x);
        btnSaveUni.setVisible(x);
        btnCancelUni.setVisible(x);
        btnEditUni.setDisable(x);

    }

}