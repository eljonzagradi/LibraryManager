package controllers;

import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Book;
import models.Borrow;
import models.Database;
import models.General;
import models.Student;

public class BorrowController implements Initializable {

    @FXML private Pane pane;
    @FXML private GridPane calendar, weekdays;
    @FXML private HBox header;
    @FXML private VBox vBox;
    @FXML private Button btnBorrow, btnReturn, btnSave, btnCancel;
    @FXML private Button btnNext, btnPrev;
    @FXML private ToggleButton tgStart;
    @FXML private ToggleButton tgDue;

    @FXML private TextField txtBook, txtStudent, txtStudentBarcode, txtBookBarcode, txtStart, txtDue;

    @FXML private Text lblMonth;
    @FXML private Label lblMonday, lblTuesday, lblWednesday, lblThursday, lblFriday, lblSaturday, lblSunday;

    @FXML private ListView < Book > lvBooks;
    @FXML private ListView < Student > lvStudents;

    @FXML private TableView < Borrow > tblBorrow;
    @FXML private TableColumn < Borrow, String > tblColBook;
    @FXML private TableColumn < Borrow, Date > tblColDue;
    @FXML private TableColumn < Borrow, Date > tblColStart;
    @FXML private TableColumn < Borrow, String > tblColStatus;
    @FXML private TableColumn < Borrow, String > tblColStudent;

    private ArrayList < DayNode > allCalendarDays = new ArrayList < > (42);
    private YearMonth currentYearMonth;
    private LocalDate start, due;
    private ObservableList < Student > students = FXCollections.observableArrayList();
    private ObservableList < Book > books = FXCollections.observableArrayList();
    private int bookID = 0, studentID = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        txtStudentBarcode.textProperty().addListener(e -> getStudents());
        txtBookBarcode.textProperty().addListener(e -> getBooks());
        
        txtBookBarcode.setOnMouseClicked(e -> {
            if (e.getClickCount() == 3) {
            	txtBookBarcode.clear();
            	txtBook.setText(null);
            	General.bookBarcode = null;
            	lvBooks.getItems().clear();
            	books.clear();
            	
            }
        });
        
        txtStudentBarcode.setOnMouseClicked(e -> {
            if (e.getClickCount() == 3) {
            	txtStudentBarcode.clear();
            	txtStudent.setText(null);
            	General.studentBarcode = null;
            	lvStudents.getItems().clear();
            	students.clear();
            	
            }
        });
        
