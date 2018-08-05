package bgu.spl181.net.api;

import java.util.ArrayList;

public class User {
    private String username;
    private String type;
    private String password;
    private String country;
    private ArrayList<MovieDetails> movies;
    private int balance;

    public User(String username, String type, String password, String country, ArrayList<MovieDetails> movies, int balance) {
        this.username = username;
        this.type = type;
        this.password = password;
        this.country = country;
        this.movies = movies;
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public ArrayList<MovieDetails> getMovies() {
        return movies;
    }

    public int getBalance() { return balance; }


    public void setBalanceUp(int amount){
        this.balance = balance + amount;
    }

    public void setBalanceDown(int amount){
        this.balance = balance - amount;
    }

    public boolean isRentedByThis(String movieName){
        for(MovieDetails movie : movies){
            if (movie.getName().equals(movieName))
                return true;
        }
        return false;
    }
}
