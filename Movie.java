package hyon;

import java.util.ArrayList;

/**
 * This Movie class represents a single Movie object.
 * A movie object has title, genres, year, and running time.
 * 
 * @author Hyon Lee
 */
public class Movie {
    private String title;
    private int year;
    private int runningTime;
    private String genres;
    
    public Movie() {
        
    }
    
    public Movie(String title, int year, int time, ArrayList<String> genres) {
        setTitle(title);
        setYear(year);
        setRunningTime(time);
        setGenres(genres);
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public void setRunningTime(int time) {
        this.runningTime = time;
    }   

    public void setGenres(ArrayList<String> genre) {
        this.genres = genre.toString().replace("[", "").replace("]", "");
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getYear() {
        return year;
    }
    
    public int getRunningTime() {
        return runningTime;
    }
    
    public String getGenres() {
        return genres;
    }  
}
