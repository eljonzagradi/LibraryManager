package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import models.Database;
import models.General;
import models.Student;

public class StudentsController implements Initializable {

    @FXML private AnchorPane anpane;
    @FXML private Pane pane;

    @FXML private Button btnImport, btnDownload, btnRegister, btnModify, btnSave, btnCancel, btnRefresh;

    @FXML private ComboBox < String > cmbStudyProgram1, cmbStudyProgram2;

    @FXML private ComboBox < Integer > cmbYear;

    @FXML private Label lblStudent, lblName, lblYear, lblStudyPrg, lblStudyProgram, lblBarcode;
    @FXML private TextField txtName, txtStudent, txtBarcode, txtBarkode;

    @FXML private TableView < Student > tblStudents;
    @FXML private TableColumn < Student, String > tblColStudent;
    @FXML private TableColumn < Student, String > tblColStudyProgram;
    @FXML private TableColumn < Student, Number > tblColYear;
    @FXML private TableColumn < Student, Date > tblColRegistrationDate;
    @FXML private TableColumn < Student, String > tblColStatus;
    @FXML private TableColumn < Student, String > tblColBarcode;

    private ObservableList < Student > students = FXCollections.observableArrayList();
    private ObservableList < String > studyprograms = FXCollections.observableArrayList();
    private String metoda;
    private int studentID = 0, studyprogramID = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tblColStudyProgram.setCellValueFactory(new PropertyValueFactory < > ("studyprogram"));
        tblColRegistrationDate.setCellValueFactory(new PropertyValueFactory < > ("registrationDate"));
        tblColStudent.setCellValueFactory(new PropertyValueFactory < > ("fullName"));
        tblColYear.setCellValueFactory(new PropertyValueFactory < > ("year"));
        tblColBarcode.setCellValueFactory(new PropertyValueFactory < > ("barcode"));

        UnaryOperator < Change > onlyStrings = change -> {
            String input = change.getText();
            if (input.matches("[a-z A-Z]*")) {
                return change;
            }
            return null;
        };

        txtStudent.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtName.setTextFormatter(new TextFormatter < String > (onlyStrings));
        getStudyPrograms();
        getStudents();

