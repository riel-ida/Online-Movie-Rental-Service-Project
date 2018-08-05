package bgu.spl181.net.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BBProtocolSharedData {
    private UsersData usersData;
    private MoviesData moviesData;
    final ReentrantReadWriteLock lockUsers = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock lockMovies = new ReentrantReadWriteLock();

    public BBProtocolSharedData(UsersData usersData, MoviesData moviesData) {
        this.usersData = usersData;
        this.moviesData = moviesData;
    }

    public Movie searchMovieByName(String movieName) {
        for (int i = 0; i < moviesData.getMovies().size(); i++) {
            if (moviesData.getMovies().get(i).getName().equalsIgnoreCase(movieName))
                return moviesData.getMovies().get(i);
        }
        return null;
    }

    public User searchUserByName(String userName) {
        for (int i = 0; i < usersData.getUsers().size(); i++) {
            if (usersData.getUsers().get(i).getUsername().equalsIgnoreCase(userName))
                return usersData.getUsers().get(i);
        }
        return null;
    }

    public String getAllMovies(){
        String allMovies = "";
        for (Movie movie : moviesData.getMovies()) {
            allMovies = allMovies +" " + '"' + movie.getName() + '"';
        }
        return allMovies;
    }

    public void addMovie(Movie movie){
        moviesData.getMovies().add(movie);
    }

    public void removeMovie(Movie movie){
        moviesData.getMovies().remove(movie);
    }

    public void addUser(User user){
        usersData.getUsers().add(user);
    }

    public boolean checkIfRented(String movieName){
        for(User user : usersData.getUsers()){
            if(user.isRentedByThis(movieName))
                return true;
        }
        return false;
    }

    public int getHighestID(){ //looking for the highest movie's id
        int highestID = 0;
        for(int i=0; i<moviesData.getMovies().size(); i++){
            if(moviesData.getMovies().get(i).getId() >= highestID){
                highestID = moviesData.getMovies().get(i).getId() +1;
            }
        }
        return highestID;
    }

    public void removeMovieFromUser(User user, String movieName){
        for(int i=0; i<user.getMovies().size(); i++){
            if(user.getMovies().get(i).getName().equalsIgnoreCase(movieName)) {
                MovieDetails movie = user.getMovies().get(i);
                user.getMovies().remove(movie);
            }
        }
    }

    public void addMovieToUser(User user, Movie movie){
        user.getMovies().add(new MovieDetails(movie.getId(), movie.getName()));
    }

    protected boolean writeDataToJson(Object jsonData, String path){
        // Initializing
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Integer.class, (JsonSerializer<Integer>)
                        (integer, type, jsonSerializationContext) -> new JsonPrimitive(integer.toString()))
                .registerTypeAdapter(AtomicInteger.class, (JsonSerializer<AtomicInteger>)
                        (integer, type, jsonSerializationContext) -> new JsonPrimitive(integer.toString())).create();
        // Creating json string from data
        String json = gson.toJson(jsonData);
        // Writing json string to file
        try(PrintWriter out = new PrintWriter(path)){
            // Writing
            out.println( json );
            // Declaring success
            return true;
        } catch (FileNotFoundException e) {
            // Printing stack trace
            e.printStackTrace();
            // Declaring failure
            return false;
        }
    }

    protected boolean writeUsersDataToJson(){
        return writeDataToJson(usersData, "DataBase/Users.json");
    }

    protected boolean writeMoviesDataToJson(){
        return writeDataToJson(moviesData, "DataBase/Movies.json");
    }
}





