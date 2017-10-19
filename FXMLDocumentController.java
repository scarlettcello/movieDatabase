package hyon;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * receives the user-input year and genre and displays the filtered movie list.
 * 
 * @author Hyon Lee
 */
public class FXMLDocumentController implements Initializable {
    
    JdbcHelper jdbc = new JdbcHelper();
    HashMap<String, Integer> genres = new HashMap<>();
    boolean connected = false;
            
    @FXML
    private TextField url, user, password;
    
    @FXML
    private Button connect;
    
    @FXML
    private ComboBox<Integer> yearBox;    
    private final ObservableList<Integer> years = FXCollections.observableArrayList();

    @FXML
    private ToggleGroup genre;
    
    @FXML
    private Label count;
    
    @FXML
    private TableView<Movie> movieTable;
    
    @FXML
    TableColumn<Movie, String> titleColumn, genreColumn, yearColumn, timeColumn;
    
    /**
     * creates connection between java and the DB
     * 
     * @param event by clicking the connect button, the action is implemented.
     */
    @FXML
    private void connect(ActionEvent event) {
        
        // connects to the DB
        String db_url = url.getText();
        String db_user = user.getText();
        String db_pass = "PROG32758";
        if(connect.getText().equals("Connect")) {
            if(jdbc.connect(db_url, db_user, db_pass)) {
                connect.setText("Disconnect");
                connected = true;
                try {
                    String sql = "SELECT DISTINCT year FROM Movie";
                    ResultSet rs = jdbc.query(sql);
                    while (rs.next()) {
                        int year = rs.getInt("year");
                        years.add(year);
                    }
                } catch (SQLException e) {
                    System.err.println(e.getSQLState() + ": " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Collections.sort(years, Collections.reverseOrder());
                yearBox.setItems(years);
            } 
        } else {
            jdbc.disconnect();
            connect.setText("Connect");
            connected = false;
        }       
    }
    
    /**
     * retrieves the current JdbcHelper instance in the program
     * 
     * @return jdbcHelper instance
     */
    public JdbcHelper getJdbc() {
        return this.jdbc;
    }
     
    /**
     * sets genres HashMap for reverse-lookup for index by genre name
     * 
     * @param genres receives empty HashMap
     * @return mapped genres
     */
    private HashMap mappingGenres(HashMap genres) {
        try {
            String sql = "SELECT * FROM Genre";
            ResultSet rs = jdbc.query(sql);
            while(rs.next()) {            
                String name = rs.getString("name");
                int id = rs.getInt("id");
                genres.put(name, id);
            }
        } catch (SQLException e) {
            System.err.println(e.getSQLState() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genres;
    }
    
    /**
     * displays the filtered result by user selection.
     * The user can see the movies list by either genre or year, or both.
     */
    private void display() {
        HashMap mappedGenres = mappingGenres(genres);
        ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();    
        ArrayList<Object> param1 = new ArrayList();
        String sql1 = null;        

        // If the user doesn't select any year
        if (yearBox.getSelectionModel().isEmpty()) {
            RadioButton selectedGenre = (RadioButton)genre.getSelectedToggle();
            
            // If the user select "Any" genre 
            if (selectedGenre.getText().equals("Any")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Selection Error");
                alert.setContentText("Please select a year.");
                alert.showAndWait();
                
            // If the user doesn't select a year but selects a genre
            } else {
                sql1 = "SELECT * FROM Movie INNER JOIN MovieGenre "
                     + "ON id = MovieGenre.movieId "
                     + "WHERE MovieGenre.genreId = ?";
                param1.add(mappedGenres.get(selectedGenre.getText()));
            }  
            
        // If the user doesn't select a genre    
        } else if (genre.getSelectedToggle() == null) {
            int selectedYear = yearBox.getSelectionModel().getSelectedItem();
            sql1 = "SELECT * FROM Movie INNER JOIN MovieGenre "
                 + "ON id = MovieGenre.movieId "
                 + "WHERE year = ?";
            param1.add(selectedYear);
            
        // If the user selects a year and a genre
        } else {
            int selectedYear = yearBox.getSelectionModel().getSelectedItem();
            RadioButton selectedGenre = (RadioButton)genre.getSelectedToggle();
            
            // If the user selets "Any" genre
            if (selectedGenre.getText().equals("Any")) {
                sql1 = "SELECT * FROM Movie INNER JOIN MovieGenre "
                 + "ON id = MovieGenre.movieId "
                 + "WHERE year = ?";
                param1.add(selectedYear);
                
            // If the user selects a year and a genre besides "Any"    
            } else {
                sql1 = "SELECT * FROM Movie INNER JOIN MovieGenre "
                     + "ON id = MovieGenre.movieId "
                     + "WHERE year = ? AND MovieGenre.genreId = ?";
                param1.add(selectedYear);
                param1.add(mappedGenres.get(selectedGenre.getText()));
            }
        }  
        
        try {
            
            // gets the results through jdbcHelper
            ResultSet rs1 = jdbc.query(sql1, param1);
            
            // If there are no movies, message appears
            if (!rs1.isBeforeFirst()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No DATA");
                alert.setContentText("There are no movies found.");
                alert.showAndWait();
            } else {
                
                // retrieve the data by field
                while(rs1.next()) {
                    int id = rs1.getInt("id");
                    String title = rs1.getString("title");
                    int year = rs1.getInt("year");
                    int runningTime = rs1.getInt("runningTime");

                    //to display genres for each filtered movie
                    String sql2 = "SELECT name FROM Genre INNER JOIN MovieGenre "
                                + "ON id = MovieGenre.genreId "
                                + "WHERE MovieGenre.movieId = ?";
                    ArrayList<Object> param2 = new ArrayList();
                    param2.add(id);
                    ResultSet rs2 = jdbc.query(sql2, param2);
                    ArrayList<String> filteredGenres = new ArrayList();

                    while(rs2.next()) {
                        String filteredGenre = rs2.getString("name");
                        filteredGenres.add(filteredGenre);
                    }

                    Movie movie = new Movie(title, year, runningTime, filteredGenres);
                    if (filteredMovies.size() > 0) {
                        if (!filteredMovies.get(filteredMovies.size()-1)
                            .getTitle().equals(movie.getTitle())){
                            filteredMovies.add(movie);
                        }
                    } else {
                        filteredMovies.add(movie);
                    }                   

                    // shows the filtered movies list    
                    movieTable.setItems(filteredMovies);
                    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                    genreColumn.setCellValueFactory(new PropertyValueFactory<>("genres"));
                    yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
                    timeColumn.setCellValueFactory(new PropertyValueFactory<>("runningTime"));
                    count.setText("Movies: "+ filteredMovies.size());
                }
            }
        } catch (SQLException e) {
            System.err.print(e.getSQLState() + ": " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }       
        
    /**
     * creates the list of years to set in the ComboBox before connection
     * 
     * @return 
     */
    /*
    private ObservableList<Integer> yearList() {       
        for (int i = 144; i > 0; i--) {
            years.add(i + 1873);
        }
        return years;
    }
    */
    
    /**
     * initializes GUI with its components
     * 
     * @param url is the FXML address
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //sets the column width by percentage
        titleColumn.prefWidthProperty().bind(movieTable.widthProperty().multiply(0.60));
        genreColumn.prefWidthProperty().bind(movieTable.widthProperty().multiply(0.20));
        yearColumn.prefWidthProperty().bind(movieTable.widthProperty().multiply(0.10));
        timeColumn.prefWidthProperty().bind(movieTable.widthProperty().multiply(0.10));   
        
        //fills in the year ComboBox
        //yearBox.setItems(yearList());
        
        // adds listener to the year combobox and executes display method 
        // if the program is connected to the DB
        yearBox.getSelectionModel().selectedItemProperty().addListener
        (new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (!connected) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("No connection");
                    alert.setContentText("Please press Connect button to connect to the DB.");
                    alert.showAndWait();
                } else {
                   display(); 
                }                
            }
        });

        // adds listener to the genre radio buttons and executes display method
        // if the prgram is connected to the DB
        genre.selectedToggleProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) { 
                if (!connected) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("No connection");
                    alert.setContentText("Please press Connect button to connect to the DB.");
                    alert.showAndWait();
                } else {
                   display(); 
                }
            }
        });        
    }  
}


