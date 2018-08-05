package bgu.spl181.net.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MoviesData {
    private List<Movie> movies = new ArrayList<>();


    public List<Movie> getMovies() {
        return movies;
    }
}