        lvBooks.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 
            		&& lvBooks.getSelectionModel()
            		.getSelectedItem() != null ) {
            	txtBook.setText(lvBooks.getSelectionModel()
                		.getSelectedItem().getTitle());
            	bookID = lvBooks.getSelectionModel()
                		.getSelectedItem().getBookID();
            }
        });
        
        lvStudents.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 
            		&& lvStudents.getSelectionModel()
            		.getSelectedItem() != null ) {
            	txtStudent.setText(lvStudents.getSelectionModel()
                		.getSelectedItem().getFullName());
            	studentID = lvStudents.getSelectionModel()
                		.getSelectedItem().getStudentID();
            }
        });

        tgStart.setOnAction(e -> {
            setStart(LocalDate.now());
            setDue(LocalDate.now().plusWeeks(2));
            tgDue.setDisable(false);
            populateCalendar(currentYearMonth);
        });

        CalendarView(YearMonth.now());
    }

    public void CalendarView(YearMonth yearMonth) {
        allCalendarDays.clear();
        currentYearMonth = yearMonth;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                DayNode dateCell = new DayNode();
                dateCell.setStyle("-fx-background-color: white;");
                calendar.add(dateCell, j, i);
                allCalendarDays.add(dateCell);
            }
        }

        populateCalendar(currentYearMonth);

    }

    public void populateCalendar(YearMonth yearMonth) {

        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();

        LocalDate date = LocalDate.of(year, month, 1);

        while (!date.getDayOfWeek().toString().equals("MONDAY")) {
            date = date.minusDays(1);
        }

        for (DayNode dateCell: allCalendarDays) {

            if (date.getMonthValue() != month) {
                dateCell.setStyle("-fx-background-color: white;");
                dateCell.setText(null);
                dateCell.setDate(null);
                dateCell.setDisable(true);
            } else {
                String txt = String.valueOf(date.getDayOfMonth());
                dateCell.setDisable(false);
                dateCell.setDate(date);
                dateCell.setText(txt);
            }

            dateCell.setOnMouseClicked(e -> {

                if ((tgStart.isSelected() ||
                        tgDue.isSelected()) &&
                    dateCell.getDate()
                    .isBefore(LocalDate.now())) {

                    General.WARNING("Warninig", "You can not select this date !");
                    return;

                }

                if (tgStart.isSelected()) {

                    setStart(dateCell.getDate());
                    tgDue.setSelected(true);

                } else if (tgDue.isSelected()) {
                    if (getStart() != null) {
                        LocalDate temp = null;
                        if (!dateCell.getDate().isEqual(getStart())) {
                            if (dateCell.getDate().isBefore(getStart())) {
                                temp = getStart();
                                setStart(dateCell.getDate());
                            }

                        } else {
                            return;
                        }

                        if (temp == null) {
                            setDue(dateCell.getDate());
                        } else {
                            setDue(temp);
                        }
                    }

                } else {
                    return;
                }

                populateCalendar(currentYearMonth);

            });

            String color = "white;";
            String borderWidth = "2;";
            String borderColor = "black;";

            if (dateCell.getDate() != null) {

                if (getStart() != null &&
                    dateCell.getDate().isEqual(getStart())) {
                    color = "yellow;";
                }

                if (getStart() != null &&
                    getDue() != null &&
                    dateCell.getDate().compareTo(getStart()) >= 0 &&
                    dateCell.getDate().compareTo(getDue()) <= 0) {
                    color = "yellow;";
                }

                dateCell.setStyle(" -fx-background-color: " + color +
                    " -fx-border-width: " + borderWidth +
                    " -fx-border-color: " + borderColor);
            }

            date = date.plusDays(1);

        }

        lblMonth.setText(
            yearMonth.getMonth() +
            " " +
            String.valueOf(yearMonth.getYear()));
    }

    public void setStart(LocalDate start) {
        this.start = start;

        if (this.start == null) {
            this.txtStart.setText("");
        } else {
            this.txtStart.setText(this.start.toString());
        }
    }

    public void setDue(LocalDate due) {
        this.due = due;

        if (this.due == null) {
            this.txtDue.setText("");
        } else {
            this.txtDue.setText(this.due.toString());
        }
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getDue() {
        return due;
    }

    public void getBorrows() {

    }

    public void getStudents() {
    	
        lvStudents.getItems().clear();
        students.clear();
        PreparedStatement select = null;
        ResultSet result = null;
        String student = "";

        String sqlQuery =
            "SELECT DISTINCT studentID, student, grade, barcode, registration_date, sp.name\n" +
            "FROM students s INNER JOIN studyprograms sp ON sp.studyprogramID = s.studyprogramID\n" +
            "WHERE barcode LIKE ? OR student LIKE ? ;";

        if (txtStudentBarcode.getText() == null) {
            student = "";
        } else {
            student = txtStudentBarcode.getText();
        }

        try {

            select = Database.statement(sqlQuery);
            select.setString(1, student + "%");
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

                lvStudents.setItems(students);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getBooks() {
    	
        lvBooks.getItems().clear();
        books.clear();
        PreparedStatement select = null;
        ResultSet result = null;
        String book = "";

        String sqlQuery =
            "SELECT DISTINCT bookID, title, barcode, category, num_pages,\n" +
            "language, author, publisher, copies FROM books\n" +
            "WHERE barcode LIKE ? OR title LIKE ? ";

        if (txtBookBarcode.getText() == null) {
            book = "";
        } else {
            book = txtBookBarcode.getText();
        }

        try {

            select = Database.statement(sqlQuery);
            select.setString(1, book + "%");
            select.setString(2, book + "%");
            result = select.executeQuery();

            while (result.next()) {

                books.add(new Book(
                    result.getInt(1),
                    result.getString(2),
                    result.getString(4),
                    result.getInt(5),
                    result.getString(6),
                    result.getString(7),
                    result.getString(8),
                    result.getString(3),
                    result.getInt(9)

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

                lvBooks.setItems(books);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    @FXML public void save() {

    }

    @FXML public void cancel() {
        
    	setStart(null);
        setDue(null);
        tgDue.setSelected(false);
        populateCalendar(currentYearMonth);
        btnBorrow.setVisible(true);
        btnReturn.setVisible(true);
        General.studentBarcode = null;
        General.bookBarcode = null;
        btnSave.setVisible(false);
        btnCancel.setVisible(false);
        txtBookBarcode.setEditable(false);
        txtStudentBarcode.setEditable(false);
        txtStudent.clear();
        txtBook.clear();
        txtBookBarcode.clear();
        txtStudentBarcode.clear();
        lvStudents.getItems().clear();
        lvBooks.getItems().clear();
        students.clear();
        books.clear();
        
    }

    @FXML public void borrow() {

        setStart(LocalDate.now());
        setDue(LocalDate.now().plusWeeks(2));
        tgDue.setSelected(true);
        populateCalendar(currentYearMonth);
        btnBorrow.setVisible(false);
        btnReturn.setVisible(false);
        studentID = 0;
        bookID = 0;
        btnSave.setVisible(true);
        btnCancel.setVisible(true);
        txtBookBarcode.setEditable(true);
        txtStudentBarcode.setEditable(true);
    }

    @FXML public void returnBook() {

    }

    @FXML public void next() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        populateCalendar(currentYearMonth);
    }

    @FXML public void prev() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        populateCalendar(currentYearMonth);
    }

    public static class DayNode extends Label {

        private LocalDate date;

        public DayNode() {
            super();
            this.setAlignment(Pos.CENTER);
            this.setPrefHeight(45);
            this.setPrefWidth(45);
            this.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            this.getStyleClass().add("costum");
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

    }

}