        txtBarcode.textProperty().addListener(e -> getStudents());
        cmbStudyProgram1.valueProperty().addListener(e -> getStudents());
        txtStudent.textProperty().addListener(e -> getStudents());
        cmbStudyProgram2.valueProperty().addListener((options, oldValue, newValue) -> {

            if (newValue != oldValue &&
                newValue != null) {
                cmbYear.getItems().clear();
                int nrMax = 0;
                PreparedStatement select = Database.statement("SELECT studyprogramID, duration FROM studyprograms WHERE name = ?;");
                try {
                    select.setString(1, newValue);
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        studyprogramID = result.getInt(1);
                        nrMax = result.getInt(2);
                    }

                    for (int i = 1; i <= nrMax; i++) {
                        cmbYear.getItems().add(i);
                    }

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            }

        });

    }

    public void getStudents() {

        students.clear();
        PreparedStatement select = null;
        ResultSet result = null;
        String barcode = null, student = null;

        String sqlQuery =
            "SELECT studentID, student, grade, barcode, registration_date, sp.name\n" +
            "FROM students s INNER JOIN studyprograms sp ON sp.studyprogramID = s.studyprogramID\n" +
            "WHERE barcode LIKE ? AND student LIKE ? ";

        if (txtBarcode.getText() == null) {
            barcode = "";
        } else {
            barcode = txtBarcode.getText();
        }

        if (txtStudent.getText() == null) {
            student = "";
        } else {
            student = txtStudent.getText();
        }

        if (cmbStudyProgram1.getValue() != null &&
            !cmbStudyProgram1.getValue().equals("All")) {
            sqlQuery += " AND sp.name = '" + cmbStudyProgram1.getValue() + "' ";
        }

        try {

            select = Database.statement(sqlQuery);
            select.setString(1, barcode + "%");
            select.setString(2, student + "%");
            result = select.executeQuery();

            while (result.next()) {

                students.add(new Student(

                    result.getInt(1),
                    result.getString(2),
                    result.getString(6),
                    result.getInt(3),
                    result.getString(4),
                    result.getDate(5)
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

                tblStudents.setItems(students);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getStudyPrograms() {

        studyprograms.clear();
        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT name FROM studyprograms;";

        try {

            select = Database.statement(sqlQuery);
            result = select.executeQuery();

            while (result.next()) {

                studyprograms.add(result.getString(1));

            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                cmbStudyProgram1.getItems().add("All");
                cmbStudyProgram1.getItems().addAll(studyprograms);
                cmbStudyProgram1.setValue("All");
                cmbStudyProgram2.setItems(studyprograms);

                anpane.requestFocus();

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    @FXML public void registerStudent() {
        metoda = "SHTO";
        pane.setVisible(true);
        btnRegister.setDisable(true);
        btnModify.setDisable(true);
        tblStudents.setDisable(true);
        txtName.setText(null);
        cmbStudyProgram2.setValue(null);
        cmbYear.setValue(null);
        txtBarkode.setText(null);
    }

    @FXML public void editStudent() {

        if (tblStudents.getSelectionModel().getSelectedItem() != null) {

            Student record = tblStudents.getSelectionModel().getSelectedItem();

            tblStudents.setDisable(true);
            studentID = record.getStudentID();

            txtName.setText(record.getFullName());
            cmbStudyProgram2.setValue(record.getStudyprogram());
            cmbYear.setValue(record.getYear());
            txtBarkode.setText(record.getBarcode());

            metoda = "EDIT";
            pane.setVisible(true);
            btnRegister.setDisable(true);
            btnModify.setDisable(true);
        } else {
            General.WARNING("Warning", "Please select the record you want to edit !");
        }
    }

    @FXML public void saveStudent() {

        if (txtName.getText() == null ||
            txtName.getText().trim().isEmpty() ||
            cmbStudyProgram2.getValue() == null ||
            cmbStudyProgram2.getValue().trim().isEmpty() ||
            cmbYear.getValue() == null ||
            txtBarkode.getText() == null ||
            txtBarkode.getText().trim().isEmpty()
        ) {

            General.WARNING("Warning", "Please complete all the required fields to continue");
            return;
        }

        PreparedStatement insert = null, update = null;

        String sqlQueryInsert =
            "INSERT INTO `students` (`student`, `grade`, `barcode`, `studyprogramID`)\n" +
            "VALUES (?, ?, ?, ?, ?);";
        String sqlQueryUpdate =
            "UPDATE `students` SET `student` = ?, \n" +
            "`grade` = ?, `barcode` = ?,\n" +
            "`studyprogramID` = ? WHERE `studentID` = ?;";

        try {

            if (metoda.equals("SHTO")) {
                insert = Database.statement(sqlQueryInsert);
                insert.setString(1, txtName.getText());
                insert.setInt(2, cmbYear.getValue());
                insert.setString(3, txtBarkode.getText());
                insert.setInt(4, studyprogramID);
                insert.execute();
            } else if (metoda.equals("EDIT") && studentID != 0) {
                update = Database.statement(sqlQueryUpdate);
                update.setString(1, txtName.getText());
                update.setInt(2, cmbYear.getValue());
                update.setString(3, txtBarkode.getText());
                update.setInt(4, studyprogramID);
                update.setInt(5, studentID);
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

                refresh();
                cancelStudent();
                General.INFORMATION("Success !", "Changes saved successfully !");

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    @FXML public void cancelStudent() {
        metoda = null;
        pane.setVisible(false);
        btnRegister.setDisable(false);
        btnModify.setDisable(false);
        tblStudents.setDisable(false);
    }

    @FXML public void refresh() {
        txtStudent.clear();
        cmbStudyProgram1.setValue("All");
        txtBarcode.clear();
    }

    @FXML public void downloadTemplate() throws Exception {

        FileChooser fileChooser = new FileChooser();
        InputStream in = null;

        in = getClass().getResourceAsStream("/files/Students.csv");

        fileChooser.setInitialFileName("Students - Template " + LocalDate.now());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        File dest = fileChooser.showSaveDialog(null);
        if (dest != null) {
            if (!dest.exists()) {
                Files.copy(in, dest.toPath());
                General.INFORMATION("Success", "File Downloaded Successfully");

            }
        }
    }

    @FXML
    public void uploadCVS() {

        if (!General.CONFIRMATION("Information", "Before you procced please make sure that all your data is correct and follows the rules below:\n" +
                "1) Study Program is registred\n" +
                "2) No empty cells\n" +
                "3) Year of study should be a number\n" +
                "4) No duplicate barcodes\n" +
                "If these rules are not followed the import process will be interrupted or the faulty records will not be recorded\n" +
                "Do you wish to continue ?")) {
            return;
        }

        PreparedStatement select = null, insert = null;
        ResultSet result = null;
        int counter = 1;

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        String path = "", student = "", programstudy = "", barcode = "";
        int year = 0;

        String sqlQuerySelect = "SELECT studyprogramID FROM studyprograms WHERE name = ?;";
        String sqlQueryInsert =
            "INSERT INTO `students`\n" +
            "( `student`, `grade`, `barcode`,`studyprogramID`)\n" +
            "VALUES (?,?,?,?);";

        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            return;
        }
        path = file.getPath();

        try (Reader reader = Files.newBufferedReader(Paths.get(path), Charset.forName("windows-1251")); CSVParser csvParser = new CSVParser(reader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
            for (CSVRecord csvRecord: csvParser) {
                counter++;

                if (csvRecord.get("Student Full Name").equals("") || csvRecord.get("Student Full Name") == null) {
                    General.WARNING("Warninig", "Empty Cell A(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Study Program").equals("") || csvRecord.get("Study Program") == null) {
                    General.WARNING("Warninig", "Empty Cell B(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Year of Study").equals("") || csvRecord.get("Year of Study") == null) {
                    General.WARNING("Warninig", "Empty Cell C(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Barcode").equals("") || csvRecord.get("Barcode") == null) {
                    General.WARNING("Warninig", "Empty Cell D(" + counter + ")");
                    return;
                }

                student = csvRecord.get("Student Full Name");
                programstudy = csvRecord.get("Study Program");
                year = Integer.parseInt(csvRecord.get("Year of Study"));
                barcode = csvRecord.get("Barcode");

                select = Database.statement(sqlQuerySelect);
                try {
                    select.setString(1, programstudy);
                    result = select.executeQuery();
                } catch (SQLException e) {
                    General.ERROR("Error", e.getMessage());
                }

                try {
                    if (result.next()) {

                        insert = Database.statement(sqlQueryInsert);
                        insert.setString(1, student);
                        insert.setInt(2, year);
                        insert.setString(3, barcode);
                        insert.setInt(4, result.getInt(1));
                        insert.execute();

                    } else {
                        continue;
                    }

                } catch (SQLException e) {
                    General.ERROR("Error", e.getMessage() + "\nROW: " + counter);
                    continue;
                }

            }
        } catch (IOException e) {
            General.ERROR("Error", e.getMessage());
        } finally {
            getStudents();
        }

    }

}